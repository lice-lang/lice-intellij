package org.lice.lang.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.lice.lang.LiceLanguage

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	companion object {
		@JvmField val WHITE_SPACE = TokenType.WHITE_SPACE
		@JvmField val COMMENT = LiceTokenType("COMMENT")
		@JvmField val LB = LiceTokenType("LEFT_BRACKET")
		@JvmField val RB = LiceTokenType("RIGHT_BRACKET")
		@JvmField val SYM = LiceTokenType("SYMBOL")
		@JvmField val STR = LiceTokenType("STRING")
	}

	override fun toString() = "Lice: ${super.toString()}"
}


