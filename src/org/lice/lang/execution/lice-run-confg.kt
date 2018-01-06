package org.lice.lang.execution

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizer
import org.jdom.Element
import org.lice.lang.LICE_NAME

class LiceRunConfiguration(
		project: Project,
		factory: ConfigurationFactory)
	: RunConfigurationBase(project, factory, LICE_NAME) {
	var vmParams = ""
	var workingDir = ""
	var programArgument = ""
	var jarLocation = ""
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
	override fun createEditor() = LiceRunConfigurationEditor(project).createEditor()
	override fun applyEditorTo(configuration: LiceRunConfiguration) {
		configuration.vmParams = settings.vmParams
		configuration.jarLocation = settings.jarLocation
	}

	override fun resetEditorFrom(configuration: LiceRunConfiguration) {
		settings.vmParams = configuration.vmParams
		settings.jarLocation = configuration.jarLocation
	}
}

class LiceRunConfigState
