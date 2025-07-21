/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.offensive.statistics.presenter;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.gui.ai.offensive.statistics.view.OffensiveStatisticsPanel;
import edu.tigers.sumatra.gui.ai.offensive.statistics.view.TeamOffensiveStatisticsPanel;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;

/**
 * OffensiveStrategy Presenter
 */
@Log4j2
public class OffensiveStatisticsPresenter implements ISumatraViewPresenter, IVisualizationFrameObserver
{
	@Getter
	private final OffensiveStatisticsPanel viewPanel = new OffensiveStatisticsPanel();


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
		TeamOffensiveStatisticsPanel statisticsPanel;
		if (frame.getTeamColor() == ETeamColor.BLUE)
		{
			statisticsPanel = viewPanel.getBluePanel();
		} else
		{
			statisticsPanel = viewPanel.getYellowPanel();
		}

		SwingUtilities.invokeLater(() -> statisticsPanel.updatePassStats(frame.getPassStats()));
	}
}