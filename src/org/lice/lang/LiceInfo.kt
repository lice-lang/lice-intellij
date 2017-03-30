/**
 * Created by ice1000 on 2017/3/29.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.openapi.util.IconLoader
import java.io.File

object LiceInfo {
	val LICE_ICON = IconLoader.getIcon("/icons/lice.png")
	val LICE_BIG_ICON = IconLoader.getIcon("/icons/big_icon.png")
	val EXTENSION = "lice"
	val LANGUAGE_NAME = "Lice"
	val LICE_PATH = "${System
			.getProperties()
			.getProperty("idea.plugins.path")}/lice-intellij/lib/lice.jar"
	val JAVA_EXECUTABLE_PATH = File(
			if (File("../jre").exists()) "../jre/bin/java.exe"
			else "../jre64/bin/java.exe"
	).absolutePath!!
}
