/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 18, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ExtIdentifier;


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
