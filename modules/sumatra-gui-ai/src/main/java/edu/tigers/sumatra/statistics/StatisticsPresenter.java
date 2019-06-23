/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.ids.EAiTeam;
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
	private static final Logger log = Logger.getLogger(StatisticsPresenter.class.getName());
	
	/** Each x-th frame will be passed to Panel, others will be ignored */
	private static final int STAT_SHOW_THRESHOLD = 10;
	
	private final StatisticsPanel statisticsPanel;
	
	/** Used to limit updates */
	private int statShowCounter = 0;
	
	
	/**
	 * Default
	 */
	public StatisticsPresenter()
	{
		statisticsPanel = new StatisticsPanel();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				try
				{
					Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
					agent.addVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module", err);
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				try
				{
					Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
					agent.removeVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module", err);
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
		if (frame.getTeamColor() != statisticsPanel.getSelectedTeamColor())
		{
			return;
		}
		statShowCounter++;
		if ((statShowCounter % STAT_SHOW_THRESHOLD) == 0)
		{
			statisticsPanel.onNewVisualizationFrame(frame);
			statShowCounter = 0;
		}
	}
	
	
	@Override
	public void onClearVisualizationFrame(final EAiTeam teamColor)
	{
		statisticsPanel.reset();
	}
}
