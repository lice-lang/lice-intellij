package org.lice.lang

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.lice.lang.psi.LiceTypes

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	companion object {
		@JvmField val COMMENTS = TokenSet.create(LiceTypes.COMMENT)
		@JvmField val STRINGS = TokenSet.create(LiceTypes.STR)
	}
}

class LiceElementType(debugName: String) : IElementType(debugName, LiceLanguage)
