/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


@FunctionalInterface
public interface SimpleDocumentListener extends DocumentListener
{
	void update(DocumentEvent e);

	@Override
	default void insertUpdate(DocumentEvent e)
	{
		update(e);
	}

	@Override
	default void removeUpdate(DocumentEvent e)
	{
		update(e);
	}

	@Override
	default void changedUpdate(DocumentEvent e)
	{
		update(e);
	}
}
