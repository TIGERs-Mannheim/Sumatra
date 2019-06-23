/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

/**
 * Observe merged cam frames
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IMergedCamFrameObserver
{
	/**
	 * @param frame
	 */
	void onNewCameraFrame(final MergedCamDetectionFrame frame);
}
