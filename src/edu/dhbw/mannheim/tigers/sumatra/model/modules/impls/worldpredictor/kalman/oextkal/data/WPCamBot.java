/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2011
 * Author(s): copy
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;


/**
 *
 */
public class WPCamBot extends AWPCamObject
{
	/** */
	public double	orientation;
	
	
	/**
	 * @param robot
	 */
	public WPCamBot(final CamRobot robot)
	{
		x = robot.getPos().x() * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		y = robot.getPos().y() * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		orientation = robot.getOrientation();
		id = robot.getRobotID();
	}
}
