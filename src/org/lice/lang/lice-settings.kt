package org.lice.lang

import com.intellij.openapi.options.colors.*
import org.intellij.lang.annotations.Language

class LiceColorSettingsPage : ColorSettingsPage {
	private companion object {
		val DESCRIPTORS = arrayOf(
				AttributesDescriptor("Symbols//Ordinary symbols", LiceSyntaxHighlighter.SYMBOL),
				AttributesDescriptor("Symbols//Important symbols", LiceSyntaxHighlighter.IMPORTANT_SYMBOLS),
				AttributesDescriptor("Bracket", LiceSyntaxHighlighter.BRACKET),
				AttributesDescriptor("String", LiceSyntaxHighlighter.STRING),
				AttributesDescriptor("Number", LiceSyntaxHighlighter.NUMBER),
				AttributesDescriptor("Ignored character", LiceSyntaxHighlighter.COMMENT),
				AttributesDescriptor("Definitions//Function definition", LiceSyntaxHighlighter.FUNCTION_DEFINITION),
				AttributesDescriptor("Definitions//Variable definition", LiceSyntaxHighlighter.VARIABLE_DEFINITION),
				AttributesDescriptor("Reference//Unresolved function", LiceSyntaxHighlighter.UNRESOLVED_SYMBOL))
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
	(if (<= n 2)
		1
		(+ (fib (- n 1)) (fib (- n 2)))))

(undef unresolved-reference)

;; command line output
(print "String", "literals")
"""
}