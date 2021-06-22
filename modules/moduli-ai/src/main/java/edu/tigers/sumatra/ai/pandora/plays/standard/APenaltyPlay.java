/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;

import java.util.ArrayList;
import java.util.List;


/**
 * Base for penalty plays
 */
public abstract class APenaltyPlay extends APlay
{
	@Configurable(comment = "Distance to a-axis on line", defValue = "1000.0")
	private static double distanceToX = 1000;


	protected APenaltyPlay(final EPlay ePlay)
	{
		super(ePlay);
	}


	protected void updateMoveRoles(final double xSign, final double xLine)
	{
		double yOffset = 0;
		double ySign = 1;
		double yOffsetStep = 200;
		List<IVector2> destinations = new ArrayList<>();
		for (int i = 0; i < getRoles().size(); i++)
		{
			IVector2 destination = Vector2.fromXY(xLine, ySign * (distanceToX - yOffset));
			yOffset -= yOffsetStep;
			destinations.add(destination);

			ySign *= -1;
		}

		for (ARole role : getRoles())
		{
			if (!role.getClass().equals(MoveRole.class))
			{
				continue;
			}
			MoveRole moveRole = (MoveRole) role;
			IVector2 destination = destinations.stream().min(new VectorDistanceComparator(moveRole.getPos())).orElseThrow();
			destinations.remove(destination);
			for (int j = 0; j < 10; j++)
			{
				if (!AiMath.isShapeOccupiedByBots(Circle.createCircle(destination, (Geometry.getBotRadius() * 2) + 50),
						getWorldFrame().getOpponentBots(), role.getBotID()))
				{
					break;
				}
				destination = destination.addNew(Vector2.fromX(xSign * 100.0));
			}
			moveRole.updateDestination(destination);
			moveRole.updateTargetAngle(AngleMath.DEG_180_IN_RAD);
		}
	}
}
