package org.lice.lang.editing

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.lice.lang.LICE_NAME

class CovRemoveBlockIntention(private val element: PsiElement, private val intentionText: String) : BaseIntentionAction() {
	override fun getText() = intentionText
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LICE_NAME
	override operator fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
		// val file = psiFile?.let { PsiManager.getInstance(project).findFile(it.virtualFile) as? CovFile } ?: return
		element.delete()
	}
}
