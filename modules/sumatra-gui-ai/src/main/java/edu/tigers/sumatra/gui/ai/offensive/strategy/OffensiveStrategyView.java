/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.offensive.strategy;

import edu.tigers.sumatra.gui.ai.offensive.strategy.presenter.OffensiveStrategyPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This view shows information about offensiveStrategy
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStrategyView extends ASumatraView
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
	public OffensiveStrategyView()
	{
		super(ESumatraViewType.OFFENSIVE_STRATEGY);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new OffensiveStrategyPresenter();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
