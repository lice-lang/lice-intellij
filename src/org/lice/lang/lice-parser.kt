package org.lice.lang

import com.intellij.lang.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet


class LiceParserDefinition: ParserDefinition {
	override fun createParser(project: Project): PsiParser {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun createFile(viewProvider: FileViewProvider): PsiFile {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun spaceExistanceTypeBetweenTokens(astNode: ASTNode, astNode2: ASTNode): ParserDefinition.SpaceRequirements {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getStringLiteralElements(): TokenSet {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getFileNodeType(): IFileElementType {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun createLexer(project: Project): Lexer {
		return LiceLexerAdapter()
	}

	override fun createElement(astNode: ASTNode): PsiElement {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getCommentTokens(): TokenSet {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

}
