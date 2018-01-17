package org.lice.lang.module

import com.intellij.facet.ui.libraries.LibraryInfo
import com.intellij.ide.util.frameworkSupport.FrameworkVersion
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls
import org.lice.lang.*

class LiceSdkVersion(version: String) : FrameworkVersion(
		version,
		LiceBundle.message("lice.name"),
		arrayOf(createJarDownloadInfo(version)))

private fun makeLiceDownloadUrl(version: String) = "$URL_GITHUB/download/v$version/lice-$version-all.jar"
fun createJarDownloadInfo(@NonNls versionNullable: String) =
		versionNullable.let { version ->
			LibraryInfo("lice-$version-all.jar", makeLiceDownloadUrl(version), URL_GITHUB, null, LICE_MAIN_DEFAULT)
		}

val Project.moduleSettings
	get() = ModuleManager
			.getInstance(this)
			.modules
			.map(LiceFacet.InstanceHolder::getInstance)
			.firstOrNull()
			?.configuration
			?.settings
