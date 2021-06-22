/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper class for providing common math problems.
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AiMath
{
	/**
	 * Adjusts destination when near friendly bot.
	 *
	 * @param dest the desired destination
	 * @return a valid destination
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
	public static boolean isShapeOccupiedByBots(final I2DShape shape, final Map<BotID, ITrackedBot> bots,
			final BotID ignoredBot)
	{
		for (ITrackedBot bot : bots.values())
		{
			if (bot.getBotId().equals(ignoredBot))
			{
				continue;
			}
			if (shape.isPointInShape(bot.getPos()))
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * Get all bots with a velocity <= velThreshold
	 *
	 * @param bots
	 * @param velThreshold
	 * @return
	 */
	public static Map<BotID, ITrackedBot> getNonMovingBots(final Map<BotID, ITrackedBot> bots, final double velThreshold)
	{
		Map<BotID, ITrackedBot> result = new IdentityHashMap<>();
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
	private static boolean p2pVisibility(final Collection<ITrackedBot> bots, final IVector2 start, final IVector2 end,
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
}
