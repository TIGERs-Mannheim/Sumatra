/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2011
 * Author(s): copy
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;

public class WPCamBall extends AWPCamObject
{
	public double z; 
	
	public WPCamBall(CamBall ball)
	{
		id = 0;
		x = ball.pos.x * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		y = ball.pos.y * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		z = ball.pos.z * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
	}
}
