/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICamFrameObserver
{
	/**
	 * @param frame
	 */
	void onNewCameraFrame(CamDetectionFrame frame);
	
	
	/**
	 * @param geometry
	 */
	void onNewCameraGeometry(CamGeometry geometry);
}
