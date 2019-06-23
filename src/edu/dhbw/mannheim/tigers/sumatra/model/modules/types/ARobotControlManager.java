/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.moduli.AModule;


/**
 * The base class for implementations robot control functionalities for Gamepads etc.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public abstract class ARobotControlManager extends AModule implements IBotManagerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** */
	public static final String						MODULE_TYPE	= "ARobotControlManager";
	/** */
	public static final String						MODULE_ID	= "rcm";
	
	protected final List<IBotManagerObserver>	observers	= new ArrayList<IBotManagerObserver>();
	
	
	/**
	 * 
	 * @param o
	 */
	public void addObserver(IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
		
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeObserver(IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
		
	}
	
	
}
