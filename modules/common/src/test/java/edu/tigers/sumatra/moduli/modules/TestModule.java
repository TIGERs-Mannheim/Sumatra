/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.moduli.modules;

import edu.tigers.sumatra.moduli.AModule;


public abstract class TestModule extends AModule
{
	public abstract boolean isConstructed();
	
	
	public abstract boolean isInitialized();
	
	
	public abstract boolean isStarted();
	
	
	public abstract boolean isStopped();
	
	
	public abstract boolean isDeinitialized();
}
