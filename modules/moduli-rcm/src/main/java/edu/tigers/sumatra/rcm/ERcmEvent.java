/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 16, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.rcm;

/**
 * Possible events that can be send from controller to RCM
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ERcmEvent
{
	/**  */
	UNASSIGNED,
	/**  */
	NEXT_BOT,
	/**  */
	PREV_BOT,
	/**  */
	UNASSIGN_BOT,
	/**  */
	SPEED_MODE_TOGGLE,
	/**  */
	SPEED_MODE_ENABLE,
	/**  */
	SPEED_MODE_DISABLE,
	/**  */
	EMERGENCY_MODE,
	/**  */
	MATCH_MODE,
	/**  */
	RECORD_START_STOP,
	/**  */
	CHARGE_BOT,
	/**  */
	DISCHARGE_BOT,
	
	/**  */
	RECORD_VISION_START,
	/**  */
	RECORD_VISION_STOP
}
