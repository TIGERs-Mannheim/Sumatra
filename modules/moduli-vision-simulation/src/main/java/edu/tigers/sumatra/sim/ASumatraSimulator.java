/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.vision.AVisionFilter;

import java.util.ArrayList;
import java.util.Collection;


/**
 * The abstract simulator is used by both, the local and the remote simulator
 */
public abstract class ASumatraSimulator extends AVisionFilter implements Runnable
{
	protected final Collection<ISimulatorActionCallback> simulatorActionCallbacks = new ArrayList<>();
	protected final Collection<ISimulatorObserver> simulatorObservers = new ArrayList<>();


	public void addSimulatorActionCallback(ISimulatorActionCallback cb)
	{
		simulatorActionCallbacks.add(cb);
	}


	public void removeSimulatorActionCallback(ISimulatorActionCallback cb)
	{
		simulatorActionCallbacks.remove(cb);
	}


	public void addSimulatorObserver(ISimulatorObserver cb)
	{
		simulatorObservers.add(cb);
	}


	public void removeSimulatorObserver(ISimulatorObserver cb)
	{
		simulatorObservers.remove(cb);
	}
}
