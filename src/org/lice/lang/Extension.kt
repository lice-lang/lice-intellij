/**
 * Created by ice1000 on 2017/3/29.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

val PsiElement?.elementType : IElementType? get() = this?.node?.elementType
