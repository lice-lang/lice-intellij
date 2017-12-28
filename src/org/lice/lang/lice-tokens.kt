package org.lice.lang

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	companion object {
		@JvmField val WHITE_SPACE: IElementType = TokenType.WHITE_SPACE
		@JvmField val COMMENT = LiceTokenType("COMMENT")
		@JvmField val LB = LiceTokenType("LEFT_BRACKET")
		@JvmField val RB = LiceTokenType("RIGHT_BRACKET")
		@JvmField val SYM = LiceTokenType("SYMBOL")
		@JvmField val STR = LiceTokenType("STRING")
	}

	override fun toString() = "Lice: ${super.toString()}"
}


