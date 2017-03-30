/**
 * Created by ice1000 on 2017/3/30.
 *
 * @author ice1000
 */
package org.lice.gui

import com.intellij.ide.ui.laf.darcula.DarculaLaf
import com.intellij.ui.components.JBScrollPane
import org.lice.compiler.model.StringLeafNode
import org.lice.compiler.model.StringMiddleNode
import org.lice.compiler.model.StringNode
import org.lice.compiler.parse.buildNode
import org.lice.repl.VERSION_CODE
import java.awt.BorderLayout
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

typealias UINode = DefaultMutableTreeNode

/**
 * map the ast
 */
private fun mapAst2Display(
		node: StringNode,
		viewRoot: UINode
): UINode = when (node) {
	is StringLeafNode -> UINode(node)
	is StringMiddleNode -> viewRoot.apply {
		node.list
				.subList(1, node.list.size)
				.forEach { add(mapAst2Display(it, UINode(it))) }
	}
	else -> UINode("null")
}

/**
 * map the ast
 */
private fun mapDisplay2Ast(
		node: UINode,
		gen: StringBuilder,
		numOfIndents: Int = 0) {
	if (numOfIndents == 0) gen.append("\n")
	when {
		node.isLeaf -> gen
				.append(" ")
				.append(node.userObject.toString())
				.append("")
		else -> {
			gen
					.append("\n")
					.append("  ".repeat(numOfIndents))
					.append("(")
					.append(node.userObject.toString())
			node.children()
					.toList()
//					.map { it as UINode }
					.forEach {
						mapDisplay2Ast(
								it as UINode,
								gen,
								numOfIndents + 1
						)
					}
			gen.append(")")
		}
	}
}

private fun createTreeRootFromFile(file: File): UINode {
	val ast = buildNode(file.readText())
	return mapAst2Display(ast, UINode(ast))
}

/**
 * @author ice1000
 */
fun main(args: Array<String>) {
	UIManager.setLookAndFeel(DarculaLaf())
	val frame = JFrame("Lice language Syntax Tree Viewer $VERSION_CODE")
	frame.layout = BorderLayout()
	frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
	frame.setLocation(80, 80)
	frame.setSize(480, 480)
	fun File.neighbour() = "$parent/$name-edited-${System.currentTimeMillis()}.lice"
	val file = File(args[0])
	val root = createTreeRootFromFile(file)
	frame.add(
			JBScrollPane(JTree(root).apply { isEditable = true }),
			BorderLayout.CENTER
	)
	val button = JButton("Export Lice Code")
	button.addActionListener {
		val sb = StringBuilder()
		root.children()
				.toList()
				.forEach {
					mapDisplay2Ast(it as UINode, sb)
				}
		val name = file.neighbour()
		File(name)
				.apply { if (!exists()) createNewFile() }
				.writeText(sb.toString())
		JOptionPane.showMessageDialog(frame, """Successfully Export to:
$name""")
	}
	frame.add(button, BorderLayout.SOUTH)
	frame.isVisible = true
}