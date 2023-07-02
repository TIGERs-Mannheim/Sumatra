/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.util.Arrays;
import java.util.List;


/**
 * Common utility methods for skills
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class SkillUtil
{
	private static final double INSIDE_FIELD_MARGIN = 20;


	@SuppressWarnings("unused")
	private SkillUtil()
	{
	}


	/**
	 * Check if pos is inside field and adapt position if necessary with respect to the ball position
	 *
	 * @param pos     the target position of the robots kicker position
	 * @param ballPos the current ball position
	 * @return the pos, if outside. An adapted pos else
	 */
	public static IVector2 movePosInsideFieldWrtBall(
			final IVector2 pos,
			final IVector2 ballPos)
	{
		IRectangle field = Geometry.getField().withMargin(-INSIDE_FIELD_MARGIN);
		if (!field.isPointInShape(pos))
		{
			List<IVector2> intersections = field.intersectPerimeterPath(Lines.lineFromPoints(ballPos, pos));
			if (intersections.isEmpty())
			{
				return field.nearestPointInside(pos);
			}
			return pos.nearestTo(intersections);
		}
		return pos;
	}


	/**
	 * Check if pos is outside the penalty areas and adapt position if necessary.<br>
	 * The check considers the ball's position and velocity:
	 * <ul>
	 * <li>If ball is moving: Move to nearest point on ball travel line</li>
	 * <li>If ball inside penArea: Move to nearest point to ball</li>
	 * <li>Else: Move to nearest point to given pos</li>
	 * </ul>
	 *
	 * @param pos          the target position of the robot
	 * @param ball         the current ball
	 * @param penaltyAreas the penalty areas to check
	 * @return the pos, if outside. An adapted pos else
	 */
	public static IVector2 movePosOutOfPenAreaWrtBall(
			final IVector2 pos,
			final ITrackedBall ball,
			final IPenaltyArea... penaltyAreas)
	{
		return movePosOutOfPenAreaWrtBall(pos, ball, Arrays.asList(penaltyAreas));
	}


	/**
	 * Check if pos is outside the penalty areas and adapt position if necessary.<br>
	 * The check considers the ball's position and velocity:
	 * <ul>
	 * <li>If ball is moving: Move to nearest point on ball travel line</li>
	 * <li>If ball inside penArea: Move to nearest point to ball</li>
	 * <li>Else: Move to nearest point to given pos</li>
	 * </ul>
	 *
	 * @param pos          the target position of the robot
	 * @param ball         the current ball
	 * @param penaltyAreas the penalty areas to check
	 * @return the pos, if outside. An adapted pos else
	 */
	public static IVector2 movePosOutOfPenAreaWrtBall(
			final IVector2 pos,
			final ITrackedBall ball,
			final List<IPenaltyArea> penaltyAreas)
	{
		for (IPenaltyArea penArea : penaltyAreas)
		{
			if (penArea.isPointInShapeOrBehind(pos))
			{
				if (ball.getVel().getLength2() > 0.1)
				{
					return pos.nearestToOpt(penArea.intersectPerimeterPath(Lines.lineFromPoints(pos, ball.getPos())))
							.orElseGet(() -> penArea.nearestPointOutside(pos));
				} else if (penArea.isPointInShape(ball.getPos()))
				{
					// move as near as possible to the ball inside penArea
					return penArea.nearestPointOutside(ball.getPos());
				}
				// move to nearest pos outside based on current pos
				return penArea.nearestPointOutside(pos);
			}
		}
		return pos;
	}


}
