package org.lice.lang.module;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.lice.lang.LiceModuleSettings;

import javax.swing.*;

import static org.lice.lang.Lice_constantsKt.*;

/**
 * @author ice1000
 */
public class LiceFacetSettingsTab extends FacetEditorTab {
	private @NotNull JPanel mainPanel;
	private @NotNull TextFieldWithBrowseButton jarPathField;
	private @NotNull JTextField mainClassField;
	private @NotNull JButton usePluginJarButton;
	private @NotNull JButton resetToDefaultButton;
	private @NotNull LiceModuleSettings settings;

	public LiceFacetSettingsTab(@NotNull LiceModuleSettings settings) {
		this.settings = settings;
		mainClassField.setText(settings.getMainClass());
		mainClassField.addActionListener(actionEvent -> settings.setMainClass(mainClassField.getText()));
		jarPathField.addBrowseFolderListener("Select Lice Jar",
				"Selecting a Lice jar file",
				null,
				new FileChooserDescriptor(false, false, true, false, false, false));
		jarPathField.setText(settings.getJarPath());
		jarPathField.addActionListener(actionEvent -> settings.setJarPath(jarPathField.getText()));
		resetToDefaultButton.addActionListener(actionEvent -> {
			mainClassField.setText(LICE_MAIN_DEFAULT);
			settings.setMainClass(LICE_MAIN_DEFAULT);
		});
		usePluginJarButton.addActionListener(actionEvent -> {
			if (JOptionPane.showConfirmDialog(mainPanel,
					"Are you sure to give up the old path?",
					"Use Lice jar in the plugin",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				jarPathField.setText(LICE_PATH);
				settings.setJarPath(LICE_PATH);
			}
		});
	}

	@Override public @NotNull JComponent createComponent() {
		return mainPanel;
	}

	@Override public boolean isModified() {
		return jarPathField.getText().trim().equals(settings.getJarPath()) &&
				mainClassField.getText().trim().equals(settings.getMainClass());
	}

	@Override public @Nls @NotNull String getDisplayName() {
		return LICE_NAME;
	}
}
