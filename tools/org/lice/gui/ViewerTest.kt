package org.lice.gui

import org.lice.tools.displaySemanticTree
import java.io.File

/**
 * Created by ice1000 on 2017/3/31.
 *
 * @author ice1000
 */
fun main(args: Array<String>) {
	displaySemanticTree(File("test.lice"))
}

