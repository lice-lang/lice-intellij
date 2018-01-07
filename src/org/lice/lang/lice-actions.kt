package org.lice.lang

import com.intellij.CommonBundle
import com.intellij.ide.actions.CreateFileAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.*
import org.lice.lang.tool.LiceSemanticTreeViewerFactory
import org.lice.lang.tool.LiceSyntaxTreeViewerFactory
import java.awt.Dimension
import java.nio.file.Paths
import java.time.LocalDate
import javax.swing.Icon

class NewLiceFile : CreateFileAction(CAPTION, "", LICE_ICON) {
	private companion object Caption {
		private const val CAPTION = "New Lice File"
	}

	override fun getActionName(directory: PsiDirectory?, s: String?) = CAPTION
	override fun getErrorTitle(): String = CommonBundle.getErrorTitle()
	override fun getDefaultExtension() = LICE_EXTENSION

	override fun create(name: String, directory: PsiDirectory): Array<PsiElement> {
		val fixedExtension = when (FileUtilRt.getExtension(name)) {
			LICE_EXTENSION -> name
			else -> "$name.$LICE_EXTENSION"
		}
		return arrayOf(directory.add(PsiFileFactory
				.getInstance(directory.project)
				.createFileFromText(fixedExtension, LiceFileType, """;;
;; Created by ${System.getenv("USER")} on ${LocalDate.now()}
;;

(|>)
""")))
	}

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

abstract class LiceFileActions(text: String, description: String, icon: Icon) : AnAction(text, description, icon) {
	protected fun compatibleFiles(e: AnActionEvent) = CommonDataKeys
			.VIRTUAL_FILE_ARRAY
			.getData(e.dataContext)
			?.filter { file -> file.fileType is LiceFileType }
			?: emptyList()

	override fun update(e: AnActionEvent) {
		e.presentation.isEnabledAndVisible = compatibleFiles(e).run {
			isNotEmpty() and all { LICE_EXTENSION == it.extension }
		}
	}
}

class ShowLiceFileSyntaxTree : LiceFileActions(
		"View Lice Syntax Tree",
		"View Lice file syntax tree in a window",
		LICE_AST_NODE_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)?.let(FileDocumentManager.getInstance()::saveDocument)
			val view = LiceSyntaxTreeViewerFactory.create(Paths.get(file.path))
			val popup = JBPopupFactory.getInstance()
					.createComponentPopupBuilder(view, view)
					.createPopup()
			popup.size = Dimension(520, 520)
			ApplicationManager.getApplication().invokeLater(popup::showInFocusCenter)
		}
	}
}

class ShowLiceFileSemanticTree : LiceFileActions(
		"View Lice Semantic Tree",
		"View Lice file semantic tree in a window",
		LICE_AST_NODE2_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)
					?.let(FileDocumentManager.getInstance()::saveDocument)
			val view = LiceSemanticTreeViewerFactory.create(Paths.get(file.path))
			val popup = JBPopupFactory.getInstance()
					.createComponentPopupBuilder(view, view)
					.createPopup()
			popup.size = Dimension(520, 520)
			ApplicationManager.getApplication().invokeLater(popup::showInFocusCenter)
		}
	}
}
