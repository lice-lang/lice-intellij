package org.lice.lang

import com.intellij.lang.*
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import org.lice.lang.psi.LiceTypes


class LiceParserDefinition : ParserDefinition {
	override fun createElement(astNode: ASTNode): PsiElement = LiceTypes.Factory.createElement(astNode)
	override fun getFileNodeType() = FILE
	override fun createFile(viewProvider: FileViewProvider) = LiceFile(viewProvider)
	override fun createParser(project: Project) = LiceParser()
	override fun getStringLiteralElements() = LiceTokenType.STRINGS
	override fun getCommentTokens() = LiceTokenType.COMMENTS
	override fun createLexer(project: Project) = LiceLexerAdapter()
	override fun spaceExistanceTypeBetweenTokens(astNode: ASTNode, astNode2: ASTNode) =
			ParserDefinition.SpaceRequirements.MAY

	companion object {
		@JvmField val FILE = IFileElementType(Language.findInstance(LiceLanguage::class.java))
	}
}
