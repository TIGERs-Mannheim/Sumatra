/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

interface MovingOffsetFunction
{
	/**
	 * @param tHorizon            time horizon in seconds
	 * @param tAdditionalReaction additional reaction time in seconds
	 * @return moving offsets
	 */
	MovingOffsets forwardBackwardOffset(double tHorizon, double tAdditionalReaction);
}
