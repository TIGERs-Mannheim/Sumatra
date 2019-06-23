/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.rcm;

import java.util.List;

import edu.tigers.sumatra.rcm.ExtIdentifier;


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
