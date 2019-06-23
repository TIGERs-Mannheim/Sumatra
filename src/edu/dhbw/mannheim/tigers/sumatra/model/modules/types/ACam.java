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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.CamDetnObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.CamGeomObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObserver;
import edu.moduli.AModule;


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
	public static final String				MODULE_TYPE				= "ACam";
	public static final String				MODULE_ID				= "cam";
	

	protected final Logger					log						= Logger.getLogger(getClass());
	
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
	public void addCamDetectionObserver(ICamDetnObserver o)
	{
		detectionObservable.addObservers(o);
	}
	

	public void removeCamDetectionObserver(ICamDetnObserver o)
	{
		detectionObservable.removeObserver(o);
	}
	

	public void addCamGeometryObserver(ICamGeomObserver o)
	{
		geometryObservable.addObservers(o);
	}
	

	public void removeCamGeometryObserver(ICamGeomObserver o)
	{
		geometryObservable.removeObserver(o);
	}
}
