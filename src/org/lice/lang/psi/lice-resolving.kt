package org.lice.lang.psi

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import org.lice.lang.psi.impl.isDeclaration
import org.lice.lang.psi.impl.treeWalkUp

class LiceSymbolRef(symbol: LiceSymbol, private var refTo: PsiElement? = null) :
		PsiPolyVariantReferenceBase<LiceSymbol>(symbol, TextRange(0, symbol.textLength), true) {
	private val project = symbol.project
	override fun equals(other: Any?) = (other as? LiceSymbolRef)?.element == element
	override fun hashCode() = element.hashCode()
	override fun isReferenceTo(o: PsiElement?) = o == refTo ||
			(o as? PsiNameIdentifierOwner)?.nameIdentifier?.text == element.text

	override fun getCanonicalText(): String = element.text
	override fun getVariants(): Array<out Any> {
		val variantsProcessor = CompletionProcessor(this, true)
		treeWalkUp(element, variantsProcessor)
		return variantsProcessor.resultElement
	}

	override fun resolve() = refTo ?: super.resolve().also { refTo = it }
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> =
			if (element.isDeclaration) emptyArray()
			else ResolveCache
					.getInstance(project)
					.resolveWithCaching(this, Resolver, true, incompleteCode)

	private companion object Resolver : ResolveCache.PolyVariantResolver<LiceSymbolRef> {
		override fun resolve(ref: LiceSymbolRef, incompleteCode: Boolean): Array<out ResolveResult> {
			val currentSymbol = ref.element ?: return emptyArray()
			val processor = SymbolResolveProcessor(ref, incompleteCode)
			treeWalkUp(currentSymbol, processor)
			PsiTreeUtil
					.getParentOfType(currentSymbol, LiceFunctionCall::class.java)
					?.processDeclarations(processor, ResolveState.initial(), currentSymbol, processor.place)
			return processor.candidates
		}
	}
}

class LiceRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?) = true
}

abstract class ResolveProcessor : PsiScopeProcessor {
	private var candidateSet = hashSetOf<PsiElementResolveResult>()
	val candidates get() = candidateSet.toTypedArray()
	val resultElement get() = candidates.map(LookupElementBuilder::create).toTypedArray()
	override fun handleEvent(event: PsiScopeProcessor.Event, o: Any?) = Unit
	fun addCandidate(symbol: PsiElement) = addCandidate(PsiElementResolveResult(symbol, true))
	fun addCandidate(candidate: PsiElementResolveResult) = candidateSet.add(candidate)
	fun hasCandidate(candidate: PsiElement) = candidateSet.any { it.element == candidate }
}

class SymbolResolveProcessor(private val name: String, val place: PsiElement, val incompleteCode: Boolean) :
		ResolveProcessor() {
	constructor(ref: LiceSymbolRef, incompleteCode: Boolean) : this(ref.canonicalText, ref.element, incompleteCode)

	private val processed = hashSetOf<PsiElement>()
	override fun <T : Any?> getHint(hintKey: Key<T>): T? = null
	override fun execute(element: PsiElement, resolveState: ResolveState) =
			if (element is LiceSymbol && element !in processed) {
				val accessible = name == element.text
				if (accessible && element.isDeclaration &&
						!((element as? StubBasedPsiElement<*>)?.stub == null && PsiTreeUtil.hasErrorElements(element)))
					addCandidate(element)
				processed.add(element)
				!accessible
			} else true
}

class CompletionProcessor(val place: PsiElement, val incompleteCode: Boolean) : ResolveProcessor() {
	constructor(ref: LiceSymbolRef, incompleteCode: Boolean) : this(ref.element, incompleteCode)

	override fun <T : Any?> getHint(hintKey: Key<T>): T? = null
	override fun execute(element: PsiElement, resolveState: ResolveState): Boolean {
		if (element is LiceSymbol && hasCandidate(element))
			if (!((element as? StubBasedPsiElement<*>)?.stub == null && PsiTreeUtil.hasErrorElements(element)))
				addCandidate(element)
		return true
	}
}
