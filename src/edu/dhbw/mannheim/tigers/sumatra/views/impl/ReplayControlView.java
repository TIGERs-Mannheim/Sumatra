/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.views.impl;

import edu.dhbw.mannheim.tigers.sumatra.presenter.replay.ReplayControlPresenter;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ESumatraViewType;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayControlView extends ASumatraView
{
	/**
 * 
 */
	public ReplayControlView()
	{
		super(ESumatraViewType.REPLAY_CONTROL);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new ReplayControlPresenter();
	}
	
}
