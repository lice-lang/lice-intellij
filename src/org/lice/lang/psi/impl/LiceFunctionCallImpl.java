package org.lice.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lice.lang.psi.*;

import java.util.List;

/**
 * Human edited, so move to git repo
 *
 * @author ice1000
 */
public class LiceFunctionCallImpl extends ASTWrapperPsiElement implements LiceFunctionCall {
	private LiceSymbolReference[] references = null;
	private boolean possibleEval = true;

	public LiceFunctionCallImpl(ASTNode node) {
		super(node);
	}

	public void accept(@NotNull LiceVisitor visitor) {
		visitor.visitFunctionCall(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof LiceVisitor) accept((LiceVisitor) visitor);
		else super.accept(visitor);
	}

	@Override public @NotNull List<LiceElement> getElementList() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, LiceElement.class);
	}

	public @Nullable LiceElement getLiceCallee() {
		return LicePsiImplUtils.getLiceCallee(this);
	}

	public @NotNull List<LiceElement> getNonCommentElements() {
		return LicePsiImplUtils.getNonCommentElements(this);
	}

	public @NotNull PsiElement setName(String newName) {
		return LicePsiImplUtils.setName(this, newName);
	}

	public @Nullable String getName() {
		return LicePsiImplUtils.getName(this);
	}

	public @Nullable LiceSymbol getNameIdentifier() {
		return LicePsiImplUtils.getNameIdentifier(this);
	}

	public @NotNull List<LiceElement> getNameIdentifierAndParams() {
		return LicePsiImplUtils.getNameIdentifierAndParams(this);
	}

	public @NotNull PsiReference[] getReferences() {
		if (references == null) references = LicePsiImplUtils.getReferences(this);
		for (LiceSymbolReference reference : references) reference.getSymbol().setResolved(true);
		return references;
	}

	@Override public void subtreeChanged() {
		references = null;
		possibleEval = true;
		super.subtreeChanged();
	}

	@Override public boolean isPossibleEval() {
		return possibleEval;
	}

	@Override public void setPossibleEval(boolean possibleEval) {
		this.possibleEval = possibleEval;
	}
}
