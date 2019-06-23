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

import java.util.concurrent.CountDownLatch;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.CamDetnObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.CamGeomObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObserver;


/**
 * This is the base class for camera-modules which are capable of receiving data and convert them
 * 
 * @author Gero
 * 
 */
public abstract class ACam extends AModule implements ICamDetnFrameProducer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	// private static final Logger log = Logger.getLogger(ACam.class.getName());
	
	/** */
	public static final String				MODULE_TYPE				= "ACam";
	/** */
	public static final String				MODULE_ID				= "cam";
	
	
	protected final ICamDetnObservable	detectionObservable	= new CamDetnObservable(null);
	protected final ICamGeomObservable	geometryObservable	= new CamGeomObservable(null);
	
	protected ICamDetnFrameConsumer		consumer;
	protected static final int				SIGNAL_COUNT			= 1;
	protected CountDownLatch				startSignal;
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setCamFrameConsumer(ICamDetnFrameConsumer consumer)
	{
		this.consumer = consumer;
		startSignal.countDown();
	}
	
	
	protected void resetCountDownLatch()
	{
		startSignal = new CountDownLatch(SIGNAL_COUNT);
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param o
	 */
	public void addCamDetectionObserver(ICamDetnObserver o)
	{
		detectionObservable.addObservers(o);
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeCamDetectionObserver(ICamDetnObserver o)
	{
		detectionObservable.removeObserver(o);
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void addCamGeometryObserver(ICamGeomObserver o)
	{
		geometryObservable.addObservers(o);
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeCamGeometryObserver(ICamGeomObserver o)
	{
		geometryObservable.removeObserver(o);
	}
}
