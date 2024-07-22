/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles.input;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;


public interface CollisionInput
{

	IVector2 getRobotPos();

	IVector2 getRobotVel();

	IVector2 getRobotAcc();

	double getTimeOffset();

	default boolean accelerating()
	{
		return getRobotVel().angleToAbs(getRobotAcc()).map(angle -> angle < AngleMath.DEG_090_IN_RAD).orElse(false);
	}

	default double getExtraMargin()
	{
		return DynamicMargin.getExtraMargin(getRobotVel().getLength2());
	}
}
