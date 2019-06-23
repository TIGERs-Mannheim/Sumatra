/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2011
 * Author(s): copy
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;


/**
 *
 */
public class WPCamBall extends AWPCamObject
{
	/** */
	public double	z;
	
	
	/**
	 * 
	 * @param ball
	 */
	public WPCamBall(CamBall ball)
	{
		id = 0;
		x = ball.getPos().x() * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		y = ball.getPos().y() * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		z = ball.getPos().z() * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
	}
}
