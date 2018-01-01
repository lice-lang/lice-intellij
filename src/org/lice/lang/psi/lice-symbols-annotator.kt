package org.lice.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.lice.core.SymbolList
import org.lice.lang.LiceSyntaxHighlighter

class LiceAnnotator : Annotator {
	companion object {
		private val defFamily = listOf("def", "deflazy", "defexpr")
		private val setFamily = listOf("->", "<->")
		private val closureFamily = listOf("lambda", "expr", "lazy")

		private val importantFamily = defFamily + setFamily + closureFamily
	}

	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		if (element is LiceMethodCall) element.liceCallee?.let { callee ->
			when (callee.text) {
				"undef" -> {
					val funUndefined = simplyCheckName(element, holder, callee, "function") ?: return@let
					if (funUndefined.text in SymbolList.preludeSymbols) {
						holder.createWarningAnnotation(
								TextRange(funUndefined.textRange.startOffset, funUndefined.textRange.endOffset),
								"Trying to undef a standard function")
					}
				}
				in setFamily -> dealWithSetFamily(element, holder, callee)
				in defFamily -> dealWithDefFamily(element, holder, callee)
			}
		}
	}

	private fun dealWithDefFamily(element: LiceMethodCall, holder: AnnotationHolder, callee: ASTNode) {
		val funDefined = checkName(element, holder, callee, "function or variable") ?: return
		val symbol = funDefined.symbol ?: run {
			holder.createErrorAnnotation(
					TextRange(funDefined.textRange.startOffset, funDefined.textRange.endOffset),
					"Function name should be a symbol")
			return
		}
		holder.createInfoAnnotation(TextRange(symbol.textRange.startOffset, symbol.textRange.endOffset), null)
				.textAttributes = LiceSyntaxHighlighter.FUNCTION_DEFINITION
		if (element.elementList.size <= 2) {
			holder.createErrorAnnotation(
					TextRange(element.textRange.endOffset - 1, element.textRange.endOffset),
					"Missing function body")
			return
		}
	}

	private fun dealWithSetFamily(element: LiceMethodCall, holder: AnnotationHolder, callee: ASTNode) {
		val varDefined = checkName(element, holder, callee, "variable") ?: return
		val symbol = varDefined.symbol ?: run {
			holder.createErrorAnnotation(
					TextRange(varDefined.textRange.startOffset, varDefined.textRange.endOffset),
					"Variable name should be a symbol")
			return
		}
		holder.createInfoAnnotation(TextRange(symbol.textRange.startOffset, symbol.textRange.endOffset), null)
				.textAttributes = LiceSyntaxHighlighter.VARIABLE_DEFINITION
		if (element.elementList.size <= 2) {
			holder.createErrorAnnotation(
					TextRange(element.textRange.endOffset - 1, element.textRange.endOffset),
					"Missing variable value")
			return
		}
	}

	/**
	 * @author ice1000
	 * @return null if unavailable
	 */
	private fun checkName(element: LiceMethodCall, holder: AnnotationHolder, callee: ASTNode, type: String): LiceElement? {
		val text = simplyCheckName(element, holder, callee, type) ?: return null
		if (text.text in SymbolList.preludeSymbols) {
			val range = TextRange(text.textRange.startOffset, text.textRange.endOffset)
			val txt = text.text
			if (txt in importantFamily)
				holder.createErrorAnnotation(range, "Trying to overwrite an important standard name")
			else holder.createWarningAnnotation(range, "Trying to overwrite a standard name")
		}
		return text
	}

	private fun simplyCheckName(element: LiceMethodCall, holder: AnnotationHolder, callee: ASTNode, type: String): LiceElement? {
		if (callee.text in importantFamily) holder.createInfoAnnotation(TextRange(callee.textRange.startOffset, callee.textRange.endOffset), null)
				.textAttributes = LiceSyntaxHighlighter.IMPORTANT_SYMBOLS
		val elementCount = element.elementList.size
		if (elementCount <= 1) {
			holder.createErrorAnnotation(
					TextRange(callee.textRange.endOffset, element.textRange.endOffset + 1),
					"Missing $type name")
			return null
		}
		return element.elementList[1]
	}

}