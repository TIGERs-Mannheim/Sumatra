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
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.IBotCenterPresenter;
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
	protected BotCenterTreeNode	node						= null;
	protected IBotCenterPresenter	botCenterPresenter	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public ABotPresenter()
	{
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotCenterTreeNode getTreeNode()
	{
		return node;
	}
	

	public void setBotCenterPresenter(IBotCenterPresenter i)
	{
		botCenterPresenter = i;
	}
	

	public abstract ABot getBot();
	

	public abstract JPanel getSummaryPanel();
	

	public abstract JPanel getFastChgPanel();
	

	/**
	 * Do some preparation for deleting this presenter.
	 */
	public void delete()
	{
	}
}
