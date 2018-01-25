package org.lice.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.*
import com.intellij.util.IncorrectOperationException
import org.lice.lang.*
import org.lice.lang.editing.LiceSymbols
import org.lice.lang.psi.*

interface ILiceFunctionCallMixin : PsiNameIdentifierOwner {
	var isPossibleEval: Boolean

	/** Should be implemented lazily. */
	val nonCommentElements: List<LiceElement>
	val liceCallee: LiceElement?
	fun forceResolve(): Array<LiceSymbolReference>
}

abstract class LiceFunctionCallMixin(node: ASTNode) :
		ASTWrapperPsiElement(node),
		LiceFunctionCall {
	private var references: Array<LiceSymbolReference>? = null
	private var nonCommentElementsCache: List<LiceElement>? = null
	override val nonCommentElements
		get() = nonCommentElementsCache ?: elementList.filter { it.comment == null }.also { nonCommentElementsCache = it }
	override val liceCallee get() = elementList.firstOrNull { it.comment == null }
	final override var isPossibleEval = true
	override fun forceResolve(): Array<LiceSymbolReference> {
		val it = makeRef()
		references = it
		for (reference in it) reference.symbol.isResolved = true
		return it
	}

	private fun makeRef(): Array<LiceSymbolReference> {
		val innerNames = nameIdentifierAndParams.mapNotNull(LiceElement::getSymbol)
		val innerNameTexts = innerNames.map(LiceSymbol::getText)
		if (this.liceCallee?.text in LiceSymbols.closureFamily) return SyntaxTraverser.psiTraverser(this)
				.filterIsInstance<LiceSymbol>()
				.filter { it !in innerNames && it.text in innerNameTexts }
				.map { symbol -> LiceSymbolReference(symbol, this) }
				.toTypedArray()
		val name = innerNames.firstOrNull() ?: return emptyArray()
		val nameText = name.text
		val params = innerNames.drop(1)
		val paramTexts = params.map(LiceSymbol::getText)
		if (this.liceCallee?.text !in LiceSymbols.nameIntroducingFamily) return emptyArray()
		val list1 = SyntaxTraverser.psiTraverser(parent.parent)
				.filterIsInstance<LiceSymbol>()
				.filter { it != name && it.text == nameText }
				.map { symbol -> LiceSymbolReference(symbol, this) }
		val list2 = SyntaxTraverser.psiTraverser(this)
				.filterIsInstance<LiceSymbol>()
				.filter { it !in params && it.text in paramTexts }
				.map { symbol -> LiceSymbolReference(symbol, this) }
		return (list1 + list2).toTypedArray()
	}

	override fun getReferences() = references ?: forceResolve()
	private val nameIdentifierAndParams
		get() = when (liceCallee?.text) {
			in LiceSymbols.nameIntroducingFamily,
			in LiceSymbols.closureFamily ->
				nonCommentElements.run { if (size >= 1) subList(1, size).toList() else emptyList() }
			else -> emptyList()
		}

	override fun getNameIdentifier() = when (liceCallee?.text) {
		in LiceSymbols.closureFamily -> nonCommentElements.firstOrNull()?.symbol
		!in LiceSymbols.nameIntroducingFamily -> null
		else -> nonCommentElements.getOrNull(1)?.symbol
	}

	override fun setName(newName: String): PsiElement {
		val liceSymbol = nameIdentifier
				?: throw IncorrectOperationException(LiceBundle.message("lice.messages.psi.cannot-rename", newName))
		val newChild = PsiFileFactory
				.getInstance(liceSymbol.project)
				.createFileFromText(LiceLanguage, newName)
				.takeIf { it is LiceFile }
				?.firstChild
				.let { if (it is LiceElement) it.symbol else it }
				?: throw IncorrectOperationException(
						LiceBundle.message("lice.messages.psi.cannot-rename-to", liceSymbol.text, newName))
		references?.forEach { it.element.replace(newChild) }
		return liceSymbol.replace(newChild)
	}

	override fun getName() = nameIdentifier?.text
	override fun subtreeChanged() {
		references = null
		nonCommentElementsCache = null
		isPossibleEval = true
		super.subtreeChanged()
	}
}

interface ILiceSymbolMixin : PsiElement {
	var isResolved: Boolean
}

abstract class LiceSymbolMixin(node: ASTNode) :
		ASTWrapperPsiElement(node),
		LiceSymbol {
	override var isResolved = false
	private var reference: LiceSymbolReference? = null
	override fun getReference() = reference ?: LiceSymbolReference(this).also { reference = it }
	override fun getReferences(): Array<PsiReference> = parent.parent.references
	override fun subtreeChanged() {
		isResolved = false
		reference = null
		super.subtreeChanged()
	}
}


