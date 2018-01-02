package org.lice.lang.editing

import com.intellij.lang.ASTNode
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.lice.core.SymbolList
import org.lice.lang.LiceSyntaxHighlighter
import org.lice.lang.psi.LiceElement
import org.lice.lang.psi.LiceMethodCall

object LiceSymbols {
	@JvmField val defFamily = listOf("def", "deflazy", "defexpr")
	@JvmField val setFamily = listOf("->", "<->")
	@JvmField val closureFamily = listOf("lambda", "expr", "lazy")

	@JvmField val importantFamily = defFamily + setFamily + closureFamily
	@JvmField val allSymbols = SymbolList.preludeSymbols + SymbolList.preludeVariables
}

class LiceAnnotator : Annotator {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		if (element is LiceMethodCall) element.liceCallee?.let { callee ->
			if (callee.text in LiceSymbols.importantFamily) holder.createInfoAnnotation(TextRange(callee.textRange.startOffset, callee.textRange.endOffset), null)
					.textAttributes = LiceSyntaxHighlighter.IMPORTANT_SYMBOLS
			when (callee.text) {
				"undef" -> {
					val funUndefined = simplyCheckName(element, holder, callee, "function") ?: return@let
					if (funUndefined.text in SymbolList.preludeSymbols) {
						holder.createWeakWarningAnnotation(
								TextRange(funUndefined.textRange.startOffset, funUndefined.textRange.endOffset),
								"Trying to undef a standard function")
					}
				}
				in LiceSymbols.defFamily -> {
					val funDefined = simplyCheckName(element, holder, callee, "function") ?: return@let
					checkName(funDefined, holder)
					val symbol = funDefined.getSafeSymbol(holder, "Function") ?: return@let
					holder.createInfoAnnotation(symbol, null).textAttributes = LiceSyntaxHighlighter.FUNCTION_DEFINITION
					if (element.elementList.size <= 2) missingBody(holder, element, "function body")
				}
				in LiceSymbols.setFamily -> {
					val varDefined = simplyCheckName(element, holder, callee, "variable") ?: return
					checkName(varDefined, holder)
					val symbol = varDefined.getSafeSymbol(holder, "Variable") ?: return
					holder.createInfoAnnotation(symbol, null).textAttributes = LiceSyntaxHighlighter.VARIABLE_DEFINITION
					if (element.elementList.size <= 2) missingBody(holder, element, "variable value")
				}
				in LiceSymbols.closureFamily -> {
					val elementList = element.elementList
					for (i in 1..elementList.size - 2) if (checkParameter(elementList[i], holder)) break
					if (elementList.size <= 1) missingBody(holder, element, "lambda body")
				}
			}
		}
	}

	private fun LiceElement.getSafeSymbol(holder: AnnotationHolder, type: String) = symbol ?: run {
		holder.createErrorAnnotation(textRange, "$type name should be a symbol")
		return@run null
	}

	private fun checkName(text: LiceElement, holder: AnnotationHolder) {
		if (text.text in SymbolList.preludeSymbols) {
			val range = TextRange(text.textRange.startOffset, text.textRange.endOffset)
			val txt = text.text
			if (txt in LiceSymbols.importantFamily)
				holder.createErrorAnnotation(range, "Trying to overwrite an important standard name")
			else holder.createWeakWarningAnnotation(range, "Trying to overwrite a standard name")
		}
	}

	private fun missingBody(holder: AnnotationHolder, element: LiceMethodCall, type: String) {
		holder.createWarningAnnotation(
				TextRange(element.textRange.endOffset - 1, element.textRange.endOffset), "Missing $type")
	}

	/**
	 * @author ice1000
	 * @return null if unavailable
	 */
	private fun simplyCheckName(element: LiceMethodCall, holder: AnnotationHolder, callee: ASTNode, type: String): LiceElement? {
		val elementList: MutableList<LiceElement> = element.elementList
		val elementCount = elementList.size
		if (elementCount <= 1) {
			holder.createWarningAnnotation(
					TextRange(callee.textRange.endOffset, element.textRange.endOffset + 1),
					"Missing $type name")
			return null
		}
		for (i in 2..elementCount - 2) if (checkParameter(elementList[i], holder)) break
		return elementList[1]
	}

	private fun checkParameter(el: LiceElement, holder: AnnotationHolder): Boolean {
		val symbol = el.getSafeSymbol(holder, "Parameter") ?: return true
		holder.createInfoAnnotation(symbol, null).textAttributes = LiceSyntaxHighlighter.PARAMETER
		return false
	}

}