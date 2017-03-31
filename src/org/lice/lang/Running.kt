/**
 * Created by ice1000 on 2017/3/31.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
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

class LiceRunConfiguration(
		project: Project,
		factory: ConfigurationFactory
) : RunConfigurationBase(project, factory, "Lice") {
	override fun checkConfiguration() = Unit
	override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
			object : SettingsEditor<LiceRunConfiguration>() {
				override fun createEditor() = JPanel()
				override fun resetEditorFrom(configuration: LiceRunConfiguration) = Unit
				override fun applyEditorTo(configuration: LiceRunConfiguration) = Unit
			}

	override fun getState(p0: Executor, p1: ExecutionEnvironment): RunProfileState? {
		return null
	}

}

object LiceConfigurationFactory : ConfigurationFactory(LiceConfigurationType) {
	override fun createTemplateConfiguration(p0: Project) = LiceRunConfiguration(p0, this)
}

