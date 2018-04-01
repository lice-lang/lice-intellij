package org.lice.lang.action;

import org.jetbrains.annotations.NotNull;
import org.lice.model.MetaData;
import org.lice.util.LiceException;

import java.util.List;

/**
 * @author ice1000
 * @see org.lice.util.LiceException
 */
public class LiceExceptionWrapper extends LiceException {
	public @NotNull String show(@NotNull List<String> cachedCodes) {
		return prettify(cachedCodes);
	}

	public LiceExceptionWrapper(@NotNull LiceException original) throws NoSuchFieldException, IllegalAccessException {
		super(original.getMessage(), (MetaData) LiceException.class.getDeclaredField("meta").get(original));
	}
}
