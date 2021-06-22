/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.view.OffensiveInterceptionsPanel;
import edu.tigers.sumatra.offensive.view.TeamOffensiveInterceptionsPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;

import java.awt.Component;
import java.util.Map;


/**
 * OffensiveInterceptions Presenter
 *
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveInterceptionsPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{

	private final OffensiveInterceptionsPanel offensiveInterceptionsPanel = new OffensiveInterceptionsPanel();


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		if (state == ModulesState.ACTIVE)
		{
			Agent agent = SumatraModel.getInstance().getModule(Agent.class);
			agent.addVisObserver(this);
		} else if (state == ModulesState.RESOLVED)
		{
			Agent agent = SumatraModel.getInstance().getModule(Agent.class);
			agent.removeVisObserver(this);
		}
	}


	@Override
	public Component getComponent()
	{
		return offensiveInterceptionsPanel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return offensiveInterceptionsPanel;
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		TeamOffensiveInterceptionsPanel interceptionsPanel;
		if (frame.getTeamColor() == ETeamColor.BLUE)
		{
			interceptionsPanel = offensiveInterceptionsPanel.getBlueStrategyPanel();
		} else
		{
			interceptionsPanel = offensiveInterceptionsPanel.getYellowStrategyPanel();
		}

		// fill interceptionsPanel here with data
		Map<BotID, BallInterceptionInformation> info = frame.getBallInterceptionInformationMap();
		interceptionsPanel.fillComboBox(info.keySet());
		interceptionsPanel.setBallInterceptionsInformation(info);
	}
}
