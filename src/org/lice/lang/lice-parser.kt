package org.lice.lang

import com.intellij.lang.*
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.tree.IFileElementType


class LiceParserDefinition : ParserDefinition {
	override fun createFile(viewProvider: FileViewProvider): PsiFile {
		TODO("not implemented")
	}

	override fun createElement(astNode: ASTNode): PsiElement {
		TODO("not implemented")
	}

	override fun getFileNodeType(): IFileElementType {
		TODO("not implemented")
	}

	override fun createParser(project: Project) = LiceParser()
	override fun getStringLiteralElements() = LiceTokenType.STRINGS
	override fun getCommentTokens() = LiceTokenType.COMMENTS
	override fun createLexer(project: Project) = LiceLexerAdapter()
	override fun spaceExistanceTypeBetweenTokens(astNode: ASTNode, astNode2: ASTNode) =
			ParserDefinition.SpaceRequirements.MAY
}
