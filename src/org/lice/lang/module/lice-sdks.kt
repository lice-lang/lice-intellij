package org.lice.lang.module

import com.intellij.facet.ui.libraries.LibraryInfo
import org.lice.lang.LICE_MAIN_DEFAULT
import org.lice.lang.URL_GITHUB

class LiceSDKs(@JvmField val version: String?) {
	@JvmField val jars = arrayOf(createJarDownloadInfo(version, LICE_MAIN_DEFAULT))
}


private fun makeLiceDownloadUrl(version: String) = "$URL_GITHUB/download/v$version/lice-$version-all.jar"

fun createJarDownloadInfo(versionNullable: String?, vararg requiredClasses: String) =
		(versionNullable ?: "3.2.0").let { version ->
			LibraryInfo("lice-$version-all.jar", makeLiceDownloadUrl(version), URL_GITHUB, null, * requiredClasses)
		}
