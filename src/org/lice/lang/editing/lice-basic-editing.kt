/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang.editing

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.lang.*
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.navigation.LocationPresentation
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

const val SHORT_TEXT_MAX = 12
const val LONG_TEXT_MAX = 24
private fun cutText(it: String, textMax: Int) = if (it.length <= textMax) it else "${it.take(textMax)}…"

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
				else -> cutText(it, SHORT_TEXT_MAX)
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
			.forceDisregardTypes { it == GeneratedParserUtilBase.DUMMY_BLOCK }
			.traverse()
			.filter { it is LiceFunctionCall || it is LiceNull }
			.filter {
				it.textRange.let { r ->
					document.getLineNumber(r.endOffset) - document.getLineNumber(r.startOffset) >= 1 ||
							r.length >= LONG_TEXT_MAX
				}
			}
			.transform { FoldingDescriptor(it, it.textRange) }
			.toList()
			.toTypedArray()
}

class LiceStructureViewFactory : PsiStructureViewFactory {
	override fun getStructureViewBuilder(psiFile: PsiFile) = object : TreeBasedStructureViewBuilder() {
		override fun createStructureViewModel(editor: Editor?) = LiceModel(psiFile, editor)
		override fun isRootNodeShown() = true
	}

	private class LiceModel(file: PsiFile, editor: Editor?) :
			StructureViewModelBase(file, editor, LiceStructureElement(file)), StructureViewModel.ElementInfoProvider {
		init {
			withSuitableClasses(LiceFunctionCall::class.java, PsiFile::class.java)
		}

		override fun isAlwaysShowsPlus(o: StructureViewTreeElement) = false
		override fun isAlwaysLeaf(o: StructureViewTreeElement) = false
		override fun shouldEnterElement(o: Any?) = true
	}

	private class LiceStructureElement(o: PsiElement) : PsiTreeElementBase<PsiElement>(o), SortableTreeElement,
			LocationPresentation {
		override fun getIcon(open: Boolean) = element.let { o ->
			when (o) {
				is LiceFile -> LICE_ICON
				is LiceFunctionCall -> LICE_AST_NODE_ICON
				is LiceNull -> LICE_AST_NODE0_ICON
				else -> LICE_AST_LEAF_ICON
			}
		}

		override fun getAlphaSortKey() = presentableText
		override fun getPresentableText() = cutText(element.let { o ->
			when (o) {
				is LiceFile -> "Lice file"
				is LiceFunctionCall -> o.liceCallee?.text ?: "??"
				is LiceSymbol -> o.text ?: "??"
				is LiceNull -> "()"
				is LiceNumber -> "Number: ${o.text}"
				is LiceString -> "String: ${o.text}"
				else -> "??"
			}
		}, LONG_TEXT_MAX)

		override fun getLocationString() = ""
		override fun getLocationPrefix() = ""
		override fun getLocationSuffix() = ""
		override fun getChildrenBase(): List<LiceStructureElement> = element.let { o ->
			@Suppress("UNCHECKED_CAST") when (o) {
				is LiceFile -> o
						.children.mapNotNull { (it as? LiceElement)?.nonCommentElements }
				is LiceFunctionCall -> o
						.children.drop(1).mapNotNull { (it as? LiceElement)?.nonCommentElements }
				else -> emptyList()
			}.map(::LiceStructureElement)
		}
	}
}
