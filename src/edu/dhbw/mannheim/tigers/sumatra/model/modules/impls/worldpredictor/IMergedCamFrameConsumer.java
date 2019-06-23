/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.05.2014
 * Author(s): Kai
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;



/**
 * This Interface is for the asynchronous communication from the {@link SyncedCamFrameBufferV2} to an
 * {@link IMergedCamFrameConsumer} object.
 * 
 * @author KaiE
 */
public interface IMergedCamFrameConsumer
{
	/**
	 * called consumer Method
	 * 
	 * @param frame
	 */
	void notifyNewSyncedCamFrame(MergedCamDetectionFrame frame);
	
}
