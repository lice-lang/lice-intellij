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
	private @NotNull LiceRunConfiguration settings;

	public LiceRunConfigurationEditor(
			@NotNull Project project, @NotNull LiceRunConfiguration settings) {
		this.project = project;
		this.settings = settings;
		javaParamsPanel.getProgramParametersComponent().setEnabled(false);
	}

	@Override protected void resetEditorFrom(@NotNull LiceRunConfiguration configuration) {
		javaParamsPanel.reset(configuration);
		settings.setJarLocation(configuration.getJarLocation());
	}

	@Override protected void applyEditorTo(@NotNull LiceRunConfiguration configuration) {
		javaParamsPanel.applyTo(configuration);
		configuration.setJarLocation(settings.getJarLocation());
	}

	@NotNull @Override protected JPanel createEditor() {
		return mainPanel;
	}
}
