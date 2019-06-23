/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statistics;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This view shows information about gamestatistics
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class StatisticsView extends ASumatraView
{
	
	
	private final ETeamColor	teamColor;
	
	
	/**
	 * @param color
	 */
	public StatisticsView(final ETeamColor color)
	{
		super(color == ETeamColor.YELLOW ? ESumatraViewType.STATISTICS_YELLOW : ESumatraViewType.STATISTICS_BLUE);
		teamColor = color;
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new StatisticsPresenter(teamColor);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
