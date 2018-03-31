package org.lice.lang.module;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import icons.LiceIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lice.lang.LiceBundle;
import org.lice.lang.LiceModuleSettings;
import org.lice.lang.Lice_constantsKt;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

import static org.lice.lang.Lice_constantsKt.LICE_MAIN_DEFAULT;
import static org.lice.lang.execution.Lice_run_confgKt.jarChooser;
import static org.lice.lang.execution.Lice_run_confgKt.validateLice;

/**
 * @author ice1000
 */
public class LiceFacetSettingsTab extends FacetEditorTab {
	private @NotNull JPanel mainPanel;
	private @NotNull TextFieldWithBrowseButton jarPathField;
	private @NotNull JTextField mainClassField;
	private @NotNull JButton usePluginJarButton;
	private @NotNull JButton resetToDefaultButton;
	private @NotNull JLabel validationInfo;
	private @NotNull JFormattedTextField timeLimitField;
	private @NotNull JFormattedTextField textLimitField;
	private @NotNull LiceModuleSettings settings;

	public LiceFacetSettingsTab(@NotNull LiceModuleSettings settings, @Nullable Project project) {
		this.settings = settings;
		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(new NumberFormatter(format));
		mainClassField.setText(settings.getMainClass());
		timeLimitField.setValue(settings.getTryEvaluateTimeLimit());
		timeLimitField.setFormatterFactory(factory);
		textLimitField.setValue(settings.getTryEvaluateTextLimit());
		textLimitField.setFormatterFactory(factory);
		jarPathField.setText(settings.getJarPath());
		jarPathField.addBrowseFolderListener(LiceBundle.message("lice.messages.select-jar.title"),
				LiceBundle.message("lice.messages.select-jar.body"),
				project,
				jarChooser);
		jarPathField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
			@Override protected void textChanged(DocumentEvent documentEvent) {
				validationInfo.setVisible(!validateLice(jarPathField.getText()));
			}
		});
		resetToDefaultButton.addActionListener(actionEvent -> {
			mainClassField.setText(LICE_MAIN_DEFAULT);
			settings.setMainClass(LICE_MAIN_DEFAULT);
		});
		usePluginJarButton.addActionListener(actionEvent -> {
			if (Messages.showYesNoDialog(LiceBundle.message("lice.messages.give-up-old"),
					LiceBundle.message("lice.messages.use-plugin-jar"),
					LiceBundle.message("lice.messages.yes-yes-yes"),
					LiceBundle.message("lice.messages.no-no-no"),
					LiceIcons.JOJO_ICON) == Messages.YES) {
				jarPathField.setText(Lice_constantsKt.liceJarPath);
				settings.setJarPath(Lice_constantsKt.liceJarPath);
			}
		});
		validationInfo.setVisible(false);
	}

	@Override public void apply() throws ConfigurationException {
		settings.setMainClass(mainClassField.getText());
		settings.setTryEvaluateTextLimit(((Number) textLimitField.getValue()).intValue());
		settings.setTryEvaluateTimeLimit(((Number) timeLimitField.getValue()).longValue());
		settings.setJarPath(jarPathField.getText());
		if (validationInfo.isVisible()) throw new ConfigurationException("Invalid Lice jar");
		super.apply();
	}

	@Override public @NotNull JComponent createComponent() {
		return mainPanel;
	}

	@Override public boolean isModified() {
		return !jarPathField.getText().trim().equals(settings.getJarPath()) ||
				!mainClassField.getText().trim().equals(settings.getMainClass()) ||
				!textLimitField.getText().trim().equals(Integer.toString(settings.getTryEvaluateTextLimit())) ||
				!timeLimitField.getText().trim().equals(Long.toString(settings.getTryEvaluateTimeLimit()));
	}

	@Override public @Nls @NotNull String getDisplayName() {
		return LiceBundle.message("lice.name");
	}
}
