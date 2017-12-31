@file:JvmName("LicePsiImplUtils")

package org.lice.lang.psi.impl

import com.intellij.lang.ASTNode
import org.lice.lang.psi.LiceMethodCall
import org.lice.lang.psi.LiceTypes

fun getCallee(element: LiceMethodCall): ASTNode? = element.node.findChildByType(LiceTypes.ELEMENT)
