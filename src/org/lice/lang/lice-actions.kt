package org.lice.lang

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.CreateFileAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.*
import org.lice.tools.LiceSemanticTreeViewerFactory
import org.lice.tools.LiceSyntaxTreeViewerFactory
import java.io.File
import java.time.LocalDate
import javax.swing.Icon

class NewLiceFile : CreateFileAction(CAPTION, "", LiceInfo.LICE_ICON) {
	override fun getActionName(p0: PsiDirectory?, p1: String?) = CAPTION
	override fun getErrorTitle(): String = CommonBundle.getErrorTitle()
	override fun getDefaultExtension() = LiceInfo.EXTENSION

	override fun create(name: String, directory: PsiDirectory): Array<PsiElement> {
		val fixedExtension = when (FileUtilRt.getExtension(name)) {
			LiceInfo.EXTENSION -> name
			else -> "$name.${LiceInfo.EXTENSION}"
		}
		return arrayOf(directory.add(PsiFileFactory
				.getInstance(directory.project)
				.createFileFromText(fixedExtension, LiceFileType, """;
; Created by ${System.getenv("USERNAME")} on ${LocalDate.now()}
;

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
				"file-name${System.currentTimeMillis()}.${LiceInfo.EXTENSION}",
				validator
		)
		return validator.createdElements
	}

	private companion object Caption {
		private val CAPTION = "New Lice File"
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
			isNotEmpty() and all { LiceInfo.EXTENSION == it.extension }
		}
	}

}

class RunLiceFile : LiceFileActions(
		"Run Lice script",
		"Run Lice script with lice interpreter",
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
//			Runtime.getRuntime().exec(
//					StringUtils.join(arrayOf(
//							LiceInfo.JAVA_PATH_WRAPPED,
//							"-classpath",
//							"\"" + StringUtils.join(arrayOf(
//									LiceInfo.KOTLIN_RUNTIME_PATH,
//									LiceInfo.KOTLIN_REFLECT_PATH,
//									LiceInfo.LICE_PATH
//							), ";") + "\"",
//							"org.lice.repl.Main",
//							"\"" + file.path + "\""
//					), " ").also { println(it) },
//					null,
//					File(file.parent.path)
//			)
		}
	}
}

class ShowLiceFileSyntaxTree : LiceFileActions(
		"View Syntax Tree",
		"View Lice file syntax tree in a window",
		LiceInfo.LICE_AST_NODE_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)?.let(FileDocumentManager.getInstance()::saveDocument)
			val view = LiceSyntaxTreeViewerFactory.create(File(file.path))
			JBPopupFactory.getInstance()
					.createComponentPopupBuilder(view, view)
					.createPopup()
					.showInFocusCenter()
		}
	}
}

class ShowLiceFileSemanticTree : LiceFileActions(
		"View Semantic Tree",
		"View Lice file semantic tree in a window",
		LiceInfo.LICE_AST_NODE2_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)
					?.let(FileDocumentManager.getInstance()::saveDocument)
			val view = LiceSemanticTreeViewerFactory.create(File(file.path))
			JBPopupFactory.getInstance()
					.createComponentPopupBuilder(view, view)
					.createPopup()
					.showInFocusCenter()
		}
	}
}
