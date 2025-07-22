/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.data.CamObjectFilterParams;
import edu.tigers.sumatra.cam.proto.SslVisionDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.cam.proto.SslVisionWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.moduli.AModule;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * This is the base class for camera-modules which are capable of receiving data and convert them
 */
public abstract class ACam extends AModule
{
	private final List<ICamFrameObserver> observers = new CopyOnWriteArrayList<>();

	private final CamDetectionConverter camDetectionConverter = new CamDetectionConverter();
	private final CamObjectFilter camObjectFilter = new CamObjectFilter();


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


	public Optional<CamObjectFilterParams> getParams()
	{
		return camObjectFilter.getParams();
	}


	protected void notifyNewCameraFrame(final SSL_DetectionFrame frame)
	{
		CamDetectionFrame camDetectionFrame = camDetectionConverter.convertDetectionFrame(frame);
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
