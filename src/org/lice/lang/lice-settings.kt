package org.lice.lang

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.colors.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizer
import com.intellij.ui.layout.panel
import org.intellij.lang.annotations.Language
import org.jdom.Element

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

class LiceConfigurationFactory(
		type: ConfigurationType) : ConfigurationFactory(type) {
	override fun getName() = "Lice configuration factory"
	override fun createTemplateConfiguration(project: Project) = LiceRunConfiguration(project, this)
}

class LiceRunConfiguration(
		project: Project,
		factory: ConfigurationFactory)
	: RunConfigurationBase(project, factory, LICE_NAME) {
	@JvmField var vmParams = ""
	@JvmField var jarLocation = ""

	override fun getConfigurationEditor() = LiceSettingsEditor(this, project)
	override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? = null
	override fun writeExternal(element: Element) {
		PathMacroManager.getInstance(project).expandPaths(element)
		super.writeExternal(element)
		JDOMExternalizer.write(element, "vmParams", vmParams)
		JDOMExternalizer.write(element, "jarLocation", jarLocation)
	}

	override fun readExternal(element: Element) {
		super.readExternal(element)
		vmParams = JDOMExternalizer.readString(element, "vmParams") ?: ""
		jarLocation = JDOMExternalizer.readString(element, "jarLocation") ?: ""
		PathMacroManager.getInstance(project).collapsePathsRecursively(element)
	}
}

class LiceSettingsEditor(
		@JvmField internal var settings: LiceRunConfiguration,
		@JvmField val project: Project) : SettingsEditor<LiceRunConfiguration>() {
	override fun createEditor() = panel(title = "Lice Run Configuration") {
		row("Lice jar path") {
			textFieldWithBrowseButton("Select Lice jar",
					fileChooserDescriptor = FileChooserDescriptor(false, false, true, false, false, false),
					project = project,
					fileChoosen = {
						settings.jarLocation = it.path
						settings.jarLocation
					})
		}
	}

	override fun applyEditorTo(configuration: LiceRunConfiguration) {
		configuration.vmParams = settings.vmParams
		configuration.jarLocation = settings.jarLocation
	}

	override fun resetEditorFrom(configuration: LiceRunConfiguration) {
		settings.vmParams = configuration.vmParams
		settings.jarLocation = configuration.jarLocation
	}
}
