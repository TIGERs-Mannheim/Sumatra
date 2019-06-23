/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.interfaces.IPointChecker;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;


/**
 * Helper class for providing common math problems.
 * 
 * @author stei_ol
 * 
 */
public final class AiMath
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// Logger
	private static final Logger	log	= Logger.getLogger(AiMath.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private AiMath()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Convert a bot-local vector to the equivalent global one.
	 * 
	 * @param local Bot-local vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned global vector
	 * 
	 * @author AndreR
	 */
	public static Vector2 convertLocalBotVector2Global(IVector2 local, float wpAngle)
	{
		return local.turnNew(-AngleMath.PI_HALF + wpAngle);
	}
	
	
	/**
	 * Convert a global vector to a bot-local one
	 * 
	 * @param global Global vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned local vector
	 * 
	 * @author AndreR
	 */
	public static Vector2 convertGlobalBotVector2Local(IVector2 global, float wpAngle)
	{
		return global.turnNew(AngleMath.PI_HALF - wpAngle);
	}
	
	
	/**
	 * Convert a global bot angle to a bot-local one
	 * 
	 * @param angle global angle
	 * @return local angle
	 * 
	 * @author AndreR
	 */
	public static float convertGlobalBotAngle2Local(float angle)
	{
		return AngleMath.PI_HALF - angle;
	}
	
	
	/**
	 * Convert a local bot angle to a global one
	 * 
	 * @param angle local angle
	 * @return global angle
	 * 
	 * @author AndreR
	 */
	public static float convertLocalBotAngle2Global(float angle)
	{
		return -AngleMath.PI_HALF + angle;
	}
	
	
	/**
	 * Finds the nearest bot to a given position (p).
	 * 
	 * @param botMap
	 * @param p
	 * @return
	 * @author Oliver Steinbrecher
	 */
	public static TrackedBot getNearestBot(IBotIDMap<TrackedBot> botMap, IVector2 p)
	{
		float distance = Float.MAX_VALUE;
		TrackedBot result = null;
		if (botMap.size() < 1)
		{
			log.warn("Input list in #getNearestBot has no elements!");
			return null;
		}
		for (final TrackedBot bot : botMap.values())
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
	public static List<BotID> getTigerBotsNearestToPointSorted(AIInfoFrame aiFrame, IVector2 pos)
	{
		final TreeMap<Float, BotID> botToBallDists = new TreeMap<Float, BotID>();
		for (final Map.Entry<BotID, TrackedTigerBot> bot : aiFrame.worldFrame.tigerBotsAvailable)
		{
			final float cost = GeoMath.distancePP(bot.getValue().getPos(), pos);
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
	public static List<BotID> getFoeBotsNearestToPointSorted(AIInfoFrame aiFrame, IVector2 pos)
	{
		// sry for duplicate code... this BotIDMap sucks...
		final TreeMap<Float, BotID> botToBallDists = new TreeMap<Float, BotID>();
		for (final Map.Entry<BotID, TrackedBot> bot : aiFrame.worldFrame.foeBots)
		{
			final float cost = GeoMath.distancePP(bot.getValue().getPos(), pos);
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
	public static IVector2 normalizeIntoField(IVector2 pos)
	{
		final float border = AIConfig.getGeometry().getBotRadius();
		final float fieldMaxY = (AIConfig.getGeometry().getFieldWidth() / 2) - border;
		final float fieldMaxX = (AIConfig.getGeometry().getFieldLength() / 2) - border;
		
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
	public static BotID getReceiverInEnemyHalf(AIInfoFrame aiFrame, IBotIDMap<TrackedTigerBot> bots)
	{
		SortedMap<Float, BotID> sortedDists = new ConcurrentSkipListMap<Float, BotID>();
		for (TrackedTigerBot bot : bots.values())
		{
			if (bot.getPos().x() > 0)
			{
				float distance = GeoMath.distancePP(bot, aiFrame.worldFrame.ball.getPos());
				Float[] sortedArray = new Float[sortedDists.size()];
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
						sortedDists.put(distance, bot.getId());
					}
				} else
				{
					sortedDists.put(distance, bot.getId());
				}
			}
		}
		if ((sortedDists.size() <= 1) && (aiFrame.worldFrame.ball.getPos().x() > 0))
		{
			return null;
		}
		if (sortedDists.size() == 0)
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
	public static BotID getReceiver(AIInfoFrame aiFrame, IBotIDMap<TrackedTigerBot> botSelection)
	{
		IVector2 ballPos = aiFrame.worldFrame.ball.getPos();
		IVector2 goalCenter = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		// 1. determine nearest bots to ball we will not use the nearest, as this is our passer
		List<BotID> botsNearestToBallSorted = AiMath.getTigerBotsNearestToPointSorted(aiFrame,
				aiFrame.worldFrame.ball.getPos());
		if (botsNearestToBallSorted.isEmpty())
		{
			return null;
		}
		BotID potShooter = botsNearestToBallSorted.get(0);
		
		List<BotID> botsNearestToGoalSorted = AiMath.getTigerBotsNearestToPointSorted(aiFrame, AIConfig.getGeometry()
				.getGoalTheir().getGoalCenter());
		botsNearestToGoalSorted.remove(potShooter);
		
		// 2. determine a list of potential bots that the passer can pass to.
		// also map them with the distance to the goal
		TreeMap<Float, BotID> potBots = new TreeMap<Float, BotID>();
		for (BotID botID : botsNearestToGoalSorted)
		{
			if (!botSelection.containsKey(botID))
			{
				continue;
			}
			ABot bot = botSelection.get(botID).getBot();
			if ((bot != null) && (bot.getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.WORKING)
					&& (bot.getBotFeatures().get(EFeature.CHIP_KICKER) != EFeatureState.WORKING))
			{
				continue;
			}
			// second nearest to ball
			IVector2 potReceiverPos = aiFrame.worldFrame.tigerBotsVisible.get(botID).getPos();
			// can passer shoot to receiver?
			if (GeoMath.p2pVisibility(aiFrame.worldFrame, ballPos, potReceiverPos, potShooter))
			{
				potBots.put(GeoMath.distancePP(potReceiverPos, goalCenter), botID);
			}
		}
		
		// 3. does receiver has free look to opponent goal?
		// in the loop, the bot with smallest distance to goal will come first
		for (BotID botID : potBots.values())
		{
			final IVector2 receiverPos = aiFrame.worldFrame.tigerBotsVisible.get(botID).getPos();
			
			// filter bots in our field as they are potentially dangerous receivers
			if (receiverPos.x() < 0)
			{
				continue;
			}
			
			// check visibility of goal
			List<BotID> ignoreBotIDs = new ArrayList<BotID>(1);
			ignoreBotIDs.add(botID);
			ignoreBotIDs.add(potShooter);
			if (isGoalVisible(aiFrame.worldFrame, AIConfig.getGeometry().getGoalTheir(), receiverPos, ignoreBotIDs))
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
	public static BotIDMap<TrackedTigerBot> getOtherBots(AIInfoFrame aiFrame)
	{
		BotIDMap<TrackedTigerBot> otherBots = new BotIDMap<TrackedTigerBot>(aiFrame.worldFrame.tigerBotsVisible);
		for (BotID botID : aiFrame.worldFrame.tigerBotsAvailable.keySet())
		{
			otherBots.remove(botID);
		}
		return otherBots;
	}
	
	
	/**
	 * Calculates the position of the dribbler/kicker of the given bot.
	 * Use this position for ball receivers, etc.
	 * 
	 * @param bot
	 * @return
	 */
	public static IVector2 getBotKickerPos(TrackedTigerBot bot)
	{
		return getBotKickerPos(bot.getPos(), bot.getAngle());
	}
	
	
	/**
	 * Calculates the position of the dribbler/kicker depending on bot position and orientation (angle)
	 * 
	 * @param botPos
	 * @param orientation
	 * @return
	 */
	public static IVector2 getBotKickerPos(IVector2 botPos, float orientation)
	{
		
		return botPos.addNew(new Vector2(orientation).scaleTo(AIConfig.getGeometry().getBotCenterToDribblerDist()));
	}
	
	
	/**
	 * Get the initial bot kicker position for the dynamic IndirectShot.
	 * 
	 * @param botPos
	 * @return
	 */
	public static IVector2 getBotKickerPosDynamic(IVector2 botPos)
	{
		return new Vector2(-botPos.x(), (botPos.y() + (AIConfig.getGeometry().getGoalTheir().getGoalCenter()
				.subtractNew(botPos).y() / 2)));
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
	public static boolean isGoalVisible(WorldFrame wf, Goal goal, IVector2 start, List<BotID> ignoreIds)
	{
		float starty = Math.max(goal.getGoalPostLeft().y(), goal.getGoalPostRight().y());
		float length = Math.abs(goal.getGoalPostLeft().y()) + Math.abs(goal.getGoalPostRight().y());
		List<IVector2> goalLine = new ArrayList<IVector2>((int) length / 50);
		for (int i = 0; i < (length / 50); i++)
		{
			goalLine.add(new Vector2(goal.getGoalCenter().x(), starty + ((i / (length / 50)) * length)));
		}
		return GeoMath.p2pVisibility(wf, start, goalLine, ignoreIds);
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
	public static IVector2 findBestPoint(IVector2 center, IVector2 start, IPointChecker pointChecker, int rounds)
	{
		float radius = GeoMath.distancePP(start, center);
		IVector2 current = start;
		for (int i = 1; i <= rounds; i++)
		{
			current = (GeoMath.stepAlongLine(center, current, radius * i));
			for (float angle = 0; angle < AngleMath.PI_TWO; angle += AngleMath.PI_QUART / ((i / 3) + 1))
			{
				IVector2 pos = GeoMath.stepAlongCircle(current, center, angle);
				if (pointChecker.checkPoint(pos))
				{
					return pos;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * @author Mark Geiger
	 * 
	 * @param wf World Frame
	 * @param ignoreEnemyBotDistance bots to near to the shooter bot get ignored
	 * @param shooterPosition position of the "shooting Bot"
	 * @param chipLandingSpotX this value says, where the ball should hit the ground ( just X value )
	 * @return the best spot to Chip Kick to, to score a Goal and the chance to make this goal
	 */
	public static ValuePoint determinChipShotTarget(WorldFrame wf, float ignoreEnemyBotDistance,
			IVector2 shooterPosition, int chipLandingSpotX)
	{
		final int numberOfIterations = 30;
		int chanceChecker = numberOfIterations + 1;
		
		int seriesStartBest = 0;
		int seriesStartNow = 0;
		int seriesSizeBest = 0;
		int seriesSizeNow = 0;
		
		float enemyGoalSize = AIConfig.getGeometry().getGoalTheir().getSize();
		Vector2f enemyGoalRight = AIConfig.getGeometry().getGoalTheir().getGoalPostRight();
		
		for (int i = 0; i <= numberOfIterations; i++)
		{
			Vector2 checkingPoint = new Vector2(enemyGoalRight.subtractNew(new Vector2(0,
					(-enemyGoalSize / numberOfIterations) * i)));
			
			List<BotID> ignoredBots = new ArrayList<BotID>();
			for (Entry<BotID, TrackedBot> foeBot : wf.foeBots)
			{
				if (GeoMath.distancePP(shooterPosition, foeBot.getValue().getPos()) < ignoreEnemyBotDistance)
				{
					ignoredBots.add(foeBot.getKey());
				}
			}
			
			float raySize = (((enemyGoalSize / numberOfIterations) / 5));
			boolean freeLine = GeoMath.p2pVisibility(wf, shooterPosition, checkingPoint, raySize, ignoredBots);
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
			// So return zero Vector.
			return new ValuePoint(new Vector2(), 0);
		}
		if (seriesSizeBest <= seriesSizeNow)
		{
			seriesStartBest = seriesStartNow;
			seriesSizeBest = seriesSizeNow;
		}
		float rayDistance = enemyGoalSize / numberOfIterations;
		Vector2 target = new Vector2(enemyGoalRight.subtractNew(new Vector2(0, (-rayDistance * seriesStartBest)
				- ((rayDistance * seriesSizeBest) / 2))));
		
		Line line = new Line(target, shooterPosition.subtractNew(target));
		
		float y = 0;
		float x = chipLandingSpotX;
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
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
