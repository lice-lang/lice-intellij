package org.lice.lang.editing

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.lice.lang.psi.LiceTypes

class LiceBuiltinSymbolsCompletionContributor : CompletionContributor() {
	private companion object Completions {
		private val list = LiceSymbols.allSymbolsForCompletion.map(LookupElementBuilder::create)
	}

	init {
		extend(
				CompletionType.BASIC,
				PlatformPatterns.psiElement(LiceTypes.SYM).afterLeaf("("),
				object : CompletionProvider<CompletionParameters>() {
					override fun addCompletions(
							parameters: CompletionParameters,
							context: ProcessingContext?,
							resultSet: CompletionResultSet) {
						list.forEach(resultSet::addElement)
					}
				})
	}
}
