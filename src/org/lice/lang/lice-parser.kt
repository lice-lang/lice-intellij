package org.lice.lang

import com.intellij.lang.*
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.*
import org.lice.lang.psi.LiceTypes


class LiceParserDefinition : ParserDefinition {
	private companion object {
		private val FILE = IFileElementType(Language.findInstance(LiceLanguage::class.java))
	}

	override fun getWhitespaceTokens(): TokenSet = TokenSet.WHITE_SPACE
	override fun createElement(astNode: ASTNode): PsiElement = LiceTypes.Factory.createElement(astNode)
	override fun getFileNodeType() = FILE
	override fun createFile(viewProvider: FileViewProvider) = LiceFile(viewProvider)
	override fun createParser(project: Project) = LiceParser()
	override fun getStringLiteralElements() = LiceTokenType.STRINGS
	override fun getCommentTokens() = LiceTokenType.COMMENTS
	override fun createLexer(project: Project) = LiceLexerAdapter()
	override fun spaceExistanceTypeBetweenTokens(astNode: ASTNode, astNode2: ASTNode) =
			ParserDefinition.SpaceRequirements.MAY
}

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	companion object {
		@JvmField val COMMENTS = TokenSet.create(LiceTypes.COMMENT)
		@JvmField val STRINGS = TokenSet.create(LiceTypes.STR)
	}
}

class LiceElementType(debugName: String) : IElementType(debugName, LiceLanguage)
