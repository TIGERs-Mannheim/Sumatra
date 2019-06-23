/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import javax.swing.JPanel;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;


/**
 * Presenter base for all bots. Every presenter is responsible for one bot.
 * 
 * @author AndreR
 * 
 */
public abstract class ABotPresenter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected BotCenterTreeNode	node			= null;
	
	private boolean					statsActive	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public ABotPresenter()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public BotCenterTreeNode getTreeNode()
	{
		return node;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public abstract ABot getBot();
	
	
	/**
	 * 
	 * @return
	 */
	public abstract JPanel getSummaryPanel();
	
	
	/**
	 * 
	 * @return
	 */
	public abstract JPanel getFastChgPanel();
	
	
	/**
	 * Do some preparation for deleting this presenter.
	 */
	public void delete()
	{
	}
	
	
	/**
	 * @return the statsActive
	 */
	public final boolean isStatsActive()
	{
		return statsActive;
	}
	
	
	/**
	 * @param statsActive the statsActive to set
	 */
	public final void setStatsActive(boolean statsActive)
	{
		this.statsActive = statsActive;
	}
}
