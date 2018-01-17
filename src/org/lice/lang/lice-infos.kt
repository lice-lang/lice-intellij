/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.CommonBundle
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

object LiceLanguage : Language(LICE_NAME, "text/${LiceBundle.message("lice.file.extension")}") {
	override fun getDisplayName() = LICE_NAME
	override fun isCaseSensitive() = true
}

object LiceFileType : LanguageFileType(LiceLanguage) {
	override fun getDefaultExtension() = LiceBundle.message("lice.file.extension")
	override fun getName() = LiceBundle.message("lice.file.name")
	override fun getIcon() = LICE_ICON
	override fun getDescription() = LICE_NAME
}

class LiceFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LiceLanguage) {
	override fun getFileType() = LiceFileType
}

class LiceFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(consumer: FileTypeConsumer) = consumer.consume(LiceFileType, LiceBundle.message("lice.file.extension"))
}

class LiceContext : TemplateContextType(LICE_NAME, LICE_NAME) {
	override fun isInContext(file: PsiFile, p1: Int) = file.name.endsWith(".${LiceBundle.message("lice.file.extension")}")
}

object LiceBundle {
	@NonNls private const val BUNDLE = "org.lice.lang.lice-bundle"
	private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE) }

	@JvmStatic
	fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
			CommonBundle.message(bundle, key, *params)
}
