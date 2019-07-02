/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates a score for a goal kick based on the maximum free angle
 *
 * @author sabolc fabio stefan
 * @author mark felix
 */
public final class MaxAngleKickRater
{
	@Configurable(comment = "Angle (in rad) that is considered to be a 100% chance to score", defValue = "0.35")
	private static double maxAngle = 0.35;
	
	static
	{
		ConfigRegistration.registerClass("metis", MaxAngleKickRater.class);
	}
	
	
	private MaxAngleKickRater()
	{
	}
	
	
	/**
	 * Chance for our bots to score in the other goal from position origin.
	 * 
	 * @param bots
	 * @param origin position to test the chance from
	 * @return between 0 (bad) and 1 (good)
	 */
	public static double getDirectShootScoreChance(final Collection<ITrackedBot> bots, final IVector2 origin)
	{
		Goal targetGoal = Geometry.getGoalTheir();
		
		if (isInTeamBot(bots, origin))
			return 0; // not possible if point is inside enemy bot
			
		Optional<ValuePoint> target = getBestRatedDirectShotTarget(targetGoal,
				new ArrayList<>(bots), origin);
		return target.map(ValuePoint::getValue).orElse(0.0);
	}
	
	
	/**
	 * Get the score chance by the width of an angle.
	 * Using the 'maxAngle' as angle which indicates the maximal score chance.
	 *
	 * @param angle
	 * @return
	 */
	public static double getScoreChanceFromAngle(double angle)
	{
		return Math.min(angle / maxAngle, 1);
	}
	
	
	/**
	 * Chance for their bots to score in our goal from position origin.
	 * 
	 * @param bots
	 * @param origin position to test the chance from
	 * @return between 0 and 1
	 */
	public static double getFoeScoreChanceWithDefender(final Collection<ITrackedBot> bots, final IVector2 origin)
	{
		Goal targetGoal = Geometry.getGoalOur();
		
		if (isInTeamBot(bots, origin))
			return 0; // not possible if point is inside enemy bot
			
		Optional<ValuePoint> target = getBestRatedDirectShotTarget(targetGoal, bots, origin);
		return target.map(ValuePoint::getValue).orElse(0.0);
	}
	
	
	/**
	 * gets the biggest free angle from origin to the target goal.
	 *
	 * @param targetGoal
	 * @param obstacleBots
	 * @param origin
	 * @return
	 */
	public static Optional<ValuePoint> getBestRatedDirectShotTarget(Goal targetGoal,
			Collection<ITrackedBot> obstacleBots, IVector2 origin)
	{
		return getBestRatedDirectShotTarget(targetGoal, obstacleBots, origin, new ArrayList<>());
	}
	
	
	/**
	 * gets the biggest free angle from origin to the target goal.
	 * 
	 * @param targetGoal
	 * @param obstacleBots
	 * @param origin
	 * @param shapes
	 * @return
	 */
	private static Optional<ValuePoint> getBestRatedDirectShotTarget(Goal targetGoal,
			Collection<ITrackedBot> obstacleBots, IVector2 origin, List<IDrawableShape> shapes)
	{
		Optional<ValuePoint> bestShootTarget = Optional.empty();
		List<ShadowPoint> shadowPointList = new ArrayList<>();
		
		List<IVector2> botList = getRelevantBots(targetGoal, obstacleBots, origin);
		for (IVector2 bot : botList)
		{
			shadowPointList.addAll(getShadowOfBot(bot, targetGoal, origin));
		}
		
		List<ILine> freeSpaces = getFreeSpaces(shadowPointList, targetGoal);
		List<Double> angleList = getAnglesFromLines(freeSpaces, origin);
		
		// get the biggest angle out of the angle list
		double biggestAngle = 0;
		int biggestIdx = -1;
		for (int i = 0; i < angleList.size(); i++)
		{
			final double angle = angleList.get(i);
			
			if (angle > biggestAngle)
			{
				biggestAngle = angle;
				biggestIdx = i;
			}
		}
		if (biggestIdx > -1)
		{
			IVector2 start = freeSpaces.get(biggestIdx).getStart();
			IVector2 end = freeSpaces.get(biggestIdx).getEnd();
			
			IVector2 pointInGoal = TriangleMath.bisector(origin, start, end);
			
			ValuePoint bestShootPoint = new ValuePoint(pointInGoal, getScoreChanceFromAngle(biggestAngle));
			bestShootTarget = Optional.of(bestShootPoint);
			
			shapes.add(new DrawableLine(Line.fromPoints(origin, pointInGoal)));
		}
		
		for (int i = 0; i < freeSpaces.size(); i++)
		{
			ILine space = freeSpaces.get(i);
			Double angle = angleList.get(i);
			
			double scoreChance = getScoreChanceFromAngle(angle);
			
			Color color = new Color((int) ((1 - scoreChance) * 255), 0, (int) (scoreChance * 255), 100);
			
			DrawableTriangle triangle = new DrawableTriangle(origin, space.getStart(), space.getEnd(), color);
			triangle.setFill(true);
			
			shapes.add(triangle);
		}
		
		return bestShootTarget;
	}
	
	
	/**
	 * checks whether the tested position is inside any obstacle bots.
	 */
	private static boolean isInTeamBot(Collection<ITrackedBot> obstacleBots, IVector2 origin)
	{
		for (ITrackedBot bot : obstacleBots)
		{
			if (origin.distanceTo(bot.getPos()) <= Geometry.getBotRadius())
				return true;
		}
		return false;
	}
	
	
	/**
	 * gets all obstacleBots touching the trinagle beween the targetGoal and origin
	 */
	private static List<IVector2> getRelevantBots(Goal targetGoal, Collection<ITrackedBot> obstacleBots,
			final IVector2 origin)
	{
		List<IVector2> botPos = new ArrayList<>();
		
		IVector2 leftPost = targetGoal.getLeftPost();
		IVector2 rightPost = targetGoal.getRightPost();
		Triangle triangle = Triangle.fromCorners(leftPost, rightPost, origin);
		
		// when point is on goalline => no real triangle possible
		if (Math.abs(origin.x() - rightPost.x()) > 10e-3)
		{
			// iterate through the bots
			for (ITrackedBot bot : obstacleBots)
			{
				// if point is in triangle
				if ((triangle.isPointInShape(bot.getPos(), Geometry.getBotRadius())) &&
				// if bot is closer to goal than origin
						(bot.getPos().distanceTo(targetGoal.getCenter()) < origin.distanceTo(targetGoal.getCenter())))
				{
					botPos.add(bot.getPos());
				}
			}
		}
		
		return botPos;
	}
	
	
	/**
	 * gets the "Shadow" thrown on the targetGoal by a bot as seen from origin.
	 */
	private static List<ShadowPoint> getShadowOfBot(final IVector2 botPos, Goal targetGoal, final IVector2 origin)
	{
		List<IHalfLine> lines = getTangents(botPos, origin);
		
		IHalfLine l1;
		IHalfLine l2;
		
		if (targetGoal == Geometry.getGoalTheir())
		{
			l1 = lines.get(0);
			l2 = lines.get(1);
		} else
		{
			l1 = lines.get(1);
			l2 = lines.get(0);
		}
		
		// get projected point on extended goal line
		IVector2 p1 = targetGoal.getLine().intersectHalfLine(l1).orElse(targetGoal.getLeftPost());
		IVector2 p2 = targetGoal.getLine().intersectHalfLine(l2).orElse(targetGoal.getRightPost());
		
		
		// if the projected point is behind the direction of the line, the point will be set to the goal of the opposite
		// side.
		if (!l1.isPointInFront(p1))
		{
			p1 = Vector2.fromXY(p1.x(), -p1.y());
			p1 = p1.nearestTo(targetGoal.getLeftPost(), targetGoal.getRightPost());
		}
		if (!l2.isPointInFront(p2))
		{
			p2 = Vector2.fromXY(p2.x(), -p2.y());
			p2 = p2.nearestTo(targetGoal.getLeftPost(), targetGoal.getRightPost());
		}
		
		// extend line to projected point
		l1 = Lines.halfLineFromPoints(l1.supportVector(), p1);
		l2 = Lines.halfLineFromPoints(l2.supportVector(), p2);
		
		// get point in actual goal range
		IVector2 start = targetGoal.getLineSegment().intersectHalfLine(l1).orElse(targetGoal.getLeftPost());
		IVector2 end = targetGoal.getLineSegment().intersectHalfLine(l2).orElse(targetGoal.getRightPost());
		
		
		List<ShadowPoint> returnList = new ArrayList<>();
		returnList.add(new ShadowPoint(start, true));
		returnList.add(new ShadowPoint(end, false));
		
		return returnList;
		
	}
	
	
	/**
	 * gets the the lines from origin to the tangential points a bot.
	 */
	private static List<IHalfLine> getTangents(final IVector2 botPos, final IVector2 origin)
	{
		// calculate tangents
		ICircle botMargin = Circle.createCircle(botPos, Geometry.getBotRadius());
		List<IVector2> tan = CircleMath.tangentialIntersections(botMargin, origin);
		
		// line from ball to bot tangent
		IHalfLine l1 = Lines.halfLineFromPoints(origin, tan.get(1));
		IHalfLine l2 = Lines.halfLineFromPoints(origin, tan.get(0));
		
		List<IHalfLine> list = new ArrayList<>();
		list.add(l1);
		list.add(l2);
		
		return list;
	}
	
	
	/**
	 * returns all spaces to which the view is not over-shadowed.
	 */
	private static List<ILine> getFreeSpaces(final List<ShadowPoint> shadows, Goal targetGoal)
	{
		List<ILine> lineList = new ArrayList<>();
		
		// sorts descending
		shadows.sort((s1, s2) -> (s1.pos.y() < s2.pos.y()) ? 1 : -1);
		
		
		int shadowState = 0;
		IVector2 startPoint = targetGoal.getLeftPost();
		
		for (ShadowPoint shadowpoint : shadows)
		{
			if (shadowpoint.isShadowBegin)
			{
				shadowState++;
				if (shadowState == 1) // free space just ended
				{
					ILine line = Line.fromPoints(startPoint, shadowpoint.pos);
					lineList.add(line);
				}
			} else
			{
				shadowState--;
				if (shadowState == 0) // free space just started
				{
					startPoint = shadowpoint.pos;
				}
			}
			
		}
		
		// if the last shadowpoint is checked but there is still free space on the goalline to the right post
		if (shadowState == 0)
		{
			ILine line = Line.fromPoints(startPoint, targetGoal.getRightPost());
			lineList.add(line);
		}
		
		return lineList;
	}
	
	
	/**
	 * for each line, calculates the realistic view angle from the origin point to the end points of the line.
	 */
	private static List<Double> getAnglesFromLines(List<ILine> lineList, IVector2 origin)
	{
		List<Double> angleList = new ArrayList<>();
		for (ILine line : lineList)
		{
			IVector2 v1 = Vector2.fromPoints(origin, line.getStart());
			IVector2 v2 = Vector2.fromPoints(origin, line.getEnd());
			
			angleList.add(v1.angleToAbs(v2).orElse(0d));
		}
		return angleList;
	}
	
	private static class ShadowPoint
	{
		private IVector2 pos;
		private boolean isShadowBegin;
		
		
		public ShadowPoint(IVector2 pos, boolean isShadowBegin)
		{
			this.pos = pos;
			this.isShadowBegin = isShadowBegin;
		}
	}
}
