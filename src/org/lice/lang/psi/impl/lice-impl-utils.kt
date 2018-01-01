@file:JvmName("LicePsiImplUtils")

package org.lice.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.injected.StringLiteralEscaper
import org.lice.lang.psi.LiceMethodCall
import org.lice.lang.psi.LiceTypes

fun LiceMethodCall.getCallee(): ASTNode? = node.findChildByType(LiceTypes.ELEMENT)
fun LiceInjectionElement.isValidHost() = true

fun LiceInjectionElement.updateText(string: String): LiceInjectionElement {
	val valueNode = node.firstChildNode as? LeafElement ?: return this
	valueNode.replaceWithText(string)
	return this
}

fun LiceInjectionElement.createLiteralTextEscaper()
		= StringLiteralEscaper(this)

interface LiceInjectionElement : PsiLanguageInjectionHost
