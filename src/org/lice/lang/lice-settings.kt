package org.lice.lang

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.options.colors.*
import com.intellij.openapi.project.Project
import icons.LiceIcons
import org.lice.lang.execution.LiceRunConfiguration
import org.lice.lang.execution.trimMysteriousPath

class LiceColorSettingsPage : ColorSettingsPage {
	private companion object {
		private val DESCRIPTORS = arrayOf(
				AttributesDescriptor(LiceBundle.message("lice.settings.color.symbols.ordinary"), LiceSyntaxHighlighter.SYMBOL),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.symbols.important"), LiceSyntaxHighlighter.IMPORTANT_SYMBOLS),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.bracket"), LiceSyntaxHighlighter.BRACKET),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.strings.literals"), LiceSyntaxHighlighter.STRING),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.strings.escape"), LiceSyntaxHighlighter.STRING_ESCAPE),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.strings.invalid"), LiceSyntaxHighlighter.STRING_ESCAPE_INVALID),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.number"), LiceSyntaxHighlighter.NUMBER),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.ignored"), LiceSyntaxHighlighter.COMMENT),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.def.function"), LiceSyntaxHighlighter.FUNCTION_DEFINITION),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.def.variable"), LiceSyntaxHighlighter.VARIABLE_DEFINITION),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.def.parameter"), LiceSyntaxHighlighter.PARAMETER),
				AttributesDescriptor(LiceBundle.message("lice.settings.color.symbols.unresolved"), LiceSyntaxHighlighter.UNRESOLVED_SYMBOL))
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
	override fun getIcon() = LiceIcons.LICE_ICON
	override fun getDisplayName() = LiceBundle.message("lice.name")
	override fun getAttributeDescriptors() = DESCRIPTORS
	override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
	// @Language("Lice")
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
	override fun getIcon() = LiceIcons.LICE_BIG_ICON
	override fun getDisplayName() = LiceBundle.message("lice.name")
	override fun getConfigurationTypeDescription() = LiceBundle.message("lice.run.config.description")
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
	var jarPath = liceJarPath
		set(value) {
			field = value.trimMysteriousPath()
		}
}
