package org.lice.lang.action

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
import icons.LiceIcons
import org.lice.core.Func
import org.lice.core.SymbolList
import org.lice.lang.LiceBundle
import org.lice.lang.LiceFileType
import org.lice.lang.module.moduleSettings
import org.lice.parse.Lexer
import org.lice.parse.Parser
import org.lice.util.LiceException
import org.lice.util.className
import java.awt.Dimension
import java.util.concurrent.Executors
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
	lateinit var prelude: Set<MutableMap.MutableEntry<String, Any?>>
	private val executor = Executors.newCachedThreadPool()
	private fun symbolList() = SymbolList().apply {
		prelude = variables.entries
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
			val symbolList = symbolList()
			val result = executor.submit {
				Parser
						.parseTokenStream(Lexer(text))
						.accept(symbolList)
						.eval()
			}.get(timeLimit, TimeUnit.MILLISECONDS)
			if (popupWhenSuccess) {
				builder.insertOutputIfNonBlank()
				@Suppress("UNCHECKED_CAST")
				val resultString = (result as? Func)?.let { function ->
					symbolList
							.entries
							.firstOrNull { it.value == function }
							?.takeUnless { it.key.run { startsWith("λ") and substring(1).all(Char::isDigit) } }
							?.let { LiceBundle.message("lice.messages.try-eval.function-named", it.key) }
							?: LiceBundle.message("lice.messages.try-eval.function-unnamed")
				} ?: "$result: ${result.className()}"
				builder.insert(0, LiceBundle.message("lice.messages.try-eval.result", resultString))
				showPopupWindow(builder.toString(), editor, 0x0013F9, 0x000CA1)
			}
			return Ref.create(result)
		} catch (e: UncheckedTimeoutException) {
			builder.insertOutputIfNonBlank()
			builder.insert(0, LiceBundle.message("lice.messages.try-eval.timeout"))
			showPopupWindow(builder.toString(), editor, 0xEDC209, 0xC26500)
		} catch (original: Throwable) {
			val e = original.cause ?: original
			builder.insertOutputIfNonBlank()
			when (e) {
				is UseOfBannedFuncException -> {
					builder.insert(0, LiceBundle.message("lice.messages.try-eval.unsupported", e.name))
					showPopupWindow(builder.toString(), editor, 0xEDC209, 0xC26500)
				}
				is LiceException -> {
					builder.insert(0, LiceBundle.message("lice.messages.try-eval.exception",
							e.javaClass.simpleName, e.prettify(text.split("\n"))))
					showPopupWindow(builder.toString(), editor, 0xE20911, 0xC20022)
				}
				else -> {
					builder.insert(0, LiceBundle.message("lice.messages.try-eval.exception",
							e.javaClass.simpleName, e.message.orEmpty()))
					showPopupWindow(builder.toString(), editor, 0xE20911, 0xC20022)
				}
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
				JBPopupFactory
						.getInstance()
						.createHtmlTextBalloonBuilder(result, LiceIcons.LICE_BIG_ICON, JBColor(color, colorDark), null)
						.setFadeoutTime(8000)
						.setHideOnAction(true)
						.createBalloon()
						.show(relativePoint, Balloon.Position.below)
			}
		else
			ApplicationManager.getApplication().invokeLater {
				JBPopupFactory
						.getInstance()
						.createComponentPopupBuilder(JBUI.Panels.simplePanel()
								.addToTop(JLabel(LiceIcons.LICE_BIG_ICON))
								.addToCenter(ScrollPaneFactory.createScrollPane(JTextArea(result).apply {
									toolTipText = LiceBundle.message("lice.messages.try-eval.overflowed-text", textLimit)
									lineWrap = true
									wrapStyleWord = true
									isEditable = false
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
		AnAction(LiceBundle.message("lice.actions.try-eval.name"),
				LiceBundle.message("lice.actions.try-eval.description"), LiceIcons.LICE_BIG_ICON), DumbAware {
	private val core = TryEvaluate()
	override fun actionPerformed(event: AnActionEvent) {
		val editor = event.getData(CommonDataKeys.EDITOR) ?: return
		core.tryEval(editor, editor.selectionModel.selectedText ?: return, event.getData(CommonDataKeys.PROJECT), true)
	}

	override fun update(event: AnActionEvent) {
		event.presentation.isEnabledAndVisible = event.getData(CommonDataKeys.VIRTUAL_FILE)?.fileType == LiceFileType
	}
}
