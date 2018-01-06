package org.lice.lang.execution;

import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ice1000
 */
public class LiceRunConfigurationEditor extends SettingsEditor<LiceRunConfiguration> {
	private @NotNull JPanel mainPanel;
	private @NotNull CommonJavaParametersPanel javaParamsPanel;
	private @NotNull Project project;

	public LiceRunConfigurationEditor(@NotNull Project project) {
		this.project = project;
	}

	public @NotNull JPanel getMainPanel() {
		return mainPanel;
	}

	@Override protected void resetEditorFrom(@NotNull LiceRunConfiguration configuration) {
	}

	@Override protected void applyEditorTo(@NotNull LiceRunConfiguration configuration) {
	}

	@NotNull @Override protected JPanel createEditor() {
		return mainPanel;
	}
}
