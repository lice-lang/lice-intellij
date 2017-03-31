/**
 * Created by ice1000 on 2017/3/6.
 *
 * @author ice1000
 */

package org.lice.tools

import com.intellij.ui.components.JBScrollPane
import org.lice.compiler.model.*
import org.lice.compiler.parse.buildNode
import org.lice.compiler.parse.mapAst
import org.lice.repl.VERSION_CODE
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.JFrame
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.WindowConstants

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
	val frame = JFrame("Lice Semantic Tree $VERSION_CODE")
	frame.layout = BorderLayout()
	frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	frame.setLocation(80, 80)
	try {
		frame.add(
				JBScrollPane(createTreeRootFromFile(file).apply {
					preferredSize = Dimension(480, 480)
				}),
				BorderLayout.CENTER
		)
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
