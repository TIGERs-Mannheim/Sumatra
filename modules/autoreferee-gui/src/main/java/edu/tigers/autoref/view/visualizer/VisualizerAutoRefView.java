/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.visualizer;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.visualizer.VisualizerPresenter;


public class VisualizerAutoRefView extends ASumatraView
{
	public VisualizerAutoRefView()
	{
		super(ESumatraViewType.VISUALIZER);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new VisualizerPresenter();
	}
}
