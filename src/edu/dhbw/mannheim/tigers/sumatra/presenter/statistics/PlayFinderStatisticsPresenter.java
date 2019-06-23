/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 20, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.statistics;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.MatchStatistics;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.PlayFinderStatisticsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.IStatisticsObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.ITimeSliderObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.history.HistoryTableModel;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.summary.SummaryTableModel;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * presenter for the statistics view
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class PlayFinderStatisticsPresenter implements IStatisticsObserver, IModuliStateObserver, ITimeSliderObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger			log	= Logger.getLogger(PlayFinderStatisticsPresenter.class.getName());
	
	private PlayFinderStatisticsPanel	panel	= null;
	
	private HistoryTableModel				historyTableModel;
	private SummaryTableModel				summaryTableModel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public PlayFinderStatisticsPresenter()
	{
		historyTableModel = new HistoryTableModel();
		summaryTableModel = new SummaryTableModel();
		
		panel = new PlayFinderStatisticsPanel(historyTableModel, summaryTableModel);
		panel.addITimeSliderObserver(this);
		
		try
		{
			onNewStatistics(historyTableModel.getMatchStats());
		} catch (IndexOutOfBoundsException e)
		{
			// fullStats.odb does not exist (Maybe Sumatra has never been in matchmode)
		}
		
		// --- register on moduli ---
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewStatistics(MatchStatistics matchStatistics)
	{
		historyTableModel.setMatchStats(matchStatistics);
		summaryTableModel.setMatchStats(matchStatistics);
		panel.setTimeSlider(historyTableModel.getFirstTime(), historyTableModel.getLastTime());
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		if (state == ModulesState.RESOLVED)
		{
			try
			{
				final SumatraModel model = SumatraModel.getInstance();
				
				Agent agent = (Agent) model.getModule(Agent.MODULE_ID);
				agent.removePlayStatisticsObserver(this);
				panel.enableFileLoad(true);
			} catch (final ModuleNotFoundException err)
			{
				log.error("agent not found!!!");
			}
		} else if (state == ModulesState.ACTIVE)
		{
			try
			{
				final SumatraModel model = SumatraModel.getInstance();
				
				Agent agent = (Agent) model.getModule(Agent.MODULE_ID);
				agent.addPlayStatisticsObserver(this);
				panel.enableFileLoad(false);
				try
				{
					onFileLoad("fullStats");
				} catch (IndexOutOfBoundsException e)
				{
					
				}
			} catch (final ModuleNotFoundException err)
			{
				log.error("agent not found!!!");
			}
		}
	}
	
	
	@Override
	public void onTimeSlide()
	{
		long startTime = panel.getTime();
		historyTableModel.setStartTime(startTime);
		summaryTableModel.setStartTime(startTime);
	}
	
	
	@Override
	public void onFileLoad(String filename)
	{
		MatchStatistics ms = new MatchStatistics(filename);
		onNewStatistics(ms);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public ISumatraView getView()
	{
		return panel;
	}
}
