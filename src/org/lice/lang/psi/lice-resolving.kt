package org.lice.lang.psi

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.lice.lang.LiceFile
import org.lice.lang.LiceLanguage

class LiceSymbolReference(val symbol: LiceSymbol, definition: LiceFunctionCall? = null) : PsiReference {
	init {
		symbol.isResolved = true
	}

	private val definition = definition ?: (symbol.containingFile as? LiceFile)?.let {
		it.children
				.firstOrNull { it is LiceElement && it.symbol != null }
				?.let { it as LiceElement }
				?.symbol
				?.also { it.isResolved = true }
	}
	private val range = 0.let { TextRange(it, it + symbol.textLength) }
	override fun equals(other: Any?) = (other as? LiceSymbolReference)?.symbol == symbol
	override fun toString() = "${symbol.text}: ${symbol.javaClass.name}"
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

class LiceRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?) =
			element is LiceSymbol || element is LiceElement && element.symbol != null
}
