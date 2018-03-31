package org.lice.lang.module;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ice1000
 */
public abstract class LiceFacetSettingsTab extends FacetEditorTab {
	protected @NotNull JPanel mainPanel;
	protected @NotNull TextFieldWithBrowseButton jarPathField;
	protected @NotNull JTextField mainClassField;
	protected @NotNull JButton usePluginJarButton;
	protected @NotNull JButton resetToDefaultButton;
	protected @NotNull JLabel validationInfo;
	protected @NotNull JFormattedTextField timeLimitField;
	protected @NotNull JFormattedTextField textLimitField;
}
