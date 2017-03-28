package org.lice.lang

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.lang.Commenter
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
class NewLiceFile : CreateFileFromTemplateAction(CAPTION, "", LICE_ICON), DumbAware {
	override fun buildDialog(
			p0: Project?,
			p1: PsiDirectory?,
			builder: CreateFileFromTemplateDialog.Builder) {
		builder
				.setTitle(CAPTION)
				.addKind("Empty File", LICE_ICON, "Lice File")
	}

	override fun getActionName(p0: PsiDirectory?, p1: String?, p2: String?) = CAPTION

	private companion object Caption {
		val CAPTION = "New Lice File"
	}
}


class LiceCommenter : Commenter {
	override fun getCommentedBlockCommentPrefix() = null
	override fun getCommentedBlockCommentSuffix() = null
	override fun getBlockCommentPrefix() = null
	override fun getBlockCommentSuffix() = null

	override fun getLineCommentPrefix() = ";"
}

