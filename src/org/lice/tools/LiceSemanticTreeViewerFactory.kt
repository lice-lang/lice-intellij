package org.lice.tools

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import org.lice.model.*
import org.lice.parse.buildNode
import org.lice.parse.mapAst
import java.awt.Dimension
import java.io.File
import javax.swing.JComponent
import javax.swing.JTextArea
import javax.swing.tree.DefaultMutableTreeNode

object LiceSemanticTreeViewerFactory {
	/**
	 * map the ast
	 */
	private fun mapAst2Display(
			node: Node,
			viewRoot: DefaultMutableTreeNode
	): DefaultMutableTreeNode = when (node) {
		is ValueNode,
		is SymbolNode,
		is LazyValueNode -> DefaultMutableTreeNode(node)
		is ExpressionNode -> viewRoot.apply {
			add(mapAst2Display(node.node, DefaultMutableTreeNode(node.node)))
			node.params.forEach { add(mapAst2Display(it, DefaultMutableTreeNode(it))) }
		}
		else -> DefaultMutableTreeNode("unknown node")
	}

	private fun createTreeRootFromFile(file: File) =
			mapAst(buildNode(file.readText())).let { ast -> Tree(mapAst2Display(ast, DefaultMutableTreeNode(ast))) }

	fun create(file: File): JComponent = try {
		JBScrollPane(createTreeRootFromFile(file).apply {
			preferredSize = Dimension(520, 520)
		})
	} catch (e: Exception) {
		JTextArea(e.message)
	}
}
