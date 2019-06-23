/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statistics;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.statistics.view.StatisticsPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Game Statistics Presenter
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class StatisticsPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver, IAIObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(StatisticsPresenter.class.getName());
	
	private final ETeamColor		teamColor;
	
	private final StatisticsPanel	statisticsPanel;
	
	/** Used to limit updates */
	private int							statShowCounter	= 0;
	/** Each x-th frame will be passed to Panel, others will be ignored */
	private final int					statShowTreshold	= 60;
	
	
	/**
	 * @param teamColor
	 */
	public StatisticsPresenter(final ETeamColor teamColor)
	{
		this.teamColor = teamColor;
		
		statisticsPanel = new StatisticsPanel(teamColor);
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
		return statisticsPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return statisticsPanel;
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		if (frame.getTeamColor() != teamColor)
		{
			return;
		}
		statShowCounter++;
		if ((statShowCounter % statShowTreshold) == 0)
		{
			statisticsPanel.onNewVisualizationFrame(frame);
			statShowCounter = 0;
		}
	}
}
