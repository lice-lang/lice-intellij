@file:JvmName("LicePsiImplUtils")
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER", "ConflictingExtensionProperty")

package org.lice.lang.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import com.intellij.util.containers.JBIterable
import org.lice.lang.LiceFile
import org.lice.lang.LiceLanguage
import org.lice.lang.editing.LiceSymbolsHelper
import org.lice.lang.psi.*
import java.lang.StringBuilder

val LiceFunctionCall.liceCallee get() = elementList.firstOrNull { it is LiceElement }
val LiceFunctionCall.nonCommentElements: List<LiceElement> get() = elementList.filter { it is LiceElement && it.comment == null }

val LiceComment.tokenType: IElementType get() = LiceTypes.COMMENT
val LiceComment.isValidHost get() = true
fun LiceComment.updateText(string: String): LiceComment = ElementManipulators.handleContentChange(this, string)
fun LiceComment.createLiteralTextEscaper() = object : LiteralTextEscaper<LiceComment>(this@createLiteralTextEscaper) {
	private var numOfSemicolon = 1
	override fun isOneLine() = true
	override fun getOffsetInHost(offsetInDecoded: Int, rangeInHost: TextRange) = offsetInDecoded + numOfSemicolon
	override fun decode(rangeInHost: TextRange, builder: StringBuilder): Boolean {
		numOfSemicolon = myHost.text.indexOfFirst { it != ';' }
		builder.append(myHost.text, rangeInHost.startOffset + numOfSemicolon, rangeInHost.endOffset)
		return true
	}
}

val LiceElement.nonCommentElements: PsiElement? get() = functionCall ?: `null` ?: symbol ?: number ?: string

// val LiceString.isValidHost get() = true
// fun LiceString.updateText(string: String): LiceString = ElementManipulators.handleContentChange(this, string)
// fun LiceString.createLiteralTextEscaper() = object : LiteralTextEscaper<LiceString>(this@createLiteralTextEscaper) {
//   // private val mapping = IntIntHashMap()
//  override fun isOneLine() = false
//  override fun getOffsetInHost(offsetInDecoded: Int, rangeInHost: TextRange) = offsetInDecoded + 1
//  override fun decode(rangeInHost: TextRange, builder: StringBuilder): Boolean {
//    builder.append(myHost.text, rangeInHost.startOffset + 1, rangeInHost.endOffset - 1)
//    return true
//  }
//}

class LiceResolveResult(private val element: PsiElement) : ResolveResult {
	override fun getElement() = element
	override fun isValidResult() = true
}

val PsiElement.topParent: PsiElement
	get() {
		var p = this
		while (p.parent != null) p = p.parent
		return p
	}

val LiceSymbol.canonicalText: String get() = text
val LiceSymbol.element get() = this
val LiceSymbol.isSoft get() = true
val LiceSymbol.rangeInElement: TextRange get() = textRange
val LiceSymbol.variants get() = LiceSymbolsHelper.allSymbols + allDefinitions.map { it.nonCommentElements[1] }
val LiceSymbol.nameIdentifier get() = canonicalText
fun LiceSymbol.isReferenceTo(element: PsiElement) = (element.parent as? LiceFunctionCall)?.let {
	it.liceCallee?.text in LiceSymbolsHelper.nameIntroducingFamily && it.nonCommentElements[1].text == canonicalText
} ?: false

val LiceSymbol.allDefinitions: JBIterable<LiceFunctionCall>
	get() = SyntaxTraverser
			.psiTraverser(topParent)
			.filter(LiceFunctionCall::class.java)
			.filter { it.nonCommentElements.let { it.size >= 2 && it[0].text in LiceSymbolsHelper.nameIntroducingFamily } }

fun LiceSymbol.resolve() = multiResolve(false).firstOrNull()?.element
fun LiceSymbol.multiResolve(b: Boolean) = allDefinitions.transform(::LiceResolveResult).toList().toTypedArray()
fun LiceSymbol.bindToElement(element: PsiElement) = this
fun LiceSymbol.handleElementRename(newName: String): PsiElement = setName(newName)
fun LiceSymbol.setName(newName: String): PsiElement = PsiFileFactory
		.getInstance(project)
		.createFileFromText(LiceLanguage, newName.let {
			if (it.all { it in LiceSymbolsHelper.validChars }) it
			else throw IncorrectOperationException("Invalid name $newName")
		})
		.takeIf { it is LiceFile }
		?.firstChild
		?: throw IncorrectOperationException("Unable to rename $text to $newName")
