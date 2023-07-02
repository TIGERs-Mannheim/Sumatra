/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles.input;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Value;


@Value
public class CollisionInputStatic implements CollisionInput
{
	IVector2 robotPos;
	IVector2 robotVel;
	IVector2 robotAcc;
	double timeOffset;


	public static CollisionInput ofStaticPos(IVector2 pos)
	{
		return new CollisionInputStatic(pos, Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR, 0.0);
	}
}
