/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IElementType
import org.lice.lang.LiceFileType
import org.lice.lang.LiceInfo.LANGUAGE_NAME
import org.lice.lang.LiceInfo.LICE_ICON
import org.lice.lang.LiceLanguage

class LiceTokenType(debugName: String) : IElementType(debugName, LiceLanguage) {
	companion object TokenTypes {
		val LEFT_BRACE = LiceTokenType("LEFT_BRACE")
		val RIGHT_BRACE = LiceTokenType("RIGHT_BRACE")
	}

	override fun toString() = "LiceTokenType.${super.toString()}"
}

class LiceElementType(debugName: String) : IElementType(debugName, LiceLanguage)

class LiceFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LiceLanguage) {
	override fun getFileType() = LiceFileType
	override fun toString() = "$LANGUAGE_NAME File"
	override fun getIcon(flags: Int) = LICE_ICON
}
