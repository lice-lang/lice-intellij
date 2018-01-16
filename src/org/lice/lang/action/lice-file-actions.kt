package org.lice.lang.action

import com.intellij.CommonBundle
import com.intellij.ide.actions.CreateFileAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.*
import org.intellij.lang.annotations.Language
import org.lice.lang.*
import org.lice.lang.tool.LiceSemanticTreeViewerFactory
import java.awt.Dimension
import java.nio.file.Paths
import java.time.LocalDate
import javax.swing.Icon

class NewLiceFileAction : CreateFileAction(CAPTION, "", LICE_ICON), DumbAware {
	private companion object Caption {
		private const val CAPTION = "New Lice File"
	}

	override fun getActionName(directory: PsiDirectory?, s: String?) = CAPTION
	override fun getErrorTitle(): String = CommonBundle.getErrorTitle()
	override fun getDefaultExtension() = LICE_EXTENSION

	@Language("Lice")
	override fun create(name: String, directory: PsiDirectory) =
			arrayOf(directory.add(PsiFileFactory
					.getInstance(directory.project)
					.createFileFromText(when (FileUtilRt.getExtension(name)) {
						LICE_EXTENSION -> name
						else -> "$name.$LICE_EXTENSION"
					}, LiceFileType, """;;
;; Created by ${System.getProperty("user.name")} on ${LocalDate.now()}
;;

(|>)
""")))

	override fun invokeDialog(project: Project, directory: PsiDirectory): Array<PsiElement> {
		val validator = MyInputValidator(project, directory)
		Messages.showInputDialog(
				project,
				"Enter a new lice script name",
				"New Lice script",
				Messages.getQuestionIcon(),
				"",
				validator)
		return validator.createdElements
	}
}

abstract class LiceFileAction(text: String?, description: String?, icon: Icon?) : AnAction(text, description, icon) {
	protected fun compatibleFiles(event: AnActionEvent) = CommonDataKeys
			.VIRTUAL_FILE_ARRAY
			.getData(event.dataContext)
			?.filter { file -> file.fileType is LiceFileType }
			?: emptyList()

	override fun update(event: AnActionEvent) {
		event.presentation.isEnabledAndVisible = compatibleFiles(event).run {
			isNotEmpty() and all { LICE_EXTENSION == it.extension }
		}
	}
}

class ShowLiceFileSemanticTreeAction : LiceFileAction(
		"View Lice Semantic Tree",
		"View Lice file semantic tree in a window",
		LICE_AST_NODE2_ICON), DumbAware {
	override fun actionPerformed(event: AnActionEvent) {
		compatibleFiles(event).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)
					?.let(FileDocumentManager.getInstance()::saveDocument)
			val view = LiceSemanticTreeViewerFactory.create(Paths.get(file.path))
			val popup = JBPopupFactory.getInstance()
					.createComponentPopupBuilder(view, view)
					.createPopup()
			popup.size = Dimension(600, 600)
			ApplicationManager.getApplication().invokeLater(popup::showInFocusCenter)
		}
	}
}
