/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2011
 * Author(s): copy
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman.data;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.wp.kalman.WPConfig;


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
