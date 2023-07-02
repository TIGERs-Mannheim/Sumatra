/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.common;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;


/**
 * Helper class to find a destination that keeps a robot away from the ball during ball placement.
 */
@Log4j2
@RequiredArgsConstructor
public class KeepDistanceToBall
{
	private static final int CIRCLE_STEPS = 32;
	private static final double DISTANCE_STEP_SIZE = 30;

	private final PointChecker pointChecker;

	@Setter
	private List<IDrawableShape> debugShapes;


	public boolean isOk(AthenaAiFrame aiFrame, IVector2 destination, BotID botID)
	{
		return pointChecker.allMatch(aiFrame.getBaseAiFrame(), destination, botID);
	}


	public IVector2 findNextFreeDest(AthenaAiFrame aiFrame, IVector2 destination, BotID botID)
	{
		IVector2 dest = destination;
		double size = RuleConstraints.getStopRadius();
		int i = 0;
		IVector2 refPos = getRefPos(aiFrame, destination);
		IVector2 primeDir = getPrimeDir(destination, refPos);
		while (true)
		{
			var nonMatching = pointChecker.findFirstNonMatching(aiFrame.getBaseAiFrame(), dest, botID);
			if (nonMatching.isEmpty())
			{
				return dest;
			}
			if (debugShapes != null)
			{
				debugShapes.add(new DrawablePoint(dest));
				debugShapes.add(new DrawableAnnotation(dest, nonMatching.get()));
			}
			if (i == (CIRCLE_STEPS + 1) / 2)
			{
				i = 0;
				size += DISTANCE_STEP_SIZE;
				if (size > Geometry.getFieldLength() * 2)
				{
					log.warn("Could not find next free dest at {}", destination, new Exception());
					return destination;
				}
			}

			IVector2 dir = primeDir.turnNew(i * AngleMath.PI_TWO / CIRCLE_STEPS);
			dest = destination.addNew(dir.scaleToNew(size));

			if (i > 0)
			{
				i = -i;
			} else
			{
				i = -i + 1;
			}
		}
	}


	private IVector2 getPrimeDir(IVector2 destination, IVector2 refPos)
	{
		IVector2 primeDir = destination.subtractNew(refPos);
		if (primeDir.isZeroVector())
		{
			return Vector2.fromX(1);
		}
		return primeDir;
	}


	private IVector2 getRefPos(AthenaAiFrame aiFrame, IVector2 destination)
	{
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		IVector2 ballPlacementPos = aiFrame.getGameState().getBallPlacementPositionForUs();
		if (ballPlacementPos != null)
		{
			return Lines.segmentFromPoints(ballPlacementPos, ballPos).closestPointOnPath(destination);
		}
		return ballPos;
	}
}
