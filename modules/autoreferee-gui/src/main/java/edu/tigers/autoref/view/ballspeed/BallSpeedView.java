/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.ballspeed;

import edu.tigers.autoref.presenter.BallSpeedPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author "Lukas Magel"
 */
public class BallSpeedView extends ASumatraView
{
	/**
	 * 
	 */
	public BallSpeedView()
	{
		super(ESumatraViewType.BALL_SPEED);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new BallSpeedPresenter();
	}
	
}
