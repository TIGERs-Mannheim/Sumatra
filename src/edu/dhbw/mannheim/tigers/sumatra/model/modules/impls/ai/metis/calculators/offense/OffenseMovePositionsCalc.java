/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2013
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.TuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Calculates movePositions for the OffenseRole.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffenseMovePositionsCalc extends ACalculator
{
	/*
	 * Todo:
	 * - future prediction Zeiten anpassen.
	 */
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static float							ballSpeedCap			= 0.7f;
	
	private static float							distanceToBall			= 300;
	
	@Configurable(comment = "Dist [mm] - distance between ball and bot for move destination")
	private static float							securityDistBotBall	= 100;
	
	private static final long					WAIT_BEFORE_NEW_CALC	= 160;
	
	private static Map<BotID, Long>			waitTimers				= new HashMap<BotID, Long>();
	
	private static Map<BotID, ValuePoint>	oldPositions;
	
	private enum EBallMovement
	{
		BALL_MOVING_TOWARDS_BOT,
		BALL_MOVING_AWAY_FROM_BOT,
		BALL_IN_PENALTY_OUR,
		BALL_IN_PENALTY_THEIR,
		BALL_STANDING_STILL,
		BALL_MOVING_SLOW,
		BALL_OUTSIDE_OF_FIELD,
		UNDEFINED
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------- ------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		Map<BotID, ValuePoint> positions = oldPositions;
		if (positions == null)
		{
			positions = new HashMap<BotID, ValuePoint>();
		}
		
		for (BotID key : wFrame.tigerBotsVisible.keySet())
		{
			if (waitTimers.get(key) == null)
			{
				waitTimers.put(key, (long) 0);
			}
			ValuePoint dest = calculateMovementPosition(wFrame, key);
			if (dest != null)
			{
				positions.put(key, dest);
			} else
			{
				if (positions.get(key) == null)
				{
					// just to avoid NullPointer in initPhase,
					// this won't effect actions in game.
					positions.put(key, new ValuePoint(0, 0));
				}
			}
		}
		oldPositions = positions;
		newTacticalField.setOffenseMovePositions(positions);
	}
	
	
	private ValuePoint calculateMovementPosition(final WorldFrame wFrame, final BotID botid)
	{
		
		
		ValuePoint destination = new ValuePoint(0, 0);
		EBallMovement ballMovement = determineBallMovement(wFrame, wFrame.tigerBotsVisible.get(botid).getPos(), botid,
				AngleMath.deg2rad(25));
		switch (ballMovement)
		{
			case BALL_IN_PENALTY_OUR:
				destination = calculateMovePositionWhenBallIsInOurPenArea(wFrame, botid);
				destination.value = 1;
				break;
			case BALL_IN_PENALTY_THEIR:
				destination = calculateMovePositionWhenBallIsInTheirPenArea(wFrame, botid);
				destination.value = 2;
				break;
			case BALL_MOVING_AWAY_FROM_BOT:
				destination = calculateMovePositionWhenBallIsMovingAwayFromBot(wFrame, botid);
				destination.value = 3;
				break;
			case BALL_MOVING_SLOW:
				destination = calculateMovePositionWhenBallIsMovingSlow(wFrame, botid);
				destination.value = 4;
				break;
			case BALL_MOVING_TOWARDS_BOT:
				destination = calculateMovePositionWhenBallIsMovingTowardsBot(wFrame, botid);
				destination.value = 5;
				break;
			case BALL_OUTSIDE_OF_FIELD:
				destination = calculateMovePositionWhenBallIsOutsideOfField(wFrame, botid);
				destination.value = 6;
				break;
			case BALL_STANDING_STILL:
				destination = calculateMovePositionWhenBallIsStandingStill(wFrame, botid);
				destination.value = 7;
				break;
			case UNDEFINED:
				destination = calculateMovePositionWhenBallMovementIsUndefined(wFrame, botid);
				if (destination == null)
				{
					return null;
				}
				destination.value = Float.MAX_VALUE;
				break;
		}
		destination = new ValuePoint(AiMath.adjustMovePositionWhenItsInvalid(wFrame, botid, destination),
				destination.value);
		return destination;
	}
	
	
	/**
	 * @author KaiE <kai@ehrensperger.de>
	 *         MarkG
	 * @param tolerance in radian
	 * @return EBallMovement
	 */
	private EBallMovement determineBallMovement(final WorldFrame wFrame, final IVector2 botPos, final BotID botId,
			final float tolerance)
	{
		IVector2 ballPos = wFrame.ball.getPos();
		IVector2 futureBallPos = wFrame.getWorldFramePrediction().getBall().getPosAt(1.5f);
		IVector2 ballDV = futureBallPos.subtractNew(ballPos);
		ballDV = ballDV.normalizeNew();
		
		// add here check for insider enemyPen if needed
		if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(ballPos, -300))
		{
			return EBallMovement.BALL_IN_PENALTY_THEIR;
		}
		else if (AIConfig.getGeometry().getPenaltyAreaOur()
				.isPointInShape(ballPos, AIConfig.getGeometry().getBotRadius() * 2))
		{
			return EBallMovement.BALL_IN_PENALTY_OUR;
		} else if (!AIConfig.getGeometry().getField().isPointInShape(ballPos))
		{
			return EBallMovement.BALL_OUTSIDE_OF_FIELD;
		} else
		{
			TrackedBall ball = wFrame.getBall();
			IVector2 ballToBot = botPos.subtractNew(ball.getPos());
			
			float angle = GeoMath.angleBetweenVectorAndVectorWithNegative(ballToBot, ball.getVel());
			
			angle = Math.abs(angle);
			angle = Math.abs(angle > (Math.PI / 2) ? angle - ((float) Math.PI) : angle);
			
			boolean con1 = (angle - tolerance) < 0;
			boolean con2 = ((ball.getVel().x() * ballToBot.x()) > 0) && ((ball.getVel().y() * ballToBot.y()) > 0);
			boolean con3 = ((angle < ((Math.PI / 2) + tolerance)) && (angle > ((Math.PI / 2) - tolerance)));
			
			int movement = -1;
			movement = con1 ? (con2) ? 0 : 1 : con3 ? -1 : -1;
			
			if ((movement == 0) && (wFrame.getBall().getPos().x() > botPos.x()) && (futureBallPos.x() < botPos.x()))
			{
				return EBallMovement.BALL_MOVING_TOWARDS_BOT;
			} else if ((movement == 1) && (ballDV.x() > (ballDV.y() * 2)) && (ballDV.x() > 0))
			{
				return EBallMovement.BALL_MOVING_AWAY_FROM_BOT;
			}
		}
		if ((wFrame.getBall().getVel().getLength2() < 0.2))
		{
			return EBallMovement.BALL_STANDING_STILL;
		} else if ((wFrame.getBall().getVel().getLength2() < (ballSpeedCap)))
		{
			return EBallMovement.BALL_MOVING_SLOW;
		}
		return EBallMovement.UNDEFINED;
	}
	
	
	// --------------------------------------------------------------------------
	// --- Movement-Calculators -------------------------------------------------
	// --------------------------------------------------------------------------
	
	private ValuePoint calculateMovePositionWhenBallIsMovingTowardsBot(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.ball.getPos();
		IVector2 futureBallPos = wFrame.getWorldFramePrediction().getBall().getPosAt(1.5f);
		IVector2 botPos = wFrame.tigerBotsVisible.get(key).getPos();
		
		IVector2 kickerPos = AiMath.getBotKickerPos(wFrame.getTiger(key));
		IVector2 offset = botPos.subtractNew(kickerPos);
		
		destination = new ValuePoint(GeoMath.leadPointOnLine(botPos,
				new Line(ballPos, futureBallPos.subtractNew(ballPos))), 0);
		destination = new ValuePoint(destination.addNew(offset));
		return destination;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallIsMovingAwayFromBot(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 futureBallPos = wFrame.getWorldFramePrediction().getBall().getPosAt(1.5f);
		
		destination = new ValuePoint(futureBallPos, 0);
		return destination;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallIsInOurPenArea(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.ball.getPos();
		IVector2 botPos = wFrame.tigerBotsVisible.get(key).getPos();
		PenaltyArea penArea = AIConfig.getGeometry().getPenaltyAreaOur();
		
		// behind penArea?
		if (botPos.x() <= (-AIConfig.getGeometry().getFieldLength() / 2))
		{
			// this will result in an acceptable new destination
			botPos = AVector2.ZERO_VECTOR;
		}
		destination = new ValuePoint(penArea.nearestPointOutside(ballPos, botPos).addNew(new Vector2(800, 0)), 0);
		return destination;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallIsInTheirPenArea(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.ball.getPos();
		destination = new ValuePoint(ballPos.addNew(AIConfig.getGeometry().getGoalOur().getGoalCenter()
				.subtractNew(ballPos).normalizeNew().multiplyNew(1500)), 0);
		return destination;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallIsStandingStill(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.ball.getPos();
		IVector2 target = AiMath.determineChipShotTarget(wFrame, 0, AIConfig.getGeometry().getGoalTheir().getGoalCenter()
				.x());
		if (target == null)
		{
			target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		}
		IVector2 targetDir = target.subtractNew(ballPos).normalizeNew();
		destination = new ValuePoint(ballPos.addNew(targetDir.multiplyNew(-distanceToBall)));
		return destination;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallIsMovingSlow(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.ball.getPos();
		IVector2 ballVel = wFrame.ball.getVel().multiplyNew(300);
		IVector2 target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		IVector2 ballToGoal = target.subtractNew(ballPos).normalizeNew();
		destination = new ValuePoint(ballPos.addNew(ballToGoal.multiplyNew(-200)).addNew(ballVel));
		return destination;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallIsOutsideOfField(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.ball.getPos();
		destination = new ValuePoint(ballPos.addNew(AVector2.ZERO_VECTOR.subtractNew(ballPos).normalizeNew()
				.multiplyNew(800)), 0);
		return destination;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallMovementIsUndefined(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		
		if ((System.currentTimeMillis() - waitTimers.get(key)) > WAIT_BEFORE_NEW_CALC)
		{
			float neededTime = 1.0f;
			destination = new ValuePoint(wFrame.ball.getPosAt(neededTime), 0);
			destination = new ValuePoint(calcMovePosition(destination, wFrame, key));
			
			waitTimers.put(key, System.currentTimeMillis());
		}
		return destination;
	}
	
	
	private IVector2 calcMovePosition(final Vector2 initDestination, final WorldFrame wFrame, final BotID key)
	{
		IVector2 destination = initDestination;
		Sisyphus sis = new Sisyphus();
		MovementCon moveCon = new MovementCon();
		
		destination = AiMath.adjustMovePositionWhenItsInvalid(wFrame, key, destination);
		
		moveCon.updateDestination(destination);
		moveCon.setBallObstacle(true);
		moveCon.setPenaltyAreaAllowed(true);
		
		float splineLength = sis.calculateSpline(key, wFrame, moveCon, TuneableParameter.getParamsForApproximation())
				.getTotalTime();
		destination = wFrame.getBall().getPosAt(splineLength);
		
		return destination;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
