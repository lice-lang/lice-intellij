/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile

object LiceLanguage : Language(LICE_NAME, "text/$LICE_EXTENSION") {
	override fun getDisplayName() = LICE_NAME
	override fun isCaseSensitive() = true
}

object LiceFileType : LanguageFileType(LiceLanguage) {
	override fun getDefaultExtension() = LICE_EXTENSION
	override fun getName() = "$LICE_NAME file"
	override fun getIcon() = LICE_ICON
	override fun getDescription() = LICE_NAME
}

class LiceFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LiceLanguage) {
	override fun getFileType() = LiceFileType
}

class LiceFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(consumer: FileTypeConsumer) = consumer.consume(LiceFileType, LICE_EXTENSION)
}

class LiceContext : TemplateContextType(LICE_NAME, LICE_NAME) {
	override fun isInContext(file: PsiFile, p1: Int) = file.name.endsWith(".$LICE_EXTENSION")
}
