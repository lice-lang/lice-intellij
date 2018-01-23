@file:JvmName("LicePsiImplUtils")
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER", "ConflictingExtensionProperty")

package org.lice.lang.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IElementType
import org.lice.lang.editing.LiceSymbols
import org.lice.lang.psi.*
import java.lang.StringBuilder

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

fun PsiElement.processDeclTrivial(
		processor: PsiScopeProcessor,
		substitutor: ResolveState,
		lastParent: PsiElement?,
		place: PsiElement): Boolean {
	var run: PsiElement? = lastParent?.prevSibling ?: lastChild
	while (run != null) {
		if (!run.processDeclarations(processor, substitutor, null, place)) return false
		run = run.prevSibling
	}
	return true
}

val PsiElement.isDeclaration: Boolean
	get() = parent.parent.let {
		it is LiceFunctionCall &&
				it.liceCallee?.text in LiceSymbols.nameIntroducingFamily &&
				it.nonCommentElements.lastOrNull() != this
	}

fun collectFrom(startPoint: PsiElement, name: String) = SyntaxTraverser
		.psiTraverser(startPoint)
		.filter { it is LiceSymbol && it.text == name }
		.mapNotNull(PsiElement::getReference)
		.toList()
		.toTypedArray()

fun treeWalkUp(place: PsiElement, processor: PsiScopeProcessor): Boolean {
	var lastParent: PsiElement? = null
	var run: PsiElement? = place
	while (run != null) {
		if (!run.processDeclarations(processor, ResolveState.initial(), lastParent, place)) return false
		lastParent = run
		run = run.parent
	}
	return true
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
