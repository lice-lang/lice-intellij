@file:JvmName("LicePsiImplUtils")

package org.lice.lang.psi.impl

import org.lice.lang.psi.*

fun getSymbol(element: LiceNumberOrSymbol) = element.node.findChildByType(LiceTypes.SYM)?.text

