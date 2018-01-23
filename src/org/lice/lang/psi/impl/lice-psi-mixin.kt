package org.lice.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import org.lice.lang.LiceTokenType
import org.lice.lang.editing.LiceSymbols
import org.lice.lang.psi.*

interface ILiceFunctionCallMixin : PsiNameIdentifierOwner {
	var isPossibleEval: Boolean

	/** Should be implemented lazily. */
	val nonCommentElements: List<LiceElement>
	val liceCallee: LiceElement?
}

abstract class LiceFunctionCallMixin(node: ASTNode) :
		ASTWrapperPsiElement(node),
		LiceFunctionCall,
		PsiNameIdentifierOwner {
	internal var refCache: Array<out PsiReference>? = null
	private var nonCommentElementsCache: List<LiceElement>? = null
	override val nonCommentElements
		get() = nonCommentElementsCache ?: elementList.filter { it.comment == null }.also { nonCommentElementsCache = it }
	override val liceCallee get() = elementList.firstOrNull { it.comment == null }
	final override var isPossibleEval = true

	override fun getReferences() = refCache ?: nameIdentifier?.let {
		collectFrom(parent.parent.parent, it.text).also {
			refCache = it
			for (reference in it) (reference.element as? LiceSymbol)?.isResolved = true
		}
	} ?: emptyArray()

	override fun getNameIdentifier() = when (liceCallee?.text) {
		in LiceSymbols.closureFamily -> nonCommentElements.firstOrNull()?.symbol
		!in LiceSymbols.nameIntroducingFamily -> null
		else -> nonCommentElements.getOrNull(1)?.symbol
	}

	override fun setName(newName: String) = LiceTokenType.fromText(newName, project)
			.let { nameIdentifier?.replace(it) }
			.also {
				if (it is LiceFunctionCallMixin)
					it.refCache = references.mapNotNull { it.handleElementRename(newName).reference }.toTypedArray()
			}

	override fun processDeclarations(
			processor: PsiScopeProcessor,
			substitutor: ResolveState,
			lastParent: PsiElement?,
			place: PsiElement) = nameIdentifier?.let { processor.execute(it, substitutor) } ?: true

	override fun getName() = nameIdentifier?.text
	override fun subtreeChanged() {
		nonCommentElementsCache = null
		isPossibleEval = true
		super.subtreeChanged()
	}
}

interface ILiceSymbolMixin : PsiElement, PsiNameIdentifierOwner {
	var isResolved: Boolean
}

abstract class LiceSymbolMixin(node: ASTNode) :
		ASTWrapperPsiElement(node),
		LiceSymbol {
	override var isResolved = false
	private var refCache: Array<out PsiReference>? = null
	private var reference: LiceSymbolRef? = null
	override fun getNameIdentifier() = takeIf { isDeclaration }
	override fun getReference() = reference ?: LiceSymbolRef(this).also { reference = it }
	override fun getReferences(): Array<out PsiReference> = refCache
			?: nameIdentifier?.let { collectFrom(parent.parent, it.text) }?.also {
				refCache = it
				for (reference in it) (reference.element as? LiceSymbol)?.isResolved = true
			}
			?: emptyArray()

	override fun setName(newName: String) = LiceTokenType.fromText(newName, project)
			.let { nameIdentifier?.replace(it) }
			.also {
				if (it is LiceFunctionCallMixin)
					it.refCache = references.mapNotNull { it.handleElementRename(newName).reference }.toTypedArray()
			}

	override fun processDeclarations(
			processor: PsiScopeProcessor,
			substitutor: ResolveState,
			lastParent: PsiElement?,
			place: PsiElement) = nameIdentifier?.let { processor.execute(it, substitutor) } ?: true

	override fun subtreeChanged() {
		isResolved = false
		reference = null
		super.subtreeChanged()
	}
}


