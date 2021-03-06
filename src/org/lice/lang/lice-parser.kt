package org.lice.lang

import com.intellij.lang.*
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.tree.*
import org.lice.lang.psi.LiceTypes

class LiceParserDefinition : ParserDefinition {
	private companion object {
		private val FILE = IFileElementType(LiceLanguage)
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
		fun fromText(project: Project, code: String) = PsiFileFactory
				.getInstance(project)
				.createFileFromText(LiceLanguage, code)
				.let { it as? LiceFile }
				?.firstChild
	}
}

class LiceElementType(debugName: String) : IElementType(debugName, LiceLanguage)
