/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ITimerObserver;
import edu.moduli.AModule;


/**
 * This is the base class for all timer-module implementations (there should be only one, though...). It watches the
 * packets on their way from {@link ACam} to {@link ABotManager} to provide exact measurements and profound timing
 * advices
 * 
 * @author Gero
 * 
 */
public abstract class ATimer extends AModule implements ITimer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public static final String					MODULE_TYPE	= "ATimer";
	public static final String					MODULE_ID	= "timer";
	

	protected final List<ITimerObserver>	observers	= new ArrayList<ITimerObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ITimerObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(ITimerObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
}
