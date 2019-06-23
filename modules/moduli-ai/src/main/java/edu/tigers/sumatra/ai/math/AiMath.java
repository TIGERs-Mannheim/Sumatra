/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Helper class for providing common math problems.
 * 
 * @author stei_ol
 */
public final class AiMath
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AiMath.class.getName());
	
	
	private AiMath()
	{
		
	}
	
	
	/**
	 * Adjusts destination when near friendly bot.
	 *
	 * @param dest the desired destinaton
	 * @return a valid destinaton
	 */
	private static IVector2 adjustPositionWhenNearBot(final WorldFrame wFrame, final BotID botID, final IVector2 dest)
	{
		double speedTolerance = 0.3;
		IVector2 tmpDest = dest;
		for (ITrackedBot bot : wFrame.getBots().values())
		{
			if (bot.getBotId().getTeamColor() != botID.getTeamColor()
					|| bot.getBotId().equals(botID)
					|| bot.getVel().getLength2() > speedTolerance)
			{
				// only consider our own bots
				// and ignore myself
				// and ignore moving bots
				continue;
			}
			double tolerance = (Geometry.getBotRadius() * 2) - 20;
			if (bot.getPos().isCloseTo(dest, tolerance))
			{
				// position is inside other bot, move outside
				tmpDest = LineMath.stepAlongLine(bot.getPos(), dest, tolerance + 20);
			}
		}
		return tmpDest;
	}
	
	
	/**
	 * This method adjusts a MoveDestination when its invalid:
	 * - Position is to close to ball.
	 * - Position is in PenArea.
	 * - Position is Near friendly Bot.
	 *
	 * @param wFrame
	 * @param botID
	 * @param dest
	 * @return
	 */
	public static IVector2 adjustMovePositionWhenItsInvalid(final WorldFrame wFrame, final BotID botID,
			final IVector2 dest)
	{
		IVector2 dest1 = adjustPositionWhenNearBot(wFrame, botID, dest);
		return SkillUtil.movePosOutOfPenAreaWrtBall(dest1, wFrame.getBall(),
				Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius()),
				Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius()));
	}
	
	
	/**
	 * Check if there are bots in a given shape, ignoring one bot
	 *
	 * @param shape
	 * @param bots
	 * @param ignoredBot may be null
	 * @return
	 */
	public static boolean isShapeFreeOfBots(final I2DShape shape, final IBotIDMap<ITrackedBot> bots,
			final ITrackedBot ignoredBot)
	{
		for (ITrackedBot bot : bots.values())
		{
			if (bot.equals(ignoredBot))
			{
				continue;
			}
			if (shape.isPointInShape(bot.getPos()))
			{
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Get all bots with a velocity <= velThreshold
	 *
	 * @param bots
	 * @param velThreshold
	 * @return
	 */
	public static IBotIDMap<ITrackedBot> getNonMovingBots(final IBotIDMap<ITrackedBot> bots, final double velThreshold)
	{
		IBotIDMap<ITrackedBot> result = new BotIDMap<>();
		for (ITrackedBot bot : bots.values())
		{
			if (bot.getVel().getLength2() <= velThreshold)
			{
				result.put(bot.getBotId(), bot);
			}
		}
		return result;
	}


	
	/**
	 * Checks if the beam between two points is blocked or not.
	 * ray looks like this:
	 *
	 * <pre>
	 * | * |
	 * |   |
	 * |   |
	 * | * |
	 * </pre>
	 *
	 * @param bots
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreIds
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibility(final Collection<ITrackedBot> bots, final IVector2 start, final IVector2 end,
			final double raySize,
			final Collection<BotID> ignoreIds)
	{
		final double minDistance = Geometry.getBallRadius() + Geometry.getBotRadius()
				+ raySize;
		
		// checking free line
		final double distanceStartEndSquared = VectorMath.distancePPSqr(start, end);
		final ILine startEndLine = Line.fromPoints(start, end);
		for (final ITrackedBot bot : bots)
		{
			if (ignoreIds.contains(bot.getBotId()))
			{
				continue;
			}
			final double distanceBotStartSquared = VectorMath.distancePPSqr(bot.getPos(), start);
			final double distanceBotEndSquared = VectorMath.distancePPSqr(bot.getPos(), end);
			if ((distanceStartEndSquared > distanceBotStartSquared) && (distanceStartEndSquared > distanceBotEndSquared))
			{
				// only check those bots that possibly can be in between start and end
				final double distanceBotLine = LineMath.distancePL(bot.getPos(), startEndLine);
				if (distanceBotLine < minDistance)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * {@link AiMath#p2pVisibility(Collection, IVector2, IVector2, List)}
	 *
	 * @param bots
	 * @param start
	 * @param end
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(
			final Collection<ITrackedBot> bots,
			final IVector2 start,
			final IVector2 end,
			final BotID... ignoreBotId)
	{
		return AiMath.p2pVisibility(bots, start, end, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link AiMath#p2pVisibility(Collection, IVector2, IVector2, List)}
	 *
	 * @param bots
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(
			final Collection<ITrackedBot> bots,
			final IVector2 start,
			final IVector2 end,
			final Double raySize,
			final BotID... ignoreBotId)
	{
		return p2pVisibility(bots, start, end, raySize, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link AiMath#p2pVisibility(Collection, IVector2, IVector2, double, Collection)}
	 *
	 * @param bots
	 * @param start
	 * @param end
	 * @param ignoreIds
	 * @return
	 */
	private static boolean p2pVisibility(
			final Collection<ITrackedBot> bots,
			final IVector2 start,
			final IVector2 end,
			final List<BotID> ignoreIds)
	{
		return p2pVisibility(bots, start, end, 0, ignoreIds);
	}


	
	/**
	 * @param pos some pos in field
	 * @return farthest point on field border from pos
	 */
	public static IVector2 getFarthestPointOnFieldBorder(IVector2 pos)
	{
		List<IVector2> targets = new ArrayList<>();
		for (double a = 0; a < AngleMath.PI_TWO; a += 0.1)
		{
			ILine line = Line.fromDirection(pos, Vector2.fromAngle(a));
			targets.add(
					pos.farthestToOpt(Geometry.getField().lineIntersections(line)).orElse(Vector2f.ZERO_VECTOR));
		}
		return pos.farthestTo(targets);
	}
}
