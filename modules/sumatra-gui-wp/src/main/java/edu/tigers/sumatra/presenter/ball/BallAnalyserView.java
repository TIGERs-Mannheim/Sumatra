/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.ball;

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
