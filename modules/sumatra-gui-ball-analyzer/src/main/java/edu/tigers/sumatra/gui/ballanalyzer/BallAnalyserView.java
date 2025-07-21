/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ballanalyzer;

import edu.tigers.sumatra.gui.ballanalyzer.presenter.VisionAnalyserPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * View for managing ball data
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallAnalyserView extends ASumatraView
{
	/**
	 * 
	 */
	public BallAnalyserView()
	{
		super(ESumatraViewType.VISION_ANALYSER);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new VisionAnalyserPresenter();
	}
}
