/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

/**
 * Network panel observer interface.
 * 
 * @author AndreR
 * 
 */
public interface INetworkPanelObserver
{
	public void onStartPing(int numPings);
	public void onStopPing();
}
