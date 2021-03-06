/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.view;

import edu.tigers.sumatra.botcenter.presenter.BotCenterPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 */
public class BotCenterView extends ASumatraView
{
	public BotCenterView()
	{
		super(ESumatraViewType.BOT_CENTER);
	}


	@Override
	public ISumatraViewPresenter createPresenter()
	{
		return new BotCenterPresenter();
	}
}
