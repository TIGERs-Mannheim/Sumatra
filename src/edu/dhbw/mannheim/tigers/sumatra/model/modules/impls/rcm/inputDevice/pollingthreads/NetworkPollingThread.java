/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.pollingthreads;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.IPollingControll;


/**
 * 
 * @author Manuel
 * 
 */
public class NetworkPollingThread implements IPollingControll
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean startPolling()
	{
		return true;
	}
	
	
	@Override
	public void stopPolling()
	{
	}
	
	
	@Override
	public boolean isPolling()
	{
		return false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
