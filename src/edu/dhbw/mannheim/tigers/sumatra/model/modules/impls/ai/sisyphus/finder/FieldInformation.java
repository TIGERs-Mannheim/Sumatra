/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 10, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictionInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * data holder class
 * 
 * @author DirkK
 */
public final class FieldInformation
{
	private static final float		GOALPOST_RADIUS					= 10f;
	/** Config values */
	private final float				botRadius							= AIConfig.getGeometry().getBotRadius();
	private final float				ballRadius							= AIConfig.getGeometry().getBallRadius();
	private final Goal				goalTheir							= AIConfig.getGeometry().getGoalTheir();
	private final Goal				goalOur								= AIConfig.getGeometry().getGoalOur();
	private final PenaltyArea		penaltyAreaOur						= AIConfig.getGeometry()
																							.getPenaltyAreaOur();
	private final PenaltyArea		penaltyAreaTheir					= AIConfig.getGeometry()
																							.getPenaltyAreaTheir();
	// safety distance
	@Configurable(comment = "min. distance to obstacles [mm]")
	private static float				safetyDistance						= 100;
	@Configurable(comment = "min. distance to obstacles in 2nd round [mm]")
	private static float				secondSafetyDistance				= 45;
	@Configurable(comment = "min. distance to the ball", defValue = "50")
	private static float				safetyDistanceBall				= 200;
	@Configurable(comment = "puffer between end of safety distance and possible start / target positions for the ERRT")
	private static float				pufferSafetyEndToDest			= 10;
	@Configurable(comment = "time in seconds which are predicted for the path planning")
	private static float				predictionIterationsMaximum	= 10f;
	@Configurable(comment = "time in seconds for the prediction step size")
	private static float				predictionStepSize				= 0.1f;
	@Configurable(comment = "Margin between our penArea border and bot center to keep")
	private static float				margin2PenaltyAreaOurInner		= 200;
	// @Configurable(comment =
	// "Margin between our penArea border and bot center to keep, should be bigger than margin2PenaltyAreaPathPlanning")
	private float						margin2PenaltyAreaOurOuter		= Geometry.getPenaltyAreaMargin();
	@Configurable(comment = "Margin between their penArea border and bot center to keep")
	private static float				margin2PenaltyAreaTheirInner	= 90;
	@Configurable(comment = "Margin between their penArea border and bot center to keep, should be bigger than margin2PenaltyAreaPathPlanning")
	private static float				margin2PenaltyAreaTheirOuter	= 150;
	@Configurable(comment = "Path planning max distance, calculated by factor times velocy", defValue = "2000")
	private static float				pathPlanningCircleVelocity		= 2000;
	@Configurable(comment = "Path planning max distance, calculated by factor times velocy", defValue = "200")
	private static float				pathPlanningCircleFixed			= 200;
	
	
	/** set by the ERRT algorithm (can change dynamically) */
	private boolean					isSecondTry							= false;
	
	/** initialization values */
	private final MovementCon		moveCon;
	private SimpleWorldFrame		wFrame								= null;
	private final BotID				botId;
	
	/** fields used in this class */
	private boolean					prohibitTigersPenArea			= true;
	private boolean					prohibitTheirPenArea				= false;
	private float						safetyDistanceBot					= safetyDistance;
	
	private boolean					targetAdjustedBecauseOfBall	= false;
	private boolean					startAdjustedBecauseOfBall		= false;
	private IVector2					preprocessedDestination			= null;
	private IVector2					preprocessedStart					= null;
	
	private final List<IVector2>	ignoredPoints						= new ArrayList<IVector2>();
	private final List<BotID>		ignoredBotsForCollDetect		= new ArrayList<BotID>();
	
	/** for the bot at the position of the IVector2 a special safety distance should be used **/
	private Map<BotID, Float>		specialSafetyDistances			= new HashMap<BotID, Float>();
	
	/**
	 * list, all bots except thisBot are stored in, IVector2 instead of TrackedTigerBot is used
	 * because it can also be the position of the bots in the future
	 */
	private List<FutureBot>			botPosList							= new ArrayList<FutureBot>();
	private FutureObstacle			ball;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 * @param moveCon
	 */
	public FieldInformation(final BotID botId, final MovementCon moveCon)
	{
		this.botId = botId;
		this.moveCon = moveCon;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param wFrame
	 */
	@SuppressWarnings("unchecked")
	public void updateWorldFrame(final SimpleWorldFrame wFrame)
	{
		if (wFrame.getBot(botId) == null)
		{
			// do not update this time
			return;
		}
		this.wFrame = wFrame;
		ball = new FutureObstacle(wFrame.getBall().getPos(), wFrame.getBall().getVel());
		moveCon.update(wFrame, botId);
		updateInternalFields();
	}
	
	
	private void updateInternalFields()
	{
		if (isSecondTry)
		{
			safetyDistanceBot = secondSafetyDistance;
		} else
		{
			safetyDistanceBot = safetyDistance;
		}
		preprocessedStart = wFrame.getBot(botId).getPos();
		preprocessedDestination = moveCon.getDestCon().getDestination();
		handleProhibitPenaltyArea();
		
		// ORDER IMPORTANT
		adjustStartIfInPenArea();
		adjustTargetIfInPenArea();
		adjustStartIfBallOrBotIsAtStart();
		adjustTargetIfBallIsTarget();
		adjustTargetIfBotIsTarget();
		fillBotPosList(0);
	}
	
	
	private void handleProhibitPenaltyArea()
	{
		if (moveCon.isPenaltyAreaAllowedOur()
				|| penaltyAreaOur.isPointInShape(wFrame.getBot(botId).getPos()))
		{
			prohibitTigersPenArea = false;
		}
		else
		{
			prohibitTigersPenArea = true;
		}
		
		if (moveCon.isPenaltyAreaAllowedTheir()
				|| penaltyAreaTheir.isPointInShape(wFrame.getBot(botId).getPos()))
		{
			prohibitTheirPenArea = false;
		}
		else
		{
			prohibitTheirPenArea = true;
		}
	}
	
	
	/************************************************************************************
	 ****************************** Fill the bot list ***********************************
	 ************************************************************************************/
	
	
	/**
	 * fills the botPosList with the position of all bots except me to the given time
	 * 
	 * @param time
	 */
	public void fillBotPosList(final float time)
	{
		setBotAndBallToTimeAndMaxDist(time, AIConfig.getGeometry().getFieldLength());
	}
	
	
	/**
	 * fill the list of all bots on the field
	 * the bot which owns this field information is excluded
	 * 
	 * @param time word predictor time
	 * @param maxDist include only bots which are closer than this
	 */
	public void setBotAndBallToTimeAndMaxDist(final float time, final float maxDist)
	{
		botPosList.clear();
		
		botPosList.addAll(getAllBotsExceptMe(time, maxDist));
		
		// if bot blocks anything, don't consider it
		for (IVector2 obstacle : ignoredPoints)
		{
			removeBotsNearPoint(obstacle);
		}
		WorldFramePrediction wfp = wFrame.getWorldFramePrediction();
		ball = new FutureObstacle(wfp.getBall().getPosAt(time), wfp.getBall().getVel());
	}
	
	
	private List<FutureBot> getAllBotsExceptMe(final float time)
	{
		return getAllBotsExceptMe(time, AIConfig.getGeometry().getFieldLength());
	}
	
	
	private List<FutureBot> getAllBotsExceptMe(final float time, final float maxDist)
	{
		List<FutureBot> newBotPosList = new ArrayList<FutureBot>(13);
		WorldFramePrediction wfp = wFrame.getWorldFramePrediction();
		for (Entry<BotID, FieldPredictionInformation> fpi : wfp.getBots().entrySet())
		{
			if (!botId.equals(fpi.getKey())
					&& (GeoMath.distancePP(fpi.getValue().getPosAt(time), wFrame.getBot(botId).getPos()) < maxDist))
			{
				newBotPosList.add(new FutureBot(fpi.getKey(), fpi.getValue().getPosAt(time), fpi.getValue().getVel()));
			}
		}
		return newBotPosList;
		
	}
	
	
	/**
	 * remove all bots which are nearer to the given point than safetyDistanceBot to the time 0 of the spline
	 * 
	 * @param point
	 * @return
	 */
	private List<IVector2> removeBotsNearPoint(final IVector2 point)
	{
		final List<IVector2> botsAtPoint = new ArrayList<IVector2>();
		for (final IVector2 bot : getAllBotsExceptMe(0))
		{
			if (isBotNearPoint(bot, point, safetyDistanceBot, botRadius))
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
	private boolean isBotNearPoint(final IVector2 bot, final IVector2 point, final float safetyDistance,
			final float obstacleRadius)
	{
		return GeoMath.distancePP(bot, point) < (safetyDistance + botRadius + obstacleRadius);
	}
	
	
	/************************************************************************************
	 ************************* Adjust Start behavior and target *************************
	 ************************************************************************************/
	/**
	 * Idea: if the destination is in the penalty area, stop outside of the penalty area
	 */
	private void adjustTargetIfInPenArea()
	{
		if (penaltyAreaOur.isPointInShape(preprocessedDestination, margin2PenaltyAreaOurOuter)
				&& prohibitTigersPenArea)
		{
			// preprocessedDestination = AIConfig.getGeometry().getPenaltyAreaOur()
			// .nearestPointOutside(preprocessedDestination);
			preprocessedDestination = penaltyAreaOur.nearestPointOutside(preprocessedDestination,
					margin2PenaltyAreaOurOuter + 1);
		}
		
		if (penaltyAreaTheir.isPointInShape(preprocessedDestination, margin2PenaltyAreaTheirOuter)
				&& prohibitTheirPenArea)
		{
			preprocessedDestination = penaltyAreaTheir.nearestPointOutside(preprocessedDestination,
					margin2PenaltyAreaTheirOuter + 1);
		}
	}
	
	
	/**
	 * Idea: first drive away from the obstacle and than start path planning
	 */
	private void adjustStartIfBallOrBotIsAtStart()
	{
		IVector2 possiblePreprocessedStart = null;
		startAdjustedBecauseOfBall = false;
		if (moveCon.isBotsObstacle())
		{
			List<FutureBot> obstacles = obstaclesAtPoint(preprocessedStart);
			ignoredBotsForCollDetect.clear();
			for (FutureBot obstacle : obstacles)
			{
				possiblePreprocessedStart = preprocessStartForObstacle(obstacle, false);
				ignoredBotsForCollDetect.add(obstacle.getBot());
				startAdjustedBecauseOfBall = true;
			}
		}
		if (moveCon.isBallObstacle())
		{
			if (isBotNearPoint(ball, preprocessedStart, safetyDistanceBall, ballRadius)
					&& !isBotNearPoint(wFrame.getBot(botId).getPos(), ball, safetyDistanceBall,
							botRadius))
			{
				possiblePreprocessedStart = preprocessStartForObstacle(ball, true);
				startAdjustedBecauseOfBall = true;
			}
		}
		if (startAdjustedBecauseOfBall)
		{
			if (possiblePreprocessedStart != null)
			{
				preprocessedStart = possiblePreprocessedStart;
			}
		}
	}
	
	
	private void adjustStartIfInPenArea()
	{
		startAdjustedBecauseOfBall = false;
		if (!moveCon.isPenaltyAreaAllowedOur()
				&& penaltyAreaOur.isPointInShape(preprocessedStart, margin2PenaltyAreaOurInner))
		{
			startAdjustedBecauseOfBall = true;
			preprocessedStart = penaltyAreaOur.nearestPointOutside(preprocessedStart,
					margin2PenaltyAreaOurInner + 1);
		}
		if (!moveCon.isPenaltyAreaAllowedTheir()
				&& penaltyAreaTheir.isPointInShape(preprocessedStart, margin2PenaltyAreaTheirInner))
		{
			startAdjustedBecauseOfBall = true;
			preprocessedStart = penaltyAreaTheir.nearestPointOutside(preprocessedStart,
					margin2PenaltyAreaTheirInner + 1);
		}
	}
	
	
	/**
	 * Idea: first drive in a curve behind the ball and then approach the ball
	 */
	private void adjustTargetIfBallIsTarget()
	{
		if (moveCon.isBallObstacle())
		{
			if (isBallAtTarget()
					&& !isBotNearPoint(wFrame.getBot(botId).getPos(), ball,
							getNeededDistanceFromBallToStartPathPlanning(),
							botRadius))
			{
				preprocessedDestination = botToObstacleLeadPoint(wFrame.getBot(botId), preprocessedDestination, wFrame
						.getBall().getPos(), getNeededDistanceFromBallToStartPathPlanning());
				targetAdjustedBecauseOfBall = true;
			}
		}
	}
	
	
	/**
	 * Idea: if there is a bot at the target reduce the safety distance for this bot,
	 * we will perhaps ram slowly into this bot but that is ok. Don't play defensive!!!
	 */
	private void adjustTargetIfBotIsTarget()
	{
		List<FutureBot> botsAtTarget = obstaclesAtPoint(preprocessedDestination);
		for (FutureBot bot : botsAtTarget)
		{
			float distBotToDest = bot.subtractNew(preprocessedDestination).getLength2();
			if (distBotToDest > pufferSafetyEndToDest)
			{
				specialSafetyDistances.put(bot.getBot(), distBotToDest - pufferSafetyEndToDest - (botRadius * 2));
			} else
			{
				ignoredPoints.add(bot);
			}
		}
		
	}
	
	
	private IVector2 botToObstacleLeadPoint(final TrackedTigerBot bot, final IVector2 destination,
			final IVector2 obstacle, final float distance)
	{
		IVector2 obstacleToDestination = destination.subtractNew(obstacle);
		IVector2 earlierStoppingDirection = null;
		if (obstacleToDestination.isZeroVector())
		{
			IVector2 obstacleToBot = bot.getPos().subtractNew(obstacle);
			earlierStoppingDirection = obstacleToBot;
		} else
		{
			earlierStoppingDirection = obstacleToDestination;
		}
		IVector2 obstacleToDestinationScaled = earlierStoppingDirection.scaleToNew(distance);
		return obstacle.addNew(obstacleToDestinationScaled);
	}
	
	
	private List<FutureBot> obstaclesAtPoint(final IVector2 point)
	{
		return botAtPoint(point, safetyDistanceBot);
	}
	
	
	private List<FutureBot> botAtPoint(final IVector2 point, final float safetyDist)
	{
		List<FutureBot> obstacles = new ArrayList<FutureBot>();
		for (final FutureBot bot : getAllBotsExceptMe(0))
		{
			if (isBotNearPoint(bot, point, safetyDist, botRadius))
			{
				obstacles.add(bot);
			}
		}
		return obstacles;
	}
	
	
	/**
	 * if the bot is currently to close to an obstacle (closer than security distance), find a point near the start,
	 * where the bot could drive first
	 * 
	 * @param obstacle obstacle which is in way
	 * @param isBall
	 * @return null if no point found, otherwise the point
	 */
	private IVector2 preprocessStartForObstacle(final IVector2 obstacle, final boolean isBall)
	{
		IVector2 possiblePreprocessedStart = null;
		IVector2 obstacleToStart = preprocessedStart.subtractNew(obstacle);
		if (obstacleToStart.isZeroVector())
		{
			// log.info("A bot has the same position than an obstacle (bot or ball).");
			return preprocessedStart;
		}
		float radius = botRadius;
		float safety = safetyDistanceBot;
		if (isBall)
		{
			radius = ballRadius;
			safety = safetyDistanceBall;
		}
		// Idea of the following code:
		// If the bot is too close to an obstacle first drive away form the obstacle in the most direct way and then
		// continue with path planning
		float neededDistance = radius + safety + botRadius;
		double distanceOrthogonalMovementSqare = ((neededDistance * neededDistance) - (obstacleToStart.getLength2() * obstacleToStart
				.getLength2()));
		float distanceOrthogonalMovement = (float) Math.sqrt(distanceOrthogonalMovementSqare);
		IVector2 orthogonalToObstacleToStart = obstacleToStart.getNormalVector();
		IVector2 startOnCircle = preprocessedStart.addNew(orthogonalToObstacleToStart
				.scaleToNew(distanceOrthogonalMovement));
		IVector2 obstacleToStartOnCircle = startOnCircle.subtractNew(obstacle);
		float angleToRotate = Math.abs(AngleMath.difference(obstacleToStart.getAngle(),
				obstacleToStartOnCircle.getAngle()));
		float distanceToRealTarget = Float.MAX_VALUE;
		
		float endAngle = angleToRotate * 2;
		// log.warn("circle around: " + obstacle + "(" + preprocessedStart + ") " + angleToRotate);
		for (float currentAngle = 0; currentAngle < endAngle; currentAngle += 0.1)
		{
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
		return ball.equals(preprocessedDestination, getNeededDistanceFromBallToStartPathPlanning());
	}
	
	
	private float getNeededDistanceFromBallToStartPathPlanning()
	{
		return botRadius + ballRadius + (safetyDistanceBall * 2);
	}
	
	
	/************************************************************************************
	 ****************************** Collision Checks ************************************
	 ************************************************************************************/
	/**
	 * the ...InWay methods are used by the ERRT
	 * the test... methods are used by finding an optimal start and by the collision detection
	 */
	
	/**
	 * checks direct connection between given point a (e.g. botPosition) and point b (e.g. target)
	 * 
	 * @param a start point
	 * @param b end point
	 * @return true if connection is FREE
	 */
	public boolean isWayOK(final IVector2 a, final IVector2 b)
	{
		return isWayOK(a, b, 1.0f);
	}
	
	
	/**
	 * @param a
	 * @param b
	 * @param safetyFactor
	 * @return
	 */
	public boolean isWayOK(final IVector2 a, final IVector2 b, final float safetyFactor)
	{
		float maxDist = (((wFrame.getBot(botId).getVel().getLength2() * pathPlanningCircleVelocity) + pathPlanningCircleFixed));
		setBotAndBallToTimeAndMaxDist(0f, maxDist);
		boolean result1 = isWayOKImpl(a, b, safetyFactor);
		setBotAndBallToTimeAndMaxDist(0.1f, maxDist);
		boolean result2 = isWayOKImpl(a, b, safetyFactor);
		setBotAndBallToTimeAndMaxDist(0f, 0f);
		return result1 && result2;
	}
	
	
	private boolean isWayOKImpl(final IVector2 a, final IVector2 b, final float safetyFactor)
	{
		if (moveCon.isBotsObstacle())
		{
			if (isBotInWay(a, b, safetyDistanceBot * safetyFactor))
			{
				return false;
			}
		}
		
		if (moveCon.isGoalPostObstacle())
		{
			if (isGoalPostInWay(a, b, safetyDistanceBot * safetyFactor))
			{
				return false;
			}
		}
		
		if (isProhibitedFieldAreaInWay(a, b))
		{
			return false;
		}
		if (moveCon.isBallObstacle())
		{
			if (isBallInWay(a, b, safetyDistanceBall * safetyFactor))
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
	public boolean isPointOK(final IVector2 point)
	{
		return isPointOK(point, safetyDistanceBot, false);
	}
	
	
	/**
	 * Checks if a position is allowed and not obstructed.
	 * 
	 * @param point Point to test
	 * @param calcSafetyDistance safety distance around the point
	 * @param forceConsiderBall overwrite the global considerBall with true, false keeps the value of the global
	 *           considerBall
	 * @return
	 */
	public boolean isPointOK(final IVector2 point, final float calcSafetyDistance, final boolean forceConsiderBall)
	{
		return (isPointOKPP(point, calcSafetyDistance, forceConsiderBall) == null);
	}
	
	
	/**
	 * Checks if a position is allowed and not obstructed.
	 * 
	 * @param point
	 * @param calcSafetyDistance
	 * @param forceConsiderBall
	 * @return
	 */
	public IVector2 isPointOKPP(final IVector2 point, final float calcSafetyDistance, final boolean forceConsiderBall)
	{
		if (moveCon.isBotsObstacle())
		{
			IVector2 obstacle = testBot(point, calcSafetyDistance);
			if (obstacle != null)
			{
				return obstacle;
			}
		}
		if (moveCon.isGoalPostObstacle())
		{
			// if (testGoalPost(point, calcSafetyDistance))
			// {
			// return false;
			// }
		}
		
		if (testProhibitedFieldArea(point))
		{
			return penaltyAreaOur.getPenaltyRectangle().getMidPoint();
		}
		
		if (moveCon.isBallObstacle() || forceConsiderBall)
		{
			if (testBall(point, calcSafetyDistance))
			{
				return ball;
			}
		}
		
		return null;
	}
	
	
	/**
	 * @return the moveCon
	 */
	public MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	/**
	 * checks if ball would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isBallInWay(final IVector2 nodeA, final IVector2 nodeB, final float safetyDistance)
	{
		for (float i = 0; (i <= (predictionIterationsMaximum * predictionStepSize)) && (!isSecondTry || (i == 0)); i += predictionStepSize)
		{
			IVector2 ballPos = wFrame.getWorldFramePrediction().getBall().getPosAt(i);
			if (isElementInWay(nodeA, nodeB, ballPos, ballRadius, safetyDistance))
			{
				return true;
			}
		}
		return false;
	}
	
	
	private boolean testBall(final IVector2 node, final float safetyDistance)
	{
		if (testElement(node, ball, ballRadius, safetyDistance))
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
	private boolean isGoalPostInWay(final IVector2 nodeA, final IVector2 nodeB, final float safetyDistance)
	{
		float safety = safetyDistance + GOALPOST_RADIUS + botRadius;
		if ((nodeA.x() < (goalOur.getGoalCenter().x() + safety)) || (nodeB.x() < (goalOur.getGoalCenter().x() + safety)))
		{
			if (goalOur.isLineCrossingGoal(nodeA, nodeB, safety))
			{
				return true;
			}
		}
		if ((nodeA.x() > (goalTheir.getGoalCenter().x() - safety))
				|| (nodeB.x() > (goalTheir.getGoalCenter().x() - safety)))
		{
			if (goalTheir.isLineCrossingGoal(nodeA, nodeB, safety))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	// @SuppressWarnings("unused")
	// private boolean testGoalPost(final IVector2 node, final float safetyDistance)
	// {
	// float safety = safetyDistance + GOALPOST_RADIUS + botRadius;
	// if ((node.x() < (goalOur.getGoalCenter().x() + safety)))
	// {
	// // this function is called very often, so we check first
	// if (goalOur.isLineCrossingGoal(node, node, safety))
	// {
	// return true;
	// }
	// }
	// if ((node.x() > (goalTheir.getGoalCenter().x() - safety)))
	// {
	// if (goalTheir.isLineCrossingGoal(node, node, safety))
	// {
	// return true;
	// }
	// }
	// return false;
	// }
	
	
	private boolean isProhibitedFieldAreaInWay(final IVector2 nodeA, final IVector2 nodeB)
	{
		if (prohibitTigersPenArea)
		{
			if (isPenaltyAreaInWay(nodeA, nodeB, penaltyAreaOur, margin2PenaltyAreaOurInner))
			{
				return true;
			}
		}
		
		if (prohibitTheirPenArea)
		{
			if (isPenaltyAreaInWay(nodeA, nodeB, penaltyAreaTheir, margin2PenaltyAreaTheirInner))
			{
				return true;
			}
		}
		
		if (moveCon.isRefereeStop())
		{
			ICircle stopArea = new Circle(ball, AIConfig.getGeometry().getBotToBallDistanceStop());
			if (!stopArea.lineIntersections(new Line(nodeA, nodeB.subtractNew(nodeA))).isEmpty())
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	private boolean testProhibitedFieldArea(final IVector2 node)
	{
		if (prohibitTigersPenArea)
		{
			// no safety needed
			if (testPenaltyAreaOur(node))
			{
				return true;
			}
		}
		
		if (prohibitTheirPenArea)
		{
			// no safety needed
			if (testPenaltyAreaTheir(node))
			{
				return true;
			}
		}
		
		if (moveCon.isRefereeStop())
		{
			ICircle stopArea = new Circle(ball, AIConfig.getGeometry().getBotToBallDistanceStop());
			if (stopArea.isPointInShape(node))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	private boolean isPenaltyAreaInWay(final IVector2 nodeA, final IVector2 nodeB, final PenaltyArea penArea,
			final float margin)
	{
		int stepSize = 10;
		for (int i = 0; (i < 1000) && ((i * 10) < nodeA.subtractNew(nodeB).getLength2()); i = i + 10)
		{
			IVector2 testPoint = GeoMath.stepAlongLine(nodeA, nodeB, i * stepSize);
			if (penArea.isPointInShape(testPoint, margin))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	private boolean testPenaltyAreaOur(final IVector2 node)
	{
		return penaltyAreaOur.isPointInShape(node, margin2PenaltyAreaOurOuter);
	}
	
	
	private boolean testPenaltyAreaTheir(final IVector2 node)
	{
		return penaltyAreaTheir.isPointInShape(node, margin2PenaltyAreaTheirOuter);
	}
	
	
	/**
	 * checks if the direct link between two points is free. returns true if a collision happens
	 */
	private boolean isBotInWay(final IVector2 a, final IVector2 b, final float safetyDistance)
	{
		for (final FutureBot bot : botPosList)
		{
			for (float i = 0; (i <= (predictionIterationsMaximum * predictionStepSize)) && (!isSecondTry || (i == 0)); i += predictionStepSize)
			{
				IVector2 testPos = wFrame.getWorldFramePrediction().getBot(bot.getBot()).getPosAt(i);
				if (isElementInWay(a, b, testPos, botRadius, safetyDistanceForBot(bot, safetyDistance)))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	private IVector2 testBot(final IVector2 point, final float safetyDistance)
	{
		for (final FutureBot bot : botPosList)
		{
			if (!ignoredBotsForCollDetect.contains(bot.getBot())
					&&
					
					testElement(point, bot, botRadius, safetyDistanceForBot(bot, safetyDistance)
					))
			{
				// log.info("Collision: " + botId.getTeamColor().toString() + " " + botId.getNumber() + " with "
				// + bot.getBot().getTeamColor().toString() + " " + bot.getBot().getNumber());
				return bot;
			}
		}
		
		return null;
	}
	
	
	private float safetyDistanceForBot(final FutureBot bot, float defaultSafetyDistance)
	{
		if (specialSafetyDistances.containsKey(bot.getBot()))
		{
			defaultSafetyDistance = Math.min(specialSafetyDistances.get(bot.getBot()), defaultSafetyDistance);
		}
		return defaultSafetyDistance;
	}
	
	
	/**
	 * checks if element would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isElementInWay(final IVector2 nodeA, final IVector2 nodeB, final IVector2 elementPos,
			final float elementRadius,
			final float safetyDistance)
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
	
	
	private boolean testElement(final IVector2 node, final IVector2 elementPos, final float elementRadius,
			final float safetyDistance)
	{
		if (GeoMath.distancePP(node, elementPos) < (botRadius + elementRadius + safetyDistance))
		{
			return true;
		}
		
		return false;
	}
	
	
	/************************************************************************************
	 ****************** Handling if the bot is in the penalty area **********************
	 ************************************************************************************/
	
	/**
	 * checks if the bot drives into the penalty area despite he is not allowed to do this
	 * 
	 * @return
	 */
	public boolean isBotIllegallyInPenaltyArea()
	{
		return (!moveCon.isPenaltyAreaAllowedOur() && penaltyAreaOur.isPointInShape(wFrame.getBot(botId).getPos(),
				margin2PenaltyAreaOurOuter));
	}
	
	
	/**
	 * gets the nearest point outside of the penalty area for the bot (the bot needs to be in the pen area)<br>
	 * it is not the point on the penarea line but in a distance of botRadius outside of the penalty area
	 * 
	 * @return
	 */
	public IVector2 getNearestNodeOutsidePenArea()
	{
		IVector2 tigerPos = wFrame.getBot(botId).getPos();
		IVector2 pointOnLine = penaltyAreaOur.nearestPointOutside(tigerPos);
		IVector2 directionVectorToOutside = pointOnLine.subtractNew(tigerPos);
		directionVectorToOutside = directionVectorToOutside.scaleToNew(0);
		return pointOnLine.addNew(directionVectorToOutside);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
	 * @return the wFrame
	 */
	public SimpleWorldFrame getwFrame()
	{
		return wFrame;
	}
	
	
	/**
	 * @return the isSecondTry
	 */
	public boolean isSecondTry()
	{
		return isSecondTry;
	}
	
	
	/**
	 * @param isSecondTry the isSecondTry to set
	 */
	public void setSecondTry(final boolean isSecondTry)
	{
		this.isSecondTry = isSecondTry;
		updateInternalFields();
	}
	
	
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
	public void setPreprocessedStart(final IVector2 preprocessedStart)
	{
		this.preprocessedStart = preprocessedStart;
	}
	
	private static class FutureBot extends FutureObstacle
	{
		/**  */
		private final BotID	bot;
		
		
		private FutureBot(final BotID botID, final IVector2 pos, final IVector2 vel)
		{
			super(pos, vel);
			bot = botID;
		}
		
		
		/**
		 * @return the bot
		 */
		public BotID getBot()
		{
			return bot;
		}
		
	}
	
	private static class FutureObstacle extends Vector2
	{
		private IVector2	vel;
		
		
		private FutureObstacle(final IVector2 pos, final IVector2 vel)
		{
			super(pos);
			this.vel = vel;
		}
		
		
		/**
		 * @return the vel
		 */
		@SuppressWarnings("unused")
		public IVector2 getVel()
		{
			return vel;
		}
	}
	
}
