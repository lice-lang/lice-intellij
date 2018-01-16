package org.lice.lang.psi

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.lice.lang.LiceFile
import org.lice.lang.LiceLanguage
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
			val innerNames = element.nameIdentifierAndParams.mapNotNull(LiceElement::getSymbol)
			val innerNameTexts = innerNames.map(LiceSymbol::getText)
			if (element.liceCallee?.text in LiceSymbols.closureFamily) return SyntaxTraverser.psiTraverser(element)
					.mapNotNull { it as? LiceSymbol }
					.filter { it !in innerNames && it.text in innerNameTexts }
					.map { symbol ->
						symbol.isResolved = true
						LiceSymbolReference(symbol, element)
					}.toTypedArray()
			val name = innerNames.firstOrNull() ?: return PsiReference.EMPTY_ARRAY
			val nameText = name.text
			val params = innerNames.drop(1)
			val paramTexts = params.map(LiceSymbol::getText)
			if (element.liceCallee?.text !in LiceSymbols.nameIntroducingFamily) return PsiReference.EMPTY_ARRAY
			val list1 = SyntaxTraverser.psiTraverser(element.parent.parent)
					.mapNotNull { it as? LiceSymbol }
					.filter { it != name && it.text == nameText }
					.map { symbol ->
						symbol.isResolved = true
						LiceSymbolReference(symbol, element)
					}
			val list2 = SyntaxTraverser.psiTraverser(element)
					.mapNotNull { it as? LiceSymbol }
					.filter { it !in params && it.text in paramTexts }
					.map { symbol ->
						symbol.isResolved = true
						LiceSymbolReference(symbol, element)
					}
			return (list1 + list2).toTypedArray()
		}
	}

	override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement(LiceFunctionCall::class.java), Companion)
	}
}

class LiceRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?) = element is LiceSymbol
}
