package org.lice.lang.module

import com.intellij.facet.ui.libraries.LibraryInfo
import com.intellij.ide.util.frameworkSupport.FrameworkVersion
import org.jetbrains.annotations.NonNls
import org.lice.lang.*

class LiceSdkVersion(version: String) : FrameworkVersion(
		version,
		"Lice v$version",
		arrayOf(createJarDownloadInfo(version)))

private fun makeLiceDownloadUrl(version: String) = "$URL_GITHUB/download/v$version/lice-$version-all.jar"

fun createJarDownloadInfo(@NonNls versionNullable: String) =
		versionNullable.let { version ->
			LibraryInfo("lice-$version-all.jar", makeLiceDownloadUrl(version), URL_GITHUB, null, LICE_MAIN_DEFAULT)
		}
