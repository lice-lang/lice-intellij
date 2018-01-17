package org.lice.lang.action

import com.google.common.util.concurrent.SimpleTimeLimiter
import com.google.common.util.concurrent.UncheckedTimeoutException
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Ref
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.JBUI
import org.lice.core.SymbolList
import org.lice.lang.*
import org.lice.lang.module.moduleSettings
import org.lice.parse.*
import org.lice.util.className
import java.awt.Dimension
import java.util.concurrent.TimeUnit
import javax.swing.JLabel
import javax.swing.JTextArea

private class UseOfBannedFuncException(val name: String) : Throwable()
class TryEvaluate {
	private companion object SymbolListHolder {
		private fun SymbolList.ban(name: String) = provideFunction(name) { throw UseOfBannedFuncException(name) }
	}

	private var textLimit = 360
	private var timeLimit = 1500L
	private var builder = StringBuilder()
	private val symbolList
		get() = SymbolList().apply {
			ban("getBigDecs")
			ban("getBigInts")
			ban("getDoubles")
			ban("getFloats")
			ban("getInts")
			ban("getLines")
			ban("getTokens")
			ban("exit")
			ban("extern")
			provideFunction("print") { it.forEach { builder.append(it) } }
			provideFunction("println") { it.forEach { builder.appendln(it) } }
		}

	private fun StringBuilder.insertOutputIfNonBlank() = insert(0, if (isNotBlank()) "\nOutput:\n" else "")
	fun tryEval(editor: Editor, text: String, project: Project?, popupWhenSuccess: Boolean): Ref<Any?>? {
		project?.moduleSettings?.let {
			timeLimit = it.tryEvaluateTimeLimit
			textLimit = it.tryEvaluateTextLimit
		}
		if (builder.isNotBlank()) builder = StringBuilder()
		try {
			val result = SimpleTimeLimiter().callWithTimeout({
				Parser
						.parseTokenStream(Lexer(text))
						.accept(Sema(symbolList))
						.eval()
			}, timeLimit, TimeUnit.MILLISECONDS, true)
			builder.insertOutputIfNonBlank()
			builder.insert(0, LiceBundle.message("lice.messages.try-eval.result", result.toString(), result.className()))
			if (popupWhenSuccess) showPopupWindow(builder.toString(), editor, 0x0013F9, 0x000CA1)
			return Ref.create(result)
		} catch (e: UncheckedTimeoutException) {
			builder.insertOutputIfNonBlank()
			builder.insert(0, LiceBundle.message("lice.messages.try-eval.timeout"))
			showPopupWindow(builder.toString(), editor, 0xEDC209, 0xC26500)
		} catch (e: Throwable) {
			val cause = e as? UseOfBannedFuncException ?: e.cause as? UseOfBannedFuncException
			builder.insertOutputIfNonBlank()
			if (cause != null) {
				builder.insert(0, LiceBundle.message("lice.messages.try-eval.unsupported", cause.name))
				showPopupWindow(builder.toString(), editor, 0xEDC209, 0xC26500)
			} else {
				builder.insert(0, LiceBundle.message("lice.messages.try-eval.exception", e.javaClass.simpleName, e.message.orEmpty()))
				showPopupWindow(builder.toString(), editor, 0xE20911, 0xC20022)
			}
		}
		return null
	}

	fun showPopupWindow(
			result: String,
			editor: Editor,
			color: Int,
			colorDark: Int) {
		val relativePoint = JBPopupFactory.getInstance().guessBestPopupLocation(editor)
		if (result.length < textLimit)
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
									it.toolTipText = LiceBundle.message("lice.messages.try-eval.overflowed-text", textLimit)
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
}

class TryEvaluateLiceExpressionAction :
		AnAction(LiceBundle.message("lice.messages.try-eval.name"),
				LiceBundle.message("lice.messages.try-eval.description"), LICE_BIG_ICON), DumbAware {
	private val core = TryEvaluate()
	override fun actionPerformed(event: AnActionEvent) {
		val editor = event.getData(CommonDataKeys.EDITOR) ?: return
		core.tryEval(editor, editor.selectionModel.selectedText ?: return, event.getData(CommonDataKeys.PROJECT), true)
	}

	override fun update(event: AnActionEvent) {
		event.presentation.isEnabledAndVisible = event.getData(CommonDataKeys.VIRTUAL_FILE)?.fileType == LiceFileType
	}
}
