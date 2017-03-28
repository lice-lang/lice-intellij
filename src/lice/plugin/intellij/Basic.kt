
/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package lice.plugin.intellij

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader

object LiceLanguage : Language("LICE") {
	override fun getDisplayName() = "Lice"
}

val LICE_ICON = IconLoader.getIcon("/lice/plugin/intellij/lice.png")

object LiceFileType : LanguageFileType(LiceLanguage) {
	override fun getDefaultExtension() = "lice"
	override fun getName() = "Lice file"
	override fun getIcon() = LICE_ICON
	override fun getDescription() = "Lice language script"
}

class LiceFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(p0: FileTypeConsumer) = p0.consume(LiceFileType, "lice")
}

