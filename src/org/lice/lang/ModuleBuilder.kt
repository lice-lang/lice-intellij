/**
 * Created by ice1000 on 2017/3/29.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import javax.swing.JLabel

class LiceModuleWizardSetup : ModuleBuilder() {
	override fun setupRootModel(model: ModifiableRootModel) {
	}

	override fun getModuleType() = LiceModuleType()
	override fun createWizardSteps(
			wizardContext: WizardContext,
			modulesProvider: ModulesProvider): Array<ModuleWizardStep> = arrayOf(
			object : ModuleWizardStep() {
				override fun getComponent() = JLabel("Nothing to be done here.").apply {
				}

				override fun updateDataModel() {
				}
			}
	)
}

class LiceModuleType : ModuleType<LiceModuleWizardSetup>(ID) {
	companion object LiceId {
		val ID = "LICE_MODULE_TYPE"
	}

	override fun createModuleBuilder() = LiceModuleWizardSetup()

	override fun getName() = "Lice Module Type"
	override fun getDescription() = "Lice Module Type dec"
	override fun getBigIcon() = LiceInfo.LICE_BIG_ICON
	override fun getNodeIcon(p0: Boolean) = bigIcon
}

