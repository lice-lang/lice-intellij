@file:JvmName("LicePsiImplUtils")
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER", "ConflictingExtensionProperty")

package org.lice.lang.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import org.lice.lang.LiceFile
import org.lice.lang.LiceLanguage
import org.lice.lang.editing.LiceSymbols
import org.lice.lang.psi.*
import java.lang.StringBuilder

val LiceFunctionCall.liceCallee get() = elementList.firstOrNull { it.comment == null }
val LiceFunctionCall.nonCommentElements: List<LiceElement> get() = elementList.filter { it.comment == null }

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

val LiceSymbol.nameIdentifier
	get() = (parent.parent as? LiceFunctionCall)?.let {
		if (it.liceCallee?.text !in LiceSymbols.nameIntroducingFamily) null
		else it.nonCommentElements.getOrNull(1)?.takeIf(::equals)
	}

val LiceSymbol.references get() = ReferenceProvidersRegistry.getReferencesFromProviders(this)
val LiceSymbol.name get() = nameIdentifier?.text
fun LiceSymbol.setName(newName: String): PsiElement {
	val liceSymbol = nameIdentifier
	val functionCall = parent.parent as? LiceFunctionCall
	if (newName.all { it in LiceSymbols.validChars } &&
			liceSymbol != null &&
			functionCall?.liceCallee?.text in LiceSymbols.nameIntroducingFamily) {
		val newChild = PsiFileFactory
				.getInstance(liceSymbol.project)
				.createFileFromText(LiceLanguage, newName)
				.takeIf { it is LiceFile }
				?.firstChild
				?: throw IncorrectOperationException("Unable to rename ${liceSymbol.text} to $newName")
		liceSymbol.replace(newChild)
		return this
	} else throw IncorrectOperationException("Unable to rename to $newName")
}

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
