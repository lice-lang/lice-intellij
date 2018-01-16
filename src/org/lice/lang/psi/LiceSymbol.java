package org.lice.lang.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Human edited, so move to git repop
 *
 * @author ice1000
 */
public interface LiceSymbol extends PsiElement {

	boolean isResolved();

	void setResolved(boolean resolved);

	@NotNull
  LiceSymbolReference getReference();

}
