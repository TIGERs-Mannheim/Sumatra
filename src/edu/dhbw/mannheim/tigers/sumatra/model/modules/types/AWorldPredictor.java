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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;


/**
 * This is the base class for all prediction-implementations, providing basic connections to the predecessor/successor
 * in data-flow and an observable to spread messages.
 * 
 * @author Gero
 * 
 */
public abstract class AWorldPredictor extends AModule implements ICamDetnFrameConsumer, IWorldFrameProducer,
		IBotManagerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String								MODULE_TYPE	= "AWorldPredictor";
	/** */
	public static final String								MODULE_ID	= "worldpredictor";
	
	protected List<IWorldFrameConsumer>					consumers	= new CopyOnWriteArrayList<IWorldFrameConsumer>();
	protected final List<IWorldPredictorObserver>	observers	= new CopyOnWriteArrayList<IWorldPredictorObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void addWorldFrameConsumer(IWorldFrameConsumer consumer)
	{
		consumers.add(consumer);
	}
	
	
	@Override
	public void removeWorldFrameConsumer(IWorldFrameConsumer consumer)
	{
		consumers.remove(consumer);
	}
	
	
	/**
	 * WOrldframeObservers
	 * 
	 * @param observer
	 */
	public void addObserver(IWorldPredictorObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IWorldPredictorObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
}
