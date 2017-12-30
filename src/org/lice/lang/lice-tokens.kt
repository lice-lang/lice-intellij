package org.lice.lang

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	companion object {
		@JvmField val WHITE_SPACE: IElementType = TokenType.WHITE_SPACE
		@JvmField val COMMENT = LiceTokenType("comment")
		@JvmField val LB = LiceTokenType("(")
		@JvmField val RB = LiceTokenType(")")
		@JvmField val SYM = LiceTokenType("symbol")
		@JvmField val STR = LiceTokenType("string literal")
		@JvmField val COMMENTS = TokenSet.create(COMMENT)
		@JvmField val WHITE_SPACES = TokenSet.create(WHITE_SPACE)
		@JvmField val STRINGS = TokenSet.create(STR)
	}
}

class LiceElementType(debugName: String) : IElementType(debugName, LiceLanguage)
