package org.lice.lang.psi

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.tree.TokenSet
import com.intellij.util.ProcessingContext
import org.lice.lang.*
import org.lice.lang.editing.LiceSymbols
import org.lice.util.className

class LiceSymbolReference(private val symbol: LiceSymbol, private val definition: LiceFunctionCall) : PsiReference {
	private val range = 0.let { TextRange(it, it + symbol.textLength) }
	override fun equals(other: Any?) = (other as? LiceSymbolReference)?.symbol == symbol
	override fun toString() = "${symbol.text}: ${symbol.className()}"
	override fun hashCode() = symbol.hashCode()
	override fun isReferenceTo(element: PsiElement) = element == definition
	override fun bindToElement(element: PsiElement) = element
	override fun isSoft() = false
	override fun getElement() = symbol
	override fun getRangeInElement(): TextRange = range
	override fun getCanonicalText(): String = symbol.text
	override fun resolve() = definition
	override fun getVariants() = emptyArray<Any>()
	override fun handleElementRename(newElementName: String) = PsiFileFactory
			.getInstance(symbol.project)
			.createFileFromText(LiceLanguage, newElementName)
			.let { it as? LiceFile }
			?.firstChild
}

class LiceReferenceContributor : PsiReferenceContributor() {
	companion object : PsiReferenceProvider() {
		override fun getReferencesByElement(
				element: PsiElement,
				context: ProcessingContext): Array<PsiReference> {
			if (element !is LiceFunctionCall) return PsiReference.EMPTY_ARRAY
			val innerName = element.nameIdentifier ?: return PsiReference.EMPTY_ARRAY
			if (element.liceCallee?.text !in LiceSymbols.nameIntroducingFamily) return PsiReference.EMPTY_ARRAY
			return SyntaxTraverser.psiTraverser(element.parent.parent)
					.mapNotNull { it as? LiceSymbol }
					.filter { it != innerName && it.text == innerName.text }
					.map { symbol -> LiceSymbolReference(symbol, element) }
					.toTypedArray()
		}
	}

	override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement(LiceFunctionCall::class.java), Companion)
	}
}

class LiceRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?) = element is LiceSymbol
}
