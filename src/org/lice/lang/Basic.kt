/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader

object LiceLanguage : Language("Lice", "text/lice") {
	override fun getDisplayName() = "Lice"
	override fun isCaseSensitive() = true
}

val LICE_ICON = IconLoader.getIcon("/org/lice/lang/lice.png")

object LiceFileType : LanguageFileType(LiceLanguage) {
	override fun getDefaultExtension() = "lice"
	override fun getName() = "Lice file"
	override fun getIcon() = LICE_ICON
	override fun getDescription() = "Lice language script"
}

class LiceFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(p0: FileTypeConsumer) = p0.consume(LiceFileType, "lice")
}

