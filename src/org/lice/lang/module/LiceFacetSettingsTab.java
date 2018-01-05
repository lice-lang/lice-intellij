package org.lice.lang.module;

import com.intellij.facet.ui.FacetEditorTab;
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
	private @NotNull JTextField jarPathField;
	private @NotNull JTextField mainClassField;
	private @NotNull JButton usePluginJarButton;
	private @NotNull JButton resetToDefaultButton;
	private @NotNull LiceModuleSettings settings;

	public LiceFacetSettingsTab(@NotNull LiceModuleSettings settings) {
		this.settings = settings;
		mainClassField.setText(settings.mainClass);
		jarPathField.setText(settings.jarPath);
		resetToDefaultButton.addActionListener(actionEvent -> mainClassField.setText(LICE_MAIN_DEFAULT));
		usePluginJarButton.addActionListener(actionEvent -> jarPathField.setText(LICE_PATH));
	}

	@Override public @NotNull JComponent createComponent() {
		return mainPanel;
	}

	@Override public boolean isModified() {
		return jarPathField.getText().trim().equals(settings.jarPath) &&
				mainClassField.getText().trim().equals(settings.mainClass);
	}

	@Override public @Nls @NotNull String getDisplayName() {
		return LICE_NAME;
	}
}
