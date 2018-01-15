package org.lice.lang.psi

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.lice.lang.editing.LiceSymbols

class LiceSymbolReference(symbol: LiceSymbol, private val definition: LiceFunctionCall) :
		PsiReferenceBase<LiceSymbol>(symbol, symbol.textRange, true) {

	override fun equals(other: Any?): Boolean = (other as? LiceSymbolReference)?.myElement == myElement ?: false
	override fun hashCode() = myElement.hashCode()
	override fun isReferenceTo(element: PsiElement) = element == definition
	override fun resolve(): LiceFunctionCall {
		println("resolve called.")
		return definition
	}
	override fun getVariants() = (LiceSymbols.allSymbols + (myElement.containingFile
			?.children ?: emptyArray())
			.filterIsInstance<LiceElement>()
			.mapNotNull(LiceElement::getFunctionCall)
			.filter { it.nonCommentElements.let { it.size >= 2 && it[0].text in LiceSymbols.nameIntroducingFamily } }
			.map { it.nonCommentElements[1] })
			.toTypedArray()
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
