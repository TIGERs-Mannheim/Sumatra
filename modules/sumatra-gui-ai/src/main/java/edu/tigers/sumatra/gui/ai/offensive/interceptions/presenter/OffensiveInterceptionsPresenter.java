/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.offensive.interceptions.presenter;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.gui.ai.offensive.interceptions.view.OffensiveInterceptionsPanel;
import edu.tigers.sumatra.gui.ai.offensive.interceptions.view.TeamOffensiveInterceptionsPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;

import java.util.Map;


/**
 * OffensiveInterceptions Presenter
 */
public class OffensiveInterceptionsPresenter implements ISumatraViewPresenter, IVisualizationFrameObserver
{
	@Getter
	private final OffensiveInterceptionsPanel viewPanel = new OffensiveInterceptionsPanel();


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
		TeamOffensiveInterceptionsPanel interceptionsPanel;
		if (frame.getTeamColor() == ETeamColor.BLUE)
		{
			interceptionsPanel = viewPanel.getBluePanel();
		} else
		{
			interceptionsPanel = viewPanel.getYellowPanel();
		}

		// fill interceptionsPanel here with data
		Map<BotID, BallInterceptionInformation> info = frame.getBallInterceptionInformationMap();
		interceptionsPanel.fillComboBox(info.keySet());
		interceptionsPanel.setBallInterceptionsInformation(info);
	}
}
