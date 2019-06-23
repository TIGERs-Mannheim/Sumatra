/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice;

/**
 * 
 * @author Manuel
 * 
 */
public interface IPollingControll
{
	/**
	 * @return
	 * 
	 */
	boolean startPolling();
	
	
	/**
	 *
	 */
	void stopPolling();
	
	
	/**
	 * 
	 * @return
	 */
	boolean isPolling();
	
}