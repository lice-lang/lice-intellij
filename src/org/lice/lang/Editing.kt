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
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.*
import org.apache.commons.lang.StringUtils
import org.lice.lang.LiceInfo.EXTENSION
import org.lice.lang.LiceInfo.LICE_ICON
import org.lice.lang.psi.LiceTokenType
import org.lice.tools.displaySemanticTree
import org.lice.tools.displaySyntaxTree
import java.io.File
import java.time.LocalDate
import javax.swing.Icon


class NewLiceFile : CreateFileAction(CAPTION, "", LICE_ICON) {
	override fun getActionName(p0: PsiDirectory?, p1: String?) = CAPTION
	override fun getErrorTitle(): String = CommonBundle.getErrorTitle()
	override fun getDefaultExtension() = EXTENSION

	override fun create(name: String, directory: PsiDirectory): Array<PsiElement> {
		val fixedExtension = when (FileUtilRt.getExtension(name)) {
			EXTENSION -> name
			else -> "$name.$EXTENSION"
		}
		return arrayOf(directory.add(PsiFileFactory
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
			CommonDataKeys
					.VIRTUAL_FILE_ARRAY
					.getData(e.dataContext)
					?.filter { file -> file.fileType is LiceFileType }
					?.toTypedArray()
					?: emptyArray()

//	protected fun printFiles(e: AnActionEvent) {
//		CommonDataKeys
//				.VIRTUAL_FILE_ARRAY
//				.getData(e.dataContext)
//				?.forEach { println(it.path) }
//	}

	override fun update(e: AnActionEvent?) {
		e?.presentation?.run {
			isEnabledAndVisible = compatibleFiles(e).run {
				isNotEmpty() and all { EXTENSION == it.extension }
			}
		}
	}

}

class RunLiceFile : LiceFileActions(
		"Run Lice script",
		"Run Lice script with lice interpreter",
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
					), " ").also { println(it) },
					null,
					File(file.parent.path)
			)
		}
	}
}

class ShowLiceFileSyntaxTree : LiceFileActions(
		"View Syntax Tree",
		"View Lice file syntax tree in a window",
		LiceInfo.LICE_AST_NODE_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)?.let(FileDocumentManager.getInstance()::saveDocument)
			displaySyntaxTree(File(file.path))
		}
	}
}

class ShowLiceFileSemanticTree : LiceFileActions(
		"View Semantic Tree",
		"View Lice file semantic tree in a window",
		LiceInfo.LICE_AST_NODE2_ICON) {
	override fun actionPerformed(e: AnActionEvent) {
		compatibleFiles(e).forEach { file ->
			FileDocumentManager
					.getInstance()
					.getDocument(file)
					?.let(FileDocumentManager.getInstance()::saveDocument)
			displaySemanticTree(File(file.path))
		}
	}
}

class LiceCommenter : Commenter {
	override fun getCommentedBlockCommentPrefix() = blockCommentPrefix
	override fun getCommentedBlockCommentSuffix() = blockCommentSuffix
	override fun getBlockCommentPrefix() = null
	override fun getBlockCommentSuffix() = null
	override fun getLineCommentPrefix() = ";"
}
