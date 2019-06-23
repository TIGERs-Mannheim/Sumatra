/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


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
