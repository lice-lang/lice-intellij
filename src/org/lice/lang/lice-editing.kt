/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.lang.*
import com.intellij.openapi.options.colors.*
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

class LiceColorSettingsPage : ColorSettingsPage {
	private companion object {
		val DESCRIPTORS = arrayOf(
				AttributesDescriptor("Symbol", LiceSyntaxHighlighter.SYMBOL),
				AttributesDescriptor("Bracket", LiceSyntaxHighlighter.BRACKET),
				AttributesDescriptor("String", LiceSyntaxHighlighter.STRING),
				AttributesDescriptor("Number", LiceSyntaxHighlighter.NUMBER),
				AttributesDescriptor("Ignored character", LiceSyntaxHighlighter.COMMENT),
				AttributesDescriptor("Direct function call", LiceSyntaxHighlighter.FUNCTION_DEFINITION))
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
(print "String", " ", "literals")
"""
}
