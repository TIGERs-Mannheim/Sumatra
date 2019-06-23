/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 18, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoref.view.visualizer;

import edu.tigers.autoref.presenter.VisualizerRefPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Lukas Magel
 */
public class VisualizerAutoRefView extends ASumatraView
{
	
	/**
	 */
	public VisualizerAutoRefView()
	{
		super(ESumatraViewType.VISUALIZER);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new VisualizerRefPresenter();
	}
	
	
}
