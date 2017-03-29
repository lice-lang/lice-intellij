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

class LiceModuleWizardSetup : ModuleBuilder() {
	override fun getBuilderId() = "lice.module.builder"
	override fun getGroupName() = "Lice"
	override fun getPresentableName() = "Lice"
	override fun setupRootModel(model: ModifiableRootModel) {
	}

	override fun getModuleType() = LiceModuleType()
	override fun createWizardSteps(
			wizardContext: WizardContext,
			modulesProvider: ModulesProvider
	): Array<ModuleWizardStep> =
			moduleType.createWizardSteps(wizardContext, this, modulesProvider)
}

class LiceModuleType : ModuleType<LiceModuleWizardSetup>(ID) {
	companion object LiceId {
		val ID = "LICE_MODULE_TYPE"
	}

	override fun createModuleBuilder() = LiceModuleWizardSetup()

	override fun getName() = "Lice"
	override fun getDescription() = "Empty Lice module"
	override fun getBigIcon() = LiceInfo.LICE_BIG_ICON
	override fun getNodeIcon(p0: Boolean) = bigIcon
}

