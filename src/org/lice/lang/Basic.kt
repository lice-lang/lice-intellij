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
import org.lice.lang.LiceInfo.EXTENSION
import org.lice.lang.LiceInfo.LANGUAGE_NAME
import org.lice.lang.LiceInfo.LICE_ICON
import org.lice.lang.psi.LiceTokenType

object LiceLanguage : Language(LANGUAGE_NAME, "text/$EXTENSION") {
	override fun getDisplayName() = LANGUAGE_NAME
	override fun isCaseSensitive() = true
}

object LiceFileType : LanguageFileType(LiceLanguage) {
	override fun getDefaultExtension() = EXTENSION
	override fun getName() = "$LANGUAGE_NAME file"
	override fun getIcon() = LICE_ICON
	override fun getDescription() = LANGUAGE_NAME
}

class LiceFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(consumer: FileTypeConsumer) = consumer.consume(LiceFileType, EXTENSION)
}

class LiceBraceMatcher : PairedBraceMatcher {
	private companion object Pairs {
		private val PAIRS = arrayOf(BracePair(
				LiceTokenType.LEFT_BRACE,
				LiceTokenType.RIGHT_BRACE,
				false
		))
	}

	override fun getPairs() = PAIRS
	override fun getCodeConstructStart(psiFile: PsiFile, p1: Int) = p1
	override fun isPairedBracesAllowedBeforeType(type: IElementType, iElementType: IElementType?) = true
}

//class LiceLiveTemplateContext : FileTypeBasedContextType("Lice", "Lice", LiceFileType)

class LiceLiveTemplateProvider : DefaultLiveTemplatesProvider {
	override fun getDefaultLiveTemplateFiles() = arrayOf("liveTemplates/Lice")
	override fun getHiddenLiveTemplateFiles() = null
}

class LiceContext : TemplateContextType(LANGUAGE_NAME, LANGUAGE_NAME) {
	override fun isInContext(file: PsiFile, p1: Int) = file.name.endsWith(".$EXTENSION")
}
