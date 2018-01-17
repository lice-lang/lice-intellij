package org.lice.lang.execution

import com.intellij.execution.CommonJavaRunConfigurationParameters
import com.intellij.execution.Executor
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.jdom.Element
import org.lice.lang.*
import org.lice.lang.module.moduleSettings
import java.util.jar.*

class LiceRunConfiguration(
		project: Project,
		factory: ConfigurationFactory,
		var targetFile: String = "")
	: LocatableConfigurationBase(project, factory, LiceBundle.message("lice.name")),
		CommonJavaRunConfigurationParameters {
	var jreLocation = JAVA_PATH
	var jarLocation = project.moduleSettings?.jarPath.orEmpty()

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
	override fun getConfigurationEditor() = LiceRunConfigurationEditor(this)
	override fun getState(executor: Executor, environment: ExecutionEnvironment) = LiceCommandLineState(this, environment)
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
		jreLocation = configuration.jreLocation
		targetFile = configuration.targetFile
	}
}

@JvmField val jarChooser = FileChooserDescriptor(false, false, true, false, false, false)
fun String.trimMysteriousPath() = trimEnd('/', '!', '"', ' ', '\n', '\t', '\r').trimStart(' ', '\n', '\t', '\r')
fun validateLice(path: String) = LICE_MAIN_DEFAULT == findMainClass(path)
fun findMainClass(path: String) = try {
	JarFile(path).use { jarFile ->
		val inputStream = jarFile.getInputStream(jarFile.getJarEntry("META-INF/MANIFEST.MF"))
		Manifest(inputStream).mainAttributes.getValue(Attributes.Name.MAIN_CLASS)
	}
} catch (e: Exception) {
	null
}

class LiceRunConfigurationProducer : RunConfigurationProducer<LiceRunConfiguration>(LiceConfigurationType) {
	override fun isConfigurationFromContext(
			configuration: LiceRunConfiguration, context: ConfigurationContext) =
			configuration.targetFile == context
					.location
					?.virtualFile
					?.path
					?.trimMysteriousPath()

	override fun setupConfigurationFromContext(
			configuration: LiceRunConfiguration, context: ConfigurationContext, ref: Ref<PsiElement>?): Boolean {
		if (context.psiLocation?.containingFile !is LiceFile) return false
		configuration.targetFile = context.location?.virtualFile?.path.orEmpty().trimMysteriousPath()
		configuration.workingDirectory = context.project.basePath.orEmpty()
		configuration.jarLocation = context.project.moduleSettings?.jarPath.orEmpty()
		return true
	}
}
