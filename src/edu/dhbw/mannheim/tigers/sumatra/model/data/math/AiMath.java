/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.EFunction;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.interfaces.IPointChecker;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.CatchBallInput;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support.SupportRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.CatchBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.SimpleTimeAwareBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Helper class for providing common math problems.
 * 
 * @author stei_ol
 */
public final class AiMath
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// Logger
	private static final Logger			log										= Logger.getLogger(AiMath.class.getName());
	
	private static final float				MAX_DIST_TO_BALL_DEST_LINE			= 500;
	/** weight in the value point for distance between ball-dest-line and foe bot. higher value= less weight */
	private static final int				DIST_BALL_DEST_LINE_WEIGHT			= 5;
	
	private static final float				APPROX_ORIENT_BALL_DAMP_ACCURACY	= 0.005f;
	
	private static final int				APPROX_ORIENT_BALL_DAMP_MAX_ITER	= 100;
	
	private static final float				BALL_DAMP_FACTOR						= 0.004f;
	
	private static final float				REDIRECT_MAX_DIST_DIFF				= 1000;
	
	private static final float				securityDistBotBall					= 100;
	
	@Configurable
	private static float						minDirectShootScoreChange			= 0.3f;
	
	@Configurable(comment = "Assumed inaccuracy per distance")
	private static float						shootSpread								= 1 / 10f;
	
	@Configurable(comment = "Scaling of distance between bot and goal")
	private static float						distanceScaling						= 0.2f;
	
	@Configurable(comment = "Distance from Bot in which ignore Enemy Bots by calculate ChipKick")
	private static float						ignoreEnemyBotChipKickDistance	= 1000;
	
	@Configurable(comment = "Which method to use for calculating redirect angle: FUN or PHY")
	private static ERedirectAngleMethod	redirectAngleMethod					= ERedirectAngleMethod.PHY;
	
	private enum ERedirectAngleMethod
	{
		/** approximated function */
		FUN,
		/** physical calculation */
		PHY
	}
	
	
	private AiMath()
	{
		
	}
	
	
	/**
	 * Convert a bot-local vector to the equivalent global one.
	 * 
	 * @param local Bot-local vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned global vector
	 * @author AndreR
	 */
	public static Vector2 convertLocalBotVector2Global(final IVector2 local, final float wpAngle)
	{
		return local.turnNew(-AngleMath.PI_HALF + wpAngle);
	}
	
	
	/**
	 * Convert a global vector to a bot-local one
	 * 
	 * @param global Global vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned local vector
	 * @author AndreR
	 */
	public static Vector2 convertGlobalBotVector2Local(final IVector2 global, final float wpAngle)
	{
		return global.turnNew(AngleMath.PI_HALF - wpAngle);
	}
	
	
	/**
	 * Convert a global bot angle to a bot-local one
	 * 
	 * @param angle global angle
	 * @return local angle
	 * @author AndreR
	 */
	public static float convertGlobalBotAngle2Local(final float angle)
	{
		return AngleMath.PI_HALF - angle;
	}
	
	
	/**
	 * Convert a local bot angle to a global one
	 * 
	 * @param angle local angle
	 * @return global angle
	 * @author AndreR
	 */
	public static float convertLocalBotAngle2Global(final float angle)
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
	public static TrackedTigerBot getNearestBot(final IBotIDMap<TrackedTigerBot> botMap, final IVector2 p)
	{
		float distance = Float.MAX_VALUE;
		TrackedTigerBot result = null;
		if (botMap.size() < 1)
		{
			throw new IllegalArgumentException("Empty input list");
		}
		for (final TrackedTigerBot bot : botMap.values())
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
		final TreeMap<Float, BotID> botToBallDists = new TreeMap<Float, BotID>();
		for (final Map.Entry<BotID, TrackedTigerBot> bot : aiFrame.getWorldFrame().tigerBotsAvailable)
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
	public static List<BotID> getFoeBotsNearestToPointSorted(final AthenaAiFrame aiFrame, final IVector2 pos)
	{
		// sry for duplicate code... this BotIDMap sucks...
		final TreeMap<Float, BotID> botToBallDists = new TreeMap<Float, BotID>();
		for (final Map.Entry<BotID, TrackedTigerBot> bot : aiFrame.getWorldFrame().foeBots)
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
	public static IVector2 normalizeIntoField(final IVector2 pos)
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
	public static BotID getReceiverInEnemyHalf(final AthenaAiFrame aiFrame, final IBotIDMap<TrackedTigerBot> bots)
	{
		SortedMap<Float, BotID> sortedDists = new ConcurrentSkipListMap<Float, BotID>();
		for (TrackedTigerBot bot : bots.values())
		{
			if (bot.getPos().x() > 0)
			{
				float distance = GeoMath.distancePP(bot, aiFrame.getWorldFrame().getBall().getPos());
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
	public static BotID getReceiver(final AthenaAiFrame aiFrame, final IBotIDMap<TrackedTigerBot> botSelection)
	{
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		IVector2 goalCenter = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		// 1. determine nearest bots to ball we will not use the nearest, as this is our passer
		List<BotID> botsNearestToBallSorted = AiMath.getTigerBotsNearestToPointSorted(aiFrame,
				aiFrame.getWorldFrame().getBall().getPos());
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
			IVector2 potReceiverPos = aiFrame.getWorldFrame().tigerBotsVisible.get(botID).getPos();
			// can passer shoot to receiver?
			if (GeoMath.p2pVisibility(aiFrame.getWorldFrame(), ballPos, potReceiverPos, potShooter))
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
			if (isGoalVisible(aiFrame.getWorldFrame(), AIConfig.getGeometry().getGoalTheir(), receiverPos, ignoreBotIDs))
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
	public static BotIDMap<TrackedTigerBot> getOtherBots(final BaseAiFrame aiFrame)
	{
		BotIDMap<TrackedTigerBot> otherBots = new BotIDMap<TrackedTigerBot>(aiFrame.getWorldFrame().tigerBotsVisible);
		for (BotID botID : aiFrame.getWorldFrame().tigerBotsAvailable.keySet())
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
	public static IVector2 getBotKickerPos(final TrackedTigerBot bot)
	{
		if (bot.getBot() != null)
		{
			return getBotKickerPos(bot.getPos(), bot.getAngle(), bot.getBot().getCenter2DribblerDist());
		}
		log.warn("TrackedTigerBot has no ABot assigned. Guess center2DribblerDist");
		return getBotKickerPos(bot.getPos(), bot.getAngle(), 75);
	}
	
	
	/**
	 * Calculates the position of the dribbler/kicker depending on bot position and orientation (angle)
	 * 
	 * @param botPos
	 * @param orientation
	 * @param center2Dribbler
	 * @return
	 */
	public static IVector2 getBotKickerPos(final IVector2 botPos, final float orientation, final float center2Dribbler)
	{
		
		return botPos.addNew(new Vector2(orientation).scaleTo(center2Dribbler));
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
	public static IVector2 findBestPoint(final IVector2 center, final IVector2 start, final IPointChecker pointChecker,
			final int rounds)
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
	 * @author Mark Geiger
	 * @param wf World Frame
	 * @param ignoreEnemyBotDistance bots to near to the shooter bot get ignored
	 * @param chipLandingSpotX this value says, where the ball should hit the ground ( just X value )
	 * @return the best spot to Chip Kick to, to score a Goal and the chance to make this goal = Zero Vector if there is
	 *         no target.
	 */
	public static ValuePoint determineChipShotTarget(final WorldFrame wf, final float ignoreEnemyBotDistance,
			final float chipLandingSpotX)
	{
		final int numberOfIterations = 30;
		int chanceChecker = numberOfIterations + 1;
		IVector2 shooterPosition = wf.getBall().getPos();
		
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
			for (Entry<BotID, TrackedTigerBot> foeBot : wf.foeBots)
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
			// So return null
			return null;
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
	
	
	/**
	 * @author Mark Geiger
	 * @param wf World Frame
	 * @param chipKick get Score for ChipKick
	 * @return true -> Bot will shoot directly on the goal, false -> Bot will pass to another Bot
	 */
	public static boolean willBotShoot(final WorldFrame wf, final boolean chipKick)
	{
		if (ProbabilityMath.getDirectShootScoreChanceNew(wf, wf.getBall().getPos(), chipKick) > 0.30)
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
	public static float getScoreForStraightShot(final WorldFrame wFrame, final IVector2 origin, final IVector2 target)
	{
		float value = 0;
		// will check if there are points on the enemys goal, not being blocked by bots.
		if (GeoMath.p2pVisibility(wFrame, origin, target, (float) ((AIConfig.getGeometry().getBallRadius() * 2) + 0.1)))
		{
			// free visibility
			value = 0;
		} else
		{
			value = 0.5f;
		}
		Collection<TrackedTigerBot> allBots = new ArrayList<TrackedTigerBot>(wFrame.foeBots.values());
		allBots.addAll(wFrame.tigerBotsVisible.values());
		float ownDist = GeoMath.distancePP(origin, target);
		for (final TrackedTigerBot bot : allBots)
		{
			float enemyDist = GeoMath.distancePP(bot.getPos(), target);
			if (enemyDist < ownDist)
			{
				// evaluate the generated points: If the view to a point is unblocked the function
				// will get 100 points. Afterwards the distance between the defender and the line between
				// start and target will be added as 1/6000
				float relDist = (GeoMath.distancePL(bot.getPos(), origin, target) / MAX_DIST_TO_BALL_DEST_LINE);
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
		
		if (value > 1f)
		{
			value = 1;
		} else if (value < 0)
		{
			value = 0;
		}
		
		return value;
	}
	
	
	/**
	 * calculate a speed vector that results from damping the ball against the
	 * kicker of a bot.
	 * 
	 * @param shootSpeed should be combination of direction and speed (length of vector)
	 * @param incomingSpeedVec vector representing direction and speed (length) of ball
	 * @param ballDampFactor how much the ball is damped when hitting the kicker
	 * @return direction vector the ball will go to; length of vector = speed
	 */
	public static IVector2 ballDamp(final IVector2 shootSpeed, final IVector2 incomingSpeedVec,
			final float ballDampFactor)
	{
		// inside angle equal to outside angle
		IVector2 vec1 = incomingSpeedVec.multiplyNew(-1);
		float diff2 = AngleMath.difference(vec1.getAngle(), shootSpeed.getAngle());
		Vector2 outVec = new Vector2(vec1.getAngle() - (diff2 * 2)).scaleTo(incomingSpeedVec.getLength2());
		
		// ball is damped by hit
		outVec.multiply(1 - ballDampFactor);
		
		// ball is drilled to shoot direction
		IVector2 dampVec = shootSpeed;
		outVec.add(dampVec);
		
		return outVec;
	}
	
	
	/**
	 * Approximate the orientation of the bot that is needed to kick a ball that comes
	 * with incomingSpeedVec in targetAngle direction when kicking with shootSpeed.
	 * 
	 * @param shootSpeed velocity of the kicker
	 * @param incomingSpeedVec vector with direction and speed of the incoming ball
	 * @param initialOrientation where to start with approximation, e.g. current bot (target) orientation
	 * @param targetAngle angle of the vector from position of bot to shoot target (should be normalized!)
	 * @param ballDampFactor how much the ball is damped when hitting the kicker
	 * @return
	 */
	public static float approxOrientationBallDamp(final float shootSpeed, final IVector2 incomingSpeedVec,
			final float initialOrientation,
			final float targetAngle, final float ballDampFactor)
	{
		float destAngle = initialOrientation;
		for (int i = 0; i < APPROX_ORIENT_BALL_DAMP_MAX_ITER; i++)
		{
			IVector2 vShootSpeed = new Vector2(destAngle).scaleTo(shootSpeed);
			IVector2 outVec = ballDamp(vShootSpeed, incomingSpeedVec, ballDampFactor);
			float diff = targetAngle - outVec.getAngle();
			if (Math.abs(diff) < APPROX_ORIENT_BALL_DAMP_ACCURACY)
			{
				break;
			}
			destAngle = AngleMath.normalizeAngle(destAngle + diff);
		}
		return destAngle;
	}
	
	
	/**
	 * Calculate the required orientation of the bot in order to correctly redirect the ball.
	 * This uses an approximation method, so the performance is not that good...
	 * Use {@link AiMath#calcRedirectOrientationSimple(IVector2, IVector2, IVector2)} if you do not need precise results!
	 * 
	 * @param kickerPos Current or desired kicker position
	 * @param approxOrientation This orientation is used as starting value for approximating the target angle
	 * @param ballVel ball velocity on hit
	 * @param shootTarget The shoot target where the bot should redirect the ball to
	 * @param shootSpeed shoot speed of the redirector
	 * @return
	 */
	public static float calcRedirectOrientation(final IVector2 kickerPos, final float approxOrientation,
			final IVector2 ballVel, final IVector2 shootTarget, final float shootSpeed)
	{
		assert shootSpeed > 0;
		float shootAngle = shootTarget.subtractNew(kickerPos).getAngle();
		if (ballVel.isZeroVector())
		{
			return shootAngle;
		}
		return approxOrientationBallDamp(shootSpeed, ballVel, approxOrientation, shootAngle, BALL_DAMP_FACTOR);
	}
	
	
	/**
	 * Calc target orientation with simple bisection
	 * 
	 * @param pos
	 * @param senderPos
	 * @param shootTarget
	 * @return
	 */
	public static float calcRedirectOrientationSimple(final IVector2 pos, final IVector2 senderPos,
			final IVector2 shootTarget)
	{
		IVector2 targetAngleVec = GeoMath.calculateBisector(pos, senderPos, shootTarget);
		return targetAngleVec.getAngle();
	}
	
	
	/**
	 * Calculate redirect orientation with a function based approach. Default orientation is directly looking to the
	 * target,
	 * depending on speeds and angles, a function should calculate a correction angle
	 * 
	 * @param pos
	 * @param senderPos
	 * @param shootTarget
	 * @param ballSpeed
	 * @param shootSpeed
	 * @return
	 */
	public static float calcRedirectOrientationFunctionBased(final IVector2 pos, final IVector2 senderPos,
			final IVector2 shootTarget, final float ballSpeed, final float shootSpeed)
	{
		IVector2 shootDir = shootTarget.subtractNew(pos);
		IVector2 senderDir = senderPos.subtractNew(pos);
		float angle = GeoMath.angleBetweenVectorAndVectorWithNegative(shootDir, senderDir);
		if (angle > AngleMath.PI_HALF)
		{
			return shootDir.getAngle();
		}
		IFunction1D fn = new IFunction1D()
		{
			@Override
			public List<Float> getParameters()
			{
				return null;
			}
			
			
			@Override
			public EFunction getIdentifier()
			{
				return null;
			}
			
			
			@Override
			public float eval(final float... x)
			{
				float p00 = 0.8716f;
				float p10 = -1.195f;
				float p01 = -0.1944f;
				float p20 = 0.3418f;
				float p11 = 0.4987f;
				float p02 = 0.08771f;
				return p00 + (p10 * x[0]) + (p01 * x[1]) + (p20 * x[0] * x[0]) + (p11 * x[0] * x[1]) + (p02 * x[1]);
			}
		};
		float bs = 0;
		// float bs = ballSpeed;
		float rotation = Math.signum(angle) * fn.eval(Math.abs(angle), bs, shootSpeed);
		// System.out.println(angle + " " + ballSpeed + " -> " + rotation);
		return AngleMath.normalizeAngle(shootDir.getAngle() + rotation);
	}
	
	
	/**
	 * Calculate destination and targetOrientation (z-component) for redirecting the ball.
	 * Low ballSpeeds are considered.
	 * 
	 * @param bot
	 * @param ball
	 * @param shootTarget Where should the redirector shoot to
	 * @param shootSpeed how fast will the redirector shoot?
	 * @return
	 */
	public static IVector3 calcRedirectPositions(final TrackedTigerBot bot, final TrackedBall ball,
			final IVector2 shootTarget, final float shootSpeed)
	{
		return calcRedirectPositions(bot, bot.getPos(), bot.getAngle(), ball, shootTarget, shootSpeed);
	}
	
	
	/**
	 * Calculate destination and targetOrientation (z-component) for redirecting the ball.
	 * Low ballSpeeds are considered.
	 * 
	 * @param bot
	 * @param botDesPos Bot destination or current position
	 * @param botDesAngle Target or current orientation
	 * @param ball
	 * @param shootTarget Where should the redirector shoot to
	 * @param shootSpeed how fast will the redirector shoot?
	 * @return
	 */
	public static IVector3 calcRedirectPositions(final TrackedTigerBot bot, final IVector2 botDesPos,
			final float botDesAngle,
			final TrackedBall ball,
			final IVector2 shootTarget, final float shootSpeed)
	{
		float center2DribblerDist = bot.getBot().getCenter2DribblerDist();
		IVector2 kickerPos;
		ILine ballTravelLine;
		if (ball.getVel().getLength2() > 0.2f)
		{
			kickerPos = getBotKickerPos(bot);
			ballTravelLine = new Line(ball.getPos(), ball.getVel());
			IVector2 lp = GeoMath.leadPointOnLine(bot.getPos(), ballTravelLine);
			if (GeoMath.distancePP(lp, bot.getPos()) > REDIRECT_MAX_DIST_DIFF)
			{
				kickerPos = getBotKickerPos(botDesPos, botDesAngle, center2DribblerDist);
				ballTravelLine = Line.newLine(ball.getPos(), kickerPos);
			}
		} else
		{
			kickerPos = getBotKickerPos(botDesPos, botDesAngle, center2DribblerDist);
			ballTravelLine = Line.newLine(ball.getPos(), kickerPos);
		}
		IVector2 leadPoint = GeoMath.leadPointOnLine(kickerPos, ballTravelLine);
		float approxOrientation = botDesAngle;
		float targetOrientation;
		switch (redirectAngleMethod)
		{
			case PHY:
				targetOrientation = calcRedirectOrientation(leadPoint, approxOrientation, ball.getVel(), shootTarget,
						shootSpeed);
				break;
			case FUN:
				targetOrientation = calcRedirectOrientationFunctionBased(leadPoint, ball.getPos(), shootTarget, ball
						.getVel().getLength2(), shootSpeed);
				break;
			default:
				throw new IllegalStateException();
		}
		
		// float
		IVector2 dir = new Vector2(targetOrientation).scaleTo(-center2DribblerDist);
		IVector2 dest = leadPoint.addNew(dir);
		
		// filter high changes
		if (GeoMath.distancePP(botDesPos, dest) > REDIRECT_MAX_DIST_DIFF)
		{
			dest = botDesPos;
		}
		if (Math.abs(targetOrientation - botDesAngle) > AngleMath.PI_HALF)
		{
			targetOrientation = botDesAngle;
		}
		
		IVector3 positions = new Vector3(dest.x(), dest.y(), targetOrientation);
		
		
		// DebugShapeHacker.addDebugShape(new DrawablePoint(kickerPos, Color.red));
		// DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(leadPoint, 70), Color.magenta));
		// DebugShapeHacker.addDebugShape(new DrawableLine(Line.newLine(ball.getPos(), leadPoint), Color.blue));
		// DebugShapeHacker.addDebugShape(new DrawableLine(Line.newLine(leadPoint, shootTarget), Color.blue));
		// DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(dest, 50), Color.black));
		// DebugShapeHacker.addDebugShape(new DrawableLine(new Line(bot.getPos(), new Vector2(targetOrientation)
		// .scaleTo(100)),
		// Color.black));
		//
		// if (!leadPoint.equals(kickerPos))
		// {
		// DebugShapeHacker.addDebugShape(new DrawableLine(Line.newLine(kickerPos, leadPoint), Color.yellow));
		// }
		
		return positions;
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
			float tolerance = AIConfig.getGeometry().getBotRadius() + AIConfig.getGeometry().getBallRadius()
					+ securityDistBotBall;
			try
			{
				IVector2 ballPosFuture = wFrame.getWorldFramePrediction().getBall().getPosAt(0.5f);
				Line line = new Line(wFrame.getBall().getPos(), ballPosFuture.subtractNew(wFrame.getBall().getPos()));
				IVector2 rv = line.directionVector();
				rv = rv.normalizeNew();
				
				
				ballPosFuture = position.addNew(rv.multiplyNew(tolerance));
				return ballPosFuture;
				
			} catch (IllegalArgumentException e)
			{
				IVector2 destination = null;
				IVector2 ballPos = wFrame.getBall().getPos();
				IVector2 target = AiMath.determineChipShotTarget(wFrame, 0, AIConfig.getGeometry().getGoalTheir()
						.getGoalCenter().x());
				if (target == null)
				{
					target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
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
		float speedTolerance = 0.2f;
		for (TrackedTigerBot bot : wFrame.getBots().values())
		{
			if (bot.getId().getTeamColor() != botID.getTeamColor())
			{
				continue;
			}
			if (!bot.getId().equals(botID) && (bot.getVel().getLength2() < speedTolerance))
			{
				float tolerance = (AIConfig.getGeometry().getBotRadius() * 2) - 20;
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
		if (!GeoMath.isInsideField(dest))
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
				penArea = AIConfig.getGeometry().getPenaltyAreaTheir();
			}
			if (i == 1)
			{
				penArea = AIConfig.getGeometry().getPenaltyAreaOur();
			}
			if (penArea != null)
			{
				if (penArea.isPointInShape(dest, AIConfig.getGeometry().getBotRadius()))
				{
					IVector2 botPos = wFrame.getBot(botID).getPos();
					// behind penArea?
					if (botPos.x() <= (-AIConfig.getGeometry().getFieldLength() / 2))
					{
						// this will result in an acceptable new destination
						botPos = AVector2.ZERO_VECTOR;
					}
					IVector2 nearestPointOutside = penArea.nearestPointOutside(dest, botPos, AIConfig.getGeometry()
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
		float tolerance = AIConfig.getGeometry().getBotRadius() + AIConfig.getGeometry().getBallRadius()
				+ securityDistBotBall;
		
		IVector2 ballPos = wFrame.getBall().getPos();
		
		float distancePositionToBall = GeoMath.distancePP(ballPos, position);
		
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
	public static boolean isShapeFreeOfBots(final I2DShape shape, final IBotIDMap<TrackedTigerBot> bots,
			final TrackedTigerBot ignoredBot)
	{
		for (TrackedTigerBot bot : bots.values())
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
	 * Find a random point drawn from Gaussian distribution that lies on the ball traveling line (now till ball stops)
	 * mu should be a rough estimation, e.g. the last point.
	 * 
	 * @param ball
	 * @param mu result will be centered around this point
	 * @param sigma standard deviation for Gaussian distribution
	 * @param rnd
	 * @return
	 */
	public static IVector2 getRandomPointOnBallTravelLine(final TrackedBall ball, final IVector2 mu, final float sigma,
			final Random rnd)
	{
		IVector2 rndRangeStart = ball.getPos();
		IVector2 rndRangeEnd = ball.getPosByVel(0);
		float lastDist = GeoMath.distancePP(rndRangeStart, mu);
		float gaussDist = lastDist + ((float) rnd.nextGaussian() * sigma);
		float stepDist = Math.max(0, Math.min(gaussDist, GeoMath.distancePP(rndRangeStart, rndRangeEnd)));
		IVector2 rndDest = GeoMath.stepAlongLine(rndRangeStart, rndRangeEnd, stepDist);
		return rndDest;
	}
	
	
	/**
	 * Calculate the angle between the redirect orientation of the robot and the ball vector.
	 * This is not the angle of the robot!
	 * It is useful for checking if this angle is smaller than 90deg or sth.,
	 * so that the robot can actually redirect the ball
	 * 
	 * @param kickerPos
	 * @param receiver
	 * @param vBall
	 * @param shootSpeed
	 * @return
	 */
	public static float calcRedirectAngle(final IVector2 kickerPos, final IVector2 receiver, final IVector2 vBall,
			final float shootSpeed)
	{
		float approxOrient = 0;
		if (!kickerPos.equals(receiver))
		{
			approxOrient = receiver.subtractNew(kickerPos).getAngle();
		}
		
		float targetAngle = calcRedirectOrientation(kickerPos, approxOrient, vBall, receiver, shootSpeed);
		float ballAngle;
		if (vBall.isZeroVector())
		{
			ballAngle = targetAngle;
		} else
		{
			ballAngle = AngleMath.normalizeAngle(vBall.getAngle() + AngleMath.PI);
		}
		float redirectAngle = Math.abs(AngleMath.difference(ballAngle, targetAngle));
		return redirectAngle;
	}
	
	
	/**
	 * @param input
	 * @return
	 */
	public static TrajPath catchBallTrajPath(final CatchBallInput input)
	{
		TrackedTigerBot bot = input.getBot();
		TrackedBall ball = input.getBall();
		DynamicPosition receiver = input.getReceiver();
		List<IDrawableShape> shapes = input.getShapes();
		TrajPathFinder finder = bot.getBot().getPathFinder();
		IVector2 dest = input.getLastDest();
		TrajPath path = input.getLastPath();
		float timeTolerance = 0.3f;
		float morePreciseTol = 80;
		float fasterTol = 0.5f;
		float precisionTol = 500;
		float shootSpeed = 8;
		
		float toBallDist = (AIConfig.getGeometry().getBallRadius() + bot.getBot().getCenter2DribblerDist()) - 50;
		// List<IObstacle> obstacles = input.getObsGen().generateStaticObstacles();
		List<IObstacle> obstacles = new ArrayList<>(1);
		CatchBallObstacle catchBallObs = new CatchBallObstacle(ball, 0, receiver, shootSpeed);
		obstacles.add(catchBallObs);
		obstacles.add(new SimpleTimeAwareBallObstacle(ball, toBallDist));
		shapes.add(catchBallObs);
		
		// look at random position to find global minimum
		IVector2 shortestPoint;
		float sigma = input.getLastDist2Dest();
		if (ball.getVel().getLength2() > 0.5f)
		{
			shortestPoint = GeoMath.leadPointOnLine(bot.getPos(), ball.getPos(), ball.getPosByVel(0));
			float dist2ShortestPoint = GeoMath.distancePP(shortestPoint, bot.getPos());
			sigma = ((3 * dist2ShortestPoint) + sigma) / 4;
		} else
		{
			shortestPoint = GeoMath.stepAlongLine(ball.getPos(), receiver, -toBallDist);
		}
		IVector2 rndDest = getRandomPointOnBallTravelLine(ball, shortestPoint, sigma, input.getRnd());
		
		// calculate target orientation
		IVector2 vBallAtDest = ball.getVel().scaleToNew(ball.getVelByPos(rndDest));
		float approxOrient = 0;
		if (!rndDest.equals(receiver))
		{
			approxOrient = receiver.subtractNew(rndDest).getAngle();
		}
		float targetAngle = calcRedirectOrientation(rndDest, approxOrient, vBallAtDest, receiver, shootSpeed);
		
		IVector2 robotDest = rndDest.addNew(new Vector2(targetAngle).turn(AngleMath.PI).scaleTo(toBallDist));
		shapes.add(new DrawableBot(robotDest, targetAngle, Color.GRAY));
		
		input.getFinderInput().setTrackedBot(bot);
		input.getFinderInput().setDest(robotDest);
		input.getFinderInput().setTargetAngle(targetAngle);
		input.getFinderInput().setObstacles(obstacles);
		TrajPath rndPath = finder.calcPath(input.getFinderInput());
		if (rndPath != null)
		{
			float tBot = rndPath.getTotalTime();
			IVector2 pBall = ball.getPosByTime(tBot + timeTolerance);
			IVector2 vBall = ball.getVel().scaleToNew(ball.getVelByTime(tBot + timeTolerance));
			float distDest2Ball = GeoMath.distancePP(pBall, rndDest);
			if (input.isDebug())
			{
				DrawablePoint dP = new DrawablePoint(rndDest, Color.magenta);
				dP.setText(String.format("%.1f", distDest2Ball));
				shapes.add(dP);
			}
			
			float redirectAngle = calcRedirectAngle(rndDest, receiver, vBall, shootSpeed);
			
			boolean morePrecise = distDest2Ball < (input.getLastDist2Dest() - morePreciseTol);
			boolean faster = (path == null) || (tBot < (path.getTotalTime() - fasterTol));
			boolean preciseEnough = (distDest2Ball < precisionTol);
			boolean angleOk = redirectAngle < input.getMaxAngle();
			if (angleOk && (morePrecise || (faster && preciseEnough)))
			{
				log.debug("New Path. " + angleOk + " && (" + morePrecise + " || (" + faster + " && " + preciseEnough
						+ ")) " + distDest2Ball);
				dest = rndDest;
				path = rndPath;
			}
		}
		
		if (path != null)
		{
			float tBot = path.getRemainingTime();
			IVector2 pBall = ball.getPosByTime(tBot + timeTolerance);
			float distDest2Ball = GeoMath.distancePP(pBall, dest);
			input.setLastDist2Dest(distDest2Ball);
			if (input.isDebug())
			{
				DrawablePoint dP = new DrawablePoint(dest, Color.black);
				dP.setText(String.format("%.1f", distDest2Ball));
				shapes.add(dP);
			}
		}
		
		input.setLastDest(dest);
		input.setLastPath(path);
		return path;
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
		
		for (ARole role : aiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORT))
		{
			if (((SupportRole) role).getDestination() != null)
			{
				yBalance += ((SupportRole) role).getDestination().y() < 0 ? -1 : 1;
			}
		}
		
		boolean positiveNeeded = yBalance <= 0;
		
		return (yBalance == 0) || (positiveNeeded && (position.y() >= 0)) || (!positiveNeeded && (position.y() < 0));
	}
}
