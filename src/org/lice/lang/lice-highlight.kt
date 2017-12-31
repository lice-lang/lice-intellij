package org.lice.lang

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.lice.lang.psi.*


class LiceSyntaxHighlighter : SyntaxHighlighter {
	companion object {
		@JvmField val SYMBOL = TextAttributesKey.createTextAttributesKey("LICE_SYMBOL", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
		@JvmField val NUMBER = TextAttributesKey.createTextAttributesKey("LICE_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
		@JvmField val COMMENT = TextAttributesKey.createTextAttributesKey("LICE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
		@JvmField val STRING = TextAttributesKey.createTextAttributesKey("LICE_STRING", DefaultLanguageHighlighterColors.STRING)
		@JvmField val BRACKET = TextAttributesKey.createTextAttributesKey("LICE_BRACKET", DefaultLanguageHighlighterColors.BRACKETS)
		@JvmField val FUNCTION_DEFINITION = TextAttributesKey.createTextAttributesKey("LICE_FUNCTION_CALL", DefaultLanguageHighlighterColors.STATIC_METHOD)
		private val SYMBOL_KEYS = arrayOf(SYMBOL)
		private val NUMBER_KEYS = arrayOf(NUMBER)
		private val COMMENT_KEYS = arrayOf(COMMENT)
		private val STRING_KEYS = arrayOf(STRING)
		private val BRACKET_KEYS = arrayOf(BRACKET)
	}

	override fun getTokenHighlights(type: IElementType?): Array<TextAttributesKey> = when (type) {
		LiceTypes.RIGHT_BRACKET,
		LiceTypes.LEFT_BRACKET -> BRACKET_KEYS
		LiceTypes.STR -> STRING_KEYS
		LiceTypes.SYM -> SYMBOL_KEYS
		LiceTypes.NUMBER -> NUMBER_KEYS
		LiceTypes.COMMENT, TokenType.WHITE_SPACE -> COMMENT_KEYS
		else -> arrayOf()
	}

	override fun getHighlightingLexer() = LiceLexerAdapter()
}

class LiceSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
	override fun getSyntaxHighlighter(project: Project?, file: VirtualFile?) = LiceSyntaxHighlighter()
}

class LiceAnnotator : Annotator {
	companion object {
		private val defFamily = listOf("def", "deflazy", "defexpr")
	}

	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		if (element is LiceMethodCall) element.callee?.let { callee ->
			if (callee.text in defFamily) {
				val elementCount = element.elementList.size
				if (elementCount <= 1) {
					holder.createErrorAnnotation(
							TextRange(callee.textRange.startOffset, callee.textRange.endOffset),
							"Missing function name")
					return@let
				}
				val functionToBeDefined: LiceElement = element.elementList[1]
				val symbol = functionToBeDefined.symbol ?: run {
					holder.createErrorAnnotation(
							TextRange(functionToBeDefined.textRange.startOffset, functionToBeDefined.textRange.endOffset),
							"Function name expected")
					return@let
				}
				holder.createInfoAnnotation(TextRange(symbol.textRange.startOffset, symbol.textRange.endOffset), null)
						.textAttributes = LiceSyntaxHighlighter.FUNCTION_DEFINITION
				if (elementCount <= 2) {
					holder.createErrorAnnotation(
							TextRange(element.textRange.endOffset - 1, element.textRange.endOffset),
							"Missing function body")
					return@let
				}
			}
		}
	}
}
