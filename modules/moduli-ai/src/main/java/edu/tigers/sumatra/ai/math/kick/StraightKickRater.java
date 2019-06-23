/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math.kick;

import java.util.Collection;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Rate straight goal kicks. Assumption is, that the ball is fast enough such that no reaction of the opponent must be
 * considered.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class StraightKickRater
{
	private static final double MAX_DIST_TO_BALL_DEST_LINE = 500;
	/** weight in the value point for distance between ball-dest-line and foe bot. higher value= less weight */
	private static final int DIST_BALL_DEST_LINE_WEIGHT = 5;
	
	
	private StraightKickRater()
	{
	}
	
	
	/**
	 * Calculate a score between 0 (bad) and 1 (good) for the straight line between start and target.
	 * Visibility and bots will be considered.
	 *
	 * @param bots
	 * @param start
	 * @param target
	 * @return a score between 0 (bad) and 1 (good)
	 */
	public static double rateStraightGoalKick(final Collection<ITrackedBot> bots, final IVector2 start,
			final IVector2 target)
	{
		double value;
		// will check if there are points on the enemys goal, not being blocked by bots.
		if (AiMath.p2pVisibility(bots, start, target, (Geometry.getBallRadius() * 2) + 0.1))
		{
			// free visibility
			value = 0;
		} else
		{
			value = 0.5;
		}
		double ownDist = VectorMath.distancePP(start, target);
		ILine originTargetLine = Line.fromPoints(start, target);
		for (final ITrackedBot bot : bots)
		{
			double enemyDist = VectorMath.distancePP(bot.getPos(), target);
			if (enemyDist < ownDist)
			{
				// evaluate the generated points: If the view to a point is unblocked the function
				// will get 100 points. Afterwards the distance between the defender and the line between
				// start and target will be added as 1/6000
				double relDist = LineMath.distancePL(bot.getPos(), originTargetLine)
						/ MAX_DIST_TO_BALL_DEST_LINE;
				if (relDist > 1)
				{
					relDist = 1;
				} else if (relDist < 0)
				{
					relDist = 0;
				}
				value += (1 - relDist) / DIST_BALL_DEST_LINE_WEIGHT;
			}
		}
		
		if (value > 1)
		{
			value = 1;
		} else if (value < 0)
		{
			value = 0;
		}
		
		return 1 - value;
	}
}
