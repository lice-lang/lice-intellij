package org.lice.lang

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.options.colors.*
import com.intellij.openapi.project.Project
import org.intellij.lang.annotations.Language
import org.lice.lang.execution.LiceRunConfiguration
import org.lice.lang.execution.trimMysteriousPath

class LiceColorSettingsPage : ColorSettingsPage {
	private companion object {
		private val DESCRIPTORS = arrayOf(
				AttributesDescriptor("Symbols//Ordinary symbols", LiceSyntaxHighlighter.SYMBOL),
				AttributesDescriptor("Symbols//Important symbols", LiceSyntaxHighlighter.IMPORTANT_SYMBOLS),
				AttributesDescriptor("Bracket", LiceSyntaxHighlighter.BRACKET),
				AttributesDescriptor("Strings//String literals", LiceSyntaxHighlighter.STRING),
				AttributesDescriptor("Strings//Escape characters", LiceSyntaxHighlighter.STRING_ESCAPE),
				AttributesDescriptor("Strings//Invalid escape characters", LiceSyntaxHighlighter.STRING_ESCAPE_INVALID),
				AttributesDescriptor("Number", LiceSyntaxHighlighter.NUMBER),
				AttributesDescriptor("Ignored character", LiceSyntaxHighlighter.COMMENT),
				AttributesDescriptor("Definitions//Function definition", LiceSyntaxHighlighter.FUNCTION_DEFINITION),
				AttributesDescriptor("Definitions//Variable definition", LiceSyntaxHighlighter.VARIABLE_DEFINITION),
				AttributesDescriptor("Definitions//Parameter name", LiceSyntaxHighlighter.PARAMETER),
				AttributesDescriptor("Reference//Unresolved function", LiceSyntaxHighlighter.UNRESOLVED_SYMBOL))
		private val MAPS = mapOf(
				"unresolved" to LiceSyntaxHighlighter.UNRESOLVED_SYMBOL,
				"reservedWord" to LiceSyntaxHighlighter.IMPORTANT_SYMBOLS,
				"functionName" to LiceSyntaxHighlighter.FUNCTION_DEFINITION,
				"variableName" to LiceSyntaxHighlighter.VARIABLE_DEFINITION,
				"parameterName" to LiceSyntaxHighlighter.PARAMETER,
				"strEscape" to LiceSyntaxHighlighter.STRING_ESCAPE,
				"strEscapeInvalid" to LiceSyntaxHighlighter.STRING_ESCAPE_INVALID
		)
	}

	override fun getAdditionalHighlightingTagToDescriptorMap() = MAPS
	override fun getHighlighter() = LiceSyntaxHighlighter()
	override fun getIcon() = LICE_ICON
	override fun getDisplayName() = LICE_NAME
	override fun getAttributeDescriptors() = DESCRIPTORS
	override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
	@Language("Lice")
	override fun getDemoText() = """
;; comments
(<reservedWord>def</reservedWord> <functionName>fib</functionName> <parameterName>n</parameterName>
	(if (<= n 2)
		1
		(+ (fib (- n 1)) (fib (- n 2)))))

(undef <unresolved>unresolved-reference</unresolved>)
(-> <variableName>some-var</variableName> 233)

;; command line output
(print "Strin<strEscapeInvalid>\g</strEscapeInvalid>", "literals<strEscape>\n</strEscape>")
"""
}

object LiceConfigurationType : ConfigurationType {
	override fun getIcon() = LICE_BIG_ICON
	override fun getDisplayName() = LICE_NAME
	override fun getConfigurationTypeDescription() = LICE_RUN_CONFIG_DESCRIPTION
	override fun getId() = LICE_RUN_CONFIG_ID
	override fun getConfigurationFactories() = arrayOf(LiceConfigurationFactory(this))
}

class LiceConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
	override fun createTemplateConfiguration(project: Project) = LiceRunConfiguration(project, this)
}

class LiceModuleSettings {
	var tryEvaluateTextLimit = 300
	var tryEvaluateTimeLimit = 1500L
	var mainClass = LICE_MAIN_DEFAULT
	var jarPath = licePath
		set(value) {
			field = value.trimMysteriousPath()
		}
}
