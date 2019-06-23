/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.statistics;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.StatisticsPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * Game Statistics Presenter
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class StatisticsPresenter implements ISumatraViewPresenter, IAIObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(StatisticsPresenter.class.getName());
	private final StatisticsPanel	statisticsPanel;
	
	private AAgent						aiAgentBlue;
	private AAgent						aiAgentYellow;
	
	/** Used to limit updates */
	private int							statShowCounter	= 0;
	/** Each x-th frame will be passed to Panel, others will be ignored */
	private int							statShowTreshold	= 60;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public StatisticsPresenter()
	{
		statisticsPanel = new StatisticsPanel();
		
		ModuliStateAdapter.getInstance().addObserver(this);
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
					aiAgentBlue.addObserver(this);
					aiAgentYellow.addObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not find ai agents.", err);
				}
				break;
			case RESOLVED:
				if (aiAgentBlue != null)
				{
					aiAgentBlue.removeObserver(this);
					aiAgentBlue = null;
				}
				if (aiAgentYellow != null)
				{
					aiAgentYellow.removeObserver(this);
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
		return statisticsPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return statisticsPanel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	
	@Override
	public void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		statShowCounter++;
		if ((statShowCounter % statShowTreshold) == 0)
		{
			statisticsPanel.onNewAIInfoFrame(lastAIInfoframe);
			statShowCounter = 0;
		}
	}
	
	
	@Override
	public void onAIException(final Exception ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
