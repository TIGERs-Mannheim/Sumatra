/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.moduli.modules;

public class UnusedConcreteTestModule extends TestModule
{
	private boolean isConstructed;
	private boolean isInitialized = false;
	private boolean isStarted = false;
	private boolean isStopped = false;
	private boolean isDeinitialized = false;


	public UnusedConcreteTestModule()
	{
		this.isConstructed = true;
	}
	
	
	@Override
	public void initModule()
	{
		isInitialized = true;
	}
	
	
	@Override
	public void startModule()
	{
		isStarted = true;
	}
	
	
	@Override
	public void stopModule()
	{
		isStopped = true;
	}
	
	
	@Override
	public void deinitModule()
	{
		isDeinitialized = true;
	}
	
	
	@Override
	public boolean isConstructed()
	{
		return isConstructed;
	}
	
	
	@Override
	public boolean isInitialized()
	{
		return isInitialized;
	}
	
	
	@Override
	public boolean isStarted()
	{
		return isStarted;
	}
	
	
	@Override
	public boolean isStopped()
	{
		return isStopped;
	}
	
	
	@Override
	public boolean isDeinitialized()
	{
		return isDeinitialized;
	}
}
