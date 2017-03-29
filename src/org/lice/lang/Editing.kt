/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.CommonBundle
import com.intellij.ide.actions.CreateFileAction
import com.intellij.lang.Commenter
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.lice.lang.LiceInfo.EXTENSION
import org.lice.lang.LiceInfo.LICE_ICON


class NewLiceFile : CreateFileAction(CAPTION, "", LICE_ICON) {
	override fun getActionName(p0: PsiDirectory?, p1: String?) =
			CAPTION

	override fun getErrorTitle() =
			CommonBundle.getErrorTitle()!!

	override fun getDefaultExtension() =
			EXTENSION

	override fun create(
			p0: String?,
			p1: PsiDirectory?
	): Array<PsiElement?> {
		val origin = FileUtilRt.getExtension(
				p0 ?: "new-file${System.currentTimeMillis()}.lice")
		val fixedExtension = when (origin) {
			"lice" -> origin
			else -> "$p0.lice"
		}
		return arrayOf(p1?.add(PsiFileFactory
				.getInstance(p1.project)
				.createFileFromText(fixedExtension, LiceFileType, "")))
	}

	override fun invokeDialog(
			project: Project?,
			p1: PsiDirectory?
	): Array<PsiElement> {
		val v = MyInputValidator(project, p1)
		Messages.showInputDialog(
				project,
				"Enter a new lice script name",
				"New Lice script",
				Messages.getQuestionIcon(),
				"file-name.lice",
				v
		)
		return v.createdElements
	}

	private companion object Caption {
		private val CAPTION = "New Lice File"
	}
}

class LiceCommenter : Commenter {
	override fun getCommentedBlockCommentPrefix() =
			blockCommentPrefix

	override fun getCommentedBlockCommentSuffix() =
			blockCommentSuffix

	override fun getBlockCommentPrefix() = null
	override fun getBlockCommentSuffix() = null

	override fun getLineCommentPrefix() = ";"
}

class LiceSpellCheckingStrategy : SpellcheckingStrategy() {
	override fun getTokenizer(element: PsiElement): Tokenizer<PsiElement> {
		return super.getTokenizer(element)
//		return when (element.elementType) {
//		}
	}
}
