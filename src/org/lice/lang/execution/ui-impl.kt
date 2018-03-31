package org.lice.lang.execution

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.DocumentAdapter
import icons.LiceIcons
import org.jetbrains.annotations.Nls
import org.lice.lang.*
import org.lice.lang.module.LiceFacetSettingsTab
import java.text.NumberFormat
import javax.swing.event.DocumentEvent
import javax.swing.text.DefaultFormatterFactory
import javax.swing.text.NumberFormatter

class LiceFacetSettingsTabImpl(
		private val settings: LiceModuleSettings, project: Project?) : LiceFacetSettingsTab() {
	init {
		val format = NumberFormat.getIntegerInstance()
		format.isGroupingUsed = false
		val factory = DefaultFormatterFactory(NumberFormatter(format))
		mainClassField.text = settings.mainClass
		timeLimitField.value = settings.tryEvaluateTimeLimit
		timeLimitField.formatterFactory = factory
		textLimitField.value = settings.tryEvaluateTextLimit
		textLimitField.formatterFactory = factory
		jarPathField.text = settings.jarPath
		jarPathField.addBrowseFolderListener(LiceBundle.message("lice.messages.select-jar.title"),
				LiceBundle.message("lice.messages.select-jar.body"),
				project,
				jarChooser)
		jarPathField.textField.document.addDocumentListener(object : DocumentAdapter() {
			override fun textChanged(documentEvent: DocumentEvent) {
				validationInfo.isVisible = !validateLice(jarPathField.text)
			}
		})
		resetToDefaultButton.addActionListener {
			mainClassField.text = LICE_MAIN_DEFAULT
			settings.mainClass = LICE_MAIN_DEFAULT
		}
		usePluginJarButton.addActionListener {
			if (Messages.showYesNoDialog(LiceBundle.message("lice.messages.give-up-old"),
							LiceBundle.message("lice.messages.use-plugin-jar"),
							LiceBundle.message("lice.messages.yes-yes-yes"),
							LiceBundle.message("lice.messages.no-no-no"),
							LiceIcons.JOJO_ICON) == Messages.YES) {
				jarPathField.text = liceJarPath
				settings.jarPath = liceJarPath
			}
		}
		validationInfo.isVisible = false
	}

	@Throws(ConfigurationException::class)
	override fun apply() {
		settings.mainClass = mainClassField.text
		settings.tryEvaluateTextLimit = (textLimitField.value as Number).toInt()
		settings.tryEvaluateTimeLimit = (timeLimitField.value as Number).toLong()
		settings.jarPath = jarPathField.text
		if (validationInfo.isVisible) throw ConfigurationException("Invalid Lice jar")
		super.apply()
	}

	override fun createComponent() = mainPanel

	override fun isModified() = jarPathField.text.trim { it <= ' ' } != settings.jarPath ||
			mainClassField.text.trim { it <= ' ' } != settings.mainClass ||
			textLimitField.text.trim { it <= ' ' } != settings.tryEvaluateTextLimit.toString() ||
			timeLimitField.text.trim { it <= ' ' } != settings.tryEvaluateTimeLimit.toString()

	@Nls
	override fun getDisplayName() = LiceBundle.message("lice.name")
}

