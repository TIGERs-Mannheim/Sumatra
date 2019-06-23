/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

/**
 * Tiger bot main panel observer.
 * 
 * @author AndreR
 * 
 */
public interface ITigerBotMainPanelObserver
{
	/**
	 *
	 */
	void onConnectionChange();
	
	
	/**
	 *
 	 */
	void onSaveGeneral();
	
	
	/**
	 *
	 */
	void onSaveLogs();
	
	
	/**
	 * 
	 * @param filepath
	 * @param targetMain
	 */
	void onUpdateFirmware(String filepath, boolean targetMain);
}
