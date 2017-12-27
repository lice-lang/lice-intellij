/**
 * Created by ice1000 on 2017/3/6.
 *
 * @author ice1000
 */

package org.lice.tools

import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import org.lice.model.*
import org.lice.parse.buildNode
import org.lice.parse.mapAst
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.*

/**
 * map the ast
 */
private fun mapAst2Display(
		node: Node,
		viewRoot: UINode
): UINode = when (node) {
	is ValueNode,
	is SymbolNode,
	is LazyValueNode -> UINode(node)
	is ExpressionNode -> viewRoot.apply {
		add(mapAst2Display(node.node, UINode(node.node)))
		node.params.forEach { add(mapAst2Display(it, UINode(it))) }
	}
	else -> UINode("unknown node")
}

private fun createTreeRootFromFile(file: File) =
		mapAst(buildNode(file.readText())).let { ast -> Tree(mapAst2Display(ast, UINode(ast))) }

/**
 * @author ice1000
 */
fun displaySemanticTree(file: File) {
	val frame = JFrame("Lice Semantic Tree")
	ToolWindowFactory { project, toolWindow ->
	}
	frame.layout = BorderLayout()
	frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	frame.setLocation(80, 80)
	try {
		frame.add(JBScrollPane(createTreeRootFromFile(file).apply {
			preferredSize = Dimension(520, 520)
		}), BorderLayout.CENTER)
	} catch (e: RuntimeException) {
		val label = JTextArea(e.message)
		e.stackTrace.forEach { label.append("\t$it\n") }
		label.isEditable = false
		label.preferredSize = Dimension(600, 600)
		frame.add(label, BorderLayout.CENTER)
	}
	frame.pack()
	frame.isVisible = true
}
