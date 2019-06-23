/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct;

/**
 * Ct bot panel observer.
 * 
 * @author AndreR
 * 
 */
public interface ICtBotPanelObserver
{
	public void onSaveGeneral();
	public void onSavePid();
	public void onConnectionChange();
	public void onCalibrate();
}
