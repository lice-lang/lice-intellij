package org.lice.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.lice.core.SymbolList
import org.lice.lang.psi.*


class LiceSyntaxHighlighter : SyntaxHighlighter {
	companion object {
		@JvmField val SYMBOL = TextAttributesKey.createTextAttributesKey("LICE_SYMBOL", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
		@JvmField val NUMBER = TextAttributesKey.createTextAttributesKey("LICE_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
		@JvmField val COMMENT = TextAttributesKey.createTextAttributesKey("LICE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
		@JvmField val STRING = TextAttributesKey.createTextAttributesKey("LICE_STRING", DefaultLanguageHighlighterColors.STRING)
		@JvmField val BRACKET = TextAttributesKey.createTextAttributesKey("LICE_BRACKET", DefaultLanguageHighlighterColors.BRACKETS)
		@JvmField val FUNCTION_DEFINITION = TextAttributesKey.createTextAttributesKey("LICE_FUNCTION_DEF", DefaultLanguageHighlighterColors.STATIC_METHOD)
		@JvmField val VARIABLE_DEFINITION = TextAttributesKey.createTextAttributesKey("LICE_VARIABLE_DEF", DefaultLanguageHighlighterColors.STATIC_FIELD)
		@JvmField val UNRESOLVED_SYMBOL = TextAttributesKey.createTextAttributesKey("LICE_UNRESOLVED", HighlighterColors.TEXT)
		@JvmField val IMPORTANT_SYMBOLS = TextAttributesKey.createTextAttributesKey("LICE_IMPORTANT_SYMBOLS", DefaultLanguageHighlighterColors.KEYWORD)
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
		LiceTypes.NUM -> NUMBER_KEYS
		LiceTypes.COMMENT -> COMMENT_KEYS
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
		private val setFamily = listOf("->", "<->")
	}

	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		if (element is LiceMethodCall) element.callee?.let { callee ->
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
			if (isImportant(txt))
				holder.createErrorAnnotation(range, "Trying to overwrite an important standard name")
			else holder.createWarningAnnotation(range, "Trying to overwrite a standard name")
		}
		return text
	}

	private fun simplyCheckName(element: LiceMethodCall, holder: AnnotationHolder, callee: ASTNode, type: String): LiceElement? {
		if (isImportant(callee.text)) holder.createInfoAnnotation(TextRange(callee.textRange.startOffset, callee.textRange.endOffset), null)
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

	private fun isImportant(txt: String) = txt in defFamily || txt in setFamily || txt == "undef"
}
