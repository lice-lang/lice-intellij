/**
 * Created by ice1000 on 2017/3/29.
 *
 * @author ice1000
 */
package org.lice.lang

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.*

class LiceCodeStyleSettings(container: CodeStyleSettings?)
	: CustomCodeStyleSettings("LiceCodeStyleSettings", container)

class LiceCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
	override fun getLanguage() =
			LiceLanguage

	override fun createCustomSettings(settings: CodeStyleSettings?) =
			LiceCodeStyleSettings(settings)

	override fun createSettingsPage(
			settings: CodeStyleSettings,
			originalSettings: CodeStyleSettings
	) = LiceCodeStyleConfigurable(settings, originalSettings)
}

class LiceCodeStyleConfigurable(
		settings: CodeStyleSettings,
		cloneSettings: CodeStyleSettings
) : CodeStyleAbstractConfigurable(settings, cloneSettings, "Lice") {

	override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel
			= Panel(currentSettings, settings)

	override fun getHelpTopic() = null

	private class Panel(
			currentSettings: CodeStyleSettings,
			settings: CodeStyleSettings
	) : TabbedLanguageCodeStylePanel(
			LiceLanguage,
			currentSettings,
			settings
	)
}


class LiceLangCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	override fun getLanguage() =
			LiceLanguage

	override fun getIndentOptionsEditor() =
			SmartIndentOptionsEditor()

	override fun getDefaultCommonSettings() =
			CommonCodeStyleSettings(language).apply {
				val indentOptions = initIndentOptions()
				indentOptions.INDENT_SIZE = 2
				indentOptions.TAB_SIZE = 2
				indentOptions.CONTINUATION_INDENT_SIZE = 4
			}

	override fun getCodeSample(settingsType: SettingsType): String {
		return """
;; recursive fibonacci
(def fib n (if
  (in? (list 1 2) n) 1
  (+ (fib (- n 1)) (fib (- n 2)))))

;; greatest common divisor
(def gcd a b (if (== b 0) a (gcd b (% a b))))

;; call-by-name functions
(defexpr my-if condition a (if condition a))

;; string literals
"ass we can"


"""
	}
}
