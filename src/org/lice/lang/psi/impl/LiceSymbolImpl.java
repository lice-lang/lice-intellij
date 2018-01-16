package org.lice.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.lice.lang.psi.LiceSymbol;
import org.lice.lang.psi.LiceSymbolReference;
import org.lice.lang.psi.LiceVisitor;

/**
 * Human edited, so move to git repo
 * @author ice1000
 */
public class LiceSymbolImpl extends ASTWrapperPsiElement implements LiceSymbol {
	private boolean resolved = false;
	private LiceSymbolReference reference;

	@Override public boolean isResolved() {
		return resolved;
	}

	@Override public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public LiceSymbolImpl(ASTNode node) {
		super(node);
	}

	public void accept(@NotNull LiceVisitor visitor) {
		visitor.visitSymbol(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof LiceVisitor) accept((LiceVisitor) visitor);
		else super.accept(visitor);
	}

	public @NotNull LiceSymbolReference getReference() {
		if (reference == null) reference = LicePsiImplUtils.getReference(this);
		return reference;
	}

	@Override public void subtreeChanged() {
		reference = null;
		super.subtreeChanged();
	}

	public @NotNull PsiReference[] getReferences() {
		return LicePsiImplUtils.getReferences(this);
	}

}
