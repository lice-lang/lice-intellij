package org.lice.lang.editing

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import org.lice.lang.*
import org.lice.lang.actions.TryEvaluate
import org.lice.lang.psi.LiceFunctionCall
import java.math.BigDecimal
import java.math.BigInteger

class LiceRemoveBlockIntention(private val element: PsiElement, private val intentionText: String) :
		BaseIntentionAction() {
	override fun getText() = intentionText
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LICE_NAME
	override operator fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
		element.delete()
	}
}

class LiceReplaceWithAnotherSymbolIntention(
		private val element: PsiElement,
		private val anotherSymbolName: String,
		private val anotherSymbolCode: String) : BaseIntentionAction() {
	override fun getText() = "Replace with $anotherSymbolName"
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LICE_NAME
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
	override fun getText() = "Try replacing with evaluated result"
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LICE_NAME
	override operator fun invoke(project: Project, editor: Editor, psiFile: PsiFile?) {
		val eval = TryEvaluate()
		val selectedText = editor.selectionModel.selectedText
		val result = (eval.tryEval(editor, selectedText ?: element.text, project, false)
				?: run {
					element.isPossibleEval = false
					return
				}).get()
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
		if (selectedText != null && selectedText.indexOf(element.text) < 0) {
			val sub = element
					.children
					.sortedByDescending { it.textLength }
					.firstOrNull { it.textOffset >= editor.selectionModel.selectionStart && it.textLength <= selectedText.length }
			(sub ?: element).replace(symbol)
		} else element.replace(symbol)
	}

	private fun convert(result: Any?, isOuterPair: Boolean = false): String = when (result) {
		is String -> """"${result
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\u000C", "\\f")
				.replace("\t", "\\t")}""""
		is BigInteger -> "${result}N"
		is BigDecimal -> "${result.toPlainString()}M"
		is List<*> -> "(list ${result.joinToString(" ") { convert(it) }})"
		is Array<*> -> "(array ${result.joinToString(" ") { convert(it) }})"
		is Pair<*, *> ->
			if (isOuterPair) "${convert(result.first)} ${convert(result, true)}"
			else "([|] ${convert(result.first)} ${convert(result.second, true)})"
		is Long -> "${result}L"
		is Short -> "${result}S"
		is Byte -> "${result}B"
		is Double -> "${result}D"
		is Float -> "${result}F"
		is Number -> "$result"
		is Boolean -> result.toString()
		null -> "null"
		else -> throw UnsupportedOperationException("Cannot convert $result\nto a valid lice expression")
	}
}

class LiceReplaceWithAnotherElementIntention(
		private val element: PsiElement,
		private val anotherSymbolName: String,
		private val anotherSymbolNode: PsiElement) : BaseIntentionAction() {
	override fun getText() = "Replace with $anotherSymbolName"
	override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true
	override fun getFamilyName() = LICE_NAME
	override operator fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
		element.replace(anotherSymbolNode)
	}
}
