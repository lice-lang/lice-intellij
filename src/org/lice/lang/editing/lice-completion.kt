package org.lice.lang.editing

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.lice.lang.psi.LiceTypes

class LiceBuiltinSymbolsCompletionContributor : CompletionContributor() {
	init {
		extend(
				CompletionType.BASIC,
				PlatformPatterns.psiElement(LiceTypes.SYM).afterLeaf("("),
				object : CompletionProvider<CompletionParameters>() {
					override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, resultSet: CompletionResultSet) {
						resultSet.addAllElements(LiceSymbols.allSymbols.map(LookupElementBuilder::create))
					}
				})
	}
}
