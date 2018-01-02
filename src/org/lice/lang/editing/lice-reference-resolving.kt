package org.lice.lang.editing

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNameIdentifierOwner

interface LiceNamedElement : PsiNameIdentifierOwner
abstract class LiceNamedElementImpl(node: ASTNode) : LiceNamedElement, ASTWrapperPsiElement(node)
