/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.lang.BracePair
import com.intellij.lang.Language
import com.intellij.lang.PairedBraceMatcher
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.lice.lang.LiceInfo.EXTENSION
import org.lice.lang.LiceInfo.LICE_ICON
import org.lice.lang.psi.LiceTokenType

object LiceLanguage : Language("Lice", "text/lice") {
	override fun getDisplayName() = "Lice"
	override fun isCaseSensitive() = true
}


object LiceFileType : LanguageFileType(LiceLanguage) {
	override fun getDefaultExtension() = EXTENSION
	override fun getName() = "Lice file"
	override fun getIcon() = LICE_ICON
	override fun getDescription() = "Lice"
}

class LiceFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(consumer: FileTypeConsumer) =
			consumer.consume(LiceFileType, EXTENSION)
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
	override fun getCodeConstructStart(p0: PsiFile?, p1: Int) = p1
	override fun isPairedBracesAllowedBeforeType(
			type: IElementType,
			iElementType: IElementType?) =
			true
}

//class LiceLiveTemplateContext : FileTypeBasedContextType("Lice", "Lice", LiceFileType)

class LiceLiveTemplateProvider : DefaultLiveTemplatesProvider {
	override fun getDefaultLiveTemplateFiles() =
			arrayOf("liveTemplates/Lice")

	override fun getHiddenLiveTemplateFiles() =
			null
}

class LiceContext : TemplateContextType("Lice", "Lice") {
	override fun isInContext(p0: PsiFile, p1: Int) =
			p0.name.endsWith(".lice")
}
