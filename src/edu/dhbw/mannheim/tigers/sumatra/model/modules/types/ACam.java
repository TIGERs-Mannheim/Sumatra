/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.ICamFrameObserver;


/**
 * This is the base class for camera-modules which are capable of receiving data and convert them
 * 
 * @author Gero
 */
public abstract class ACam extends AModule
{
	/** */
	public static final String					MODULE_TYPE	= "ACam";
	/** */
	public static final String					MODULE_ID	= "cam";
	
	private final List<ICamFrameObserver>	observers	= new CopyOnWriteArrayList<ICamFrameObserver>();
	
	
	protected ACam(final SubnodeConfiguration subnodeConfiguration)
	{
	}
	
	
	@Override
	public void deinitModule()
	{
		removeAllObservers();
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ICamFrameObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ICamFrameObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected void notifyNewCameraFrame(final CamDetectionFrame frame)
	{
		for (ICamFrameObserver observer : observers)
		{
			observer.onNewCameraFrame(frame);
		}
	}
	
	
	protected void notifyNewCameraCalibration(final CamGeometry geometry)
	{
		for (ICamFrameObserver observer : observers)
		{
			observer.onNewCameraGeometry(geometry);
		}
	}
	
	
	protected void removeAllObservers()
	{
		observers.clear();
	}
}
