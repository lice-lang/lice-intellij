package org.lice.lang.psi

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.lice.lang.editing.LiceSymbols

class LiceResolveResult(private val element: PsiElement) : ResolveResult {
	override fun getElement() = element
	override fun isValidResult() = true
}

class LiceSymbolReference(symbol: LiceSymbol) :
		PsiPolyVariantReferenceBase<LiceSymbol>(symbol, symbol.textRange, true) {
	private val allDefinitions: List<LiceFunctionCall>
		get() = (myElement.containingFile
				?.children ?: emptyArray())
				.filterIsInstance<LiceElement>()
				.mapNotNull(LiceElement::getFunctionCall)
				.filter { it.nonCommentElements.let { it.size >= 2 && it[0].text in LiceSymbols.nameIntroducingFamily } }

	override fun equals(other: Any?): Boolean = (other as? LiceSymbolReference)?.myElement == myElement ?: false
	override fun hashCode() = myElement.hashCode()
	override fun isReferenceTo(element: PsiElement) = element is LiceSymbol && element.text == myElement.text
	override fun multiResolve(b: Boolean) = allDefinitions.map(::LiceResolveResult).toTypedArray()
	override fun getVariants() = (LiceSymbols.allSymbols + allDefinitions.map { it.nonCommentElements[1] }).toTypedArray()
}

class LiceReferenceContributor : PsiReferenceContributor() {
	override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement(LiceSymbol::class.java),
				object : PsiReferenceProvider() {
					override fun getReferencesByElement(
							element: PsiElement,
							context: ProcessingContext): Array<out PsiReference> {
						if (element !is LiceSymbol) return PsiReference.EMPTY_ARRAY
						val outerFuncCall = element.parent.parent
								.let { it as? LiceFunctionCall } ?: return PsiReference.EMPTY_ARRAY
						if (outerFuncCall.liceCallee?.text !in LiceSymbols.nameIntroducingFamily) return PsiReference.EMPTY_ARRAY
						return SyntaxTraverser.psiTraverser(outerFuncCall.parent.parent)
								.mapNotNull { it as? LiceSymbol }
								.filter { it.text == element.text }
								.map(::LiceSymbolReference)
								.toTypedArray()
					}
				})
	}
}
