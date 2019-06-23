/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.views.impl;

import edu.dhbw.mannheim.tigers.sumatra.presenter.ball.BallAnalyserPresenter;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ESumatraViewType;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


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
		super(ESumatraViewType.BALL_ANALYSER);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new BallAnalyserPresenter();
	}
}
