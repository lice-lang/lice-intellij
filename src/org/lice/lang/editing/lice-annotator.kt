package org.lice.lang.editing

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.Annotation
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.lice.core.SymbolList
import org.lice.lang.LiceBundle
import org.lice.lang.LiceSyntaxHighlighter
import org.lice.lang.psi.*

object LiceSymbols {
	@JvmField val defFamily = listOf("def", "deflazy", "defexpr")
	@JvmField val setFamily = listOf("->", "<->")
	@JvmField val closureFamily = listOf("lambda", "expr", "lazy")
	@JvmField val conditionedFamily = listOf("if", "while", "when")
	@JvmField val miscFamily = listOf("force|>", "|>", "null", "true", "false", "undef")
	const val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@\$^&_:=<|>?.+\\-~*/%[]#{}"

	@JvmField val nameIntroducingFamily = defFamily + setFamily
	@JvmField val importantFamily = defFamily + setFamily + closureFamily + miscFamily + conditionedFamily
	@JvmField val allSymbols = SymbolList.preludeSymbols + SymbolList.preludeVariables

	fun checkName(text: PsiElement, holder: AnnotationHolder, name: String? = null) {
		val range = text.textRange
		val txt = name ?: text.text
		val namingMessage = "Use Lice style identifier: "
		var fix: String? = null
		var annotation: Annotation? = null
		when {
			'_' in txt -> {
				fix = txt.replace('_', '-')
				annotation = holder.createWeakWarningAnnotation(text, "$namingMessage$fix")
			}
			txt.startsWith("is-", true) -> {
				fix = "${txt.substring(3)}?"
				annotation = holder.createWeakWarningAnnotation(text, "$namingMessage$fix")
			}
			txt.contains("-to-", true) -> {
				fix = txt.replace("-to-", "->")
				annotation = holder.createWeakWarningAnnotation(text, "$namingMessage$fix")
			}
			txt.contains("to-", true) -> {
				fix = txt.replace("to-", "->")
				annotation = holder.createWeakWarningAnnotation(text, "$namingMessage$fix")
			}
			txt in LiceSymbols.importantFamily -> holder.createErrorAnnotation(range, "Trying to overwrite an important standard name")
			txt in LiceSymbols.allSymbols -> holder.createWeakWarningAnnotation(range, "Trying to overwrite a standard name")
		}
		if (text is LiceSymbol) {
			text.isResolved = true
			if (fix != null && annotation != null) annotation.registerFix(LiceReplaceWithAnotherSymbolIntention(text, "better name", fix))
		}
	}
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
				when (callee.text) {
					"undef" -> {
						val funUndefined = simplyCheckName(element, holder, callee,
								LiceBundle.message("lice.lint.func")) ?: return@let
						if (funUndefined.text in LiceSymbols.allSymbols)
							holder.createWeakWarningAnnotation(funUndefined, LiceBundle.message("lice.lint.undef-std"))
					}
					"|>" -> {
						if (element.nonCommentElements.size <= 1)
							holder.createWeakWarningAnnotation(element, LiceBundle.message("lice.lint.replace-run-with-null"))
									.registerFix(LiceReplaceWithAnotherSymbolIntention(element,
											LiceBundle.message("lice.lint.null"), "null"))
						else if (element.nonCommentElements.size <= 2)
							holder.createWarningAnnotation(element, LiceBundle.message("lice.lint.can-unwrap"))
									.registerFix(LiceReplaceWithAnotherElementIntention(element,
											LiceBundle.message("lice.lint.inner-node"), element.nonCommentElements[1]))
						checkForTryEval(element, holder)
					}
					in LiceSymbols.defFamily -> {
						val funDefined = simplyCheckName(element, holder, callee,
								LiceBundle.message("lice.lint.func")) ?: return@let
						val symbol = funDefined.getSafeSymbol(holder, LiceBundle.message("lice.lint.func")) ?: return@let
						LiceSymbols.checkName(symbol, holder)
						holder.createInfoAnnotation(symbol, null).textAttributes = LiceSyntaxHighlighter.FUNCTION_DEFINITION
						if (element.nonCommentElements.size <= 2)
							missingBody(element, holder, LiceBundle.message("lice.lint.func-body"))
					}
					in LiceSymbols.setFamily -> {
						val varDefined = simplyCheckName(element, holder, callee,
								LiceBundle.message("lice.lint.var")) ?: return@let
						val symbol = varDefined.getSafeSymbol(holder, LiceBundle.message("lice.lint.var")) ?: return
						LiceSymbols.checkName(symbol, holder)
						holder.createInfoAnnotation(symbol, null)
								.textAttributes = LiceSyntaxHighlighter.VARIABLE_DEFINITION
						if (element.nonCommentElements.size <= 2)
							missingBody(element, holder, LiceBundle.message("lice.lint.var-value"))
					}
					in LiceSymbols.closureFamily -> {
						val elementList = element.nonCommentElements
						(1..elementList.size - 2).firstOrNull { checkParameter(elementList[it], holder) }
						if (elementList.size <= 1)
							missingBody(element, holder, LiceBundle.message("lice.lint.lambda-body"))
					}
					in LiceSymbols.conditionedFamily -> {

					}
				}
				checkForTryEval(element, holder)
			}
			is LiceSymbol -> {
				if (element.text in LiceSymbols.importantFamily)
					holder.createInfoAnnotation(element, null).textAttributes = LiceSyntaxHighlighter.IMPORTANT_SYMBOLS
				if (!element.isResolved) {
					if (element.text in LiceSymbols.allSymbols) element.isResolved = true
					else holder.createInfoAnnotation(element, LiceBundle.message("lice.lint.unresolved",
							cutText(element.text, SHORT_TEXT_MAX))).run {
						textAttributes = LiceSyntaxHighlighter.UNRESOLVED_SYMBOL
						setNeedsUpdateOnTyping(true)
					}
				}
			}
			is LiceNull -> holder.createWeakWarningAnnotation(element, LiceBundle.message("lice.lint.replace-empty-with-null"))
					.registerFix(LiceReplaceWithAnotherSymbolIntention(element, LiceBundle.message("lice.lint.null"), "null"))
		}
	}

	private fun checkForTryEval(element: LiceFunctionCall, holder: AnnotationHolder) {
		if (element.isPossibleEval) holder.createInfoAnnotation(element, LiceBundle.message("lice.lint.can-eval",
				cutText(element.text, SHORT_TEXT_MAX)))
				.registerFix(LiceTryReplaceEvaluatedResultIntention(element))
	}

	private fun LiceElement.getSafeSymbol(holder: AnnotationHolder, type: String) = symbol ?: run {
		holder.createErrorAnnotation(this, LiceBundle.message("lice.lint.should-be-symbol", type))
				.registerFix(LiceRemoveBlockIntention(this, LiceBundle.message("lice.lint.remove-symbol")))
		null
	}

	private fun missingBody(element: LiceFunctionCall, holder: AnnotationHolder, type: String) {
		holder.createWarningAnnotation(
				TextRange(element.textRange.endOffset - 1, element.textRange.endOffset),
				LiceBundle.message("lice.lint.missing", type))
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
		val elementList: List<LiceElement> = element.nonCommentElements
		val elementCount = elementList.size
		if (elementCount <= 1) {
			holder.createWarningAnnotation(
					TextRange(callee.textRange.endOffset, element.textRange.endOffset),
					LiceBundle.message("lice.lint.missing-name", type))
			return null
		}
		(2..elementCount - 2).firstOrNull { checkParameter(elementList[it], holder) }
		return elementList[1]
	}

	private fun checkParameter(el: LiceElement, holder: AnnotationHolder): Boolean {
		val symbol = el.getSafeSymbol(holder, LiceBundle.message("lice.lint.parameter")) ?: return true
		symbol.isResolved = true
		holder.createInfoAnnotation(symbol, null).textAttributes = LiceSyntaxHighlighter.PARAMETER
		return false
	}

	private fun dealWithEscape(element: PsiElement, index: Int, char: Char, holder: AnnotationHolder) {
		val range = TextRange(element.textRange.startOffset + index - 1, element.textRange.startOffset + index + 1)
		if (char !in "nt\\\"bfr'") holder.createErrorAnnotation(range, LiceBundle.message("lice.lint.illegal-escape"))
		else holder.createInfoAnnotation(range, null).textAttributes = LiceSyntaxHighlighter.STRING_ESCAPE
	}
}