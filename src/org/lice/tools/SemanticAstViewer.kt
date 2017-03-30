/**
 * Created by ice1000 on 2017/3/6.
 *
 * @author ice1000
 */

package org.lice.tools

import com.intellij.ide.ui.laf.darcula.DarculaLaf
import com.intellij.ui.components.JBScrollPane
import org.lice.compiler.model.*
import org.lice.compiler.parse.buildNode
import org.lice.compiler.parse.mapAst
import org.lice.lang.LiceInfo
import org.lice.repl.VERSION_CODE
import java.awt.BorderLayout
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
	is LambdaNode -> UINode(node)
	is ExpressionNode -> viewRoot.apply {
		node.params.forEach { add(mapAst2Display(it, UINode(it))) }
	}
	else -> UINode("unknown node")
}

private fun createTreeRootFromFile(file: File): JTree {
	val ast = mapAst(buildNode(file.readText()))
	return JTree(mapAst2Display(ast, UINode(ast)))
}

/**
 * @author ice1000
 */
fun displaySemanticTree(file: File) {
	UIManager.setLookAndFeel(DarculaLaf())
	UIManager.put("Tree.collapsedIcon", LiceInfo.LICE_AST_NODE0_ICON)
	UIManager.put("Tree.expandedIcon", LiceInfo.LICE_AST_NODE_ICON)
	UIManager.put("Tree.openIcon", LiceInfo.LICE_AST_NODE0_ICON)
	UIManager.put("Tree.closedIcon", LiceInfo.LICE_AST_NODE2_ICON)
	UIManager.put("Tree.leafIcon", LiceInfo.LICE_AST_LEAF_ICON)
	val frame = JFrame("Lice Semantic Tree $VERSION_CODE")
	frame.layout = BorderLayout()
	frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	frame.setLocation(80, 80)
	frame.setSize(480, 480)
	frame.add(
			JBScrollPane(createTreeRootFromFile(file)),
			BorderLayout.CENTER
	)
	frame.isVisible = true
}
