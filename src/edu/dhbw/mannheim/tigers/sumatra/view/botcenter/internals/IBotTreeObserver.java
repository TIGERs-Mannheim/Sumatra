/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

/**
 * BotTree observer interface.
 * 
 * @author AndreR
 * 
 */
public interface IBotTreeObserver
{
	/**
	 * 
	 * @param data
	 */
	void onItemSelected(BotCenterTreeNode data);
	
	
	/**
	 * 
	 * @param node
	 */
	void onNodeRightClicked(BotCenterTreeNode node);
	
	
	/**
	 *
	 */
	void onAddBot();
	
	
	/**
	 * 
	 * @param node
	 */
	void onRemoveBot(BotCenterTreeNode node);
}
