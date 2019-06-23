/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
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
	public void onStart()
	{
		try
		{
			Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agentYellow.addVisObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
		}
	}
	
	
	@Override
	public void onStop()
	{
		try
		{
			Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agentYellow.removeVisObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
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
