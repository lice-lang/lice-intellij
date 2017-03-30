/**
 * Created by ice1000 on 2017/3/30.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.apache.commons.lang.StringUtils

class StartReplAction : AnAction() {
	override fun actionPerformed(p0: AnActionEvent?) {
		Runtime.getRuntime().exec(
				StringUtils.join(arrayOf(
						LiceInfo.JAVA_PATH_WRAPPED,
						"-jar",
						LiceInfo.LICE_PATH_WRAPPED,
						"-classpath",
						"\"" + StringUtils.join(arrayOf(
								LiceInfo.KOTLIN_RUNTIME_PATH,
								LiceInfo.KOTLIN_REFLECT_PATH,
								LiceInfo.LICE_PATH
						), ";") + "\"",
						"org.lice.gui.GuiReplKt"
				), " ")
		)

	}
}

