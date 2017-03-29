/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.ide.actions.CreateFileAction
import com.intellij.lang.Commenter
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.lice.lang.LiceInfo.EXTENSION
import org.lice.lang.LiceInfo.LICE_ICON


class NewLiceFile : CreateFileAction(CAPTION, "", LICE_ICON) {
	override fun getActionName(p0: PsiDirectory?, p1: String?) =
			CAPTION

	override fun getDefaultExtension() =
			EXTENSION

	private companion object Caption {
		private val CAPTION = "New Lice File"
	}
}

class LiceCommenter : Commenter {
	override fun getCommentedBlockCommentPrefix() =
			blockCommentPrefix

	override fun getCommentedBlockCommentSuffix() =
			blockCommentSuffix

	override fun getBlockCommentPrefix() = null
	override fun getBlockCommentSuffix() = null

	override fun getLineCommentPrefix() = ";"
}

class LiceSpellCheckingStrategy : SpellcheckingStrategy() {
	override fun getTokenizer(element: PsiElement): Tokenizer<PsiElement> {
		return super.getTokenizer(element)
//		return when (element.elementType) {
//		}
	}
}
