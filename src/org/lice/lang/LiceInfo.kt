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
	val LICE_AST_LEAF_ICON = IconLoader.getIcon("/icons/ast_leaf.png")
	val LICE_AST_NODE_ICON = IconLoader.getIcon("/icons/ast_node.png")
	val LICE_AST_NODE2_ICON = IconLoader.getIcon("/icons/ast_node_2.png")
	val LICE_AST_NODE0_ICON = IconLoader.getIcon("/icons/ast_node_0.png")

	val EXTENSION = "lice"
	val LANGUAGE_NAME = "Lice"

	val LICE_PATH = "${System
			.getProperties()
			.getProperty("idea.plugins.path")}/lice-intellij/lib/lice.jar"
	val is32Bit = File("../jre").exists()
	val JAVA_PATH: String = File(
			if (is32Bit) "../jre/bin/java.exe"
			else "../jre64/bin/java.exe"
	).absolutePath
	val JAVA_PATH_WRAPPED = "\"" + JAVA_PATH + "\""
	val LICE_PATH_WRAPPED = "\"" + LICE_PATH + "\""
	val KOTLIN_RUNTIME_PATH = File("../lib/kotlin-runtime.jar").absolutePath!!
	val KOTLIN_REFLECT_PATH = File("../lib/kotlin-reflect.jar").absolutePath!!
}
