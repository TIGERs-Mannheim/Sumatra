/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


public class VisualizerView extends ASumatraView
{
	public VisualizerView()
	{
		super(ESumatraViewType.VISUALIZER);
	}


	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new VisualizerPresenter();
	}
}
