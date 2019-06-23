/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 13, 2012
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

/**
 * This enum defines if there was possible a goal.
 * It is calculated via the position of the ball. If the position is behind or on the goal line it returns WE or THEY.
 * Otherwise, NO_ONE
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public enum EPossibleGoal
{
	/** */
	WE,
	/** */
	THEY,
	/** */
	NO_ONE
}
