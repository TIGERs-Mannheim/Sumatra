/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2013
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.DrawableTriangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition.EOffensiveMoveType;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * Calculates movePositions for the OffenseRole.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveMovePositionsCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected static final Logger					log				= Logger.getLogger(OffensiveMovePositionsCalc.class
																						.getName());
	
	private static float								distanceToBall	= 400;
	
	private Map<BotID, OffensiveMovePosition>	oldPositions;
	
	private enum EBallMovement
	{
		BALL_MOVING_TOWARDS_BOT,
		BALL_MOVING_AWAY_FROM_BOT_TO_ENEMY_GOAL,
		BALL_IN_PENALTY_OUR,
		BALL_IN_PENALTY_THEIR,
		BALL_STANDING_STILL,
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
		Map<BotID, OffensiveMovePosition> positions = oldPositions;
		if (positions == null)
		{
			positions = new HashMap<BotID, OffensiveMovePosition>();
		}
		
		for (BotID key : wFrame.tigerBotsAvailable.keySet())
		{
			OffensiveMovePosition dest = calculateMovementPosition(wFrame, key, newTacticalField);
			positions.put(key, dest);
		}
		oldPositions = positions;
		drawPositions(baseAiFrame, newTacticalField);
		newTacticalField.setOffenseMovePositions(positions);
	}
	
	
	private OffensiveMovePosition calculateMovementPosition(final WorldFrame wFrame, final BotID botid,
			final TacticalField newTacticalField)
	{
		IVector2 destination = AVector2.ZERO_VECTOR;
		EBallMovement ballMovement = determineBallMovement(wFrame, wFrame.tigerBotsAvailable.get(botid).getPos(), botid,
				newTacticalField);
		EOffensiveMoveType type = null;
		
		
		if ((newTacticalField.getGameState() == EGameState.CORNER_KICK_WE) ||
				(newTacticalField.getGameState() == EGameState.GOAL_KICK_WE) ||
				(newTacticalField.getGameState() == EGameState.THROW_IN_WE) ||
				(newTacticalField.getGameState() == EGameState.DIRECT_KICK_WE))
		{
			destination = calcDelayMoveTarget(wFrame);
			type = EOffensiveMoveType.NORMAL;
		} else if (newTacticalField.getGameState() == EGameState.STOPPED)
		{
			destination = calcStopMoveTarget(wFrame);
			type = EOffensiveMoveType.NORMAL;
		} else
		{
			switch (ballMovement)
			{
				case BALL_IN_PENALTY_OUR:
					destination = calculateMovePositionWhenBallIsInOurPenArea(wFrame, botid);
					type = EOffensiveMoveType.UNREACHABLE;
					break;
				case BALL_IN_PENALTY_THEIR:
					destination = calculateMovePositionWhenBallIsInTheirPenArea(wFrame, botid);
					type = EOffensiveMoveType.NORMAL;
					break;
				case BALL_MOVING_AWAY_FROM_BOT_TO_ENEMY_GOAL:
					destination = calculateMovePositionWhenBallIsMovingAwayFromBot(wFrame, botid);
					type = EOffensiveMoveType.IGNORE_BALL;
					break;
				case BALL_MOVING_TOWARDS_BOT:
					destination = calculateMovePositionWhenBallIsMovingTowardsBot(wFrame, botid);
					type = EOffensiveMoveType.NORMAL;
					break;
				case BALL_OUTSIDE_OF_FIELD:
					destination = calculateMovePositionWhenBallIsOutsideOfField(wFrame, botid);
					type = EOffensiveMoveType.UNREACHABLE;
					break;
				case BALL_STANDING_STILL:
					destination = calculateMovePositionWhenBallIsStandingStill(wFrame, botid);
					type = EOffensiveMoveType.NORMAL;
					break;
				case UNDEFINED:
					destination = calculateMovePositionWhenBallMovementIsUndefined(wFrame, botid);
					type = EOffensiveMoveType.NORMAL;
					break;
			}
		}
		
		destination = correctMovePositionTowardsBall(destination, wFrame);
		destination = correctMovePositionCircular(destination, wFrame);
		
		PenaltyArea ourPen = AIConfig.getGeometry().getPenaltyAreaOur();
		if (ourPen.isPointInShape(destination, 300))
		{
			while (ourPen.isPointInShape(destination, 300))
			{
				IVector2 dir = wFrame.getBall().getVel();
				if (dir.isZeroVector() || (dir.getLength2() < 0.1))
				{
					dir = AIConfig.getGeometry().getGoalOur().getGoalCenter().subtractNew(wFrame.getBall().getPos());
				}
				destination = destination.addNew(dir.normalizeNew().multiplyNew(-50));
			}
		}
		if (!AIConfig.getGeometry().getField().isPointInShape(destination))
		{
			destination = AIConfig.getGeometry().getField().nearestPointInside(destination, 200f);
		}
		
		if (ourPen.isPointInShape(destination, 300))
		{
			destination = ourPen.nearestPointOutside(destination, 350f);
		}
		
		if (AIConfig.getGeometry().getPenaltyAreaTheir()
				.isPointInShape(destination, AIConfig.getGeometry().getBotRadius() + 30))
		{
			destination = AIConfig.getGeometry().getPenaltyAreaTheir().nearestPointOutside(destination, 200f);
		}
		
		if (newTacticalField.getGameState() == EGameState.PREPARE_KICKOFF_WE)
		{
			destination = wFrame.getBall().getPos().subtractNew(new Vector2(200, 0));
		}
		
		OffensiveMovePosition pos = new OffensiveMovePosition(destination, wFrame.getTiger(botid), type);
		pos.generateScoring(wFrame, newTacticalField);
		return pos;
	}
	
	
	/**
	 * @author MarkG
	 * @return EBallMovement
	 */
	private EBallMovement determineBallMovement(final WorldFrame wFrame, final IVector2 botPos, final BotID botId,
			final TacticalField newTacticalField)
	{
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 ballVel = wFrame.getBall().getVel();
		if (!AIConfig.getGeometry().getField().isPointInShape(ballPos))
		{
			return EBallMovement.BALL_OUTSIDE_OF_FIELD;
		} else if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(ballPos, 150f))
		{
			return EBallMovement.BALL_IN_PENALTY_THEIR;
		} else if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ballPos,
				Geometry.getPenaltyAreaMargin()))
		{
			return EBallMovement.BALL_IN_PENALTY_OUR;
		}
		
		if (ballVel.getLength2() > 0.5f)
		{
			final float REDIRECT_TOLERANCE = 350f;
			
			IVector2 left = new Vector2(ballVel.getAngle() - 0.4f).normalizeNew();
			IVector2 right = new Vector2(ballVel.getAngle() + 0.4f).normalizeNew();
			
			IVector2 futureBall = wFrame.getBall().getPosByVel(0f);
			
			float dist = GeoMath.distancePP(ballPos, futureBall) - REDIRECT_TOLERANCE;
			
			IVector2 lp = ballPos.addNew(left.multiplyNew(dist));
			IVector2 rp = ballPos.addNew(right.multiplyNew(dist));
			
			IVector2 lpm = ballPos.addNew(left.multiplyNew(-AIConfig.getGeometry().getFieldLength() * 2));
			IVector2 rpm = ballPos.addNew(right.multiplyNew(-AIConfig.getGeometry().getFieldLength() * 2));
			
			DrawableTriangle dtri = new DrawableTriangle(ballPos, lp, rp, new Color(255, 0, 0, 100));
			dtri.setFill(true);
			
			DrawableTriangle dtrim = new DrawableTriangle(ballPos, lpm, rpm, new Color(255, 0, 0, 100));
			dtri.setFill(true);
			
			BotID key = botId;
			IVector2 pos = wFrame.tigerBotsAvailable.get(key).getPos();
			IVector2 kpos = AiMath.getBotKickerPos(wFrame.tigerBotsAvailable.get(key));
			if (dtri.isPointInShape(pos) || dtri.isPointInShape(kpos))
			{
				return EBallMovement.BALL_MOVING_TOWARDS_BOT;
			} else if (dtrim.isPointInShape(pos) || dtrim.isPointInShape(kpos))
			{
				// ball moving away from bot
				if (ballVel.x() > 0)
				{
					return EBallMovement.BALL_MOVING_AWAY_FROM_BOT_TO_ENEMY_GOAL;
				}
				return EBallMovement.UNDEFINED;
			}
			return EBallMovement.UNDEFINED;
		}
		return EBallMovement.BALL_STANDING_STILL;
	}
	
	
	// --------------------------------------------------------------------------
	// --- Movement-Calculators -------------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2 calculateMovePositionWhenBallIsMovingTowardsBot(final WorldFrame wFrame, final BotID key)
	{
		IVector2 futureBallPos = wFrame.getWorldFramePrediction().getBall().getPosAt(0.5f);
		Line line = Line.newLine(wFrame.getBall().getPos(), futureBallPos);
		return GeoMath.leadPointOnLine(wFrame.getTiger(key).getPos(), line);
	}
	
	
	private IVector2 calculateMovePositionWhenBallIsMovingAwayFromBot(final WorldFrame wFrame, final BotID key)
	{
		IVector2 destination = null;
		IVector2 futureBallPos = wFrame.getWorldFramePrediction().getBall().getPosAt(1.5f);
		destination = new ValuePoint(futureBallPos, 0);
		if (GeoMath.distancePP(destination, wFrame.getBall().getPos()) < 50f)
		{
			IVector2 dir = wFrame.getBall().getPos().subtractNew(destination).normalizeNew();
			destination = new Vector2(destination.subtractNew(dir.multiplyNew(150f)));
		}
		return destination;
	}
	
	
	private IVector2 calculateMovePositionWhenBallIsInOurPenArea(final WorldFrame wFrame, final BotID key)
	{
		IVector2 destination = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 botPos = wFrame.tigerBotsAvailable.get(key).getPos();
		PenaltyArea penArea = AIConfig.getGeometry().getPenaltyAreaOur();
		
		// behind penArea?
		if (botPos.x() <= (-AIConfig.getGeometry().getFieldLength() / 2))
		{
			// this will result in an acceptable new destination
			botPos = AVector2.ZERO_VECTOR;
		}
		destination = new ValuePoint(penArea.nearestPointOutside(ballPos, botPos, AIConfig.getGeometry().getBotRadius())
				.addNew(new Vector2(300, 0)), 0);
		return destination;
	}
	
	
	private IVector2 calculateMovePositionWhenBallIsInTheirPenArea(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		destination = new ValuePoint(ballPos.addNew(AIConfig.getGeometry().getGoalOur().getGoalCenter()
				.subtractNew(ballPos).normalizeNew().multiplyNew(1500)), 0);
		return destination;
	}
	
	
	private IVector2 calculateMovePositionWhenBallIsStandingStill(final WorldFrame wFrame, final BotID key)
	{
		IVector2 dest = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 target = AiMath.determineChipShotTarget(wFrame, 0, AIConfig.getGeometry().getGoalTheir().getGoalCenter()
				.x());
		if (target == null)
		{
			target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		}
		
		IVector2 targetToBall = ballPos.subtractNew(target).normalizeNew();
		IVector2 ballToOurGoal = AIConfig.getGeometry().getGoalOur().getGoalCenter().subtractNew(ballPos).normalizeNew();
		
		IVector2 firstPotentialPos = ballPos.addNew(targetToBall.multiplyNew(distanceToBall));
		IVector2 secondPotentialPos = ballPos.addNew(ballToOurGoal.multiplyNew(distanceToBall));
		IVector2 thirdPotentialPos = ballPos.addNew(targetToBall.multiplyNew(-distanceToBall));
		
		if (isMoveTargetValid(firstPotentialPos))
		{
			dest = firstPotentialPos;
		} else if (isMoveTargetValid(secondPotentialPos))
		{
			dest = secondPotentialPos;
		} else if (isMoveTargetValid(thirdPotentialPos))
		{
			dest = thirdPotentialPos;
		} else
		{
			// fallback calculation, this should only happen in very special cases.
			IVector2 targetDir = target.subtractNew(ballPos).normalizeNew();
			dest = new ValuePoint(ballPos.addNew(targetDir.multiplyNew(-distanceToBall)));
			
			Rectangle fieldBorders = AIConfig.getGeometry().getField();
			if (!fieldBorders.isPointInShape(dest) && !wFrame.getBall().getVel().isZeroVector())
			{
				IVector2 dir = wFrame.getBall().getVel();
				fieldBorders = AIConfig.getGeometry().getField();
				List<IVector2> intersections = fieldBorders
						.lineIntersection((new Line(wFrame.getBall().getPos(), dir)));
				for (IVector2 intersection : intersections)
				{
					IVector2 ballToIntersection = intersection.subtractNew(wFrame.getBall().getPos()).normalizeNew();
					float angleDif = ballToIntersection.getAngle() - wFrame.getBall().getVel().normalizeNew().getAngle();
					if (Math.abs(angleDif) < 0.5f)
					{
						dest = intersection;
						break;
					}
				}
			}
			dest = correctMovePositionCircular(dest, wFrame);
		}
		return dest;
	}
	
	
	private ValuePoint calculateMovePositionWhenBallIsOutsideOfField(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		destination = new ValuePoint(ballPos.addNew(AVector2.ZERO_VECTOR.subtractNew(ballPos).normalizeNew()
				.multiplyNew(800)), 0);
		return destination;
	}
	
	
	private IVector2 calculateMovePositionWhenBallMovementIsUndefined(final WorldFrame wFrame, final BotID key)
	{
		
		// PathEstimator estimator = new PathEstimator();
		float splineLength = 1.5f;// estimator.calcTravelTime(wFrame.getBot(key), destination, wFrame,
		// wFrame.getBot(key).getAngle());
		splineLength = Math.min(splineLength, 5.0f);
		splineLength = Math.max(splineLength, 0.5f);
		IVector2 destination = wFrame.getBall().getPosByTime(splineLength);
		destination = AiMath.adjustMovePositionWhenItsInvalid(wFrame, key, destination);
		return destination;
	}
	
	
	private void drawPositions(final BaseAiFrame baseAiFrame, final TacticalField newTacticalField)
	{
		for (Map.Entry<BotID, OffensiveMovePosition> entry : oldPositions.entrySet())
		{
			EOffensiveMoveType type = oldPositions.get(entry.getKey()).getType();
			Color color = new Color(111, 120, 20, 130);
			switch (type)
			{
				case IGNORE_BALL:
					color = new Color(211, 120, 20, 220);
					break;
				case NORMAL:
					break;
				case UNREACHABLE:
					color = new Color(11, 120, 255, 220);
					break;
				default:
					break;
			}
			TrackedTigerBot tigerBot = baseAiFrame.getWorldFrame().getTiger(entry.getKey());
			if (tigerBot != null)
			{
				IVector2 botPos = tigerBot.getPos();
				IVector2 dest = oldPositions.get(entry.getKey());
				DrawableCircle dc = new DrawableCircle(new Circle(dest, 60f), color);
				IVector2 normal = botPos.subtractNew(dest).getNormalVector();
				IVector2 p1 = botPos.addNew(normal.multiplyNew(AIConfig.getGeometry().getBotRadius()));
				IVector2 p2 = dest.addNew(normal.multiplyNew(48.4f));
				IVector2 p3 = botPos.addNew(normal.multiplyNew(-AIConfig.getGeometry().getBotRadius()));
				IVector2 p4 = dest.addNew(normal.multiplyNew(-48.4f));
				DrawableLine dl1 = new DrawableLine(Line.newLine(p1, p2), color, false);
				DrawableLine dl2 = new DrawableLine(Line.newLine(p3, p4), color, false);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dc);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dl1);
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dl2);
			}
		}
	}
	
	
	private IVector2 correctMovePositionTowardsBall(final IVector2 pos, final WorldFrame wFrame)
	{
		IVector2 moveTarget = pos;
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 targetToBall = ballPos.subtractNew(moveTarget).normalizeNew().multiplyNew(20f);
		int i = 0;
		while (!isMoveTargetValid(moveTarget))
		{
			moveTarget = moveTarget.addNew(targetToBall);
			if (i > 50)
			{
				break;
			}
			i++;
		}
		return moveTarget;
	}
	
	
	private IVector2 correctMovePositionCircular(final IVector2 pos, final WorldFrame wFrame)
	{
		IVector2 ballPos = wFrame.getBall().getPos();
		float turnangle = -20;
		if (ballPos.y() > 0)
		{
			turnangle = -turnangle;
		}
		float distanceToBall = AIConfig.getGeometry().getBotToBallDistanceStop()
				+ (AIConfig.getGeometry().getBotRadius() * 1.5f);
		Circle positionCircle = new Circle(ballPos, distanceToBall);
		
		IVector2 moveTarget = pos;
		moveTarget = positionCircle.nearestPointOutside(moveTarget);
		
		int i = 0;
		while (!isMoveTargetValid(moveTarget))
		{
			moveTarget = GeoMath.stepAlongCircle(moveTarget, ballPos, AngleMath.DEG_TO_RAD * turnangle);
			if (i > 18)
			{
				break;
			}
			i++;
		}
		return moveTarget;
	}
	
	
	private boolean isMoveTargetValid(final IVector2 moveTarget)
	{
		float marginPenalty = Geometry.getPenaltyAreaMargin() + 50f;
		if (!AIConfig.getGeometry().getField().isPointInShape(moveTarget))
		{
			return false;
		}
		if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(moveTarget, marginPenalty))
		{
			return false;
		}
		if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(moveTarget, marginPenalty))
		{
			return false;
		}
		return true;
	}
	
	
	private IVector2 calcStopMoveTarget(final WorldFrame wFrame)
	{
		IVector2 moveTarget = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		float distanceToBall = AIConfig.getGeometry().getBotToBallDistanceStop()
				+ (AIConfig.getGeometry().getBotRadius() * 1.5f);
		Circle positionCircle = new Circle(ballPos, distanceToBall);
		
		moveTarget = ballPos.subtractNew(new Vector2(AIConfig.getGeometry().getBotToBallDistanceStop(), 0));
		moveTarget = positionCircle.nearestPointOutside(moveTarget);
		
		int i = 0;
		while (!isMoveTargetValid(moveTarget))
		{
			moveTarget = GeoMath.stepAlongCircle(moveTarget, ballPos, AngleMath.DEG_TO_RAD * 20);
			if (i > 18)
			{
				break;
			}
			i++;
		}
		return moveTarget;
	}
	
	
	private IVector2 calcDelayMoveTarget(final WorldFrame wFrame)
	{
		IVector2 moveTarget = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		float distanceToBall = AIConfig.getGeometry().getBotToBallDistanceStop()
				+ (AIConfig.getGeometry().getBotRadius() * 1.5f);
		Circle positionCircle = new Circle(ballPos, distanceToBall);
		
		moveTarget = ballPos.subtractNew(new Vector2(AIConfig.getGeometry().getBotToBallDistanceStop(), 0));
		positionCircle.nearestPointOutside(moveTarget);
		
		int i = 0;
		while (!isMoveTargetValid(moveTarget))
		{
			moveTarget = GeoMath.stepAlongCircle(moveTarget, ballPos, AngleMath.DEG_TO_RAD * 10);
			if (i > 36)
			{
				break;
			}
			i++;
		}
		return moveTarget;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
