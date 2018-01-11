package org.lice.lang

import com.google.common.util.concurrent.SimpleTimeLimiter
import com.google.common.util.concurrent.UncheckedTimeoutException
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
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.JBUI
import org.intellij.lang.annotations.Language
import org.lice.core.SymbolList
import org.lice.lang.tool.LiceSemanticTreeViewerFactory
import org.lice.parse.*
import org.lice.util.className
import java.awt.Dimension
import java.nio.file.Paths
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.swing.*

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
	private class UseOfBannedFuncException(val name: String) : Throwable()
	private companion object SymbolListHolder {
		private const val WORD_LIMIT = 360
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
				ban("extern")
			}
	}

	override fun actionPerformed(event: AnActionEvent) {
		val editor = event.getData(CommonDataKeys.EDITOR) ?: return
		val selectedText = editor.selectionModel.selectedText ?: return
		try {
			val result = SimpleTimeLimiter().callWithTimeout({
				Parser
						.parseTokenStream(Lexer(selectedText))
						.accept(Sema(symbolList))
						.eval()
			}, 1500L, TimeUnit.MILLISECONDS, true)
			showPopupWindow("""Result:
				|$result: ${result.className()}""".trimMargin(), editor,
					0x0013F9, 0x000CA1)
		} catch (e: UncheckedTimeoutException) {
			showPopupWindow("Execution timeout", editor, 0xEDC209, 0xC26500)
		} catch (e: Throwable) {
			val cause = e as? UseOfBannedFuncException ?: e.cause as? UseOfBannedFuncException
			if (cause != null)
				showPopupWindow("""Use of function "${cause.name}"
				|is unsupported""".trimMargin(), editor,
						0xEDC209, 0xC26500)
			else showPopupWindow("""Oops! A ${e.javaClass.simpleName} is thrown:
				|${e.message}""".trimMargin(), editor,
					0xE20911, 0xC20022)
		}
	}

	private fun showPopupWindow(
			result: String,
			editor: Editor,
			color: Int,
			colorDark: Int) {
		val relativePoint = JBPopupFactory.getInstance().guessBestPopupLocation(editor)
		if (result.length < WORD_LIMIT)
			ApplicationManager.getApplication().invokeLater {
				JBPopupFactory.getInstance()
						.createHtmlTextBalloonBuilder(result, LICE_BIG_ICON, JBColor(color, colorDark), null)
						.setFadeoutTime(8000)
						.setHideOnAction(true)
						.createBalloon()
						.show(relativePoint, Balloon.Position.below)
			}
		else
			ApplicationManager.getApplication().invokeLater {
				val textField = JTextArea(result).also {
					it.toolTipText = "Evaluation output longer than $WORD_LIMIT characters"
					it.lineWrap = true
					it.wrapStyleWord = true
					it.isEditable = false
				}
				JBPopupFactory.getInstance()
						.createComponentPopupBuilder(JBUI.Panels.simplePanel()
								.addToTop(JLabel(LICE_BIG_ICON))
								.addToCenter(ScrollPaneFactory.createScrollPane(textField))
								.apply {
									preferredSize = Dimension(500, 500)
									border = JBUI.Borders.empty(10, 5, 5, 5)
								}, null)
						.setRequestFocus(true)
						.setResizable(true)
						.setMovable(true)
						.setCancelOnClickOutside(true)
						.createPopup()
						.show(relativePoint)
			}
	}

	override fun update(event: AnActionEvent) {
		event.presentation.isEnabledAndVisible = event.getData(CommonDataKeys.VIRTUAL_FILE)?.fileType is LiceFileType
	}
}
