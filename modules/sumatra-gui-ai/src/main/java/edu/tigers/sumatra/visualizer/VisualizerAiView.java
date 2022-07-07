/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * Visualizer for Sumatra (AI).
 */
public class VisualizerAiView extends ASumatraView
{
	public VisualizerAiView()
	{
		super(ESumatraViewType.VISUALIZER);
	}


	@Override
	public ISumatraViewPresenter createPresenter()
	{
		return new VisualizerAiPresenter();
	}
}
