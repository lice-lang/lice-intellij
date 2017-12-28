/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.lang.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

object LiceLanguage : Language(LANGUAGE_NAME, "text/$LICE_EXTENSION") {
	override fun getDisplayName() = LANGUAGE_NAME
	override fun isCaseSensitive() = true
}

object LiceFileType : LanguageFileType(LiceLanguage) {
	override fun getDefaultExtension() = LICE_EXTENSION
	override fun getName() = "$LANGUAGE_NAME file"
	override fun getIcon() = LICE_ICON
	override fun getDescription() = LANGUAGE_NAME
}

class LiceFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(consumer: FileTypeConsumer) = consumer.consume(LiceFileType, LICE_EXTENSION)
}

class LiceBraceMatcher : PairedBraceMatcher {
	private companion object Pairs {
		private val PAIRS = arrayOf<BracePair>(
				// BracePair(LiceTypes.LEFT_BRACE, LiceTypes.RIGHT_BRACE, false)
		)
	}

	override fun getPairs() = PAIRS
	override fun getCodeConstructStart(psiFile: PsiFile, openingBraceOffset: Int) = openingBraceOffset
	override fun isPairedBracesAllowedBeforeType(type: IElementType, iElementType: IElementType?) = true
}

//class LiceLiveTemplateContext : FileTypeBasedContextType("Lice", "Lice", LiceFileType)

class LiceLiveTemplateProvider : DefaultLiveTemplatesProvider {
	override fun getDefaultLiveTemplateFiles() = arrayOf("liveTemplates/Lice")
	override fun getHiddenLiveTemplateFiles() = null
}

class LiceContext : TemplateContextType(LANGUAGE_NAME, LANGUAGE_NAME) {
	override fun isInContext(file: PsiFile, p1: Int) = file.name.endsWith(".$LICE_EXTENSION")
}
