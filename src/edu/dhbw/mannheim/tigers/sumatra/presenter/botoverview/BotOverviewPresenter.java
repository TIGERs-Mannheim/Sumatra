/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botoverview;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.view.botoverview.BotOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * Bot Overview Presenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotOverviewPresenter implements ISumatraViewPresenter, IAIObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger		log	= Logger.getLogger(BotOverviewPresenter.class.getName());
	private final BotOverviewPanel	botOverviewPanel;
	
	private AAgent							aiAgentBlue;
	private AAgent							aiAgentYellow;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public BotOverviewPresenter()
	{
		botOverviewPanel = new BotOverviewPanel();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
		return botOverviewPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return botOverviewPanel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	
	@Override
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		botOverviewPanel.onNewAIInfoFrame(lastAIInfoframe);
	}
	
	
	@Override
	public void onAIException(final Throwable ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
