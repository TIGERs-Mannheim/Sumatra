/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.statistics.possiblegoal;

/**
 * This enum defines if there was possible a goal.
 * It is calculated via the position of the ball. If the position is behind or on the goal line it returns WE or THEY.
 * Otherwise, NO_ONE
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public enum EPossibleGoal
{
	/** We scored a goal */
	WE,
	/** They scored a goal */
	THEY,
	/** No one scored a goal */
	NO_ONE
}
