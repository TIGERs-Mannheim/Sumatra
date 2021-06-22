/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslWrapper.SSL_WrapperPacket;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICamFrameObserver
{
	/**
	 * @param camDetectionFrame a detection frame from a single camera
	 */
	default void onNewCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
	}


	/**
	 * @param geometry geometry information received by SSL vision
	 */
	default void onNewCameraGeometry(final CamGeometry geometry)
	{
	}


	/**
	 * This is called when the vision connection was lost
	 */
	default void onClearCamFrame()
	{
	}


	/**
	 * @param packet Raw SSL Vision packet, unprocessed.
	 */
	default void onNewVisionPacket(final SSL_WrapperPacket packet)
	{
	}
}
