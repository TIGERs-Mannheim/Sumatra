/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 10, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.FieldPredictionInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;


/**
 * data holder class
 * 
 * @author DirkK
 * 
 */
public final class FieldInformation
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float				botRadius							= AIConfig.getGeometry().getBotRadius();
	private final float				ballRadius							= AIConfig.getGeometry().getBallRadius();
	private final Goal				goalTheir							= AIConfig.getGeometry().getGoalTheir();
	private final Goal				goalOur								= AIConfig.getGeometry().getGoalOur();
	private final float				centerCircleRadius				= AIConfig.getGeometry().getCenterCircleRadius();
	
	private static final float		GOALPOST_RADIUS					= 10f;
	/** shall the ball be considered? */
	private final boolean			considerBall;
	private final boolean			considerBots;
	private final boolean			considerGoalPosts;
	private boolean					prohibitCenterCircle				= false;
	// private boolean prohibitOpponentPenArea = false; //!!! NOT PROHIBITED BY THE RULES !!!
	private boolean					prohibitTigersPenArea			= true;
	private boolean					isFreekick							= false;
	private float						usedSafetyDistance				= AIConfig.getErrt().getSafetyDistance();
	private float						safetyDistanceBall;
	
	/** it's kickoff so the centercircle and the opponents half are prohibited */
	private boolean					prohibitOpponentsHalf			= false;
	
	/** ball */
	private final Vector2f			ballPos;
	
	private final IVector2			centerPoint							= new Vector2f(0, 0);
	
	/** distance bot<->ball while opponent has freekick */
	private final float				distanceAtFreekick				= AIConfig.getGeometry().getBotToBallDistanceStop();
	
	private final PenaltyArea		penaltyAreaOur						= AIConfig.getGeometry().getPenaltyAreaOur();
	
	/** list, all bots except thisBot are stored in */
	private List<IVector2>			botPosList							= new ArrayList<IVector2>();
	
	private final WorldFrame		wFrame;
	
	private final BotID				botId;
	
	// modifiable parameters
	private boolean					targetAdjustedBecauseOfBall	= false;
	private boolean					startAdjustedBecauseOfBall		= false;
	private IVector2					preprocessedDestination;
	private IVector2					preprocessedStart;
	private int							avoidedCollisons					= 0;
	
	private boolean					penAreaAllowed						= false;
	
	private final List<IVector2>	additionalObstacles				= new ArrayList<IVector2>();
	private final List<IVector2>	ignoredPoints						= new ArrayList<IVector2>();
	private List<TrackedBot>		ignoredBots							= new ArrayList<TrackedBot>();
	
	
	/**
	 * @return the ignoredBots
	 */
	public List<TrackedBot> getIgnoredBots()
	{
		return ignoredBots;
	}
	
	
	/**
	 * @param ignoredBots the ignoredBots to set
	 */
	public void setIgnoredBots(List<TrackedBot> ignoredBots)
	{
		this.ignoredBots = ignoredBots;
	}
	
	
	private static final Logger	log	= Logger.getLogger(FieldInformation.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param wFrame
	 * @param botId
	 * @param considerBall
	 * @param considerBots
	 * @param considerGoalPosts
	 * @param penAreaAllowed
	 * @param destination
	 */
	public FieldInformation(WorldFrame wFrame, BotID botId, boolean considerBall, boolean considerBots,
			boolean considerGoalPosts, boolean penAreaAllowed, Vector2f destination)
	{
		preprocessedStart = wFrame.getTiger(botId).getPos();
		preprocessedDestination = destination;
		if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(preprocessedDestination) && !penAreaAllowed)
		{
			preprocessedDestination = AIConfig.getGeometry().getPenaltyAreaOur()
					.nearestPointOutside(preprocessedDestination);
		}
		this.wFrame = wFrame;
		this.botId = botId;
		this.considerBall = considerBall;
		this.considerBots = considerBots;
		this.considerGoalPosts = considerGoalPosts;
		this.penAreaAllowed = penAreaAllowed;
		
		ballPos = new Vector2f(wFrame.ball.getPos());
		
		handleProhibitPenaltyArea();
		
		adjustStartIfBallOrBotIsAtStart();
		adjustTargetIfBallIsTarget();
		
		// ignoredPoints.add(preprocessedStart);
		ignoredPoints.add(preprocessedDestination);
		// all bots except thisBot in botPosList
		putBotsInList();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the preprocessedStart
	 */
	public IVector2 getPreprocessedStart()
	{
		return preprocessedStart;
	}
	
	
	/**
	 * @param preprocessedStart the preprocessedStart to set
	 */
	public void setPreprocessedStart(IVector2 preprocessedStart)
	{
		this.preprocessedStart = preprocessedStart;
	}
	
	
	/**
	 * changes the bots list which is used for collisions to a time in the future (or present if 0)
	 * be careful, unprecious
	 * @param time [s] in the future
	 */
	public void changeBotsInListToTimeInFuture(float time)
	{
		List<IVector2> newBotPosList = new LinkedList<IVector2>();
		WorldFramePrediction wfp = wFrame.getWorldFramePrediction();
		for (FieldPredictionInformation fpi : wfp.getFoes().values())
		{
			newBotPosList.add(fpi.getPosAt(time));
		}
		for (Entry<BotID, FieldPredictionInformation> fpi : wfp.getTigers().entrySet())
		{
			if (!botId.equals(fpi.getKey()))
			{
				newBotPosList.add(fpi.getValue().getPosAt(time));
			}
		}
		newBotPosList.add(wfp.getBall().getPosAt(time));
		botPosList = newBotPosList;
	}
	
	
	private void handleProhibitPenaltyArea()
	{
		if (penAreaAllowed || penaltyAreaOur.isPointInShape(wFrame.getTiger(botId).getPos())
				|| penaltyAreaOur.isPointInShape(preprocessedDestination))
		{
			prohibitTigersPenArea = false;
		}
	}
	
	
	/**
	 * checks if the bot drives into the penalty area despite he is not allowed to do this
	 * 
	 * @return
	 */
	public boolean isBotIllegallyInPenaltyArea()
	{
		return (!penAreaAllowed && penaltyAreaOur.isPointInShape(wFrame.getTiger(botId).getPos()));
	}
	
	
	/**
	 * gets the nearest point outside of the penalty area for the bot (the bot needs to be in the pen area)<br>
	 * 
	 * it is not the point on the penarea line but in a distance of botRadius outside of the penalty area
	 * @return
	 */
	public IVector2 getNearestNodeOutsidePenArea()
	{
		IVector2 tigerPos = wFrame.getTiger(botId).getPos();
		IVector2 pointOnLine = penaltyAreaOur.nearestPointOutside(tigerPos);
		IVector2 directionVectorToOutside = pointOnLine.subtractNew(tigerPos);
		directionVectorToOutside = directionVectorToOutside.scaleToNew(0);
		return pointOnLine.addNew(directionVectorToOutside);
	}
	
	
	/**
	 * checks direct connection between given point a (e.g. botPosition) and point b (e.g. target)
	 * 
	 * @param a start point
	 * @param b end point
	 * @return true if connection is FREE
	 */
	public boolean isWayOK(IVector2 a, IVector2 b)
	{
		if (considerBots)
		{
			if (isBotInWay(a, b, usedSafetyDistance + avoidedCollisons))
			{
				return false;
			}
		}
		
		if (considerGoalPosts)
		{
			if (isGoalPostInWay(a, b, usedSafetyDistance + avoidedCollisons))
			{
				return false;
			}
		}
		
		if (isProhibitedFieldAreaInWay(a, b))
		{
			return false;
		}
		
		if (considerBall)
		{
			if (isBallInWay(a, b, safetyDistanceBall + avoidedCollisons))
			{
				return false;
			}
		}
		
		// YOOUUUUU SHALL NOT PASS! (watching LoTR right now^^`)
		return true;
	}
	
	
	/**
	 * Checks if a position is allowed and not obstructed.
	 * 
	 * @param point Point to test.
	 * @return true if point is ok.
	 */
	public boolean isPointOK(IVector2 point)
	{
		float divSafetyDistance = 10.0f;
		float maxiumModifier = 3.0f;
		
		// Usage of divSafetyDistance with avoidedCollisions, so we get a >=1.0f number.
		float calcSafetyDistance = usedSafetyDistance / ((avoidedCollisons + divSafetyDistance) / divSafetyDistance);
		if (calcSafetyDistance < (usedSafetyDistance / maxiumModifier))
		{
			calcSafetyDistance = usedSafetyDistance / maxiumModifier;
		}
		if (considerBots)
		{
			if (testBot(point, calcSafetyDistance))
			{
				return false;
			}
		}
		if (considerGoalPosts)
		{
			if (testGoalPost(point, calcSafetyDistance))
			{
				return false;
			}
		}
		
		if (testProhibitedFieldArea(point))
		{
			return false;
		}
		
		if (considerBall)
		{
			if (testBall(point, safetyDistanceBall))
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * checks if ball would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isBallInWay(IVector2 nodeA, IVector2 nodeB, float safetyDistance)
	{
		if (isElementInWay(nodeA, nodeB, ballPos, ballRadius, safetyDistance))
		{
			return true;
		}
		return false;
	}
	
	
	private boolean testBall(IVector2 node, float safetyDistance)
	{
		if (testElement(node, ballPos, ballRadius, safetyDistance))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * checks if goalpost would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isGoalPostInWay(IVector2 nodeA, IVector2 nodeB, float safetyDistance)
	{
		if (isElementInWay(nodeA, nodeB, goalOur.getGoalPostLeft(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (isElementInWay(nodeA, nodeB, goalOur.getGoalPostRight(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (isElementInWay(nodeA, nodeB, goalTheir.getGoalPostLeft(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (isElementInWay(nodeA, nodeB, goalTheir.getGoalPostRight(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		}
		return false;
	}
	
	
	private boolean testGoalPost(IVector2 node, float safetyDistance)
	{
		if (testElement(node, goalOur.getGoalPostLeft(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (testElement(node, goalOur.getGoalPostRight(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (testElement(node, goalTheir.getGoalPostLeft(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (testElement(node, goalTheir.getGoalPostRight(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		}
		return false;
	}
	
	
	private boolean isProhibitedFieldAreaInWay(IVector2 nodeA, IVector2 nodeB)
	{
		// if there is more than one prohibited area, this check needs to be done for each element
		if (prohibitCenterCircle)
		{
			// no safety needed
			if (isElementInWay(nodeA, nodeB, centerPoint, centerCircleRadius, 0))
			{
				return true;
			}
		}
		
		// // opponents penalty area !!! NOT PROHIBITED BY THE RULES !!!
		
		if (prohibitTigersPenArea)
		{
			if (isPenaltyAreaInWay(nodeA, nodeB))
			{
				return true;
			}
		}
		
		if (isFreekick)
		{
			// no safety needed
			if (isElementInWay(nodeA, nodeB, ballPos, distanceAtFreekick, 0))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	private boolean testProhibitedFieldArea(IVector2 node)
	{
		// if there is more than one prohibited area, this check needs to be done for each element
		if (prohibitCenterCircle)
		{
			// no safety needed
			if (testElement(node, centerPoint, centerCircleRadius, 0))
			{
				return true;
			}
		}
		
		// // opponents penalty area !!! NOT PROHIBITED BY THE RULES !!!
		
		if (prohibitTigersPenArea)
		{
			// no safety needed
			if (testPenaltyArea(node))
			{
				return true;
			}
		}
		
		if (isFreekick)
		{
			// no safety needed
			if (testElement(node, ballPos, distanceAtFreekick, 0))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * checks if element would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isElementInWay(IVector2 nodeA, IVector2 nodeB, IVector2 elementPos, float elementRadius,
			float safetyDistance)
	{
		if (nodeA.equals(nodeB))
		{
			return false;
		}
		
		final float distanceX = nodeB.x() - nodeA.x();
		final float distanceY = nodeB.y() - nodeA.y();
		IVector2 nearest = null;
		
		final float u = (Math.abs((elementPos.x() - nodeA.x()) * distanceX) + Math.abs((elementPos.y() - nodeA.y())
				* distanceY))
				/ ((distanceX * distanceX) + (distanceY * distanceY));
		
		if (u < 0)
		{
			nearest = nodeA;
		} else if (u > 1)
		{
			nearest = nodeB;
		} else
		{
			// nearest point on line is between nodeA and nodeB
			nearest = new Node(nodeA.x() + (int) (u * distanceX), nodeA.y() + (int) (u * distanceY));
		}
		
		
		if (nearest.equals(elementPos, (botRadius + elementRadius + safetyDistance)))
		{
			return true;
		}
		
		return false;
	}
	
	
	private boolean testElement(IVector2 node, IVector2 elementPos, float elementRadius, float safetyDistance)
	{
		if (GeoMath.distancePP(node, elementPos) < (botRadius + elementRadius + safetyDistance))
		{
			return true;
		}
		
		return false;
	}
	
	
	private boolean isPenaltyAreaInWay(IVector2 nodeA, IVector2 nodeB)
	{
		if (nodeA.equals(nodeB))
		{
			return false;
		}
		
		final float distanceX = nodeB.x() - nodeA.x();
		final float distanceY = nodeB.y() - nodeA.y();
		IVector2 nearest = null;
		
		IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		final float u = (Math.abs((goalCenter.x() - nodeA.x()) * distanceX) + Math.abs((goalCenter.y() - nodeA.y())
				* distanceY))
				/ ((distanceX * distanceX) + (distanceY * distanceY));
		
		if (u < 0)
		{
			nearest = nodeA;
		} else if (u > 1)
		{
			nearest = nodeB;
		} else
		{
			// nearest point on line is between nodeA and nodeB
			nearest = new Node(nodeA.x() + (int) (u * distanceX), nodeA.y() + (int) (u * distanceY));
		}
		
		if (penaltyAreaOur.isPointInShape(nearest, 0))
		{
			return true;
		}
		
		return false;
	}
	
	
	private boolean testPenaltyArea(IVector2 node)
	{
		return penaltyAreaOur.isPointInShape(node);
	}
	
	
	/**
	 * checks if the direct link between two points is free. returns true if a collision happens
	 */
	private boolean isBotInWay(IVector2 a, IVector2 b, float safetyDistance)
	{
		for (final IVector2 pos : botPosList)
		{
			if (isElementInWay(a, b, pos, botRadius, safetyDistance))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	private boolean testBot(IVector2 point, float safetyDistance)
	{
		for (final IVector2 pos : botPosList)
		{
			if (testElement(point, pos, botRadius, safetyDistance))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	private void putBotsInList()
	{
		// clear botPosList from last run-cycle
		botPosList.clear();
		
		botPosList.addAll(getAllBotsExceptMe());
		
		for (TrackedBot bot : ignoredBots)
		{
			botPosList.remove(bot.getPos());
		}
		
		for (IVector2 obstacle : additionalObstacles)
		{
			botPosList.add(new Node(obstacle));
		}
		
		// if bot blocks anything, don't consider it
		for (IVector2 obstacle : ignoredPoints)
		{
			removeBotsNearPoint(obstacle);
		}
	}
	
	
	private List<IVector2> getAllBotsExceptMe()
	{
		List<IVector2> botList = new ArrayList<IVector2>(13);
		// store tigers
		for (final TrackedTigerBot bot : wFrame.tigerBotsVisible.values())
		{
			// the bot itself is no obstacle
			if (!bot.getId().equals(botId))
			{
				
				botList.add(new Node(bot.getPos()));
			}
		}
		// store opponents
		for (final TrackedBot bot : wFrame.foeBots.values())
		{
			// if bot blocks goal, don't consider it
			botList.add(new Node(bot.getPos()));
		}
		return botList;
	}
	
	
	/**
	 * @param point
	 * @return
	 */
	private List<IVector2> removeBotsNearPoint(IVector2 point)
	{
		final List<IVector2> botsAtPoint = new ArrayList<IVector2>();
		for (final IVector2 bot : botPosList)
		{
			if (isBotNearPoint(bot, point, usedSafetyDistance, botRadius))
			{
				botsAtPoint.add(bot);
			}
		}
		for (final IVector2 bot : botsAtPoint)
		{
			botPosList.remove(bot);
		}
		return botsAtPoint;
	}
	
	
	/**
	 * @param bot
	 * @param point
	 * @param safetyDistance
	 * @param obstacleRadius
	 * @return
	 */
	public boolean isBotNearPoint(IVector2 bot, IVector2 point, float safetyDistance, float obstacleRadius)
	{
		return GeoMath.distancePP(bot, point) < (safetyDistance + botRadius + obstacleRadius);
	}
	
	
	/**
	 * puts all bots (except bot, this path is for) in list, so i can iterate over them more easily
	 * @param specialObstacle
	 * 
	 */
	public void putBotsInList(IVector2 specialObstacle)
	{
		additionalObstacles.add(specialObstacle);
		putBotsInList();
	}
	
	
	/**
	 * bots ad the points added here will be ignored as obstacles
	 * 
	 * @param ignore
	 */
	public void addIgnoredPoitn(IVector2 ignore)
	{
		ignoredPoints.add(ignore);
		putBotsInList();
	}
	
	
	private void adjustTargetIfBallIsTarget()
	{
		if (considerBall)
		{
			if (isBallAtTarget()
					&& (wFrame.getTiger(botId).getPos().subtractNew(preprocessedDestination).getLength2() > getNeededDistanceFromBallToStartPathPlanning()))
			{
				IVector2 ballToGoal = preprocessedDestination.subtractNew(ballPos);
				if (ballToGoal.isZeroVector())
				{
					log.warn("Do not use the ball position as target.", new Exception());
				} else
				{
					IVector2 ballToGoalScaled = ballToGoal.scaleToNew(getNeededDistanceFromBallToStartPathPlanning());
					preprocessedDestination = ballPos.addNew(ballToGoalScaled);
				}
				targetAdjustedBecauseOfBall = true;
			}
		}
	}
	
	
	private void adjustStartIfBallOrBotIsAtStart()
	{
		IVector2 possiblePreprocessedStart = null;
		List<IVector2> obstacles = obstaclesAtPoint(preprocessedStart);
		if (considerBots)
		{
			for (IVector2 obstacle : obstacles)
			{
				possiblePreprocessedStart = preprocessStartForObstacle(obstacle, possiblePreprocessedStart, false);
				startAdjustedBecauseOfBall = true;
			}
		}
		if (considerBall)
		{
			if (isBotNearPoint(wFrame.ball.getPos(), preprocessedStart, safetyDistanceBall, ballRadius))
			{
				possiblePreprocessedStart = preprocessStartForObstacle(wFrame.ball.getPos(), possiblePreprocessedStart,
						true);
				startAdjustedBecauseOfBall = true;
			}
		}
		if (startAdjustedBecauseOfBall)
		{
			if (possiblePreprocessedStart == null)
			{
				log.warn("There was no point found for the bot to drive securely to its destination.");
			} else
			{
				preprocessedStart = possiblePreprocessedStart;
			}
		}
	}
	
	
	private List<IVector2> obstaclesAtPoint(IVector2 point)
	{
		List<IVector2> obstacles = new ArrayList<IVector2>();
		for (final IVector2 bot : getAllBotsExceptMe())
		{
			if (isBotNearPoint(bot, point, usedSafetyDistance, botRadius))
			{
				obstacles.add(bot);
			}
		}
		return obstacles;
	}
	
	
	/**
	 * find a point near the goal, where the bot could drive
	 * 
	 * @param obstacle
	 * @param isBall
	 * @return null if no point found, otherwise the point
	 */
	private IVector2 preprocessStartForObstacle(IVector2 obstacle, IVector2 possiblePreprocessedStart, boolean isBall)
	{
		IVector2 obstacleToStart = preprocessedStart.subtractNew(obstacle);
		float radius = botRadius;
		float safety = usedSafetyDistance;
		if (isBall)
		{
			radius = ballRadius;
			safety = safetyDistanceBall;
		}
		float neededDistance = radius + safety + botRadius;
		double distanceOrthogonalMovementSqare = ((neededDistance * neededDistance) - (obstacleToStart.getLength2() * obstacleToStart
				.getLength2()));
		float distanceOrthogonalMovement = (float) Math.sqrt(distanceOrthogonalMovementSqare);
		IVector2 orthogonalToObstacleToStart = obstacleToStart.getNormalVector();
		IVector2 startOnCircle = preprocessedStart.addNew(orthogonalToObstacleToStart
				.scaleToNew(distanceOrthogonalMovement));
		IVector2 obstacleToStartOnCircle = startOnCircle.subtractNew(obstacle);
		float angleToRotate = AngleMath.difference(obstacleToStart.getAngle(), obstacleToStartOnCircle.getAngle());
		float distanceToRealTarget = Float.MAX_VALUE;
		if (possiblePreprocessedStart != null)
		{
			distanceToRealTarget = possiblePreprocessedStart.subtractNew(preprocessedDestination).getLength2();
		}
		float endAngle = angleToRotate * 2;
		float currentAngle = 0;
		// log.warn("circle around: " + obstacle + "(" + preprocessedStart + ") " + angleToRotate);
		for (@SuppressWarnings("unused")
		int i = 0; currentAngle < endAngle; i++)
		{
			currentAngle += 0.1;
			IVector2 possibleStart = GeoMath.stepAlongCircle(startOnCircle, obstacle, currentAngle);
			// log.warn("possibleStart: " + possibleStart);
			if (isPointOK(possibleStart))
			{
				float distance = possibleStart.subtractNew(preprocessedDestination).getLength2();
				if (distance < distanceToRealTarget)
				{
					distanceToRealTarget = distance;
					possiblePreprocessedStart = possibleStart;
				}
			}
		}
		return possiblePreprocessedStart;
	}
	
	
	private boolean isBallAtTarget()
	{
		return wFrame.ball.getPos().equals(preprocessedDestination, getNeededDistanceFromBallToStartPathPlanning());
	}
	
	
	private float getNeededDistanceFromBallToStartPathPlanning()
	{
		return botRadius + ballRadius + (AIConfig.getErrt().getSafetyDistanceBall() * 2);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param usedSafetyDistance
	 */
	public void setUsedSafetyDistance(float usedSafetyDistance)
	{
		this.usedSafetyDistance = usedSafetyDistance;
		putBotsInList();
	}
	
	
	/**
	 * @return
	 */
	public boolean isProhibitOpponentsHalf()
	{
		return prohibitOpponentsHalf;
	}
	
	
	/**
	 * @return the botPosList
	 */
	public List<IVector2> getBotPosList()
	{
		return botPosList;
	}
	
	
	/**
	 * @return the goal
	 */
	public IVector2 getPreprocessedTarget()
	{
		return preprocessedDestination;
	}
	
	
	/**
	 * @return the safetyDistanceBall
	 */
	public float getSafetyDistanceBall()
	{
		return safetyDistanceBall;
	}
	
	
	/**
	 * @param safetyDistanceBall the safetyDistanceBall to set
	 */
	public void setSafetyDistanceBall(float safetyDistanceBall)
	{
		this.safetyDistanceBall = safetyDistanceBall;
	}
	
	
	/**
	 * @return the targetAdjustedBecauseOfBall
	 */
	public boolean isTargetAdjustedBecauseOfBall()
	{
		return targetAdjustedBecauseOfBall;
	}
	
	
	/**
	 * @return the startAdjustedBecauseOfBall
	 */
	public boolean isStartAdjustedBecauseOfBall()
	{
		return startAdjustedBecauseOfBall;
	}
	
	
	/**
	 * @return the avoidedCollisons
	 */
	public int getAvoidedCollisons()
	{
		return avoidedCollisons;
	}
	
	
	/**
	 * @param avoidedCollisons the avoidedCollisons to set
	 */
	public void setAvoidedCollisons(int avoidedCollisons)
	{
		this.avoidedCollisons = avoidedCollisons;
	}
	
	
}
