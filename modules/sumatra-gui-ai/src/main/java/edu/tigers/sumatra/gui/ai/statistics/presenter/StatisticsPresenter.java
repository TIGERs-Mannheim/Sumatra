/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.statistics.presenter;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.gui.ai.statistics.view.StatisticsPanel;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;


/**
 * Game Statistics Presenter
 */
@Log4j2
public class StatisticsPresenter implements ISumatraViewPresenter, IVisualizationFrameObserver, IAIObserver
{
	/**
	 * Each x-th frame will be passed to Panel, others will be ignored
	 */
	private static final int STAT_SHOW_THRESHOLD = 10;

	@Getter
	private final StatisticsPanel viewPanel = new StatisticsPanel();

	/**
	 * Used to limit updates
	 */
	private int statShowCounter = 0;


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
		if (frame.getTeamColor() != viewPanel.getSelectedTeamColor())
		{
			return;
		}
		statShowCounter++;
		if ((statShowCounter % STAT_SHOW_THRESHOLD) == 0)
		{
			SwingUtilities.invokeLater(() -> viewPanel.onNewVisualizationFrame(frame));
			statShowCounter = 0;
		}
	}


	@Override
	public void onClearVisualizationFrame(final EAiTeam teamColor)
	{
		SwingUtilities.invokeLater(viewPanel::reset);
	}
}
