package org.lice.lang.execution;

import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.lice.lang.LiceFileType;

import javax.swing.*;

import static org.lice.lang.execution.Lice_run_configKt.jarChooser;

/**
 * @author ice1000
 */
public class LiceRunConfigurationEditor extends SettingsEditor<LiceRunConfiguration> {
	private @NotNull JPanel mainPanel;
	private @NotNull CommonJavaParametersPanel javaParamsPanel;
	private @NotNull TextFieldWithBrowseButton jarLocationField;
	private @NotNull TextFieldWithBrowseButton targetFileField;
	private @NotNull JTextField jreLocationField;

	public LiceRunConfigurationEditor(@NotNull LiceRunConfiguration configuration) {
		jarLocationField.addBrowseFolderListener(new TextBrowseFolderListener(jarChooser));
		targetFileField.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileDescriptor(
				LiceFileType.INSTANCE)));
		javaParamsPanel.getProgramParametersComponent().setEnabled(false);
		resetEditorFrom(configuration);
	}

	@Override protected void resetEditorFrom(@NotNull LiceRunConfiguration configuration) {
		javaParamsPanel.reset(configuration);
		jreLocationField.setText(configuration.getJreLocation());
		jarLocationField.setText(configuration.getJarLocation());
		targetFileField.setText(configuration.getTargetFile());
	}

	@Override protected void applyEditorTo(@NotNull LiceRunConfiguration configuration) {
		javaParamsPanel.applyTo(configuration);
		configuration.setJarLocation(jarLocationField.getText());
		configuration.setTargetFile(targetFileField.getText());
		configuration.setJreLocation(jreLocationField.getText());
	}

	@Override protected @NotNull JPanel createEditor() {
		return mainPanel;
	}
}
