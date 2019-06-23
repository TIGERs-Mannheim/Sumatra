/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.06.2016
 * Author(s): dominik
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.math;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import org.apache.commons.math3.util.Pair;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.skills.AReceiveSkill;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.ExtendedPenaltyArea;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author DominikE
 */
public final class RedirectMath
{
	
	@Configurable(comment = "Distance from the starting-point the bot is allowed to drive")
	private static double	maximumMoveDistance			= 1000;
	
	@Configurable(comment = "Distance in mm for the search steps")
	private static double	redirectSearchStepDistance	= 200;
	
	@Configurable(comment = "Speed at which we want to catch the ball so the redirect is not to far away")
	private static double	minimumDesiredBallVelocity	= 2;
	
	
	/**
	 * Calculates a better position to receive the ball during a redirect
	 * 
	 * @param wf
	 * @param ownBot
	 * @param initialReceivePosition
	 * @param shapes
	 * @return a better position
	 */
	public static IVector2 calculateBetterPosition(final WorldFrame wf, final ITrackedBot ownBot,
			final IVector2 initialReceivePosition, final List<IDrawableShape> shapes)
	{
		IVector2 ballPos = wf.getBall().getPos();
		IVector2 startingPoint = ownBot.getPos();
		IVector2 pathToBall = ballPos.subtractNew(startingPoint);
		if (wf.getBall().getVel().getLength() > 0.2)
		{
			pathToBall = wf.getBall().getVel().multiplyNew(-1);
			startingPoint = GeoMath.leadPointOnLine(startingPoint,
					new Line(wf.getBall().getPos(), wf.getBall().getVel()));
		}
		
		
		IVector2 bestPos = startingPoint;
		
		// check for foe bots intercepting us
		if (isFoeBotIntercepting(wf, startingPoint, shapes))
		{
			// drive to the ball as far as possible
			
			IVector2 posToCheck = startingPoint;
			IVector2 increment = pathToBall.scaleToNew(redirectSearchStepDistance);
			int abortCounter = 0;
			do
			{
				posToCheck = posToCheck.addNew(increment);
			} while ((++abortCounter < (maximumMoveDistance / redirectSearchStepDistance))
					&& isPositionReachable(wf, ownBot, posToCheck));
			bestPos = posToCheck;
			
			// draw shapes
			DrawableCircle dc = new DrawableCircle(posToCheck, 100, Color.red);
			dc.setFill(true);
			shapes.add(dc);
			
		} else
		{
			// use goal-chance for optimization
			
			// check whether it is worth to start moving or if the ball is about to reach the bot
			double hiscore = 0;
			double score = 0;
			
			// check bots position and three on each side
			for (int i = -3; i <= 3; i++)
			{
				IVector2 posToCheck = startingPoint.addNew(pathToBall.scaleToNew(i * redirectSearchStepDistance));
				posToCheck = validateDest(wf, ownBot, new Vector3(posToCheck, 0)).getXYVector();
				score = RedirectMath.getScoreForPoint(wf, posToCheck, ownBot, initialReceivePosition);
				DrawableText dt = new DrawableText(posToCheck, "" + score, Color.cyan);
				shapes.add(dt);
				DrawablePoint dc = new DrawablePoint(posToCheck, Color.cyan);
				shapes.add(dc);
				if (score > hiscore)
				{
					hiscore = score;
					bestPos = posToCheck;
				}
			}
			DrawablePoint dc = new DrawablePoint(bestPos, Color.red);
			shapes.add(dc);
		}
		
		
		return bestPos;
	}
	
	
	/**
	 * @param wf
	 * @param ownBot
	 * @param posToCheck
	 * @return whether the position is reachable with enough time left as specified by
	 *         AReceiveSkill.minReceiverPositioningTime
	 */
	public static boolean isPositionReachable(final WorldFrame wf, final ITrackedBot ownBot, final IVector2 posToCheck)
	{
		if (GeoMath.distancePP(posToCheck, wf.getBall().getPos()) < 1500)
		{
			return false;
		}
		
		TrajectoryGenerator generator = new TrajectoryGenerator();
		double timeBotToPos = generator.generatePositionTrajectory(ownBot, posToCheck).getTotalTime();
		
		TrackedBall ball = wf.getBall();
		double timeBallToPos;
		if (ball.getVel().getLength2() < 0.2)
		{
			timeBallToPos = 10;
		} else
		{
			timeBallToPos = ball.getTimeByPos(posToCheck);
		}
		
		return (timeBallToPos + AReceiveSkill.minReceiverPositioningTime) > timeBotToPos;
	}
	
	
	/**
	 * @return
	 */
	private static boolean isFoeBotIntercepting(final WorldFrame wf, final IVector2 posToCheck,
			final List<IDrawableShape> shapes)
	{
		double foeTimeToIntercept = calculateFoeTimeToIntercept(wf, posToCheck);
		
		DrawablePoint dp = new DrawablePoint(posToCheck, Color.red);
		shapes.add(dp);
		DrawableText dt = new DrawableText(posToCheck,
				"Intrcpt: " + calculateFoeTimeToIntercept(wf, posToCheck),
				Color.red);
		shapes.add(dt);
		
		return foeTimeToIntercept < (5 * AReceiveSkill.minReceiverPositioningTime);
	}
	
	
	/**
	 * Calculates a score for a position respecting field, penalty and other bots
	 * 
	 * @param wf
	 * @param posToCheck
	 * @param ownBot
	 * @param initialReceivePosition
	 * @return a score for the given position
	 */
	private static double getScoreForPoint(final WorldFrame wf, final IVector2 posToCheck,
			final ITrackedBot ownBot, final IVector2 initialReceivePosition)
	{
		if (initialReceivePosition.subtractNew(posToCheck).getLength2() > maximumMoveDistance)
		{
			return -1;
		}
		
		return ProbabilityMath.getDirectShootScoreChance(wf, posToCheck, false);
	}
	
	
	/**
	 * @return the lowest time, a foe bot needs to intercept the ball line
	 */
	private static double calculateFoeTimeToIntercept(final WorldFrame wf, final IVector2 posToCheck)
	{
		OptionalDouble time = wf.foeBots.values().stream()
				.mapToDouble(bot -> calculateFoeTimeToIntercept(wf, posToCheck, bot)).min();
		if (time.isPresent())
		{
			return time.getAsDouble();
		}
		return 0;
	}
	
	
	private static double calculateFoeTimeToIntercept(final WorldFrame wf, final IVector2 posToCheck,
			final ITrackedBot bot)
	{
		IVector2 foePos = bot.getPos();
		IVector2 nearestIntercept = GeoMath.nearestPointOnLineSegment(posToCheck, wf.getBall().getPos(), foePos);
		TrajectoryGenerator trajectoryGenerator = new TrajectoryGenerator();
		
		BangBangTrajectory2D trajectory = trajectoryGenerator.generatePositionTrajectory(bot, nearestIntercept);
		Line ballLine = Line.newLine(posToCheck, wf.getBall().getPos());
		double totalTime = trajectory.getTotalTime();
		for (double t = 0; (t < 1) && (t < totalTime); t += 0.2)
		{
			if (ballLine.isPointOnLine(trajectory.getPositionMM(t), 200))
			{
				return t;
			}
		}
		return totalTime;
	}
	
	
	/**
	 * @param wf
	 * @param ownBot
	 * @param dest
	 * @return a valid position
	 */
	public static IVector3 validateDest(final WorldFrame wf, final ITrackedBot ownBot, final IVector3 dest)
	{
		TrackedBall ball = wf.getBall();
		IVector2 newDest = dest.getXYVector();
		double targetAngle = dest.z();
		
		// #####################################
		IVector2 ballPos = ball.getPos();
		if (!Geometry.getFieldWBorders().isPointInShape(newDest, 0))
		{
			try
			{
				newDest = Geometry.getFieldWBorders()
						.getNearIntersectionPoint(Line.newLine(ballPos, newDest));
			} catch (MathException e)
			{
				// backup
				newDest = Geometry.getField().nearestPointInside(newDest, -Geometry.getBotRadius());
			}
			// targetAngle = ball.getPos().subtractNew(bot.getPos()).getAngle();
		}
		
		// #####################################
		IVector2 dest2Ball = newDest.subtractNew(ball.getPos());
		double botAngle = ownBot.getAngle();
		if (!dest2Ball.isZeroVector())
		{
			botAngle = dest2Ball.getAngle();
		}
		double inFieldTol = 20;
		IVector2 kickerPos = GeoMath.getBotKickerPos(newDest, botAngle,
				(ownBot.getCenter2DribblerDist()) - inFieldTol);
		if (!Geometry.getField().isPointInShape(kickerPos) &&
				Geometry.getField().isPointInShape(ballPos))
		{
			try
			{
				IVector2 intersec = Geometry.getField()
						.getNearIntersectionPoint(Line.newLine(ball.getPos(), newDest));
				newDest = GeoMath.stepAlongLine(intersec, ballPos,
						(ownBot.getCenter2DribblerDist()) - inFieldTol);
			} catch (MathException e)
			{
				// backup
				newDest = Geometry.getField().nearestPointInside(newDest, -Geometry.getBotRadius() - inFieldTol);
			}
		}
		
		// #####################################
		List<Pair<ExtendedPenaltyArea, Double>> penAreas = new ArrayList<>(2);
		penAreas.add(new Pair<>(Geometry.getPenaltyAreaOurExtended(), -100.0));
		penAreas.add(new Pair<>(Geometry.getPenaltyAreaTheirExtended(), 00.0));
		for (Pair<ExtendedPenaltyArea, Double> pair : penAreas)
		{
			ExtendedPenaltyArea penArea = pair.getFirst();
			double margin = pair.getSecond();
			if (penArea.isPointInShape(newDest, 100 - margin))
			{
				// if (penArea.isPointInShape(ballPos, Geometry.getBallRadius()))
				// {
				// IVector2 potBallPosOutside = ballPos.addNew(wf.getBall().getVel().scaleToNew(2000));
				// if (penArea.isPointInShape(potBallPosOutside, Geometry.getBallRadius()))
				// {
				// if (Geometry.getField().isPointInShape(potBallPosOutside))
				// {
				// newDest = penArea.nearestPointOutside(ballPos,
				// ownBot.getCenter2DribblerDist() - margin);
				// } else
				// {
				// newDest = penArea.nearestPointOutside(ballPos, ownBot.getPos(),
				// ownBot.getCenter2DribblerDist() - margin);
				// }
				// } else
				// {
				// newDest = penArea.nearestPointOutside(ballPos, potBallPosOutside,
				// ownBot.getCenter2DribblerDist() - margin);
				// }
				// } else
				// {
				newDest = penArea.nearestPointOutside(newDest, ballPos, 100 - margin);
				// }
				targetAngle = ball.getPos().subtractNew(ownBot.getPos()).getAngle();
			}
		}
		
		return new Vector3(newDest, targetAngle);
	}
}
