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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.SysoutBot;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;


/**
 * Presenter for a sysout bot.
 * 
 * @author AndreR
 * 
 */
public class SysoutBotPresenter extends ABotPresenter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private SysoutBot	bot	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public SysoutBotPresenter(ABot bot)
	{
		node = new BotCenterTreeNode(bot.getName(), ETreeIconType.BOT, new JPanel());
		
		this.bot = (SysoutBot) bot;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ABot getBot()
	{
		return bot;
	}
	

	@Override
	public JPanel getSummaryPanel()
	{
		return new JPanel();
	}
	

	@Override
	public JPanel getFastChgPanel()
	{
		return new JPanel();
	}
}
