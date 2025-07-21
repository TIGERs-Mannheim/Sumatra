/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.offensive.interceptions;

import edu.tigers.sumatra.gui.ai.offensive.interceptions.presenter.OffensiveInterceptionsPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This view shows information about offensive interceptions
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveInterceptionsView extends ASumatraView
{

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	  *
	  */
	public OffensiveInterceptionsView()
	{
		super(ESumatraViewType.OFFENSIVE_INTERCEPTIONS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new OffensiveInterceptionsPresenter();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
