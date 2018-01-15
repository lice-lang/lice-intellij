package org.lice.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.lice.lang.editing.LiceSymbolsHelper

class LiceResolveResult(private val element: PsiElement) : ResolveResult {
	override fun getElement() = element
	override fun isValidResult() = true
}

class LiceSymbolReference(
		symbol: LiceSymbol,
		range: TextRange = symbol.lastChild.textRange.shiftRight(-symbol.textRange.startOffset))
	: PsiPolyVariantReferenceBase<LiceSymbol>(symbol, range, true) {
	private val allDefinitions: List<LiceFunctionCall>
		get() = (myElement.containingFile
				?.children ?: emptyArray())
				.filterIsInstance<LiceElement>()
				.mapNotNull(LiceElement::getFunctionCall)
				.filter { it.nonCommentElements.let { it.size >= 2 && it[0].text in LiceSymbolsHelper.nameIntroducingFamily } }

	override fun equals(other: Any?): Boolean = (other as? LiceSymbolReference)?.myElement == myElement ?: false
	override fun hashCode() = myElement.hashCode()
	override fun multiResolve(b: Boolean) = allDefinitions.map(::LiceResolveResult).toTypedArray()
	override fun getVariants() = (LiceSymbolsHelper.allSymbols + allDefinitions.map { it.nonCommentElements[1] }).toTypedArray()
}

class LiceReferenceContributor : PsiReferenceContributor() {
	override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement(LiceSymbol::class.java),
				object : PsiReferenceProvider() {
					override fun getReferencesByElement(element: PsiElement, context: ProcessingContext) =
							if (element !is LiceSymbol || element
											.parent
											.parent
											.let { it as? LiceFunctionCall }
											?.liceCallee
											?.text in LiceSymbolsHelper.nameIntroducingFamily) PsiReference.EMPTY_ARRAY
							else arrayOf(LiceSymbolReference(element))
				})
	}
}
