/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer;

import edu.tigers.sumatra.gui.visualizer.presenter.VisualizerPresenter;
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
