/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.botcenter;

/**
 * BotTree observer interface.
 * 
 * @author AndreR
 */
public interface IBotTreeObserver
{
	/**
	 * @param data
	 */
	void onItemSelected(BotCenterTreeNode data);
	
	
	/**
	 * @param node
	 */
	void onNodeRightClicked(BotCenterTreeNode node);
	
	
	/**
	 * Add bot.
	 */
	void onAddBot();
	
	
	/**
	 * @param node
	 */
	void onRemoveBot(BotCenterTreeNode node);
}
