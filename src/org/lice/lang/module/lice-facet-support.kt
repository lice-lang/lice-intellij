package org.lice.lang.module

import com.intellij.facet.*
import com.intellij.facet.ui.*
import com.intellij.ide.util.frameworkSupport.FrameworkVersion
import com.intellij.openapi.components.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.ui.Messages
import com.intellij.util.xmlb.XmlSerializerUtil
import icons.LiceIcons
import org.jdom.Element
import org.lice.lang.*
import org.lice.lang.execution.*

@State(
		name = "LiceFacetConfiguration",
		storages = [Storage(file = "liceConfig.xml", scheme = StorageScheme.DIRECTORY_BASED)])
class LiceFacetConfiguration : FacetConfiguration, PersistentStateComponent<LiceModuleSettings> {
	@Suppress("OverridingDeprecatedMember")
	override fun readExternal(element: Element?) = Unit

	@Suppress("OverridingDeprecatedMember")
	override fun writeExternal(element: Element?) = Unit

	val settings = LiceModuleSettings()
	override fun getState() = settings
	override fun createEditorTabs(
			context: FacetEditorContext?, manager: FacetValidatorsManager?) =
			arrayOf(LiceFacetSettingsTabImpl(settings, context?.project))

	override fun loadState(moduleSettings: LiceModuleSettings) {
		moduleSettings.let { XmlSerializerUtil.copyBean(it, settings) }
	}
}

object LiceFacetType :
		FacetType<LiceFacet, LiceFacetConfiguration>(LiceFacet.LICE_FACET_ID, LiceBundle.message("lice.name"), LiceBundle.message("lice.name")) {
	override fun createDefaultConfiguration() = LiceFacetConfiguration()
	override fun getIcon() = LiceIcons.LICE_BIG_ICON
	override fun isSuitableModuleType(type: ModuleType<*>?) = type != null
	override fun createFacet(module: Module, s: String?, configuration: LiceFacetConfiguration, facet: Facet<*>?) =
			LiceFacet(this, module, configuration, facet)
}

class LiceFacet(
		facetType: FacetType<LiceFacet, LiceFacetConfiguration>,
		module: Module,
		configuration: LiceFacetConfiguration,
		underlyingFacet: Facet<*>?) :
		Facet<LiceFacetConfiguration>(facetType, module, LiceBundle.message("lice.name"), configuration, underlyingFacet) {
	companion object InstanceHolder {
		@JvmField val LICE_FACET_ID = FacetTypeId<LiceFacet>(LiceBundle.message("lice.name"))
		fun getInstance(module: Module) = FacetManager.getInstance(module).getFacetByType(LICE_FACET_ID)
	}
}

class LiceFacetBasedFrameworkSupportProvider : FacetBasedFrameworkSupportProvider<LiceFacet>(LiceFacetType) {
	override fun getVersions() = LICE_STABLE_VERSION.map(::LiceSdkVersion)
	override fun getTitle() = LiceBundle.message("lice.name")
	override fun setupConfiguration(facet: LiceFacet, model: ModifiableRootModel, version: FrameworkVersion) {
		val orderEntry = model.orderEntries.firstOrNull { it.presentableName.contains("lice", true) } ?: return
		orderEntry.getFiles(OrderRootType.CLASSES).firstOrNull()?.let {
			val path = it.path.trimMysteriousPath()
			if (validateLice(path)) facet.configuration.settings.jarPath = path
			else Messages.showDialog(
					LiceBundle.message("lice.messages.invalid.body", path),
					LiceBundle.message("lice.messages.invalid.title"),
					arrayOf(LiceBundle.message("lice.messages.yes-yes-yes")),
					0,
					LiceIcons.JOJO_ICON)
		}
	}
}

