/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.view.OffensiveInterceptionsPanel;
import edu.tigers.sumatra.offensive.view.TeamOffensiveInterceptionsPanel;
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
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.addVisObserver(this));
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();
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
