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
	val nonCommentElements: List<LiceElement>
	val liceCallee: LiceElement?
}

abstract class LiceFunctionCallMixin(node: ASTNode) :
		ASTWrapperPsiElement(node),
		LiceFunctionCall {
	private var references: Array<LiceSymbolReference>? = null
	override val nonCommentElements get() = elementList.filter { it.comment == null }
	override val liceCallee get() = elementList.firstOrNull { it.comment == null }
	final override var isPossibleEval = true

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

	override fun getReferences() = references ?: makeRef().also {
		references = it
		for (reference in it) reference.symbol.isResolved = true
	}

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
		if (newName.all { it in LiceSymbols.validChars } &&
				liceSymbol != null &&
				liceCallee?.text in LiceSymbols.nameIntroducingFamily) {
			val newChild = PsiFileFactory
					.getInstance(liceSymbol.project)
					.createFileFromText(LiceLanguage, newName)
					.takeIf { it is LiceFile }
					?.firstChild
					?: throw IncorrectOperationException(
							LiceBundle.message("lice.messages.psi.cannot-rename-to", liceSymbol.text, newName))
			liceSymbol.replace(newChild)
			return this
		} else throw IncorrectOperationException(LiceBundle.message("lice.messages.psi.cannot-rename", newName))
	}

	override fun getName() = nameIdentifier?.text
	override fun subtreeChanged() {
		references = null
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


