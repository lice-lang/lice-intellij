package org.lice.lang.tool

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.io.readText
import org.lice.model.*
import org.lice.parse.*
import java.awt.Dimension
import java.nio.file.Path
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

	private fun createTreeRootFromFile(file: Path) = Parser
			.parseTokenStream(Lexer(file.readText()))
			.accept(Sema())
			.let { ast -> Tree(mapAst2Display(ast, DefaultMutableTreeNode(ast))) }

	fun create(file: Path): JComponent = try {
		JBScrollPane(createTreeRootFromFile(file).apply {
			preferredSize = Dimension(520, 520)
		})
	} catch (e: Exception) {
		JTextArea(e.message)
	}
}

/*
/**
 * @author ice1000
 */
fun displaySemanticTree(file: File) {
	private fun File.neighbour() = "$parent/$name-edited-${System.currentTimeMillis()}.lice"
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
 */