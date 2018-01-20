package org.lice.lang.editing

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.util.ProcessingContext
import org.lice.core.SymbolList
import org.lice.lang.psi.LiceTypes

private class Completer(private val list: List<LookupElement>) : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(
			parameters: CompletionParameters,
			context: ProcessingContext?,
			resultSet: CompletionResultSet) = list.forEach(resultSet::addElement)
}

class LiceBuiltinSymbolsCompletionContributor : CompletionContributor() {
	private companion object Completions {
		private val functions = SymbolList.preludeSymbols.map { LookupElementBuilder.create("$it ") }
		private val variables = SymbolList.preludeVariables.map(LookupElementBuilder::create)
	}

	init {
		extend(CompletionType.BASIC, psiElement(LiceTypes.SYM).afterLeaf("("), Completer(functions))
		extend(CompletionType.BASIC, psiElement(LiceTypes.SYM).andNot(psiElement().afterLeaf("(")), Completer(variables))
	}
}
