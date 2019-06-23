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

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;


/**
 *
 */
public class WPCamBot extends AWPCamObject
{
	/** */
	public double	orientation;
	/** */
	public double	accuracy;
	
	
	/**
	 * 
	 * @param robot
	 */
	public WPCamBot(CamRobot robot)
	{
		x = robot.pos.x() * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		y = robot.pos.y() * WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		orientation = robot.orientation;
		id = robot.robotID;
		accuracy = robot.confidence;
	}
}
