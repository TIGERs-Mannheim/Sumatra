/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.offensive;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
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
	@SuppressWarnings("unused")
	private static final Logger				log	= Logger.getLogger(OffensiveStrategyPresenter.class.getName());
	private final OffensiveStrategyPanel	offensiveStrategyPanel;
														
														
	/**
	  * 
	  */
	public OffensiveStrategyPresenter()
	{
		offensiveStrategyPanel = new OffensiveStrategyPanel();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				try
				{
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agentYellow.addVisObserver(this);
					Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agentBlue.addVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module");
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				try
				{
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agentYellow.removeVisObserver(this);
					Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agentBlue.removeVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module");
				}
				break;
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
			
			strategyPanel.setMinNumberOfBots(offensiveStrategy.getMinNumberOfBots());
			strategyPanel.setMaxNumberOfBots(offensiveStrategy.getMaxNumberOfBots());
			strategyPanel.setDesiredBots(offensiveStrategy.getDesiredBots());
			strategyPanel.setPlayConfiguration(offensiveStrategy.getCurrentOffensivePlayConfiguration());
			strategyPanel.setUnassignedStrategies(offensiveStrategy.getUnassignedStrategies());
			strategyPanel.setSpecialMoveCommands(offensiveStrategy.getSpecialMoveCommands());
			strategyPanel.setOffensiveActions(frame.getOffensiveActions());
		}
	}
}
