/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.rcm.view;

import edu.tigers.sumatra.rcm.ExtIdentifier;

import java.util.List;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IIdentifierSelectionObserver
{
	/**
	 * @param identifiers
	 */
	void onIdentifiersSelected(List<ExtIdentifier> identifiers);
	
	
	/**
	 */
	void onIdentifiersSelectionCanceled();
}
