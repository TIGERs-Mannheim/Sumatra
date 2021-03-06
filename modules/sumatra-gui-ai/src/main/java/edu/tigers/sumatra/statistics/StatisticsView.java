/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statistics;

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
	/**
	 * Default
	 */
	public StatisticsView()
	{
		super(ESumatraViewType.STATISTICS);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new StatisticsPresenter();
	}
}
