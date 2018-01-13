/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang.editing

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.lang.*
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.openapi.project.Project
import com.intellij.patterns.*
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.*
import com.intellij.psi.tree.IElementType
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.intellij.util.ProcessingContext
import org.lice.lang.*
import org.lice.lang.psi.*

class LiceCommenter : Commenter {
	override fun getCommentedBlockCommentPrefix() = blockCommentPrefix
	override fun getCommentedBlockCommentSuffix() = blockCommentSuffix
	override fun getBlockCommentPrefix() = null
	override fun getBlockCommentSuffix() = null
	override fun getLineCommentPrefix() = "; "
}

class LiceBraceMatcher : PairedBraceMatcher {
	private companion object Pairs {
		private val PAIRS = arrayOf(BracePair(LiceTypes.LEFT_BRACKET, LiceTypes.RIGHT_BRACKET, false))
	}

	override fun getPairs() = PAIRS
	override fun getCodeConstructStart(psiFile: PsiFile, openingBraceOffset: Int) = openingBraceOffset
	override fun isPairedBracesAllowedBeforeType(type: IElementType, elementType: IElementType?) = true
}

class LiceLiveTemplateProvider : DefaultLiveTemplatesProvider {
	override fun getDefaultLiveTemplateFiles() = arrayOf("liveTemplates/Lice")
	override fun getHiddenLiveTemplateFiles() = null
}

class LiceQuoteHandler : SimpleTokenSetQuoteHandler(LiceTokenType.STRINGS) {
	override fun hasNonClosedLiteral(editor: Editor?, iterator: HighlighterIterator?, offset: Int) = true
}

class LiceSpellCheckingStrategy : SpellcheckingStrategy() {
	override fun getTokenizer(element: PsiElement): Tokenizer<*> = when (element) {
		is LiceComment, is LiceSymbol -> super.getTokenizer(element)
		is LiceString -> super.getTokenizer(element).takeIf { it != EMPTY_TOKENIZER } ?: TEXT_TOKENIZER
		else -> EMPTY_TOKENIZER
	}
}

class LiceNamesValidator : NamesValidator, RenameInputValidator {
	override fun isKeyword(s: String, project: Project?) = s in LiceSymbols.importantFamily
	override fun isInputValid(s: String, o: PsiElement, c: ProcessingContext) = isIdentifier(s, o.project)
	override fun getPattern(): ElementPattern<out PsiElement> = PlatformPatterns.psiElement().with(object :
			PatternCondition<PsiElement>("") {
		override fun accepts(element: PsiElement, context: ProcessingContext?) =
				(element as? PomTargetPsiElement)?.navigationElement is LiceSymbol
	})

	override fun isIdentifier(name: String, project: Project?) = with(LiceLexerAdapter()) {
		start(name)
		tokenType == LiceTypes.SYM && tokenEnd == name.length
	}
}

const val SHORT_TEXT_MAX = 8
const val LONG_TEXT_MAX = 16

class LiceBreadCrumbProvider : BreadcrumbsProvider {
	override fun getLanguages() = arrayOf(LiceLanguage)
	override fun acceptElement(o: PsiElement) = o is LiceFunctionCall
	override fun getElementTooltip(o: PsiElement) = (o as? LiceFunctionCall)?.let { "function: <${it.text}>" }
	override fun getElementInfo(o: PsiElement): String = when (o) {
		is LiceFunctionCall -> o.liceCallee?.text.let {
			when (it) {
				in LiceSymbols.closureFamily -> "λ"
				in LiceSymbols.importantFamily -> "[$it]"
				null -> LICE_PLACEHOLDER
				else -> if (it.length <= SHORT_TEXT_MAX) it else "${it.take(SHORT_TEXT_MAX)}…"
			}
		}
		else -> "???"
	}
}

class LiceFoldingBuilder : FoldingBuilderEx() {
	override fun getPlaceholderText(node: ASTNode): String = LICE_PLACEHOLDER
	override fun isCollapsedByDefault(node: ASTNode) = false
	override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean) = SyntaxTraverser
			.psiTraverser(root)
			.traverse()
			.filter { it is LiceFunctionCall || it is LiceNull }
			.filter {
				it.textRange.let { r ->
					document.getLineNumber(r.endOffset) - document.getLineNumber(r.startOffset) >= 1 ||
							r.startOffset - r.endOffset > LONG_TEXT_MAX
				}
			}
			.transform { FoldingDescriptor(it, it.textRange) }.toList().toTypedArray()
}
