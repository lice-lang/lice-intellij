@file:JvmName("LicePsiImplUtils")
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package org.lice.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.injected.StringLiteralEscaper
import org.lice.lang.psi.*

val LiceFunctionCall.liceCallee: ASTNode?
	get() = node.findChildByType(LiceTypes.ELEMENT)

val LiceFunctionCall.nonCommentElements: List<LiceElement>
	get() = elementList.filter { it is LiceElement && it.comment == null }

interface LiceInjectionElement : PsiLanguageInjectionHost

fun LiceInjectionElement.isValidHost() = true

fun LiceInjectionElement.updateText(string: String): LiceInjectionElement {
	println(string)
	val value = node.firstChildNode
	println(value)
	return this
}

fun LiceInjectionElement.createLiteralTextEscaper(): StringLiteralEscaper<LiceInjectionElement> {
	return StringLiteralEscaper(this)
}

