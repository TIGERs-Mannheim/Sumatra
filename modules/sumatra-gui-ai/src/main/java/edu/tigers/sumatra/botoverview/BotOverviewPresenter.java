/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.botoverview.view.BotOverviewPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Bot Overview Presenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotOverviewPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BotOverviewPresenter.class.getName());
	
	private final BotOverviewPanel botOverviewPanel;
	
	
	/**
	 * Default
	 */
	public BotOverviewPresenter()
	{
		botOverviewPanel = new BotOverviewPanel();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				try
				{
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
					agentYellow.addVisObserver(this);
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
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
					agentYellow.removeVisObserver(this);
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
		return botOverviewPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return botOverviewPanel;
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		botOverviewPanel.update(frame);
	}
}
