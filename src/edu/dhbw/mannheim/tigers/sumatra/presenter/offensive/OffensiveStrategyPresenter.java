/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.offensive;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.view.offense.OffensiveStrategyPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.offense.internals.TeamOffensiveStrategyPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * OffensiveStrategy Presenter
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStrategyPresenter implements ISumatraViewPresenter
{
	@SuppressWarnings("unused")
	private static final Logger				log	= Logger.getLogger(OffensiveStrategyPresenter.class.getName());
	private final OffensiveStrategyPanel	offensiveStrategyPanel;
	private AAgent									aiAgentBlue;
	private AAgent									aiAgentYellow;
	
	
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
					aiAgentBlue = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					aiAgentYellow = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					// get AIInfoFrames
					aiAgentBlue.addVisObserver(this);
					aiAgentYellow.addVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not find ai agents.", err);
				}
				break;
			case RESOLVED:
				if (aiAgentBlue != null)
				{
					aiAgentBlue.removeVisObserver(this);
					aiAgentBlue = null;
				}
				if (aiAgentYellow != null)
				{
					aiAgentYellow.removeVisObserver(this);
					aiAgentYellow = null;
				}
				break;
			case NOT_LOADED:
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
	public void onEmergencyStop()
	{
	}
	
	
	@Override
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		if ((lastAIInfoframe.getTacticalField().getOffensiveStrategy() != null)
				&& (lastAIInfoframe.getTacticalField().getOffensiveActions() != null))
		{
			OffensiveStrategy offensiveStrategy = lastAIInfoframe.getTacticalField().getOffensiveStrategy();
			TeamOffensiveStrategyPanel strategyPanel;
			if (lastAIInfoframe.getTeamColor() == ETeamColor.BLUE)
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
			strategyPanel.setOffensiveActions(lastAIInfoframe.getTacticalField().getOffensiveActions());
		}
	}
}
