package org.lice.lang

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.colors.*
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import org.intellij.lang.annotations.Language
import java.awt.TextField
import javax.swing.JComponent

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

class LiceConfigurationType : ConfigurationType {
	override fun getIcon() = LICE_BIG_ICON
	override fun getDisplayName() = LICE_NAME
	override fun getConfigurationTypeDescription() = "Lice run configuration type"
	override fun getId() = "LICE_RUN_CONFIGURATION"
	override fun getConfigurationFactories() = arrayOf(LiceConfigurationFactory(this))
}

class LiceConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
	override fun getName() = "Lice configuration factory"
	override fun createTemplateConfiguration(project: Project) = LiceRunConfiguration(project, this)
}

class LiceRunConfiguration(project: Project, factory: ConfigurationFactory)
	: RunConfigurationBase(project, factory, LICE_NAME) {
	override fun getConfigurationEditor() = LiceSettingsEditor()
	override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? = null
}

class LiceSettingsEditor : SettingsEditor<LiceRunConfiguration>() {
	override fun createEditor() = panel {
		noteRow("Do something")
		row {  }
	}

	override fun applyEditorTo(configuration: LiceRunConfiguration) {
	}

	override fun resetEditorFrom(configuration: LiceRunConfiguration) {
	}
}
