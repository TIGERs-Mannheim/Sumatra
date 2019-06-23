/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisualizerView extends ASumatraView
{
	/**
	 */
	public VisualizerView()
	{
		super(ESumatraViewType.VISUALIZER);
	}
	
	
	@Override
	public ISumatraViewPresenter createPresenter()
	{
		return new VisualizerPresenter();
	}
}
