package org.lice.lang.module

import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import org.lice.lang.LICE_BIG_ICON
import org.lice.lang.LICE_NAME
import java.util.jar.*
import javax.swing.JComponent


class LiceLibraryDescription : CustomLibraryDescription() {
	override fun getSuitableLibraryKinds() = LibraryPresentationProvider
			.EP_NAME
			.extensions
			.filter { it is LiceLibraryPresentationProvider }
			.map { it.kind }
			.toSet()

	override fun createNewLibrary(component: JComponent, file: VirtualFile?): NewLibraryConfiguration? {
	}
}

class LiceLibraryProperties(val version: String) : LibraryProperties<LiceLibraryProperties>() {
	override fun getState() = null
	override fun loadState(properties: LiceLibraryProperties?) = Unit
	override fun hashCode() = version.hashCode()
	override fun equals(other: Any?) =
			this === other || other is LiceLibraryProperties && other.version == version
}

class LiceLibraryPresentationProvider : LibraryPresentationProvider<LiceLibraryProperties>(LibraryKind.create(LICE_NAME)) {
	override fun getDescription(properties: LiceLibraryProperties) = "Lice library of version ${properties.version}"
	override fun getIcon(properties: LiceLibraryProperties?) = LICE_BIG_ICON
	override fun detect(classesRoots: List<VirtualFile>) =
			VfsUtilCore.toVirtualFileArray(classesRoots)
					.mapNotNull { it.sdkJarVersion }
					.firstOrNull()
					?.let(::LiceLibraryProperties)
}

/**
 * Return value of Implementation-Version attribute in jar manifest
 *
 * @return value of Implementation-Version attribute, null if not found
 */
val VirtualFile.sdkJarVersion
	get(): String? {
		try {
			JarFile(path).use { jarFile ->
				val jarEntry = jarFile.getJarEntry("META-INF/MANIFEST.MF")
				val inputStream = jarFile.getInputStream(jarEntry)
				val manifest: Manifest
				manifest = Manifest(inputStream)
				val version: String? = manifest.mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
				if (version != null) return version
			}
		} catch (e: Exception) {
		}
		return null
	}
