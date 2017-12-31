/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.lang.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.intellij.lang.annotations.Language
import org.lice.lang.psi.LiceTypes

class LiceCommenter : Commenter {
	override fun getCommentedBlockCommentPrefix() = blockCommentPrefix
	override fun getCommentedBlockCommentSuffix() = blockCommentSuffix
	override fun getBlockCommentPrefix() = null
	override fun getBlockCommentSuffix() = null
	override fun getLineCommentPrefix() = "; "
}

class LiceBraceMatcher : PairedBraceMatcher {
	private companion object Pairs {
		private val PAIRS = arrayOf(BracePair(LiceTypes.LEFT_BRACKET, LiceTypes.RIGHT_BRACKET, false))
	}

	override fun getPairs() = PAIRS
	override fun getCodeConstructStart(psiFile: PsiFile, openingBraceOffset: Int) = openingBraceOffset
	override fun isPairedBracesAllowedBeforeType(type: IElementType, elementType: IElementType?) = true
}

// class LiceLiveTemplateContext : FileTypeBasedContextType("Lice", "Lice", LiceFileType)

class LiceLiveTemplateProvider : DefaultLiveTemplatesProvider {
	override fun getDefaultLiveTemplateFiles() = arrayOf("liveTemplates/Lice")
	override fun getHiddenLiveTemplateFiles() = null
}

class LiceSyntaxHighlighter : SyntaxHighlighter {
	companion object {
		@JvmField val SYMBOL = TextAttributesKey.createTextAttributesKey("LICE_SYMBOL", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
		@JvmField val NUMBER = TextAttributesKey.createTextAttributesKey("LICE_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
		@JvmField val COMMENT = TextAttributesKey.createTextAttributesKey("LICE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
		@JvmField val STRING = TextAttributesKey.createTextAttributesKey("LICE_STRING", DefaultLanguageHighlighterColors.STRING)
		@JvmField val BRACKET = TextAttributesKey.createTextAttributesKey("LICE_BRACKET", DefaultLanguageHighlighterColors.BRACKETS)
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
		LiceTypes.COMMENT -> COMMENT_KEYS
	// TokenType.WHITE_SPACE -> arrayOf()
		else -> arrayOf()
	}

	override fun getHighlightingLexer() = LiceLexerAdapter()
}

class LiceSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
	override fun getSyntaxHighlighter(project: Project?, file: VirtualFile?) = LiceSyntaxHighlighter()
}

class LiceColorSettingsPage : ColorSettingsPage {
	private companion object {
		val DESCRIPTORS = arrayOf(
				AttributesDescriptor("Symbol", LiceSyntaxHighlighter.SYMBOL),
				AttributesDescriptor("Bracket", LiceSyntaxHighlighter.BRACKET),
				AttributesDescriptor("String", LiceSyntaxHighlighter.STRING),
				AttributesDescriptor("Number", LiceSyntaxHighlighter.NUMBER))
	}

	override fun getHighlighter() = LiceSyntaxHighlighter()
	override fun getIcon() = LICE_ICON
	override fun getDisplayName() = LICE_NAME
	override fun getAdditionalHighlightingTagToDescriptorMap() = null
	override fun getAttributeDescriptors() = DESCRIPTORS
	override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
	@Language("Lice")
	override fun getDemoText() = """
;; comments
(def fib n
	(if (in? (list 1 2) n)
		1
		(+ (fib (- n 1)) (fib (- n 2)))))

;; command line output
(print "String literals")
"""
}
