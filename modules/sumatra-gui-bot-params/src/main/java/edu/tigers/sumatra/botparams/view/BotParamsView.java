/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botparams.view;

import edu.tigers.sumatra.botparams.presenter.BotParamsPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotParamsView extends ASumatraView
{
	/**
	 * Constructor.
	 */
	public BotParamsView()
	{
		super(ESumatraViewType.BOT_PARAMS);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new BotParamsPresenter();
	}
	
}
