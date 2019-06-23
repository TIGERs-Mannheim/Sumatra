/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 5, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.remote.impl;

import edu.tigers.autoreferee.remote.ICommandResult;


/**
 * @author "Lukas Magel"
 */
public class CommandResultImpl implements ICommandResult
{
	private boolean	completed	= false;
	private boolean	successful	= false;
	
	
	@Override
	public synchronized boolean isCompleted()
	{
		return completed;
	}
	
	
	@Override
	public synchronized boolean isSuccessful()
	{
		return successful;
	}
	
	
	@Override
	public synchronized boolean hasFailed()
	{
		return completed && !successful;
	}
	
	
	/**
	 * @param completed
	 * @param successful
	 */
	private synchronized void setResult(final boolean completed, final boolean successful)
	{
		this.completed = completed;
		this.successful = successful;
	}
	
	
	/**
	 * 
	 */
	public synchronized void setSuccessful()
	{
		setResult(true, true);
	}
	
	
	/**
	 * 
	 */
	public synchronized void setFailed()
	{
		setResult(true, false);
	}
	
}
