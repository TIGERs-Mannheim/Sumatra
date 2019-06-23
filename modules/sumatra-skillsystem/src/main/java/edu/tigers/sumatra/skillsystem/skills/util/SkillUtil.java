/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Common utility methods for skills
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class SkillUtil
{
	protected static final Logger log = Logger.getLogger(SkillUtil.class.getName());
	
	
	@SuppressWarnings("unused")
	private SkillUtil()
	{
	}
	
	
	/**
	 * Check if pos is inside field and adapt position if necessary with respect to the ball position
	 *
	 * @param pos the target position of the robots kicker position
	 * @param ballPos the current ball position
	 * @return the pos, if outside. An adapted pos else
	 */
	public static IVector2 movePosInsideFieldWrtBall(
			final IVector2 pos,
			final IVector2 ballPos)
	{
		IRectangle field = Geometry.getField();
		if (!field.isPointInShape(pos))
		{
			List<IVector2> intersections = field
					.lineIntersections(Line.fromPoints(ballPos, pos));
			if (intersections.isEmpty())
			{
				return field.nearestPointInside(pos);
			}
			return pos.nearestTo(intersections);
		}
		return pos;
	}
	
	
	/**
	 * Move bot kicker pos inside field
	 *
	 * @param tBot the tracked bot
	 * @param targetPose the target pose
	 * @param ballPos the ball pos
	 * @return a position inside field
	 */
	public static IVector2 moveBotKickerInsideFieldWrtBall(
			final ITrackedBot tBot,
			final Pose targetPose,
			final IVector2 ballPos)
	{
		IVector2 desiredKickerDest = BotShape.getKickerCenterPos(targetPose.getPos(), targetPose.getOrientation(),
				tBot.getCenter2DribblerDist());
		desiredKickerDest = SkillUtil.movePosInsideFieldWrtBall(desiredKickerDest, ballPos);
		return BotShape.getCenterFromKickerPos(desiredKickerDest, targetPose.getOrientation(),
				tBot.getCenter2DribblerDist());
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
	 * @param pos the target position of the robot
	 * @param ball the current ball
	 * @param penaltyAreas the penalty areas to check
	 * @return the pos, if outside. An adapted pos else
	 */
	public static IVector2 movePosOutOfPenAreaWrtBall(
			final IVector2 pos,
			final ITrackedBall ball,
			final IPenaltyArea... penaltyAreas)
	{
		for (IPenaltyArea penArea : penaltyAreas)
		{
			if (penArea.isPointInShapeOrBehind(pos))
			{
				if (ball.getVel().getLength2() > 0.1)
				{
					return penArea.nearestPointOutside(pos, ball.getPos());
				} else if (penArea.isPointInShapeOrBehind(ball.getPos()))
				{
					// move as near as possible to the ball inside penArea
					return penArea.nearestPointOutside(ball.getPos(), penArea.getGoalCenter());
				}
				// move to nearest pos outside based on current pos
				return penArea.nearestPointOutside(pos);
			}
		}
		return pos;
	}
	
	
	/**
	 * Get the kick speed for a pass under some contraints.
	 *
	 * @param ballConsultant the current ball consultant from {@link ITrackedBall}
	 * @param distance the pass distance from ball to receiver
	 * @param maxEndVel the max velocity at the pass receiver
	 * @param minPassTime the min time that the pass should take
	 * @return the required kick speed for the pass
	 */
	public static double passKickSpeed(IStraightBallConsultant ballConsultant, double distance, double maxEndVel,
			double minPassTime)
	{
		double endVel = maxEndVel;
		double kickSpeed = ballConsultant.getInitVelForDist(distance, endVel);
		double time4Pass = ballConsultant.getTimeForKick(distance, kickSpeed);
		if (time4Pass < minPassTime)
		{
			kickSpeed = ballConsultant.getInitVelForTimeDist(distance, minPassTime);
		}
		return kickSpeed;
	}
}
