/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.offensive.strategy.presenter;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.gui.ai.offensive.strategy.view.OffensiveStrategyPanel;
import edu.tigers.sumatra.gui.ai.offensive.strategy.view.TeamOffensiveStrategyPanel;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;


/**
 * OffensiveStrategy Presenter
 */
public class OffensiveStrategyPresenter implements ISumatraViewPresenter, IVisualizationFrameObserver
{
	@Getter
	private final OffensiveStrategyPanel viewPanel = new OffensiveStrategyPanel();


	@Override
	public void onModuliStarted()
	{
		ISumatraViewPresenter.super.onModuliStarted();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.addVisObserver(this));
	}


	@Override
	public void onModuliStopped()
	{
		ISumatraViewPresenter.super.onModuliStopped();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.removeVisObserver(this));
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		OffensiveStrategy offensiveStrategy = frame.getOffensiveStrategy();
		if (offensiveStrategy != null)
		{
			TeamOffensiveStrategyPanel strategyPanel;
			if (frame.getTeamColor() == ETeamColor.BLUE)
			{
				strategyPanel = viewPanel.getBluePanel();
			} else
			{
				strategyPanel = viewPanel.getYellowPanel();
			}

			strategyPanel.setPlayConfiguration(offensiveStrategy.getCurrentOffensivePlayConfiguration());
			strategyPanel.setOffensiveActions(frame.getOffensiveActions());
		}
	}
}
