/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.CreateFileAction
import com.intellij.lang.Commenter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.lice.Lice
import org.lice.lang.LiceInfo.EXTENSION
import org.lice.lang.LiceInfo.LICE_ICON
import java.io.File


class NewLiceFile : CreateFileAction(CAPTION, "", LICE_ICON) {
	override fun getActionName(p0: PsiDirectory?, p1: String?) =
			CAPTION

	override fun getErrorTitle() =
			CommonBundle.getErrorTitle()!!

	override fun getDefaultExtension() =
			EXTENSION

	override fun create(
			name: String?,
			directory: PsiDirectory?
	): Array<PsiElement?> {
		val origin = name ?: "new-file${System.currentTimeMillis()}.$EXTENSION"
		val fixedExtension = when (FileUtilRt.getExtension(origin)) {
			EXTENSION -> origin
			else -> "$origin.$EXTENSION"
		}
		return arrayOf(directory?.add(PsiFileFactory
				.getInstance(directory.project)
				.createFileFromText(fixedExtension, LiceFileType, "")))
	}

	override fun invokeDialog(
			project: Project?,
			psiDirectory: PsiDirectory?
	): Array<PsiElement> {
		val validator = MyInputValidator(project, psiDirectory)
		Messages.showInputDialog(
				project,
				"Enter a new lice script name",
				"New Lice script",
				Messages.getQuestionIcon(),
				"file-name${System.currentTimeMillis()}.$EXTENSION",
				validator
		)
		return validator.createdElements
	}

	private companion object Caption {
		private val CAPTION = "New Lice File"
	}
}

class RunLiceFile : AnAction(
		"Run Lice script",
		"Run Lice script",
		AllIcons.Toolwindows.ToolWindowRun) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)?.let { doc ->
				FileDocumentManager
						.getInstance()
						.saveDocument(doc)
			}
			Lice.run(File(file.path))
		}
	}

	private fun compatibleFiles(e: AnActionEvent): Array<out VirtualFile> =
			(CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext) ?: emptyArray()).filter { file ->
				file is VirtualFile && EXTENSION == file.extension
			}.toTypedArray()
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
