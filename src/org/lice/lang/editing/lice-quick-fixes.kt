package org.lice.lang.editing

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import org.lice.core.Func
import org.lice.lang.*
import org.lice.lang.action.TryEvaluate
import org.lice.lang.psi.LiceElement
import org.lice.lang.psi.LiceFunctionCall
import org.lice.util.forceRun
import java.math.BigDecimal
import java.math.BigInteger

class LiceRemoveBlockIntention(private val element: PsiElement, private val intentionText: String) :
		BaseIntentionAction() {
	override fun getText() = intentionText
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LiceBundle.message("lice.name")
	override operator fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
		element.delete()
	}
}

class LiceReplaceWithAnotherSymbolIntention(
		private val element: PsiElement,
		private val anotherSymbolName: String,
		private val anotherSymbolCode: String) : BaseIntentionAction() {
	override fun getText() = LiceBundle.message("lice.lint.fix.replace-with", anotherSymbolName)
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LiceBundle.message("lice.name")
	override operator fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
		val symbol = PsiFileFactory
				.getInstance(project)
				.createFileFromText(LiceLanguage, anotherSymbolCode)
				.let { it as? LiceFile }
				?.firstChild ?: return
		element.replace(symbol)
	}
}

class LiceTryReplaceEvaluatedResultIntention(
		private var element: LiceFunctionCall) : BaseIntentionAction() {
	private val text = LiceBundle.message("lice.lint.fix.try-eval", cutText(element.text, SHORT_TEXT_MAX))
	override fun getText() = text
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LiceBundle.message("lice.name")
	override operator fun invoke(project: Project, editor: Editor, psiFile: PsiFile?) {
		val eval = TryEvaluate()
		val selectedText = editor.selectionModel.selectedText
		val result = (eval.tryEval(editor, selectedText ?: element.text, project, false)
				?: run {
					element.isPossibleEval = false
					return
				}).get()

		@Suppress("UNCHECKED_CAST")
		fun convert(res: Any?, isOuterPair: Boolean = false): String = when (res) {
			is String -> """"${res
					.replace("\n", "\\n")
					.replace("\r", "\\r")
					.replace("\u000C", "\\f")
					.replace("\t", "\\t")}""""
			is BigInteger -> "${res}N"
			is BigDecimal -> "${res.toPlainString()}M"
			is Iterable<*> -> "(list ${res.joinToString(" ") { convert(it) }})"
			is Array<*> -> "(array ${res.joinToString(" ") { convert(it) }})"
			is Pair<*, *> ->
				if (isOuterPair) "${convert(res.first)} ${convert(res, true)}"
				else "([|] ${convert(res.first)} ${convert(res.second, true)})"
			is Long -> "${res}L"
			is Short -> "${res}S"
			is Byte -> "${res}B"
			is Double -> "${res}D"
			is Float -> "${res}F"
			is Boolean, is Number -> "$res"
			null -> "null"
			else -> (res as? Func)?.let { f -> eval.prelude.firstOrNull { it.value == f }?.key }
					?: throw UnsupportedOperationException(LiceBundle.message("lice.lint.fix.cannot-convert", res))
		}

		val code = try {
			convert(result)
		} catch (e: UnsupportedOperationException) {
			element.isPossibleEval = false
			eval.showPopupWindow(e.message.orEmpty(), editor, 0xEDC209, 0xC26500)
			return
		}
		val symbol = PsiFileFactory
				.getInstance(project)
				.createFileFromText(LiceLanguage, code)
				.let { it as? LiceFile }
				?.firstChild ?: run {
			element.isPossibleEval = false
			return
		}
		(symbol as? LiceElement)?.functionCall?.run { isPossibleEval = false }
		forceRun {
			if (selectedText != null && selectedText.indexOf(element.text) < 0) {
				val sub = element
						.children
						.filter { it.text.isNotBlank() }
						.sortedByDescending { it.textLength }
						.firstOrNull { it.textOffset >= editor.selectionModel.selectionStart && it.textLength <= selectedText.length }
				(sub ?: element).replace(symbol)
			} else element.replace(symbol)
		}
	}
}

class LiceReplaceWithAnotherElementIntention(
		private val element: PsiElement,
		private val anotherSymbolName: String,
		private val anotherSymbolNode: PsiElement) : BaseIntentionAction() {
	override fun getText() = LiceBundle.message("lice.lint.fix.replace-with", anotherSymbolName)
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LiceBundle.message("lice.name")
	override operator fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
		element.replace(anotherSymbolNode)
	}
}
