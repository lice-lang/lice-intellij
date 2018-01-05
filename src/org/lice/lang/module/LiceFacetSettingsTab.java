package org.lice.lang.module;

import com.intellij.facet.ui.FacetEditorTab;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static org.lice.lang.Lice_constantsKt.*;

public class LiceFacetSettingsTab extends FacetEditorTab {
	private JPanel mainPanel;
	private JTextField jarPathField;
	private JTextField mainClassField;
	private JButton usePluginJarButton;
	private JButton resetToDefaultButton;
	private @NotNull LiceModuleSettings settings;

	public LiceFacetSettingsTab(@NotNull LiceModuleSettings settings) {
		this.settings = settings;
		mainClassField.setText(LICE_MAIN_DEFAULT);
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

	@Override public @Nls String getDisplayName() {
		return LICE_NAME;
	}
}
