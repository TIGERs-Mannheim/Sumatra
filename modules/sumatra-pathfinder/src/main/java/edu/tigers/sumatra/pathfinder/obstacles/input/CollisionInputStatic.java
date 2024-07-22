/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles.input;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;


@Value
public class CollisionInputStatic implements CollisionInput
{
	IVector2 robotPos;
	IVector2 robotVel;
	IVector2 robotAcc;
	double timeOffset;
}
