package org.lice.lang

import com.intellij.CommonBundle
import com.intellij.ide.actions.CreateFileAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.*
import com.intellij.ui.JBColor
import org.lice.core.SymbolList
import org.lice.lang.tool.LiceSemanticTreeViewerFactory
import org.lice.parse.*
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

	override fun create(name: String, directory: PsiDirectory) =
			arrayOf(directory.add(PsiFileFactory
					.getInstance(directory.project)
					.createFileFromText(when (FileUtilRt.getExtension(name)) {
						LICE_EXTENSION -> name
						else -> "$name.$LICE_EXTENSION"
					}, LiceFileType, """;;
;; Created by ${System.getenv("USER")} on ${LocalDate.now()}
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

class TryEvaluateLiceExpressionAction : AnAction("Try evaluate", null, LICE_BIG_ICON), DumbAware {
	private companion object SymbolListHolder {
		private fun SymbolList.ban(name: String) = provideFunction(name) { throw UseOfBannedFuncException(name) }
		private val symbolList
			get() = SymbolList().apply {
				ban("getBigDecs")
				ban("getBigInts")
				ban("getDoubles")
				ban("getFloats")
				ban("getInts")
				ban("getLines")
				ban("getTokens")
				ban("print")
				ban("println")
				ban("exit")
				ban("eval")
				ban("load-file")
				ban("extern")
			}
	}

	private class UseOfBannedFuncException(val name: String) : Throwable()

	override fun actionPerformed(event: AnActionEvent) {
		val editor = event.getData(CommonDataKeys.EDITOR) ?: return
		val selectedText = editor.selectionModel.selectedText ?: return
		try {
			showPopupWindow("Result: ${Parser
					.parseTokenStream(Lexer(selectedText))
					.accept(Sema(symbolList))
					.eval()}", editor,
					0x1FEEDE, 0x000CA1)
		} catch (e: UseOfBannedFuncException) {
			showPopupWindow("""Use of function "${e.name}"
				|is unsupported""".trimMargin(), editor,
					0x5F7D1B, 0xAD7A00)
		} catch (e: Throwable) {
			showPopupWindow("Oops! Something was wrong:\n${e.message}", editor,
					0xB6AC4A, 0xC20022)
		}
	}

	private fun showPopupWindow(result: String, editor: Editor, color: Int, colorDark: Int) {
		ApplicationManager.getApplication().invokeLater {
			JBPopupFactory.getInstance()
					.createHtmlTextBalloonBuilder(result, LICE_BIG_ICON, JBColor(color, colorDark), null)
					.setFadeoutTime(8000)
					.setHideOnAction(true)
					.createBalloon()
					.show(JBPopupFactory.getInstance().guessBestPopupLocation(editor), Balloon.Position.below)
		}
	}

	override fun update(event: AnActionEvent) {
		event.presentation.isEnabledAndVisible = event.getData(CommonDataKeys.VIRTUAL_FILE)?.fileType is LiceFileType
	}
}
