package org.lice.gui

import com.intellij.ide.ui.laf.darcula.DarculaLaf
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import org.lice.compiler.util.SymbolList
import org.lice.compiler.util.forceRun
import org.lice.repl.Repl
import org.lice.repl.VERSION_CODE
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.io.OutputStream
import java.io.PrintStream
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * Created by ice1000 on 2017/3/30.
 *
 * @author ice1000
 */

val FONT_NAME = "Microsoft YaHei"

@JvmOverloads
fun JTextPane.append(
		string: String,
		color: Color = Color(0xA9B7C6)) {
	val set = SimpleAttributeSet()
	StyleConstants.setForeground(set, color)
	StyleConstants.setFontSize(set, 12)
	styledDocument.insertString(styledDocument.length, string, set)
}

fun main(args: Array<String>) {
	UIManager.setLookAndFeel(DarculaLaf())
	val sl = SymbolList()
	val output = JTextPane()
	output.isEditable = false
	output.maximumSize = Dimension(640, 640)
	val button = JButton("Clear screen")
	button.addActionListener {
		output.text = ""
		output.append(Repl.HINT)
	}
	System.setOut(PrintStream(object : OutputStream() {
		override fun write(b: Int) =
				output.append(b.toChar().toString())

		override fun write(b: ByteArray) =
				output.append(String(b))
	}))
	System.setErr(PrintStream(object : OutputStream() {
		override fun write(b: Int) =
				output.append(b.toChar().toString(), Color(0xBC3F3C))

		override fun write(b: ByteArray) =
				output.append(String(b), Color(0xBC3F3C))
	}))
	output.append("This is the GUI repl $VERSION_CODE for Lice language.\n", Color(0xCC7832))
	val repl = Repl()
	val input = JBTextField()
	input.addKeyListener(object : KeyListener {
		override fun keyTyped(e: KeyEvent?) = Unit
		override fun keyReleased(e: KeyEvent?) = Unit
		override fun keyPressed(e: KeyEvent?) {
			if (e != null && e.keyCode == KeyEvent.VK_ENTER) {
				output.append("${input.text}\n", Color(0x467CDA))
				repl.handle(input.text, sl)
				input.text = ""
			}
		}
	})
	forceRun {
		button.font = Font(FONT_NAME, 0, 14)
		input.font = Font(FONT_NAME, 0, 16)
		output.font = Font(FONT_NAME, 0, 14)
	}
	val frame = JFrame("Lice language interpreter $VERSION_CODE")
	frame.layout = BorderLayout()
	frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
	frame.add(JBScrollPane(output), BorderLayout.CENTER)
	frame.add(button, BorderLayout.NORTH)
	frame.add(input, BorderLayout.SOUTH)
	frame.setLocation(80, 80)
	frame.size = output.maximumSize
	frame.isVisible = true
	input.requestFocus()
}