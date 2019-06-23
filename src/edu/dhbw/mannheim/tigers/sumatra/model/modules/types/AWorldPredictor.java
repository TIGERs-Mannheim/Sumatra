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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.moduli.AModule;


/**
 * This is the base class for all prediction-implementations, providing basic connections to the predecessor/successor
 * in data-flow and an observable to spread messages.
 * 
 * @author Gero
 * 
 */
public abstract class AWorldPredictor extends AModule implements ICamDetnFrameConsumer, IWorldFrameProducer, IBotManagerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public static final String								MODULE_TYPE				= "AWorldPredictor";
	public static final String								MODULE_ID				= "worldpredictor";
	
	protected IWorldFrameConsumer							consumer;
//	protected static final int								SIGNAL_COUNT			= 1;
//	protected CountDownLatch								startSignal;
	protected final List<IWorldPredictorObserver>	functionalObservers	= new ArrayList<IWorldPredictorObserver>();
	protected final List<IWorldPredictorObserver>	observers				= new ArrayList<IWorldPredictorObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setWorldFrameConsumer(IWorldFrameConsumer consumer)
	{
		this.consumer = consumer;
//		startSignal.countDown();
	}
	

//	protected void resetCountDownLatch()
//	{
//		startSignal = new CountDownLatch(SIGNAL_COUNT);
//	}
	

	/**
	 * Register here if your purpose is absolutely critical for things to work out, otherwise you should use
	 * {@link #addObserver(IWorldPredictorObserver)}
	 * 
	 * @param observer
	 */
	public void addFunctionalObserver(IWorldPredictorObserver observer)
	{
		synchronized (functionalObservers)
		{
			functionalObservers.add(observer);
		}
	}
	

	public void removeFunctionalObserver(IWorldPredictorObserver observer)
	{
		synchronized (functionalObservers)
		{
			functionalObservers.remove(observer);
		}
	}
	

	/**
	 * This observer is meant for secondary purposes, like GUI e.g. (alt.:
	 * {@link #addFunctionalObserver(IWorldPredictorObserver)}
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
	

	public void removeObserver(IWorldPredictorObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
}
