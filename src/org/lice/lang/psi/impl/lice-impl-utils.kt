@file:JvmName("LicePsiImplUtils")
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER", "ConflictingExtensionProperty")

package org.lice.lang.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import org.lice.lang.LiceFile
import org.lice.lang.LiceLanguage
import org.lice.lang.editing.LiceSymbols
import org.lice.lang.psi.*
import java.lang.StringBuilder

val LiceFunctionCall.liceCallee get() = elementList.firstOrNull { it.comment == null }
val LiceFunctionCall.nonCommentElements: List<LiceElement> get() = elementList.filter { it.comment == null }
val LiceFunctionCall.nameIdentifier
	get() = when (liceCallee?.text) {
		in LiceSymbols.closureFamily -> nonCommentElements.firstOrNull()?.symbol
		!in LiceSymbols.nameIntroducingFamily -> null
		else -> nonCommentElements.getOrNull(1)?.symbol
	}
val LiceFunctionCall.nameIdentifierAndParams
	get() = when (liceCallee?.text) {
		in LiceSymbols.nameIntroducingFamily,
		in LiceSymbols.closureFamily ->
			nonCommentElements.run { if (size >= 1) subList(1, size).toList() else emptyList() }
		else -> emptyList()
	}

val LiceFunctionCall.references: Array<LiceSymbolReference>
	get() {
		val innerNames = nameIdentifierAndParams.mapNotNull(LiceElement::getSymbol)
		val innerNameTexts = innerNames.map(LiceSymbol::getText)
		if (this.liceCallee?.text in LiceSymbols.closureFamily) return SyntaxTraverser.psiTraverser(this)
				.mapNotNull { it as? LiceSymbol }
				.filter { it !in innerNames && it.text in innerNameTexts }
				.map { symbol -> LiceSymbolReference(symbol, this) }
				.toTypedArray()
		val name = innerNames.firstOrNull() ?: return emptyArray()
		val nameText = name.text
		val params = innerNames.drop(1)
		val paramTexts = params.map(LiceSymbol::getText)
		if (this.liceCallee?.text !in LiceSymbols.nameIntroducingFamily) return emptyArray()
		val list1 = SyntaxTraverser.psiTraverser(parent.parent)
				.mapNotNull { it as? LiceSymbol }
				.filter { it != name && it.text == nameText }
				.map { symbol -> LiceSymbolReference(symbol, this) }
		val list2 = SyntaxTraverser.psiTraverser(this)
				.mapNotNull { it as? LiceSymbol }
				.filter { it !in params && it.text in paramTexts }
				.map { symbol -> LiceSymbolReference(symbol, this) }
		return (list1 + list2).toTypedArray()
	}

val LiceFunctionCall.name get() = nameIdentifier?.text
fun LiceFunctionCall.setName(newName: String): PsiElement {
	val liceSymbol = nameIdentifier
	if (newName.all { it in LiceSymbols.validChars } &&
			liceSymbol != null &&
			liceCallee?.text in LiceSymbols.nameIntroducingFamily) {
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

val LiceSymbol.references: Array<PsiReference> get() = parent.parent.references
val LiceSymbol.reference: LiceSymbolReference get() = LiceSymbolReference(this)

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
