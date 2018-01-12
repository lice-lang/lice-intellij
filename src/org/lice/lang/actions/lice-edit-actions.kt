package org.lice.lang.actions

import com.google.common.util.concurrent.SimpleTimeLimiter
import com.google.common.util.concurrent.UncheckedTimeoutException
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.JBUI
import org.lice.core.SymbolList
import org.lice.lang.LICE_BIG_ICON
import org.lice.lang.LiceFileType
import org.lice.parse.*
import org.lice.util.className
import java.awt.Dimension
import java.util.concurrent.TimeUnit
import javax.swing.JLabel
import javax.swing.JTextArea

class TryEvaluateLiceExpressionAction : AnAction("Try evaluate", null, LICE_BIG_ICON), DumbAware {
	private class UseOfBannedFuncException(val name: String) : Throwable()
	private companion object SymbolListHolder {
		private const val WORD_LIMIT = 360
		private fun SymbolList.ban(name: String) = provideFunction(name) { throw UseOfBannedFuncException(name) }
	}

	private val stdout = StringBuilder()
	private val symbolList = SymbolList().apply {
		ban("getBigDecs")
		ban("getBigInts")
		ban("getDoubles")
		ban("getFloats")
		ban("getInts")
		ban("getLines")
		ban("getTokens")
		ban("exit")
		ban("extern")
		provideFunction("print") { it.forEach { stdout.append(it) } }
		provideFunction("println") { it.forEach { stdout.appendln(it) } }
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
				|$result: ${result.className()}
				|Output:
				|$stdout""".trimMargin(), editor,
					0x0013F9, 0x000CA1)
		} catch (e: UncheckedTimeoutException) {
			showPopupWindow("""Execution timeout.
				|Output:
				|$stdout""".trimMargin(), editor, 0xEDC209, 0xC26500)
		} catch (e: Throwable) {
			val cause = e as? UseOfBannedFuncException ?: e.cause as? UseOfBannedFuncException
			if (cause != null)
				showPopupWindow("""Use of function "${cause.name}"
				|is unsupported""".trimMargin(), editor,
						0xEDC209, 0xC26500)
			else showPopupWindow("""Oops! A ${e.javaClass.simpleName} is thrown:
				|${e.message}
				|Output:
				|$stdout""".trimMargin(), editor,
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
				JBPopupFactory.getInstance()
						.createComponentPopupBuilder(JBUI.Panels.simplePanel()
								.addToTop(JLabel(LICE_BIG_ICON))
								.addToCenter(ScrollPaneFactory.createScrollPane(JTextArea(result).also {
									it.toolTipText = "Evaluation output longer than $WORD_LIMIT characters"
									it.lineWrap = true
									it.wrapStyleWord = true
									it.isEditable = false
								}))
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
