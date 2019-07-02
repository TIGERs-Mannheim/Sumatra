/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview;

import java.awt.Component;

import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.botoverview.view.BotOverviewPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.UiThrottler;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Bot Overview Presenter
 */
public class BotOverviewPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{
	private final BotOverviewPanel botOverviewPanel = new BotOverviewPanel();
	private final UiThrottler visFrameThrottler = new UiThrottler(300);


	public BotOverviewPresenter()
	{
		visFrameThrottler.start();
	}


	@Override
	public void onStart()
	{
		SumatraModel.getInstance().getModule(Agent.class).addVisObserver(this);
	}


	@Override
	public void onStop()
	{
		SumatraModel.getInstance().getModule(Agent.class).removeVisObserver(this);
	}


	@Override
	public Component getComponent()
	{
		return botOverviewPanel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return botOverviewPanel;
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		visFrameThrottler.execute(() -> botOverviewPanel.update(frame));
	}
}
