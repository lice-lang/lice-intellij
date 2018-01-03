package org.lice.lang.psi

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.intellij.lang.annotations.Language
import org.lice.core.SymbolList
import org.lice.lang.LiceSyntaxHighlighter
import java.util.regex.Pattern

class LiceSymbolsExtractingAnnotator : Annotator {
	companion object RegExes {
		@Language("RegExp") private
		const val SYMBOL_CHAR = "[a-zA-Z!@\$^&_:=<|>?.\\\\+\\-~*/%\\[\\]#{}]"

		@Language("RegExp") private
		const val SYMBOL = "$SYMBOL_CHAR($SYMBOL_CHAR|[0-9])*"

		val SYMBOL_REGEX: Regex = Pattern.compile(SYMBOL).toRegex()
	}

	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		val methodCall = element as? PsiMethodCallExpression ?: return
		val callee = methodCall.methodExpression.children.firstOrNull { it is PsiExpression } as? PsiExpression ?: return
		val calleeType = callee.type ?: return
		if (calleeType.isValid && calleeType.canonicalText == SymbolList::class.java.canonicalName) {
			val method = methodCall.methodExpression.children.firstOrNull { it is PsiIdentifier } ?: return
			val possibleString = methodCall.argumentList.children
					.firstOrNull { it is PsiLiteralExpression } as? PsiLiteralExpression ?: return
			val str = possibleString.value as? String ?: return
			val isFunc = "Function" in method.text
			val isVar = "Variable" in method.text
			if ((isVar or isFunc) && !SYMBOL_REGEX.matches(str)) {
				holder.createWarningAnnotation(
						TextRange(possibleString.textRange.startOffset + 1, possibleString.textRange.endOffset - 1),
						"Not a valid Lice symbol, can't be used in Lice code")
			}
			if (isFunc) {
				holder.createInfoAnnotation(
						TextRange(possibleString.textRange.startOffset + 1, possibleString.textRange.endOffset - 1),
						"Lice function definition")
						.textAttributes = LiceSyntaxHighlighter.FUNCTION_DEFINITION
			} else if (isVar) {
				if (possibleString.value is String) holder.createInfoAnnotation(
						TextRange(possibleString.textRange.startOffset + 1, possibleString.textRange.endOffset - 1),
						"Lice variable definition")
						.textAttributes = LiceSyntaxHighlighter.VARIABLE_DEFINITION
			}
		}
	}
}