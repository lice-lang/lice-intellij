/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lexer.FlexAdapter
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.lice.lang.LiceFileType
import org.lice.lang.LiceInfo.LANGUAGE_NAME
import org.lice.lang.LiceInfo.LICE_ICON
import org.lice.lang.LiceLanguage
import org.lice.lang.LiceLexer
import org.lice.lang.LiceParser

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	companion object TokenTypes {
		val LEFT_BRACE = LiceTokenType("(")
		val RIGHT_BRACE = LiceTokenType(")")
	}

	override fun toString() =
			"LiceTokenType.${super.toString()}"
}

class LiceElementType(debugName: String) : IElementType(debugName, LiceLanguage)

class LiceFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LiceLanguage) {
	override fun getFileType() =
			LiceFileType
	override fun toString() =
			"$LANGUAGE_NAME File"
	override fun getIcon(flags: Int) =
			LICE_ICON
}

class LiceLexerAdapter : FlexAdapter(LiceLexer(null))

class LiceParserDefinition : ParserDefinition {
	private companion object Tokens {
		val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
		val COMMENTS = TokenSet.create(LiceTypes.COMMENT)

		val FILE = IFileElementType(LiceLanguage)
	}

	override fun createLexer(p0: Project?) =
			LiceLexerAdapter()

	override fun getWhitespaceTokens() =
			WHITE_SPACES

	override fun getCommentTokens() =
			COMMENTS

	override fun getStringLiteralElements(): TokenSet =
			TokenSet.EMPTY

	override fun createParser(p0: Project?) =
			LiceParser()

	override fun getFileNodeType() =
			FILE

	override fun createFile(p0: FileViewProvider) =
			LiceFile(p0)

	override fun createElement(p0: ASTNode): PsiElement =
			LiceTypes.Factory.createElement(p0)

	override fun spaceExistanceTypeBetweenTokens(p0: ASTNode?, p1: ASTNode?) =
			ParserDefinition.SpaceRequirements.MAY
}
