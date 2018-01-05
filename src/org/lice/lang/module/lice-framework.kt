package org.lice.lang.module

import com.intellij.framework.FrameworkTypeEx
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.libraries.LibraryPresentationProvider
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.vfs.VirtualFile
import org.lice.lang.LICE_BIG_ICON
import org.lice.lang.LICE_NAME
import javax.swing.JComponent

class LiceFrameworkType : FrameworkTypeEx(LICE_NAME) {
	override fun getIcon() = LICE_BIG_ICON
	override fun getPresentableName() = LICE_NAME
	override fun createProvider() = LiceSupportInModuleProvider(this)
}

class LiceSupportInModuleProvider(val type: LiceFrameworkType) : FrameworkSupportInModuleProvider() {
	override fun getFrameworkType() = type
	override fun createConfigurable(model: FrameworkSupportModel) = LiceSupportInModuleConfigurable()
	override fun isEnabledForModuleType(type: ModuleType<*>) = type.name.contains("java", ignoreCase = true) or type.name.contains("kotlin", ignoreCase = true)
}

class LiceSupportInModuleConfigurable : FrameworkSupportInModuleConfigurable() {
	override fun createComponent() = null
	override fun createLibraryDescription() = LiceLibraryDescription()
	override fun addSupport(module: Module, model: ModifiableRootModel, provider: ModifiableModelsProvider) {
	}
}
