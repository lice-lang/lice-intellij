package org.lice.lang.editing

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.lice.core.SymbolList
import org.lice.lang.LiceSyntaxHighlighter
import org.lice.lang.psi.*

object LiceSymbols {
	@JvmField val defFamily = listOf("def", "deflazy", "defexpr")
	@JvmField val setFamily = listOf("->", "<->")
	@JvmField val closureFamily = listOf("lambda", "expr", "lazy")
	@JvmField val miscFamily = listOf("thread|>", "force|>", "|>", "null", "true", "false")
	@JvmField val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@\$^&_:=<|>?.+\\-~*/%[]#{}"

	@JvmField val nameIntroducingFamily = defFamily + setFamily
	@JvmField val importantFamily = defFamily + setFamily + closureFamily + miscFamily
	@JvmField val allSymbols = SymbolList.preludeSymbols + SymbolList.preludeVariables
	@JvmField val allSymbolsForCompletion = SymbolList.preludeSymbols.map { "$it " } + SymbolList.preludeVariables
}

class LiceAnnotator : Annotator {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		when (element) {
			is LiceString -> {
				var isPrefixedByBackslash = false
				element.text.forEachIndexed { index, char ->
					isPrefixedByBackslash = if (isPrefixedByBackslash) {
						dealWithEscape(element, index, char, holder)
						false
					} else char == '\\'
				}
			}
			is LiceFunctionCall -> element.liceCallee?.let { callee ->
				if (callee.text in LiceSymbols.importantFamily)
					holder.createInfoAnnotation(TextRange(callee.textRange.startOffset, callee.textRange.endOffset), null)
							.textAttributes = LiceSyntaxHighlighter.IMPORTANT_SYMBOLS
				when (callee.text) {
					"undef" -> {
						val funUndefined = simplyCheckName(element, holder, callee, "function") ?: return@let
						if (funUndefined.text in LiceSymbols.allSymbols) {
							holder.createWeakWarningAnnotation(
									TextRange(funUndefined.textRange.startOffset, funUndefined.textRange.endOffset),
									"Trying to undef a standard function")
						}
					}
					"|>" -> {
						val ls = element.nonCommentElements
						if (ls.size <= 1)
							holder.createWeakWarningAnnotation(element, """Empty |> nodes can be replaced with "null"s""")
									.registerFix(LiceReplaceWithAnotherSymbolIntention(element, "null literal", "null"))
						else if (ls.size <= 2)
							holder.createWarningAnnotation(element, "Can be unwrapped")
									.registerFix(LiceReplaceWithAnotherElementIntention(element, "inner node", ls[1]))
					}
					in LiceSymbols.defFamily -> {
						val funDefined = simplyCheckName(element, holder, callee, "function") ?: return@let
						checkName(funDefined, holder)
						val symbol = funDefined.getSafeSymbol(holder, "Function") ?: return@let
						holder.createInfoAnnotation(symbol, null)
								.textAttributes = LiceSyntaxHighlighter.FUNCTION_DEFINITION
						if (element.nonCommentElements.size <= 2)
							missingBody(element, holder, "function body")
					}
					in LiceSymbols.setFamily -> {
						val varDefined = simplyCheckName(element, holder, callee, "variable") ?: return@let
						checkName(varDefined, holder)
						val symbol = varDefined.getSafeSymbol(holder, "Variable") ?: return
						holder.createInfoAnnotation(symbol, null)
								.textAttributes = LiceSyntaxHighlighter.VARIABLE_DEFINITION
						if (element.nonCommentElements.size <= 2)
							missingBody(element, holder, "variable value")
					}
					in LiceSymbols.closureFamily -> {
						val elementList = element.nonCommentElements
						(1..elementList.size - 2).firstOrNull { checkParameter(elementList[it], holder) }
						if (elementList.size <= 1)
							missingBody(element, holder, "lambda body")
					}
				}
			}
		}
		if (element is LiceNull)
			holder.createWeakWarningAnnotation(element, """Empty nodes can be replaced with "null"s""")
					.registerFix(LiceReplaceWithAnotherSymbolIntention(element, "null literal", "null"))
	}

	private fun LiceElement.getSafeSymbol(holder: AnnotationHolder, type: String) = symbol ?: run {
		holder.createErrorAnnotation(this, "$type name should be a symbol")
				.registerFix(LiceRemoveBlockIntention(this, "Remove current symbol"))
		null
	}

	private fun checkName(text: LiceElement, holder: AnnotationHolder) {
		if (text.text in SymbolList.preludeSymbols) {
			val range = TextRange(text.textRange.startOffset, text.textRange.endOffset)
			val txt = text.text
			(if (txt in LiceSymbols.importantFamily)
				holder.createErrorAnnotation(range, "Trying to overwrite an important standard name")
			else holder.createWeakWarningAnnotation(range, "Trying to overwrite a standard name"))
					.registerFix(LiceRemoveBlockIntention(text, "Remove dangerous statement"))
		}
	}

	private fun missingBody(element: LiceFunctionCall, holder: AnnotationHolder, type: String) {
		holder.createWarningAnnotation(
				TextRange(element.textRange.endOffset - 1, element.textRange.endOffset), "Missing $type")
	}

	/**
	 * @author ice1000
	 * @return null if unavailable
	 */
	private fun simplyCheckName(
			element: LiceFunctionCall,
			holder: AnnotationHolder,
			callee: PsiElement,
			type: String): LiceElement? {
		val elementList: MutableList<LiceElement> = element.nonCommentElements
		val elementCount = elementList.size
		if (elementCount <= 1) {
			holder.createWarningAnnotation(
					TextRange(callee.textRange.endOffset, element.textRange.endOffset),
					"Missing $type name")
			return null
		}
		(2..elementCount - 2)
				.firstOrNull { checkParameter(elementList[it], holder) }
		return elementList[1]
	}

	private fun checkParameter(el: LiceElement, holder: AnnotationHolder): Boolean {
		val symbol = el.getSafeSymbol(holder, "Parameter") ?: return true
		holder.createInfoAnnotation(symbol, null).textAttributes = LiceSyntaxHighlighter.PARAMETER
		return false
	}

	private fun dealWithEscape(element: PsiElement, index: Int, char: Char, holder: AnnotationHolder) {
		val range = TextRange(element.textRange.startOffset + index - 1, element.textRange.startOffset + index + 1)
		if (char !in "bfnrt\\\"'") holder.createErrorAnnotation(range, "Illegal escape character")
		else holder.createInfoAnnotation(range, null).textAttributes = LiceSyntaxHighlighter.STRING_ESCAPE
	}
}