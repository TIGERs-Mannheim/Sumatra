/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.botoverview;

import edu.tigers.sumatra.gui.ai.botoverview.presenter.BotOverviewPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This view shows information about each bot
 */
public class BotOverviewView extends ASumatraView
{
	public BotOverviewView()
	{
		super(ESumatraViewType.BOT_OVERVIEW);
	}


	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new BotOverviewPresenter();
	}
}
