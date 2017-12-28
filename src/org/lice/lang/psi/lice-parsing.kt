/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang.psi

import com.intellij.psi.tree.IElementType
import org.lice.lang.LiceLanguage

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	override fun toString() = "LiceTokenType.${super.toString()}"
}

object LiceTypes {
	val LEFT_BRACE = LiceTokenType("LEFT_BRACE")
	val RIGHT_BRACE = LiceTokenType("RIGHT_BRACE")

}

