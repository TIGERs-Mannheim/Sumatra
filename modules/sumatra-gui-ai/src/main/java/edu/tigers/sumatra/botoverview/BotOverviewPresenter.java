/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview;

import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.botoverview.view.BotOverviewPanel;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.UiThrottler;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;


/**
 * Bot Overview Presenter
 */
public class BotOverviewPresenter implements ISumatraViewPresenter, IVisualizationFrameObserver
{
	@Getter
	private final BotOverviewPanel viewPanel = new BotOverviewPanel();
	private final Map<EAiTeam, UiThrottler> visFrameThrottler = new EnumMap<>(EAiTeam.class);


	public BotOverviewPresenter()
	{
		visFrameThrottler.put(EAiTeam.YELLOW, new UiThrottler(300));
		visFrameThrottler.put(EAiTeam.BLUE, new UiThrottler(300));
		visFrameThrottler.values().forEach(UiThrottler::start);
	}


	@Override
	public void onStartModuli()
	{
		SumatraModel.getInstance().getModule(Agent.class).addVisObserver(this);
	}


	@Override
	public void onStopModuli()
	{
		SumatraModel.getInstance().getModule(Agent.class).removeVisObserver(this);
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		visFrameThrottler.get(frame.getAiTeam()).execute(() -> viewPanel.update(frame));
	}
}
