/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.kick;

import edu.tigers.sumatra.gui.kick.presenter.BallKickIdentPresenter;
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
