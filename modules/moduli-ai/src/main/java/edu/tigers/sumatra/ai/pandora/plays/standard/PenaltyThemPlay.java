/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Handle bots that would normally be Supporters and Offense
 */
public class PenaltyThemPlay extends APlay
{
	@Configurable(comment = "Distance to a-axis on line", defValue = "1000.0")
	private static double distanceToX = 1000;


	public PenaltyThemPlay()
	{
		super(EPlay.PENALTY_THEM);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		double sign = Math.signum(Geometry.getPenaltyMarkOur().subtractNew(Geometry.getGoalOur().getCenter()).x());
		double xLine = Geometry.getFieldLength() / 2 - Geometry.getPenaltyAreaDepth()
				- Geometry.getBotRadius() * 5;

		updateMoveRoles(sign, xLine);
	}


	private void updateMoveRoles(final double xSign, final double xLine)
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
			IVector2 destination = destinations.stream().min(new VectorDistanceComparator(moveRole.getPos()))
					.orElseThrow();
			destinations.remove(destination);
			for (int j = 0; j < 10; j++)
			{
				if (!isShapeOccupiedByBots(Circle.createCircle(destination, (Geometry.getBotRadius() * 2) + 50),
						getWorldFrame().getOpponentBots().values(), role.getBotID()))
				{
					break;
				}
				destination = destination.addNew(Vector2.fromX(xSign * 100.0));
			}
			moveRole.updateDestination(destination);
			moveRole.updateTargetAngle(AngleMath.DEG_180_IN_RAD);
		}
	}


	private boolean isShapeOccupiedByBots(I2DShape shape, Collection<ITrackedBot> bots, BotID ignoredBot)
	{
		return bots.stream()
				.filter(bot -> !bot.getBotId().equals(ignoredBot))
				.anyMatch(bot -> shape.isPointInShape(bot.getPos()));
	}
}
