/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2013
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.List;
import java.util.logging.Logger;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveMovePosition;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveMovePosition.EOffensiveMoveType;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;
import edu.tigers.sumatra.wp.data.WorldFrame;


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
	protected static final Logger	log				= Logger.getLogger(OffensiveMovePositionsCalc.class
			.getName());
	
	private static double			distanceToBall	= 400;
	
	
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
		for (BotID key : wFrame.tigerBotsAvailable.keySet())
		{
			OffensiveMovePosition dest = calculateMovementPosition(wFrame, key, newTacticalField);
			if (newTacticalField.getOffensiveActions().containsKey(key))
			{
				newTacticalField.getOffensiveActions().get(key).setMovePosition(dest);
			}
		}
		// drawPositions(newTacticalField.getOffensiveActions(), baseAiFrame, newTacticalField);
	}
	
	
	private OffensiveMovePosition calculateMovementPosition(final WorldFrame wFrame, final BotID botid,
			final TacticalField newTacticalField)
	{
		IVector2 destination = AVector2.ZERO_VECTOR;
		EBallMovement ballMovement = determineBallMovement(wFrame, wFrame.tigerBotsAvailable.get(botid).getPos(), botid,
				newTacticalField);
		EOffensiveMoveType type = null;
		
		
		if ((newTacticalField.getGameState() == EGameStateTeam.CORNER_KICK_WE) ||
				(newTacticalField.getGameState() == EGameStateTeam.GOAL_KICK_WE) ||
				(newTacticalField.getGameState() == EGameStateTeam.THROW_IN_WE) ||
				(newTacticalField.getGameState() == EGameStateTeam.DIRECT_KICK_WE))
		{
			destination = calcDelayMoveTarget(wFrame, botid);
			type = EOffensiveMoveType.NORMAL;
		} else if (newTacticalField.getGameState() == EGameStateTeam.STOPPED)
		{
			destination = calcStopMoveTarget(wFrame, botid);
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
		
		destination = correctMovePositionTowardsBall(destination, wFrame, botid);
		destination = correctMovePositionCircular(destination, wFrame, botid);
		
		PenaltyArea ourPen = Geometry.getPenaltyAreaOur();
		if (ourPen.isPointInShape(destination, 300))
		{
			while (ourPen.isPointInShape(destination, 300))
			{
				IVector2 dir = wFrame.getBall().getVel();
				if (dir.isZeroVector() || (dir.getLength2() < 0.1))
				{
					dir = Geometry.getGoalOur().getGoalCenter().subtractNew(wFrame.getBall().getPos());
				}
				destination = destination.addNew(dir.normalizeNew().multiplyNew(-50));
			}
		}
		if (!Geometry.getField().isPointInShape(destination))
		{
			destination = Geometry.getField().nearestPointInside(destination, 200);
		}
		
		if (ourPen.isPointInShape(destination, 300))
		{
			destination = ourPen.nearestPointOutside(destination, 350);
		}
		
		if (Geometry.getPenaltyAreaTheir()
				.isPointInShape(destination, Geometry.getBotRadius() + 30))
		{
			destination = Geometry.getPenaltyAreaTheir().nearestPointOutside(destination, 200);
		}
		
		if (newTacticalField.getGameState() == EGameStateTeam.PREPARE_KICKOFF_WE)
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
		if (!Geometry.getField().isPointInShape(ballPos))
		{
			return EBallMovement.BALL_OUTSIDE_OF_FIELD;
		} else if (Geometry.getPenaltyAreaTheir().isPointInShape(ballPos, 150))
		{
			return EBallMovement.BALL_IN_PENALTY_THEIR;
		} else if (Geometry.getPenaltyAreaOur().isPointInShape(ballPos,
				Geometry.getPenaltyAreaMargin()))
		{
			return EBallMovement.BALL_IN_PENALTY_OUR;
		}
		
		if (ballVel.getLength2() > 0.5)
		{
			final double REDIRECT_TOLERANCE = 350;
			
			IVector2 left = new Vector2(ballVel.getAngle() - 0.4).normalizeNew();
			IVector2 right = new Vector2(ballVel.getAngle() + 0.4).normalizeNew();
			
			IVector2 futureBall = wFrame.getBall().getPosByVel(0f);
			
			double dist = GeoMath.distancePP(ballPos, futureBall) - REDIRECT_TOLERANCE;
			
			IVector2 lp = ballPos.addNew(left.multiplyNew(dist));
			IVector2 rp = ballPos.addNew(right.multiplyNew(dist));
			
			IVector2 lpm = ballPos.addNew(left.multiplyNew(-Geometry.getFieldLength() * 2));
			IVector2 rpm = ballPos.addNew(right.multiplyNew(-Geometry.getFieldLength() * 2));
			
			DrawableTriangle dtri = new DrawableTriangle(ballPos, lp, rp, new Color(255, 0, 0, 100));
			dtri.setFill(true);
			
			DrawableTriangle dtrim = new DrawableTriangle(ballPos, lpm, rpm, new Color(255, 0, 0, 100));
			dtri.setFill(true);
			
			BotID key = botId;
			IVector2 pos = wFrame.tigerBotsAvailable.get(key).getPos();
			IVector2 kpos = wFrame.tigerBotsAvailable.get(key).getBotKickerPos();
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
		IVector2 futureBallPos = wFrame.getBall().getPosByTime(0.5f);
		Line line = Line.newLine(wFrame.getBall().getPos(), futureBallPos);
		return GeoMath.leadPointOnLine(wFrame.getTiger(key).getPos(), line);
	}
	
	
	private IVector2 calculateMovePositionWhenBallIsMovingAwayFromBot(final WorldFrame wFrame, final BotID key)
	{
		IVector2 destination = null;
		IVector2 futureBallPos = wFrame.getBall().getPosByTime(1.5f);
		destination = new ValuePoint(futureBallPos, 0);
		if (GeoMath.distancePP(destination, wFrame.getBall().getPos()) < 50)
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
		PenaltyArea penArea = Geometry.getPenaltyAreaOur();
		
		// behind penArea?
		if (botPos.x() <= (-Geometry.getFieldLength() / 2.0))
		{
			// this will result in an acceptable new destination
			botPos = AVector2.ZERO_VECTOR;
		}
		destination = new ValuePoint(penArea.nearestPointOutside(ballPos, botPos, Geometry.getBotRadius())
				.addNew(new Vector2(300, 0)), 0);
		return destination;
	}
	
	
	private IVector2 calculateMovePositionWhenBallIsInTheirPenArea(final WorldFrame wFrame, final BotID key)
	{
		ValuePoint destination = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		destination = new ValuePoint(ballPos.addNew(Geometry.getGoalOur().getGoalCenter()
				.subtractNew(ballPos).normalizeNew().multiplyNew(1500)), 0);
		return destination;
	}
	
	
	private IVector2 calculateMovePositionWhenBallIsStandingStill(final WorldFrame wFrame, final BotID key)
	{
		IVector2 dest = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 target = AiMath.determineChipShotTarget(wFrame, 0, Geometry.getGoalTheir().getGoalCenter()
				.x());
		if (target == null)
		{
			target = Geometry.getGoalTheir().getGoalCenter();
		}
		
		IVector2 targetToBall = ballPos.subtractNew(target).normalizeNew();
		IVector2 ballToOurGoal = Geometry.getGoalOur().getGoalCenter().subtractNew(ballPos).normalizeNew();
		
		IVector2 firstPotentialPos = ballPos.addNew(targetToBall.multiplyNew(distanceToBall));
		IVector2 secondPotentialPos = ballPos.addNew(ballToOurGoal.multiplyNew(distanceToBall));
		IVector2 thirdPotentialPos = ballPos.addNew(targetToBall.multiplyNew(-distanceToBall));
		
		if (isMoveTargetValid(firstPotentialPos, wFrame, key))
		{
			dest = firstPotentialPos;
		} else if (isMoveTargetValid(secondPotentialPos, wFrame, key))
		{
			dest = secondPotentialPos;
		} else if (isMoveTargetValid(thirdPotentialPos, wFrame, key))
		{
			dest = thirdPotentialPos;
		} else
		{
			// fallback calculation, this should only happen in very special cases.
			IVector2 targetDir = target.subtractNew(ballPos).normalizeNew();
			dest = new ValuePoint(ballPos.addNew(targetDir.multiplyNew(-distanceToBall)));
			
			Rectangle fieldBorders = Geometry.getField();
			if (!fieldBorders.isPointInShape(dest) && !wFrame.getBall().getVel().isZeroVector())
			{
				IVector2 dir = wFrame.getBall().getVel();
				fieldBorders = Geometry.getField();
				List<IVector2> intersections = fieldBorders
						.lineIntersection((new Line(wFrame.getBall().getPos(), dir)));
				for (IVector2 intersection : intersections)
				{
					IVector2 ballToIntersection = intersection.subtractNew(wFrame.getBall().getPos()).normalizeNew();
					double angleDif = ballToIntersection.getAngle() - wFrame.getBall().getVel().normalizeNew().getAngle();
					if (Math.abs(angleDif) < 0.5)
					{
						dest = intersection;
						break;
					}
				}
			}
			dest = correctMovePositionCircular(dest, wFrame, key);
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
		double splineLength = 1.5;// estimator.calcTravelTime(wFrame.getBot(key), destination, wFrame,
		// wFrame.getBot(key).getAngle());
		splineLength = Math.min(splineLength, 5.0);
		splineLength = Math.max(splineLength, 0.5);
		IVector2 destination = wFrame.getBall().getPosByTime(splineLength);
		destination = AiMath.adjustMovePositionWhenItsInvalid(wFrame, key, destination);
		return destination;
	}
	
	
	private IVector2 correctMovePositionTowardsBall(final IVector2 pos, final WorldFrame wFrame, final BotID key)
	{
		IVector2 moveTarget = pos;
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 targetToBall = ballPos.subtractNew(moveTarget).normalizeNew().multiplyNew(20f);
		int i = 0;
		while (!isMoveTargetValid(moveTarget, wFrame, key))
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
	
	
	private IVector2 correctMovePositionCircular(final IVector2 pos, final WorldFrame wFrame, final BotID key)
	{
		IVector2 ballPos = wFrame.getBall().getPos();
		double turnangle = -20;
		if (ballPos.y() > 0)
		{
			turnangle = -turnangle;
		}
		double distanceToBall = Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 1.5);
		Circle positionCircle = new Circle(ballPos, distanceToBall);
		
		IVector2 moveTarget = pos;
		moveTarget = positionCircle.nearestPointOutside(moveTarget);
		
		int i = 0;
		while (!isMoveTargetValid(moveTarget, wFrame, key))
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
	
	
	private boolean isMoveTargetValid(final IVector2 moveTarget, final WorldFrame wFrame, final BotID key)
	{
		double marginPenalty = Geometry.getPenaltyAreaMargin() + 50;
		if (!Geometry.getField().isPointInShape(moveTarget))
		{
			return false;
		}
		if (Geometry.getPenaltyAreaOur().isPointInShape(moveTarget, marginPenalty))
		{
			return false;
		}
		if (Geometry.getPenaltyAreaTheir().isPointInShape(moveTarget, marginPenalty))
		{
			return false;
		}
		BotIDMap<ITrackedBot> botMap = new BotIDMap<>();
		for (BotID id : wFrame.getTigerBotsAvailable().keySet())
		{
			botMap.put(id, wFrame.getTigerBotsVisible().get(id));
		}
		for (BotID id : wFrame.getFoeBots().keySet())
		{
			botMap.put(id, wFrame.getFoeBot(id));
		}
		botMap.remove(key);
		
		for (BotID id : botMap.keySet())
		{
			if (GeoMath.distancePP(moveTarget, botMap.get(id).getPos()) < (Geometry.getBotRadius() * 3))
			{
				return false;
			}
		}
		return true;
	}
	
	
	private IVector2 calcStopMoveTarget(final WorldFrame wFrame, final BotID key)
	{
		IVector2 moveTarget = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		double distanceToBall = Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 2.0);
		Circle positionCircle = new Circle(ballPos, distanceToBall);
		
		moveTarget = ballPos.subtractNew(new Vector2(Geometry.getBotToBallDistanceStop(), 0));
		moveTarget = positionCircle.nearestPointOutside(moveTarget);
		
		int i = 0;
		while (!isMoveTargetValid(moveTarget, wFrame, key))
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
	
	
	private IVector2 calcDelayMoveTarget(final WorldFrame wFrame, final BotID key)
	{
		IVector2 moveTarget = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		double distanceToBall = Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 1.5);
		Circle positionCircle = new Circle(ballPos, distanceToBall);
		
		moveTarget = ballPos.subtractNew(new Vector2(Geometry.getBotToBallDistanceStop(), 0));
		positionCircle.nearestPointOutside(moveTarget);
		
		int i = 0;
		while (!isMoveTargetValid(moveTarget, wFrame, key))
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
