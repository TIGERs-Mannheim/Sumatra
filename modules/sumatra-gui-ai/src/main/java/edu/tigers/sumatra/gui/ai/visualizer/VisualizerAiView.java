/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.visualizer;

import edu.tigers.sumatra.gui.ai.visualizer.presenter.VisualizerAiPresenter;
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
