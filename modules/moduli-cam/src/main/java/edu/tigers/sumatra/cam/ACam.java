/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;


/**
 * This is the base class for camera-modules which are capable of receiving data and convert them
 * 
 * @author Gero
 */
public abstract class ACam extends AModule
{
	/** */
	public static final String					MODULE_TYPE					= "ACam";
	/** */
	public static final String					MODULE_ID					= "cam";
	
	private final List<ICamFrameObserver>	observers					= new CopyOnWriteArrayList<>();
	
	private final CamDetectionConverter		camDetectionConverter	= new CamDetectionConverter();
	private final CamObjectFilter				camObjectFilter			= new CamObjectFilter();
	
	
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
	
	
	protected void notifyNewCameraFrame(final SSL_DetectionFrame frame, final TimeSync timeSync)
	{
		CamDetectionFrame camDetectionFrame = camDetectionConverter.convertDetectionFrame(frame, timeSync);
		camDetectionFrame = camObjectFilter.filter(camDetectionFrame);
		for (ICamFrameObserver observer : observers)
		{
			observer.onNewCamDetectionFrame(camDetectionFrame);
		}
	}
	
	
	protected void notifyNewCameraCalibration(final CamGeometry geometry)
	{
		for (ICamFrameObserver observer : observers)
		{
			observer.onNewCameraGeometry(geometry);
		}
	}
	
	
	protected void notifyNewVisionPacket(final SSL_WrapperPacket packet)
	{
		for (ICamFrameObserver observer : observers)
		{
			observer.onNewVisionPacket(packet);
		}
	}
	
	
	protected void notifyVisionLost()
	{
		for (ICamFrameObserver observer : observers)
		{
			observer.onClearCamFrame();
		}
	}
	
	
	protected void removeAllObservers()
	{
		observers.clear();
	}
}
