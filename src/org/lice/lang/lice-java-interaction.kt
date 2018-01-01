package org.lice.lang

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.lice.core.SymbolList

class LiceSymbolsExtractingAnnotator : Annotator {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		val methodCall = element as? PsiMethodCallExpression ?: return
		val callee = methodCall.methodExpression.children.first { it is PsiExpression } as? PsiExpression ?: return
		val calleeType = callee.type ?: return
		if (calleeType.isValid && calleeType.canonicalText == SymbolList::class.java.canonicalName) {
			val method = methodCall.methodExpression.children.first { it is PsiIdentifier }
			// SymbolList().defineFunction()
			// SymbolList().provideFunctionWithMeta()
			// SymbolList().provideFunction()
			if (method.isValid) {
				if ("Function" in method.text) {
					val possibleString = methodCall.argumentList.children
							.first { it is PsiLiteralExpression } as? PsiLiteralExpression ?: return
					if (possibleString.value is String) holder.createInfoAnnotation(
							TextRange(possibleString.textRange.startOffset + 1, possibleString.textRange.endOffset - 1), null)
							.textAttributes = LiceSyntaxHighlighter.FUNCTION_DEFINITION
				} else if ("Variable" in method.text) {
					val possibleString = methodCall.argumentList.children
							.first { it is PsiLiteralExpression } as? PsiLiteralExpression ?: return
					if (possibleString.value is String) holder.createInfoAnnotation(
							TextRange(possibleString.textRange.startOffset + 1, possibleString.textRange.endOffset - 1), null)
							.textAttributes = LiceSyntaxHighlighter.VARIABLE_DEFINITION
				}
			}
		}
	}
}