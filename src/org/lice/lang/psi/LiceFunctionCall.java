package org.lice.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Human edited, so move to git repop
 *
 * @author ice1000
 */
public interface LiceFunctionCall extends PsiNameIdentifierOwner {

	@NotNull List<LiceElement> getElementList();

	@Nullable LiceElement getLiceCallee();

	@NotNull List<LiceElement> getNonCommentElements();

	@NotNull PsiElement setName(@NotNull String newName);

	@Nullable String getName();

	@Nullable LiceSymbol getNameIdentifier();

	@NotNull List<LiceElement> getNameIdentifierAndParams();

	@NotNull PsiReference[] getReferences();

	boolean isPossibleEval();

	void setPossibleEval(boolean possibleEval);
}
