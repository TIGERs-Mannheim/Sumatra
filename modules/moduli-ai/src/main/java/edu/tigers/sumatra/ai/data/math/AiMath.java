/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.shapes.I2DShape;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Helper class for providing common math problems.
 * 
 * @author stei_ol
 */
public final class AiMath
{
	@SuppressWarnings("unused")
	private static final Logger			log										= Logger.getLogger(AiMath.class.getName());
	
	private static final double			MAX_DIST_TO_BALL_DEST_LINE			= 500;
	/** weight in the value point for distance between ball-dest-line and foe bot. higher value= less weight */
	private static final int				DIST_BALL_DEST_LINE_WEIGHT			= 5;
	
	private static final double			securityDistBotBall					= 100;
	
	@Configurable
	private static double					minDirectShootScoreChange			= 0.3;
	
	@Configurable(comment = "Assumed inaccuracy per distance")
	private static double					shootSpread								= 1.0 / 10.0;
	
	@Configurable(comment = "Scaling of distance between bot and goal")
	private static double					distanceScaling						= 0.2;
	
	@Configurable(comment = "Distance from Bot in which ignore Enemy Bots by calculate ChipKick")
	private static double					ignoreEnemyBotChipKickDistance	= 1000;
	
	@Configurable(comment = "Which method to use for calculating redirect angle: FUN or PHY")
	private static ERedirectAngleMethod	redirectAngleMethod					= ERedirectAngleMethod.PHY;
	
	private enum ERedirectAngleMethod
	{
		/** approximated function */
		FUN,
		/** physical calculation */
		PHY
	}
	
	
	static
	{
		ConfigRegistration.registerClass("math", AiMath.class);
	}
	
	
	private AiMath()
	{
		
	}
	
	
	/**
	 * Finds the nearest bot to a given position (p).
	 * 
	 * @param botMap
	 * @param p
	 * @return
	 * @author Oliver Steinbrecher
	 */
	public static ITrackedBot getNearestBot(final IBotIDMap<ITrackedBot> botMap, final IVector2 p)
	{
		double distance = Double.MAX_VALUE;
		ITrackedBot result = null;
		if (botMap.size() < 1)
		{
			throw new IllegalArgumentException("Empty input list");
		}
		for (final ITrackedBot bot : botMap.values())
		{
			if (GeoMath.distancePP(bot.getPos(), p) < distance)
			{
				distance = GeoMath.distancePP(bot.getPos(), p);
				result = bot;
			}
		}
		return result;
	}
	
	
	/**
	 * Get a sorted list of all tiger bots starting with the one nearest to the given position
	 * 
	 * @param aiFrame
	 * @param pos
	 * @return
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static List<BotID> getTigerBotsNearestToPointSorted(final AthenaAiFrame aiFrame, final IVector2 pos)
	{
		final TreeMap<Double, BotID> botToBallDists = new TreeMap<Double, BotID>();
		for (final Map.Entry<BotID, ITrackedBot> bot : aiFrame.getWorldFrame().tigerBotsAvailable)
		{
			final double cost = GeoMath.distancePP(bot.getValue().getPos(), pos);
			botToBallDists.put(cost, bot.getKey());
		}
		return new ArrayList<BotID>(botToBallDists.values());
	}
	
	
	/**
	 * Get a sorted list of all foe bots starting with the one nearest to the given position
	 * 
	 * @param aiFrame
	 * @param pos
	 * @return
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static List<BotID> getFoeBotsNearestToPointSorted(final AthenaAiFrame aiFrame, final IVector2 pos)
	{
		// sry for duplicate code... this BotIDMap sucks...
		final TreeMap<Double, BotID> botToBallDists = new TreeMap<Double, BotID>();
		for (final Map.Entry<BotID, ITrackedBot> bot : aiFrame.getWorldFrame().foeBots)
		{
			final double cost = GeoMath.distancePP(bot.getValue().getPos(), pos);
			botToBallDists.put(cost, bot.getKey());
		}
		return new ArrayList<BotID>(botToBallDists.values());
	}
	
	
	/**
	 * Get a sorted list of all foe bots starting with the one nearest to the given line
	 * 
	 * @param aiFrame The current athena frame
	 * @param line The line to get the foes for
	 * @return The sorted list of bots
	 */
	public static List<BotID> getFoeBotsNearestToLineSorted(final AthenaAiFrame aiFrame, final Line line)
	{
		// sry for duplicate code... this BotIDMap sucks...
		final TreeMap<Double, BotID> botToBallDists = new TreeMap<Double, BotID>();
		for (final Map.Entry<BotID, ITrackedBot> bot : aiFrame.getWorldFrame().foeBots)
		{
			final double cost = GeoMath.distancePL(bot.getValue().getPos(), line);
			botToBallDists.put(cost, bot.getKey());
		}
		return new ArrayList<BotID>(botToBallDists.values());
	}
	
	
	/**
	 * Get a position inside the field (with small border) from given position
	 * 
	 * @param pos
	 * @return
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static IVector2 normalizeIntoField(final IVector2 pos)
	{
		final double border = Geometry.getBotRadius();
		final double fieldMaxY = (Geometry.getFieldWidth() / 2.0) - border;
		final double fieldMaxX = (Geometry.getFieldLength() / 2.0) - border;
		
		IVector2 newPos = new Vector2f(pos);
		
		if ((newPos.x() >= 0) && (newPos.x() > fieldMaxX))
		{
			newPos = new Vector2f(fieldMaxX, newPos.y());
		} else if ((newPos.x() < 0) && (newPos.x() < -fieldMaxX))
		{
			newPos = new Vector2f(-fieldMaxX, newPos.y());
		}
		
		if ((newPos.y() >= 0) && (newPos.y() > fieldMaxY))
		{
			newPos = new Vector2f(fieldMaxY, newPos.x());
		} else if ((newPos.y() < 0) && (newPos.y() < -fieldMaxY))
		{
			newPos = new Vector2f(-fieldMaxY, newPos.x());
		}
		
		return newPos;
	}
	
	
	/**
	 * Gets the nearest receiver to the passer (only within the enemy half!)
	 * 
	 * @param aiFrame
	 * @param bots
	 * @return Returns a BotID if a bot was found within the enemy half.
	 */
	public static BotID getReceiverInEnemyHalf(final AthenaAiFrame aiFrame, final IBotIDMap<ITrackedBot> bots)
	{
		SortedMap<Double, BotID> sortedDists = new ConcurrentSkipListMap<Double, BotID>();
		for (ITrackedBot bot : bots.values())
		{
			if (bot.getPos().x() > 0)
			{
				double distance = GeoMath.distancePP(bot.getPos(), aiFrame.getWorldFrame().getBall().getPos());
				Double[] sortedArray = new Double[sortedDists.size()];
				sortedArray = sortedDists.keySet().toArray(sortedArray);
				// if we have more or equal to 2 entries (the only two we need: passer, receiver)
				if (sortedDists.size() > 1)
				{
					if ((sortedArray[0] > distance) || (sortedArray[1] > distance))
					{
						if (sortedDists.size() > 1)
						{
							sortedDists.remove(sortedArray[sortedArray.length - 1]);
						}
						sortedDists.put(distance, bot.getBotId());
					}
				} else
				{
					sortedDists.put(distance, bot.getBotId());
				}
			}
		}
		if ((sortedDists.size() <= 1) && (aiFrame.getWorldFrame().getBall().getPos().x() > 0))
		{
			return null;
		}
		if (sortedDists.isEmpty())
		{
			return null;
		}
		BotID[] resultSet = new BotID[sortedDists.size()];
		resultSet = sortedDists.values().toArray(resultSet);
		
		// If someone is within the other half, but our passer is not.
		if (resultSet.length == 1)
		{
			return resultSet[0];
		}
		// The regular occurance.
		return resultSet[1];
	}
	
	
	/**
	 * Get a position of a tiger bot that has free look to the opponents goal and the ball and is not the nearest to the
	 * ball.
	 * 
	 * @param aiFrame
	 * @param botSelection only these bots are used
	 * @return
	 */
	public static BotID getReceiver(final AthenaAiFrame aiFrame, final IBotIDMap<ITrackedBot> botSelection)
	{
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		IVector2 goalCenter = Geometry.getGoalTheir().getGoalCenter();
		
		// 1. determine nearest bots to ball we will not use the nearest, as this is our passer
		List<BotID> botsNearestToBallSorted = AiMath.getTigerBotsNearestToPointSorted(aiFrame,
				aiFrame.getWorldFrame().getBall().getPos());
		if (botsNearestToBallSorted.isEmpty())
		{
			return null;
		}
		BotID potShooter = botsNearestToBallSorted.get(0);
		
		List<BotID> botsNearestToGoalSorted = AiMath.getTigerBotsNearestToPointSorted(aiFrame, Geometry
				.getGoalTheir().getGoalCenter());
		botsNearestToGoalSorted.remove(potShooter);
		
		// 2. determine a list of potential bots that the passer can pass to.
		// also map them with the distance to the goal
		TreeMap<Double, BotID> potBots = new TreeMap<Double, BotID>();
		for (BotID botID : botsNearestToGoalSorted)
		{
			if (!botSelection.containsKey(botID))
			{
				continue;
			}
			IBot bot = botSelection.get(botID).getBot();
			if ((bot != null) && (bot.getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.WORKING)
					&& (bot.getBotFeatures().get(EFeature.CHIP_KICKER) != EFeatureState.WORKING))
			{
				continue;
			}
			// second nearest to ball
			IVector2 potReceiverPos = aiFrame.getWorldFrame().tigerBotsVisible.get(botID).getPos();
			// can passer shoot to receiver?
			if (AiMath.p2pVisibility(aiFrame.getWorldFrame(), ballPos, potReceiverPos, potShooter))
			{
				potBots.put(GeoMath.distancePP(potReceiverPos, goalCenter), botID);
			}
		}
		
		// 3. does receiver has free look to opponent goal?
		// in the loop, the bot with smallest distance to goal will come first
		for (BotID botID : potBots.values())
		{
			final IVector2 receiverPos = aiFrame.getWorldFrame().tigerBotsVisible.get(botID).getPos();
			
			// filter bots in our field as they are potentially dangerous receivers
			if (receiverPos.x() < 0)
			{
				continue;
			}
			
			// check visibility of goal
			List<BotID> ignoreBotIDs = new ArrayList<BotID>(1);
			ignoreBotIDs.add(botID);
			ignoreBotIDs.add(potShooter);
			if (isGoalVisible(aiFrame.getWorldFrame(), Geometry.getGoalTheir(), receiverPos, ignoreBotIDs))
			{
				// our number one!
				return botID;
			}
		}
		
		if (botsNearestToGoalSorted.isEmpty())
		{
			return null;
		}
		return botsNearestToGoalSorted.get(0);
	}
	
	
	/**
	 * Return the bots that are in {@link WorldFrame#tigerBotsVisible} bot not in {@link WorldFrame#tigerBotsAvailable}.
	 * 
	 * @param aiFrame
	 * @return a fresh, modifiable list of the other bots
	 */
	public static BotIDMap<ITrackedBot> getOtherBots(final BaseAiFrame aiFrame)
	{
		BotIDMap<ITrackedBot> otherBots = new BotIDMap<>(aiFrame.getWorldFrame().tigerBotsVisible);
		for (BotID botID : aiFrame.getWorldFrame().tigerBotsAvailable.keySet())
		{
			otherBots.remove(botID);
		}
		return otherBots;
	}
	
	
	/**
	 * Checks visibility to goal by generating some points on goal and testing against them
	 * 
	 * @param wf
	 * @param goal
	 * @param start
	 * @param ignoreIds
	 * @return
	 */
	public static boolean isGoalVisible(final WorldFrame wf, final Goal goal, final IVector2 start,
			final List<BotID> ignoreIds)
	{
		double starty = Math.max(goal.getGoalPostLeft().y(), goal.getGoalPostRight().y());
		double length = Math.abs(goal.getGoalPostLeft().y()) + Math.abs(goal.getGoalPostRight().y());
		List<IVector2> goalLine = new ArrayList<IVector2>((int) length / 50);
		for (int i = 0; i < (length / 50.0); i++)
		{
			goalLine.add(new Vector2(goal.getGoalCenter().x(), starty + ((i / (length / 50.0)) * length)));
		}
		return AiMath.p2pVisibility(wf, start, goalLine, ignoreIds);
	}
	
	
	/**
	 * Find the best point around center, starting with start and rotating around center in a circle from start.
	 * Use then a circle with double and three times the radius. Each point will be validated with the
	 * pointChecker. If no point was valid, center will be returned.
	 * 
	 * @param center most properly the current bot position
	 * @param start first point to check, e.g. Point 90mm behind bot
	 * @param pointChecker check algorithm for validating each point.
	 * @param rounds
	 * @return the first point that is valid or null if no point is valid
	 */
	public static IVector2 findBestPoint(final IVector2 center, final IVector2 start,
			final Function<IVector2, Boolean> pointChecker,
			final int rounds)
	{
		double radius = GeoMath.distancePP(start, center);
		IVector2 current = start;
		for (int i = 1; i <= rounds; i++)
		{
			current = (GeoMath.stepAlongLine(center, current, radius * i));
			for (double angle = 0; angle < AngleMath.PI_TWO; angle += AngleMath.PI_QUART / ((i / 3.0) + 1))
			{
				IVector2 pos = GeoMath.stepAlongCircle(current, center, angle);
				if (pointChecker.apply(pos))
				{
					return pos;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @author Mark Geiger
	 * @param wf World Frame
	 * @param ignoreEnemyBotDistance bots to near to the shooter bot get ignored
	 * @param chipLandingSpotX this value says, where the ball should hit the ground ( just X value )
	 * @return the best spot to Chip Kick to, to score a Goal and the chance to make this goal = Zero Vector if there is
	 *         no target.
	 */
	public static ValuePoint determineChipShotTarget(final WorldFrame wf, final double ignoreEnemyBotDistance,
			final double chipLandingSpotX)
	{
		final int numberOfIterations = 30;
		int chanceChecker = numberOfIterations + 1;
		IVector2 shooterPosition = wf.getBall().getPos();
		
		int seriesStartBest = 0;
		int seriesStartNow = 0;
		int seriesSizeBest = 0;
		int seriesSizeNow = 0;
		
		double enemyGoalSize = Geometry.getGoalTheir().getSize();
		Vector2f enemyGoalRight = Geometry.getGoalTheir().getGoalPostRight();
		
		for (int i = 0; i <= numberOfIterations; i++)
		{
			Vector2 checkingPoint = new Vector2(enemyGoalRight.subtractNew(new Vector2(0,
					(-enemyGoalSize / numberOfIterations) * i)));
			
			List<BotID> ignoredBots = new ArrayList<BotID>();
			for (Entry<BotID, ITrackedBot> foeBot : wf.foeBots)
			{
				if (GeoMath.distancePP(shooterPosition, foeBot.getValue().getPos()) < ignoreEnemyBotDistance)
				{
					ignoredBots.add(foeBot.getKey());
				}
			}
			
			double raySize = (((enemyGoalSize / numberOfIterations) / 5.0));
			boolean freeLine = AiMath.p2pVisibility(wf, shooterPosition, checkingPoint, raySize, ignoredBots);
			if (freeLine)
			{
				seriesSizeNow++;
			} else if (seriesSizeBest <= seriesSizeNow)
			{
				seriesStartBest = seriesStartNow;
				seriesSizeBest = seriesSizeNow;
				seriesSizeNow = 0;
				seriesStartNow = i;
				chanceChecker--;
			} else
			{
				seriesSizeNow = 0;
				seriesStartNow = i;
				chanceChecker--;
			}
		}
		if (chanceChecker == 0)
		{
			// There is no chance to make a direct Goal !
			// So return null
			return null;
		}
		if (seriesSizeBest <= seriesSizeNow)
		{
			seriesStartBest = seriesStartNow;
			seriesSizeBest = seriesSizeNow;
		}
		double rayDistance = enemyGoalSize / numberOfIterations;
		Vector2 target = new Vector2(enemyGoalRight.subtractNew(new Vector2(0, (-rayDistance * seriesStartBest)
				- ((rayDistance * seriesSizeBest) / 2.0))));
		
		Line line = new Line(target, shooterPosition.subtractNew(target));
		
		double y = 0;
		double x = chipLandingSpotX;
		try
		{
			y = line.getYValue(x);
		} catch (MathException err)
		{
			y = 0;
		}
		target = new Vector2(x, y);
		ValuePoint targetAndValue = new ValuePoint(target, seriesSizeBest);
		return targetAndValue;
	}
	
	
	/**
	 * @author Mark Geiger
	 * @param wf World Frame
	 * @param chipKick get Score for ChipKick
	 * @return true -> Bot will shoot directly on the goal, false -> Bot will pass to another Bot
	 */
	public static boolean willBotShoot(final WorldFrame wf, final boolean chipKick)
	{
		if (ProbabilityMath.getDirectShootScoreChance(wf, wf.getBall().getPos(), chipKick) > 0.30)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Calculate a score between 0 (good) and 1 (bad) for the straight line between origin and target.
	 * Visibility and bots will be considered.
	 * 
	 * @param wFrame
	 * @param origin
	 * @param target
	 * @return
	 */
	public static double getScoreForStraightShot(final WorldFrame wFrame, final IVector2 origin, final IVector2 target)
	{
		double value = 0;
		// will check if there are points on the enemys goal, not being blocked by bots.
		if (AiMath.p2pVisibility(wFrame, origin, target, (Geometry.getBallRadius() * 2) + 0.1))
		{
			// free visibility
			value = 0;
		} else
		{
			value = 0.5;
		}
		Collection<ITrackedBot> allBots = new ArrayList<>(wFrame.foeBots.values());
		allBots.addAll(wFrame.tigerBotsVisible.values());
		double ownDist = GeoMath.distancePP(origin, target);
		for (final ITrackedBot bot : allBots)
		{
			double enemyDist = GeoMath.distancePP(bot.getPos(), target);
			if (enemyDist < ownDist)
			{
				// evaluate the generated points: If the view to a point is unblocked the function
				// will get 100 points. Afterwards the distance between the defender and the line between
				// start and target will be added as 1/6000
				double relDist = (GeoMath.distancePL(bot.getPos(), origin, target) / MAX_DIST_TO_BALL_DEST_LINE);
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
		
		return value;
	}
	
	
	/**
	 * This methods takes a position and checks if the position is to close
	 * to the ball, if that is the case the position is then adjusted. The
	 * returned position is further away from the ball on its moving direction.
	 * 
	 * @param position
	 * @return
	 */
	private static IVector2 adjustPositionWhenNearBall(final WorldFrame wFrame, final IVector2 position)
	{
		if (isPositionNearBall(wFrame, position))
		{
			double tolerance = Geometry.getBotRadius() + Geometry.getBallRadius()
					+ securityDistBotBall;
			try
			{
				IVector2 ballPosFuture = wFrame.getBall().getPosByTime(0.5f);
				Line line = new Line(wFrame.getBall().getPos(), ballPosFuture.subtractNew(wFrame.getBall().getPos()));
				IVector2 rv = line.directionVector();
				rv = rv.normalizeNew();
				
				
				ballPosFuture = position.addNew(rv.multiplyNew(tolerance));
				return ballPosFuture;
				
			} catch (IllegalArgumentException e)
			{
				IVector2 destination = null;
				IVector2 ballPos = wFrame.getBall().getPos();
				IVector2 target = AiMath.determineChipShotTarget(wFrame, 0, Geometry.getGoalTheir()
						.getGoalCenter().x());
				if (target == null)
				{
					target = Geometry.getGoalTheir().getGoalCenter();
				}
				IVector2 targetDir = target.subtractNew(ballPos).normalizeNew();
				destination = new Vector2(ballPos.addNew(targetDir.multiplyNew(-300)));
				return destination;
			}
		}
		return position;
	}
	
	
	/**
	 * Adjusts destination when near friendly bot.
	 * 
	 * @param dest
	 * @return
	 */
	private static IVector2 adjustPositionWhenNearBot(final WorldFrame wFrame, final BotID botID, final IVector2 dest)
	{
		double speedTolerance = 0.2;
		for (ITrackedBot bot : wFrame.getBots().values())
		{
			if (bot.getBotId().getTeamColor() != botID.getTeamColor())
			{
				continue;
			}
			if (!bot.getBotId().equals(botID) && (bot.getVel().getLength2() < speedTolerance))
			{
				double tolerance = (Geometry.getBotRadius() * 2) - 20;
				if (bot.getPos().equals(dest, tolerance))
				{
					return GeoMath.stepAlongLine(bot.getPos(), dest, tolerance + 20);
				}
			}
		}
		return dest;
	}
	
	
	/**
	 * Adjusts destination when outside of Field.
	 * 
	 * @param wFrame
	 * @param botID
	 * @param dest
	 * @return
	 */
	public static IVector2 adjustPositionWhenOutsideOfField(final WorldFrame wFrame, final BotID botID,
			final IVector2 dest)
	{
		if (!Geometry.getField().isPointInShape(dest))
		{
			IVector2 destination = null;
			IVector2 ballPos = wFrame.getBall().getPos();
			destination = new Vector2(ballPos.addNew(AVector2.ZERO_VECTOR.subtractNew(ballPos).normalizeNew()
					.multiplyNew(800)));
			return destination;
		}
		return dest;
	}
	
	
	/**
	 * Adjusts destination when near friendly bot.
	 * 
	 * @param dest
	 * @return
	 */
	private static IVector2 adjustPositionWhenInPenArea(final WorldFrame wFrame, final BotID botID, final IVector2 dest)
	{
		/*
		 * i = 0, when adjusting position in enemyPen too.
		 * i = 1, when only adjusting positions in ourPenArea.
		 */
		for (int i = 1; i < 2; i++)
		{
			PenaltyArea penArea = null;
			if (i == 0)
			{
				penArea = Geometry.getPenaltyAreaTheir();
			}
			if (i == 1)
			{
				penArea = Geometry.getPenaltyAreaOur();
			}
			if (penArea != null)
			{
				if (penArea.isPointInShape(dest, Geometry.getBotRadius()))
				{
					IVector2 botPos = wFrame.getBot(botID).getPos();
					// behind penArea?
					if (botPos.x() <= (-Geometry.getFieldLength() / 2.0))
					{
						// this will result in an acceptable new destination
						botPos = AVector2.ZERO_VECTOR;
					}
					IVector2 nearestPointOutside = penArea.nearestPointOutside(dest, botPos, Geometry
							.getBotRadius());
					return nearestPointOutside;
				}
			}
		}
		return dest;
	}
	
	
	/**
	 * This method checks if a given position is to close to the ball.
	 * 
	 * @param position
	 * @return true or false
	 */
	private static boolean isPositionNearBall(final WorldFrame wFrame, final IVector2 position)
	{
		double tolerance = Geometry.getBotRadius() + Geometry.getBallRadius()
				+ securityDistBotBall;
		
		IVector2 ballPos = wFrame.getBall().getPos();
		
		double distancePositionToBall = GeoMath.distancePP(ballPos, position);
		
		if (distancePositionToBall > tolerance)
		{
			return false;
		}
		return true;
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
	public static IVector2 adjustMovePositionWhenItsInvalid(final WorldFrame wFrame, final BotID botID, IVector2 dest)
	{
		dest = adjustPositionWhenNearBall(wFrame, dest);
		dest = adjustPositionWhenNearBot(wFrame, botID, dest);
		dest = adjustPositionWhenOutsideOfField(wFrame, botID, dest);
		dest = adjustPositionWhenInPenArea(wFrame, botID, dest);
		return dest;
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
	 * Estimates subset of bots located inside shape
	 * 
	 * @param shape
	 * @param bots
	 * @return IBotIDMap of bots in shape
	 */
	public static IBotIDMap<ITrackedBot> getBotsInShape(final I2DShape shape, final IBotIDMap<ITrackedBot> bots)
	{
		IBotIDMap<ITrackedBot> botsInShape = new BotIDMap<ITrackedBot>();
		
		for (Entry<BotID, ITrackedBot> entry : bots.entrySet())
		{
			if (shape.isPointInShape(entry.getValue().getPos(), Geometry.getBotRadius() * 2))
			{
				botsInShape.put(entry.getKey(), entry.getValue());
			}
		}
		
		return botsInShape;
	}
	
	
	/**
	 * Estimates subset of bots located inside shape
	 * 
	 * @param shape
	 * @param bots
	 * @return List of bots in shape
	 */
	public static List<ITrackedBot> getBotsInShape(final I2DShape shape, final List<ITrackedBot> bots)
	{
		IBotIDMap<ITrackedBot> botIDMap = new BotIDMap<ITrackedBot>();
		
		for (ITrackedBot bot : bots)
		{
			botIDMap.put(bot.getBotId(), bot);
		}
		
		return new ArrayList<ITrackedBot>(getBotsInShape(shape, botIDMap).values());
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
		IBotIDMap<ITrackedBot> result = new BotIDMap<>(bots.size());
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
	 * Get all bots with a velocity > velThreshold
	 * 
	 * @param bots
	 * @param velThreshold
	 * @return
	 */
	public static IBotIDMap<ITrackedBot> getMovingBots(final IBotIDMap<ITrackedBot> bots, final double velThreshold)
	{
		IBotIDMap<ITrackedBot> result = new BotIDMap<>(bots.size());
		for (ITrackedBot bot : bots.values())
		{
			if (bot.getVel().getLength2() > velThreshold)
			{
				result.put(bot.getBotId(), bot);
			}
		}
		return result;
	}
	
	
	/**
	 * Find a random point drawn from Gaussian distribution that lies on the ball traveling line (now till ball stops)
	 * mu should be a rough estimation, e.g. the last point.
	 * 
	 * @param ball
	 * @param mu result will be centered around this point
	 * @param sigma standard deviation for Gaussian distribution
	 * @param rnd
	 * @return
	 */
	public static IVector2 getRandomPointOnBallTravelLine(final TrackedBall ball, final IVector2 mu, final double sigma,
			final Random rnd)
	{
		IVector2 rndRangeStart = ball.getPos();
		IVector2 rndRangeEnd = ball.getPosByVel(0);
		double lastDist = GeoMath.distancePP(rndRangeStart, mu);
		double gaussDist = lastDist + (rnd.nextGaussian() * sigma);
		double stepDist = Math.max(0, Math.min(gaussDist, GeoMath.distancePP(rndRangeStart, rndRangeEnd)));
		IVector2 rndDest = GeoMath.stepAlongLine(rndRangeStart, rndRangeEnd, stepDist);
		return rndDest;
	}
	
	
	/**
	 * Determines whether support bots would be equally distributed over both field sides or not
	 * 
	 * @param position Support position to consider
	 * @param aiFrame Current BaseAiFrame
	 * @return
	 */
	public static boolean isBalancedSupportPosition(final IVector2 position, final BaseAiFrame aiFrame)
	{
		int yBalance = 0;
		
		List<ARole> roles = aiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORT);
		for (ARole role : roles)
		{
			if (((SupportRole) role).getDestination() != null)
			{
				yBalance += ((SupportRole) role).getDestination().y() < 0 ? -1 : 1;
			}
		}
		
		boolean positiveNeeded = yBalance <= 0;
		
		return (Math.abs(yBalance) <= 1) || (positiveNeeded && (position.y() >= 0))
				|| (!positiveNeeded && (position.y() < 0));
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
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreIds
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibility(final SimpleWorldFrame wf, final IVector2 start, final IVector2 end,
			final double raySize,
			final Collection<BotID> ignoreIds)
	{
		final double minDistance = Geometry.getBallRadius() + Geometry.getBotRadius()
				+ raySize;
		
		// checking free line
		final double distanceStartEndSquared = GeoMath.distancePPSqr(start, end);
		for (final ITrackedBot bot : wf.getBots().values())
		{
			if (ignoreIds.contains(bot.getBotId()))
			{
				continue;
			}
			final double distanceBotStartSquared = GeoMath.distancePPSqr(bot.getPos(), start);
			final double distanceBotEndSquared = GeoMath.distancePPSqr(bot.getPos(), end);
			if ((distanceStartEndSquared > distanceBotStartSquared) && (distanceStartEndSquared > distanceBotEndSquared))
			{
				// only check those bots that possibly can be in between start and end
				final double distanceBotLine = GeoMath.distancePL(bot.getPos(), start, end);
				if (distanceBotLine < minDistance)
				{
					return false;
				}
			}
		}
		
		return true;
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
	 * @param start
	 * @param end
	 * @param raySize
	 * @param botsToCheck
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibility(final IVector2 start, final IVector2 end,
			final double raySize,
			final ITrackedBot... botsToCheck)
	{
		final double minDistance = Geometry.getBallRadius() + Geometry.getBotRadius()
				+ raySize;
		
		// checking free line
		final double distanceStartEndSquared = GeoMath.distancePPSqr(start, end);
		for (final ITrackedBot bot : botsToCheck)
		{
			final double distanceBotStartSquared = GeoMath.distancePPSqr(bot.getPos(), start);
			final double distanceBotEndSquared = GeoMath.distancePPSqr(bot.getPos(), end);
			if ((distanceStartEndSquared > distanceBotStartSquared) && (distanceStartEndSquared > distanceBotEndSquared))
			{
				// only check those bots that possibly can be in between start and end
				final double distanceBotLine = GeoMath.distancePL(bot.getPos(), start, end);
				if (distanceBotLine < minDistance)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * Finds all bots within reach blocking a beam between to points.
	 * Ray looks like this:
	 * 
	 * <pre>
	 * | * |
	 * |   |
	 * |   |
	 * | * |
	 * </pre>
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreIds
	 * @return A list of bots between the two points
	 * @author JulianT
	 */
	public static List<ITrackedBot> findBlockingBots(final SimpleWorldFrame wf, final IVector2 start,
			final IVector2 end,
			final double raySize, final Collection<BotID> ignoreIds)
	{
		List<ITrackedBot> blockingBots = new ArrayList<>();
		final double minDistance = Geometry.getBallRadius() + Geometry.getBotRadius()
				+ raySize;
		
		// checking free line
		final double distanceStartEndSquared = GeoMath.distancePPSqr(start, end);
		for (final ITrackedBot bot : wf.getBots().values())
		{
			if (ignoreIds.contains(bot.getBotId()))
			{
				continue;
			}
			final double distanceBotStartSquared = GeoMath.distancePPSqr(bot.getPos(), start);
			final double distanceBotEndSquared = GeoMath.distancePPSqr(bot.getPos(), end);
			if ((distanceStartEndSquared > distanceBotStartSquared) && (distanceStartEndSquared > distanceBotEndSquared))
			{
				// only check those bots that possibly can be in between start and end
				final double distanceBotLine = GeoMath.distancePL(bot.getPos(), start, end);
				if (distanceBotLine < minDistance)
				{
					blockingBots.add(bot);
				}
			}
		}
		
		return blockingBots;
	}
	
	
	/**
	 * Checks if the beam between two points is blocked by ball or not.
	 * ray looks like this:
	 * 
	 * <pre>
	 * | * |
	 * |   |
	 * |   |
	 * | * |
	 * </pre>
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibilityBall(final SimpleWorldFrame wf, final IVector2 start, final IVector2 end,
			final double raySize)
	{
		final double minDistance = Geometry.getBallRadius() + Geometry.getBotRadius()
				+ raySize;
		
		IVector2 ballPos = wf.getBall().getPos();
		Rectangle rect = new Rectangle(start, end);
		if (!rect.isPointInShape(ballPos, Geometry.getBotRadius()))
		{
			return true;
		}
		double dist = GeoMath.distancePL(wf.getBall().getPos(), start, end);
		if (dist < minDistance)
		{
			return false;
		}
		
		// checking free line
		// final double distanceStartEndSquared = distancePPSqr(start, end);
		// IVector2 ballPos = wf.getBall().getPos();
		// final double distanceBotStartSquared = distancePPSqr(ballPos, start);
		// final double distanceBotEndSquared = distancePPSqr(ballPos, end);
		// if ((distanceStartEndSquared > distanceBotStartSquared) && (distanceStartEndSquared > distanceBotEndSquared))
		// {
		// // only check those bots that possibly can be in between start and end
		// final double distanceBotLine = distancePL(ballPos, start, end);
		// if (distanceBotLine < minDistance)
		// {
		// return false;
		// }
		// }
		
		return true;
	}
	
	
	/**
	 * Check p2pVisibility for ball and all bots expect those given
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibilityBotBall(final SimpleWorldFrame wf, final IVector2 start, final IVector2 end,
			final double raySize, final BotID... ignoreBotId)
	{
		if (!p2pVisibilityBall(wf, start, end, raySize))
		{
			return false;
		}
		return p2pVisibility(wf, start, end, raySize, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link AiMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final IVector2 end,
			final BotID... ignoreBotId)
	{
		return AiMath.p2pVisibility(wf, start, end, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link AiMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final IVector2 end,
			final Double raySize, final BotID... ignoreBotId)
	{
		return p2pVisibility(wf, start, end, raySize, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link AiMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreIds
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final IVector2 end,
			final List<BotID> ignoreIds)
	{
		return p2pVisibility(wf, start, end, 0, ignoreIds);
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
	 * @param wf
	 * @param start
	 * @param end
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final IVector2 end)
	{
		List<BotID> emptylist = new ArrayList<BotID>();
		return p2pVisibility(wf, start, end, emptylist);
	}
	
	
	/**
	 * Check if one of the end points is visible from start
	 * 
	 * @param wf
	 * @param start
	 * @param ends
	 * @param ignoreIds
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final List<IVector2> ends,
			final List<BotID> ignoreIds)
	{
		for (IVector2 end : ends)
		{
			if (p2pVisibility(wf, start, end, ignoreIds))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * direct copy of the p2pVisibility from the opencl code
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreIds
	 * @return 0-1, 0 means full visibility
	 */
	public static double p2pVisibilityVoting(final WorldFrame wf, final IVector2 start, final IVector2 end,
			final double raySize, final List<BotID> ignoreIds)
	{
		// double p2pVisibilityIgnore(Vector2* pStart, Vector2* pEnd, Bot* botsToCheck, uint8_t numBotsToCheckFrom,
		// uint8_t
		// numBotsToCheckTo, uint8_t ignoreID, coord raySize)
		// {
		double minDistance = Geometry.getBallRadius() + Geometry.getBotRadius() + raySize;
		List<ITrackedBot> bots = new ArrayList<>(wf.getBots().values());
		
		// checking free line
		double sum = 0;
		for (ITrackedBot bot : bots)
		{
			if (ignoreIds.contains(bot.getBotId()))
			{
				continue;
			}
			// Bot bot = botsToCheck[i];
			Vector2 leadPoint = GeoMath.leadPointOnLine(bot.getPos(), start, end);
			// leadPointOnLine(&(bot.pos), pStart, pEnd, &leadPoint);
			Vector2 startToLead = leadPoint.subtract(start);
			// vec_sub(&leadPoint, pStart, &startToLead);
			Vector2 startToEnd = end.subtractNew(start);
			// vec_sub(pEnd, pStart, &startToEnd);
			if (((startToLead.x() / startToEnd.x()) > 0) && ((startToLead.x() / startToEnd.x()) < 1))
			{
				// only check those bots that possibly can be in between start and end
				double distanceBotLine = GeoMath.distancePL(bot.getPos(), start, end);
				sum += Math.max(0.0f, (minDistance - distanceBotLine) / minDistance);
			}
		}
		
		return sum;
	}
}
