package org.lice.lang.module

import com.intellij.facet.*
import com.intellij.facet.ui.*
import com.intellij.ide.util.frameworkSupport.FrameworkVersion
import com.intellij.openapi.components.*
import com.intellij.openapi.module.*
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jdom.Element
import org.lice.lang.*

class LiceModuleSettings {
	@JvmField var mainClass = LICE_MAIN_DEFAULT
	@JvmField var jarPath = LICE_PATH
}

@State(
		name = "LiceFacetConfiguration",
		storages = [Storage(file = "\$MODULE_FILE\$")])
class LiceFacetConfiguration : FacetConfiguration, PersistentStateComponent<LiceModuleSettings> {
	@Suppress("OverridingDeprecatedMember") override fun readExternal(p0: Element?) = Unit
	@Suppress("OverridingDeprecatedMember") override fun writeExternal(p0: Element?) = Unit
	private val settings = LiceModuleSettings()
	override fun getState() = settings
	override fun createEditorTabs(context: FacetEditorContext?, manager: FacetValidatorsManager?) = arrayOf(LiceFacetSettingsTab(settings))
	override fun loadState(moduleSettings: LiceModuleSettings?) {
		if (moduleSettings != null) XmlSerializerUtil.copyBean(moduleSettings, settings)
	}
}

object LiceFacetType : FacetType<LiceFacet, LiceFacetConfiguration>(LICE_FACET_ID, LICE_NAME, LICE_NAME) {
	override fun createDefaultConfiguration() = LiceFacetConfiguration()
	override fun getIcon() = LICE_BIG_ICON
	override fun isSuitableModuleType(type: ModuleType<*>?) = type is JavaModuleType || type?.id == "PLUGIN_MODULE"
	override fun createFacet(module: Module, s: String?, configuration: LiceFacetConfiguration, facet: Facet<*>?) =
			LiceFacet(this, module, configuration, facet)
}

class LiceFacet(
		facetType: FacetType<out Facet<*>, *>,
		module: Module,
		configuration: LiceFacetConfiguration,
		underlyingFacet: Facet<*>?) : Facet<LiceFacetConfiguration>(facetType, module, LICE_NAME, configuration, underlyingFacet) {
	constructor(module: Module) : this(facetType, module, LiceFacetConfiguration(), null)

	companion object {
		fun getInstance(module: Module) = FacetManager.getInstance(module).getFacetByType(LICE_FACET_ID)
		val facetType get() = FacetTypeRegistry.getInstance().findFacetType(LICE_FACET_ID)
	}
}

class LiceFacetBasedFrameworkSupportProvider : FacetBasedFrameworkSupportProvider<LiceFacet>(LiceFacetType) {
	override fun setupConfiguration(facet: LiceFacet?, model: ModifiableRootModel?, version: FrameworkVersion?) = Unit
}