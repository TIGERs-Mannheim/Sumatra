/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICamFrameObserver
{
	/**
	 * @param frame
	 * @param timeSync
	 */
	default void onNewCameraFrame(final SSL_DetectionFrame frame, final TimeSync timeSync)
	{
	}
	
	
	/**
	 * @param geometry
	 */
	default void onNewCameraGeometry(final CamGeometry geometry)
	{
	}
	
	
	/**
	 * 
	 */
	default void onClearCamFrame()
	{
	}
}
