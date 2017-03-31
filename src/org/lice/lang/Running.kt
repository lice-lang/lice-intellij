/**
 * Created by ice1000 on 2017/3/31.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBLabel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JPanel

object LiceConfigurationType : ConfigurationTypeBase(
		"LiceRunConfig",
		"Lice",
		"Lice Script",
		LiceInfo.LICE_BIG_ICON
) {
	init {
		addFactory(LiceConfigurationFactory)
	}
}

class LiceRunConfigPanel(

) : JPanel() {
	private var scriptComponent: TextFieldWithBrowseButton
	private var programParametersComponent: RawCommandLineEditor
	private var workingDirectoryComponent: TextFieldWithBrowseButton
	private var environmentVariables: EnvironmentVariablesComponent

	init {
		layout = GridBagLayout()
		scriptComponent = TextFieldWithBrowseButton()
		programParametersComponent = RawCommandLineEditor()
		workingDirectoryComponent = TextFieldWithBrowseButton()
		environmentVariables = EnvironmentVariablesComponent()
		val base: () -> GridBagConstraints = {
			GridBagConstraints().apply {
				insets = Insets(3, 3, 3, 3)
				anchor = GridBagConstraints.LINE_START
			}
		}

		val moduleLabel = JBLabel("Lice Script Path")
		add(moduleLabel, base().apply {
			gridx = 0
			gridy = 0
			weightx = 0.1
			fill = GridBagConstraints.HORIZONTAL
		})

		add(scriptComponent, base().apply {
			gridx = 1
			fill = GridBagConstraints.HORIZONTAL
			weightx = 1.0
		})

		add(JBLabel("Working directory"), base().apply {
			gridx = 0
			gridy = 1
			weightx = 0.1
		})

		add(workingDirectoryComponent, base().apply {
			gridx = 1
			gridy = 1
			weightx = 0.1
			fill = GridBagConstraints.HORIZONTAL
		})

		add(environmentVariables, base().apply {
			gridx = 0
			gridwidth = 2
			gridy = 2
			weightx = 0.1
			fill = GridBagConstraints.HORIZONTAL
		})
	}
}

class LiceRunConfiguration(
		project: Project,
		factory: ConfigurationFactory
) : RunConfigurationBase(project, factory, "Lice") {
	override fun checkConfiguration() = Unit
	override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
			object : SettingsEditor<LiceRunConfiguration>() {
				private val panel = LiceRunConfigPanel()

				override fun createEditor() = panel
				override fun resetEditorFrom(configuration: LiceRunConfiguration) = Unit
				override fun applyEditorTo(configuration: LiceRunConfiguration) = Unit
			}

	override fun getState(
			executor: Executor,
			environment: ExecutionEnvironment
	) = object : JavaCommandLineState(environment) {
		override fun createJavaParameters() = JavaParameters().apply {
			workingDirectory = project.basePath
			jarPath = LiceInfo.LICE_PATH
			classPath.add(LiceInfo.KOTLIN_RUNTIME_PATH)
			classPath.add(LiceInfo.KOTLIN_REFLECT_PATH)
			mainClass = "org.lice.repl.Main"
			vmParametersList.add("D:/git-repos/lice-intellij/test.lice")
		}
	}
}

object LiceConfigurationFactory : ConfigurationFactory(LiceConfigurationType) {
	override fun createTemplateConfiguration(project: Project) = LiceRunConfiguration(project, this)
}

