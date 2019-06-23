/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive;

import java.awt.Component;
import java.util.ArrayList;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.view.OffensiveStrategyPanel;
import edu.tigers.sumatra.offensive.view.TeamOffensiveStrategyPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * OffensiveStrategy Presenter
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStrategyPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{
	private final OffensiveStrategyPanel offensiveStrategyPanel;
	
	
	public OffensiveStrategyPresenter()
	{
		offensiveStrategyPanel = new OffensiveStrategyPanel();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		if (state == ModulesState.ACTIVE)
		{
			Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agent.addVisObserver(this);
		} else if (state == ModulesState.RESOLVED)
		{
			Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agent.removeVisObserver(this);
		}
	}
	
	
	@Override
	public Component getComponent()
	{
		return offensiveStrategyPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return offensiveStrategyPanel;
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		OffensiveStrategy offensiveStrategy = frame.getOffensiveStrategy();
		if ((offensiveStrategy != null)
				&& (frame.getOffensiveActions() != null))
		{
			TeamOffensiveStrategyPanel strategyPanel;
			if (frame.getTeamColor() == ETeamColor.BLUE)
			{
				strategyPanel = offensiveStrategyPanel.getBlueStrategyPanel();
			} else
			{
				strategyPanel = offensiveStrategyPanel.getYellowStrategyPanel();
			}
			
			strategyPanel.setDesiredBots(new ArrayList<>(offensiveStrategy.getDesiredBots()));
			strategyPanel.setPlayConfiguration(offensiveStrategy.getCurrentOffensivePlayConfiguration());
			strategyPanel.setSpecialMoveCommands(offensiveStrategy.getActivePassTarget().orElse(null));
			strategyPanel.setOffensiveActions(frame.getOffensiveActions());
		}
	}
}
