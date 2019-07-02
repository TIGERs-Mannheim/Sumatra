/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.view;

import edu.tigers.sumatra.kick.presenter.BallKickIdentPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BallKickIdentView extends ASumatraView
{
	public BallKickIdentView()
	{
		super(ESumatraViewType.BALL_KICK_IDENT);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new BallKickIdentPresenter();
	}
}
