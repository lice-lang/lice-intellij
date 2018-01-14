@file:JvmName("LicePsiImplUtils")
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER", "ConflictingExtensionProperty")

package org.lice.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.tree.IElementType
import org.lice.lang.psi.*
import java.lang.StringBuilder

val LiceFunctionCall.liceCallee: ASTNode?
	get() = node.findChildByType(LiceTypes.ELEMENT)

val LiceFunctionCall.nonCommentElements: List<LiceElement>
	get() = elementList.filter { it is LiceElement && it.comment == null }

val LiceComment.tokenType: IElementType
	get() = LiceTypes.COMMENT

val LiceElement.nonCommentElements: PsiElement?
	get() = functionCall ?: `null` ?: symbol ?: number ?: string

fun LiceComment.isValidHost() = true

fun LiceComment.updateText(string: String) = ElementManipulators.handleContentChange(this, string)

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
