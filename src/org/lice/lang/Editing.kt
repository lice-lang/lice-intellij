/**
 * Created by ice1000 on 2017/3/28.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.CreateFileAction
import com.intellij.lang.Commenter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.apache.commons.lang.StringUtils
import org.lice.compiler.util.println
import org.lice.lang.LiceInfo.EXTENSION
import org.lice.lang.LiceInfo.LICE_ICON
import org.lice.tools.displaySemanticTree
import org.lice.tools.displaySyntaxTree
import java.io.File
import java.time.LocalDate
import javax.swing.Icon


class NewLiceFile : CreateFileAction(CAPTION, "", LICE_ICON) {
	override fun getActionName(p0: PsiDirectory?, p1: String?) =
			CAPTION

	override fun getErrorTitle() =
			CommonBundle.getErrorTitle()!!

	override fun getDefaultExtension() =
			EXTENSION

	override fun create(
			name: String?,
			directory: PsiDirectory?
	): Array<PsiElement?> {
		val origin = name ?: "new-file${System.currentTimeMillis()}.$EXTENSION"
		val fixedExtension = when (FileUtilRt.getExtension(origin)) {
			EXTENSION -> origin
			else -> "$origin.$EXTENSION"
		}
		return arrayOf(directory?.add(PsiFileFactory
				.getInstance(directory.project)
				.createFileFromText(fixedExtension, LiceFileType, """;
; Created by ${System.getenv("USERNAME")} on ${LocalDate.now()}
;

(|>)
""")))
	}

	override fun invokeDialog(
			project: Project?,
			psiDirectory: PsiDirectory?
	): Array<PsiElement> {
		val validator = MyInputValidator(project, psiDirectory)
		Messages.showInputDialog(
				project,
				"Enter a new lice script name",
				"New Lice script",
				Messages.getQuestionIcon(),
				"file-name${System.currentTimeMillis()}.$EXTENSION",
				validator
		)
		return validator.createdElements
	}

	private companion object Caption {
		private val CAPTION = "New Lice File"
	}
}

abstract class LiceFileActions(
		text: String,
		description: String,
		icon: Icon
) : AnAction(text, description, icon) {

	protected fun compatibleFiles(e: AnActionEvent): Array<out VirtualFile> =
			(CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext) ?: emptyArray()).filter { file ->
				file is VirtualFile && EXTENSION == file.extension
			}.toTypedArray()
}

class RunLiceFile : LiceFileActions(
		"Run Lice script",
		"Run Lice script",
		AllIcons.Toolwindows.ToolWindowRun) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)?.let { doc ->
				FileDocumentManager
						.getInstance()
						.saveDocument(doc)
			}
			Runtime.getRuntime().exec(
					StringUtils.join(arrayOf(
							LiceInfo.JAVA_PATH_WRAPPED,
							"-classpath",
							"\"" + StringUtils.join(arrayOf(
									LiceInfo.KOTLIN_RUNTIME_PATH,
									LiceInfo.KOTLIN_REFLECT_PATH,
									LiceInfo.LICE_PATH
							), ";") + "\"",
							"org.lice.repl.Main",
							"\"" + file.path + "\""
					), " ").println(),
					null,
					File(file.parent.path)
			)
		}
	}

}

class ShowLiceFileSyntaxTree : LiceFileActions(
		"View Syntax Tree",
		"View Syntax Tree",
		LiceInfo.LICE_AST_NODE_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)?.let { doc ->
				FileDocumentManager
						.getInstance()
						.saveDocument(doc)
			}
			displaySyntaxTree(File(file.path))
		}
	}
}

class ShowLiceFileSemanticTree : LiceFileActions(
		"View Semantic Tree",
		"View Semantic Tree",
		LiceInfo.LICE_AST_NODE2_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)?.let { doc ->
				FileDocumentManager
						.getInstance()
						.saveDocument(doc)
			}
			displaySemanticTree(File(file.path))
		}
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
