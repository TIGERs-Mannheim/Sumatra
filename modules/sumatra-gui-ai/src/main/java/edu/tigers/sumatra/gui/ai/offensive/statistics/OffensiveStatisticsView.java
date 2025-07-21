/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.offensive.statistics;

import edu.tigers.sumatra.gui.ai.offensive.statistics.presenter.OffensiveStatisticsPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This view shows information about offensiveStrategy
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStatisticsView extends ASumatraView
{

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	  * displays analyzed statistic frames
	  */
	public OffensiveStatisticsView()
	{
		super(ESumatraViewType.OFFENSIVE_STATISTICS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new OffensiveStatisticsPresenter();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
