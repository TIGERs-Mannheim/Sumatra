/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

/**
 * The type of movement control the bot should use.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ECtrlMoveType
{
	/** Send splines to bot */
	SPLINE,
	/** Send velocities along spline to bot */
	VEL,
	/** Send destination positions along spline to bot */
	POS,
	/** Send splines to bot, but send positions when near destination */
	SPLINE_POS,
}
