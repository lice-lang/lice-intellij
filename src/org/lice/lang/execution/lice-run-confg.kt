package org.lice.lang.execution

import com.intellij.execution.CommonJavaRunConfigurationParameters
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizer
import org.jdom.Element
import org.lice.lang.JAVA_PATH
import org.lice.lang.LICE_NAME
import org.lice.lang.module.LiceFacet

class LiceRunConfiguration(
		project: Project,
		factory: ConfigurationFactory,
		var targetFile: String = "")
	: RunConfigurationBase(project, factory, LICE_NAME),
		CommonJavaRunConfigurationParameters {
	var jreLocation = JAVA_PATH
	var jarLocation = ModuleManager
			.getInstance(project)
			.modules
			.map(LiceFacet.InstanceHolder::getInstance)
			.firstOrNull()
			?.configuration
			?.settings
			?.jarPath
			.orEmpty()

	private var vmParams = ""
	private var workingDir = ""
	override fun setAlternativeJrePath(s: String?) = Unit
	override fun setProgramParameters(s: String?) = Unit
	override fun getEnvs() = mutableMapOf<String, String>()
	override fun isPassParentEnvs() = true
	override fun isAlternativeJrePathEnabled() = false
	override fun getPackage() = null
	override fun getRunClass() = null
	override fun getWorkingDirectory() = workingDir
	override fun getVMParameters() = vmParams
	override fun setAlternativeJrePathEnabled(bool: Boolean) = Unit
	override fun setPassParentEnvs(bool: Boolean) = Unit
	override fun setEnvs(map: MutableMap<String, String>) = Unit
	override fun getProgramParameters() = null
	override fun getAlternativeJrePath() = null
	override fun setWorkingDirectory(s: String?) = s.orEmpty().let { workingDir = it }
	override fun setVMParameters(s: String?) = s.orEmpty().let { vmParams = it }
	override fun getConfigurationEditor() = LiceRunConfigurationEditor(project, this)
	override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? = null
	override fun writeExternal(element: Element) {
		PathMacroManager.getInstance(project).expandPaths(element)
		super.writeExternal(element)
		JDOMExternalizer.write(element, "vmParams", vmParams)
		JDOMExternalizer.write(element, "jarLocation", jarLocation)
		JDOMExternalizer.write(element, "workingDir", workingDir)
		JDOMExternalizer.write(element, "targetFile", targetFile)
	}

	override fun readExternal(element: Element) {
		super.readExternal(element)
		JDOMExternalizer.readString(element, "vmParams")?.let { vmParams = it }
		JDOMExternalizer.readString(element, "jarLocation")?.let { jarLocation = it }
		JDOMExternalizer.readString(element, "workingDir")?.let { workingDir = it }
		JDOMExternalizer.readString(element, "targetFile")?.let { targetFile = it }
		PathMacroManager.getInstance(project).collapsePathsRecursively(element)
	}

	fun replaceNonJavaCommonStatesWith(configuration: LiceRunConfiguration) {
		jarLocation = configuration.jarLocation
		targetFile = configuration.targetFile
	}
}

@JvmField val jarChooser = FileChooserDescriptor(false, false, true, false, false, false)
