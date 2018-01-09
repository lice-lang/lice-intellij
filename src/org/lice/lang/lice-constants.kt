/**
 * Created by ice1000 on 2017/3/29.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.openapi.util.IconLoader
import org.jetbrains.annotations.NonNls
import java.nio.file.Files
import java.nio.file.Paths

@JvmField val LICE_ICON = IconLoader.getIcon("/icons/lice.png")
@JvmField val LICE_BIG_ICON = IconLoader.getIcon("/icons/big_icon.png")
@JvmField val LICE_AST_LEAF_ICON = IconLoader.getIcon("/icons/ast_leaf.png")
@JvmField val LICE_AST_NODE_ICON = IconLoader.getIcon("/icons/ast_node.png")
@JvmField val LICE_AST_NODE2_ICON = IconLoader.getIcon("/icons/ast_node_2.png")
@JvmField val LICE_AST_NODE0_ICON = IconLoader.getIcon("/icons/ast_node_0.png")
@JvmField val JOJO_ICON = IconLoader.getIcon("/icons/jojo.png")

@NonNls const val LICE_EXTENSION = "lice"
@NonNls const val LICE_NAME = "Lice"
@NonNls const val LICE_RUN_CONFIG_DECRIPTION = "Lice run configuration type"

@JvmField @NonNls val LICE_PATH = "${System.getProperty("idea.plugins.path")}/lice-intellij/lib/lice.jar"

@JvmField val is64Bit = Files.exists(Paths.get("../jre64"))
@JvmField val JAVA_PATH: String = Paths.get("../jre${if (!is64Bit) "" else "64"}/bin/java").toAbsolutePath().toString()
// @JvmField val JAVA_PATH = System.getProperty("java.home")

@JvmField @NonNls val KOTLIN_RUNTIME_PATH: String = Paths.get("../lib/kotlin-runtime.jar").toAbsolutePath().toString()
@JvmField @NonNls val KOTLIN_REFLECT_PATH: String = Paths.get("../lib/kotlin-reflect.jar").toAbsolutePath().toString()

@NonNls const val LICE_RUN_CONFIG_ID = "LICE_RUN_CONFIGURATION"
@NonNls const val LICE_MAIN_DEFAULT = "org.lice.repl.Main"
@NonNls const val URL_GITHUB = "https://github.com/lice-lang/lice/releases"

@JvmField @NonNls val LICE_VERSIONS: List<String> = listOf("3.2.0", "3.2.1")
