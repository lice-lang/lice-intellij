package org.lice.lang.execution;

import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.lice.lang.LiceFileType;

import javax.swing.*;

import static org.lice.lang.execution.Lice_run_confgKt.jarChooser;

/**
 * @author ice1000
 */
public class LiceRunConfigurationEditor extends SettingsEditor<LiceRunConfiguration> {
	private @NotNull JPanel mainPanel;
	private @NotNull CommonJavaParametersPanel javaParamsPanel;
	private @NotNull TextFieldWithBrowseButton jarLocationField;
	private @NotNull TextFieldWithBrowseButton targetFileField;
	private @NotNull Project project;
	private @NotNull LiceRunConfiguration settings;

	public LiceRunConfigurationEditor(
			@NotNull Project project, @NotNull LiceRunConfiguration settings) {
		this.project = project;
		this.settings = settings;
		jarLocationField.setText(settings.getJarLocation());
		targetFileField.setText(settings.getTargetFile());
		jarLocationField.addActionListener(actionEvent -> settings.setJarLocation(jarLocationField.getText()));
		jarLocationField.addBrowseFolderListener(new TextBrowseFolderListener(jarChooser));
		targetFileField.addActionListener(actionEvent -> settings.setTargetFile(targetFileField.getText()));
		targetFileField.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileDescriptor(
				LiceFileType.INSTANCE)));
		javaParamsPanel.getProgramParametersComponent().setEnabled(false);
	}

	@Override protected void resetEditorFrom(@NotNull LiceRunConfiguration configuration) {
		javaParamsPanel.reset(configuration);
		settings.replaceNonJavaCommonStatesWith(configuration);
	}

	@Override protected void applyEditorTo(@NotNull LiceRunConfiguration configuration) {
		javaParamsPanel.applyTo(configuration);
		configuration.replaceNonJavaCommonStatesWith(settings);
	}

	@Override protected @NotNull JPanel createEditor() {
		return mainPanel;
	}
}
