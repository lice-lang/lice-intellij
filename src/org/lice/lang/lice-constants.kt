/**
 * Created by ice1000 on 2017/3/29.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.facet.FacetTypeId
import com.intellij.openapi.util.IconLoader
import org.lice.lang.module.LiceFacet
import java.io.File

@JvmField val LICE_ICON = IconLoader.getIcon("/icons/lice.png")
@JvmField val LICE_BIG_ICON = IconLoader.getIcon("/icons/big_icon.png")
@JvmField val LICE_AST_LEAF_ICON = IconLoader.getIcon("/icons/ast_leaf.png")
@JvmField val LICE_AST_NODE_ICON = IconLoader.getIcon("/icons/ast_node.png")
@JvmField val LICE_AST_NODE2_ICON = IconLoader.getIcon("/icons/ast_node_2.png")
@JvmField val LICE_AST_NODE0_ICON = IconLoader.getIcon("/icons/ast_node_0.png")

const val LICE_EXTENSION = "lice"
const val LICE_NAME = "Lice"

@JvmField val LICE_PATH = "${System
		.getProperties()
		.getProperty("idea.plugins.path")}/lice-intellij/lib/lice.jar"

@JvmField val is32Bit = File("../jre").exists()
@JvmField val JAVA_PATH: String = File("../jre${if (is32Bit) "" else "64"}/bin/java.exe").absolutePath

@JvmField val JAVA_PATH_WRAPPED = "\"$JAVA_PATH\""
@JvmField val LICE_PATH_WRAPPED = "\"$LICE_PATH\""
@JvmField val KOTLIN_RUNTIME_PATH: String = File("../lib/kotlin-runtime.jar").absolutePath
@JvmField val KOTLIN_REFLECT_PATH: String = File("../lib/kotlin-reflect.jar").absolutePath

val LICE_FACET_ID = FacetTypeId<LiceFacet>(LICE_NAME)

const val LICE_MAIN_DEFAULT = "org.lice.repl.Main"
