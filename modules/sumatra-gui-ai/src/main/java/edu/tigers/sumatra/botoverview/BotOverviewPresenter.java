/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview;

import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.botoverview.view.BotOverviewPanel;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.UiThrottler;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;

import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;


/**
 * Bot Overview Presenter
 */
public class BotOverviewPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{
	private final BotOverviewPanel botOverviewPanel = new BotOverviewPanel();
	private final Map<EAiTeam, UiThrottler> visFrameThrottler = new EnumMap<>(EAiTeam.class);


	public BotOverviewPresenter()
	{
		visFrameThrottler.put(EAiTeam.YELLOW, new UiThrottler(300));
		visFrameThrottler.put(EAiTeam.BLUE, new UiThrottler(300));
		visFrameThrottler.values().forEach(UiThrottler::start);
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
		visFrameThrottler.get(frame.getAiTeam()).execute(() -> botOverviewPanel.update(frame));
	}
}
