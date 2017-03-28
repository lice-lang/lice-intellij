package lice.plugin.intellij.psi

import com.intellij.psi.tree.IElementType
import lice.plugin.intellij.LiceLanguage

/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	override fun toString() = "LiceTokenType.${super.toString()}"
}

class LiceElementType(debugName: String) : IElementType(debugName, LiceLanguage)





