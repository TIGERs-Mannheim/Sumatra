/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.PassTarget;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate.ERedirectAction;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


/**
 * This class is used to move logic out of the RedirectKickState
 */
public class OffensiveRedirectorMath
{
	private static final Logger log = Logger.getLogger(OffensiveRedirectorMath.class);
	private static final double STATE_SWITCH_MAX_BALL_DIST = 500;
	
	private IVector2 enemyCatchPos = null;
	
	
	/**
	 * @param wf
	 * @param botMap
	 * @param passSenderBot
	 * @param newTacticalField
	 * @param currentPassReceiver
	 * @return
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	public Optional<IPassTarget> calcBestRedirectPassTarget(WorldFrame wf, Map<BotID, ITrackedBot> botMap,
			ITrackedBot passSenderBot, ITacticalField newTacticalField,
			IPassTarget currentPassReceiver)
	{
		final double hysteresis = 1.4;
		final double lookahead = 0.2;
		final double minDistForValidPass = 2500;
		final double minXLine = -800;
		
		double myScore = newTacticalField.getBestDirectShotTargetsForTigerBots().get(passSenderBot.getBotId()).getValue();
		if (myScore > OffensiveConstants.getMinScoreChanceShootInsteadRedirect())
		{
			return Optional.empty();
		}
		
		double bestPassScore = 0;
		IPassTarget passTarget = null;
		for (Map.Entry<BotID, ITrackedBot> passTargetBot : botMap.entrySet())
		{
			double distance = passSenderBot.getBotKickerPos().distanceTo(passTargetBot.getValue().getBotKickerPos());
			double passScore = calculateRedirectScoringChance(newTacticalField, wf.getBall().getPos(),
					passSenderBot.getBotKickerPos(), passTargetBot.getValue(), lookahead);
			
			if (currentPassReceiver != null && passTargetBot.getKey().equals(currentPassReceiver.getBotId()))
			{
				passScore = passScore * hysteresis;
				distance += 400; // anti toggle for current receiver
			}
			
			IVector2 validatedTargetPos = passTargetBot.getValue().getBotKickerPosByTime(lookahead);
			if (!Geometry.getField().isPointInShape(validatedTargetPos)
					|| Geometry.getPenaltyAreaTheir().isPointInShape(validatedTargetPos))
			{
				validatedTargetPos = passTargetBot.getValue().getBotKickerPos();
			}
			
			boolean isNotVisible = !isPassLineFree(passSenderBot.getPos(), validatedTargetPos,
					wf.getFoeBots().values());
			if (distance < minDistForValidPass || isNotVisible || passTargetBot.getValue().getPos().x() < minXLine)
			{
				continue;
			}
			
			DrawableAnnotation da = new DrawableAnnotation(passTargetBot.getValue().getPos(),
					String.format("vaild: %.2f", passScore));
			da.setOffset(Vector2.fromY(-200));
			da.setColor(Color.MAGENTA);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_DOUBLE_PASS).add(da);
			
			if (passScore > bestPassScore)
			{
				bestPassScore = passScore;
				passTarget = new PassTarget(validatedTargetPos, passTargetBot.getKey());
			}
		}
		
		return Optional.ofNullable(passTarget);
	}
	
	
	private boolean isPassLineFree(final IVector2 passSenderPos, final IVector2 target,
			final Collection<ITrackedBot> foeBots)
	{
		// better use some triangle here
		return AiMath.p2pVisibility(foeBots, passSenderPos, target, 500.0);
	}
	
	
	/**
	 * @param origin
	 * @param target
	 * @return
	 */
	public static double getPassDuration(final IVector2 origin, final IVector2 target)
	{
		double passVelocity = OffensiveMath.calcPassSpeedForReceivers(origin,
				target, Geometry.getGoalTheir().getCenter());
		IVector2 origin2target = Vector2.fromPoints(origin, target);
		final double timeForKick = BallFactory.createStraightConsultant().getTimeForKick(origin2target.getLength(),
				passVelocity);
		if (Double.isInfinite(timeForKick))
		{
			log.warn("Pass duration is Infinity! Vectors where: origin " + origin + ", target " + target);
		}
		return timeForKick;
	}
	
	
	/**
	 * Calculate a redirect score combining the angle (chance of a successful redirect)
	 * with a scoring chance (how many defenders are in my way to score)
	 */
	private double calculateRedirectScoringChance(final ITacticalField newTacticalField, IVector2 ballPos,
			final IVector2 source, final ITrackedBot redirectTarget, final double lookahead)
	{
		IVector2 redirectorKickerPos = redirectTarget.getBotKickerPosByTime(lookahead);
		
		// incoming ball -> source -> target; range [0, 1]; K.O. criteria
		double redirectorAngle = OffensiveMath.getRedirectAngle(ballPos, source, redirectorKickerPos);
		BetaDistribution betaDistribution = new BetaDistribution(2, 5);
		
		double redirectAngleScore;
		if (OffensiveConstants.useBetaDistributionForRedirects())
		{
			redirectAngleScore = SumatraMath.relative(betaDistribution.density(SumatraMath.relative(redirectorAngle, 0,
					OffensiveConstants.getMaximumReasonableRedirectAngle())), 0, 2.5);
		} else
		{
			redirectAngleScore = 1 - SumatraMath.relative(redirectorAngle, 0,
					OffensiveConstants.getMaximumReasonableRedirectAngle()) * 0.5 + 0.5;
			
			redirectAngleScore *= SumatraMath.relative(redirectAngleScore,
					0, 1. / 8. * OffensiveConstants.getMaximumReasonableRedirectAngle());
		}
		
		
		// probability to score a goal; range [0.5, 1]
		ValuePoint target = newTacticalField.getBestDirectShotTargetsForTigerBots().get(redirectTarget.getBotId());
		double scoreGoalFactor = target.getValue() * 0.5 + 0.5;
		
		// do not redirect into our half; range [0, 1]
		double xLimitation = SumatraMath.relative(redirectTarget.getPosByTime(lookahead).x(),
				-1. / 8. * Geometry.getFieldLength(), 1. / 4. * Geometry.getFieldLength());
		
		return redirectAngleScore * scoreGoalFactor * xLimitation;
	}
	
	
	/**
	 * @param aiFrame AIFrame
	 * @param currentAction the current Action
	 * @param myBot the bot to check stateswitches
	 * @param myDestination to desired moveTo postion (interception point)
	 * @param goalShotTarget the target in the enemy goal
	 * @param doublePassTarget the potential target to do a double pass (other bot)
	 * @param catchPoint catch Point
	 * @return ERedirectAction
	 */
	public ERedirectAction checkStateSwitches(AthenaAiFrame aiFrame,
			ERedirectAction currentAction,
			ITrackedBot myBot, IVector2 myDestination, DynamicPosition goalShotTarget,
			IPassTarget doublePassTarget, IVector2 catchPoint)
	{
		ITrackedBall ball = aiFrame.getWorldFrame().getBall();
		WorldFrame worldFrame = aiFrame.getWorldFrame();
		ITacticalField tacticalField = aiFrame.getTacticalField();
		boolean isSwitchAllowed = isSwitchAllowed(myDestination, ball);
		boolean isDoublePassPossible = doublePassTarget != null;
		
		// is redirect reasonable to GoalTarget
		boolean isGoalRedirectPossible = OffensiveMath.isBallRedirectReasonable(worldFrame, myBot.getBotKickerPos(),
				goalShotTarget,
				0.1);
		
		// check here if opponet robot can catch ball before me
		boolean canOpponentCatchBallBeforeMe = false;
		if (tacticalField.getOpponentPassReceiver().isPresent() && catchPoint != null)
		{
			IDrawableShape dc = new DrawableCircle(Circle.createCircle(worldFrame.getBall().getPos(), 200),
					Color.LIGHT_GRAY);
			tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_REDIRECT_INTERCEPT).add(dc);
			canOpponentCatchBallBeforeMe = canOpponentCatchBallBeforeMe(tacticalField.getOpponentPassReceiver().get(),
					worldFrame, tacticalField, catchPoint, currentAction == ERedirectAction.INTERCEPT ? 30 : 0);
		}
		
		IVector2 textPos = myBot.getPos().addNew(Vector2.fromXY(-200, 200));
		String text = "RedirectSwitchStatus: \n" +
				"isSwitchAllowed:" + isSwitchAllowed + "\n" +
				"isDoublePassPossible:" + isDoublePassPossible + "\n" +
				"isGoalRedirectPossible:" + isGoalRedirectPossible;
		DrawableAnnotation da = new DrawableAnnotation(textPos, text, Color.ORANGE);
		tacticalField.getDrawableShapes().get(EAiShapesLayer.REDIRECT_ROLE).add(da);
		
		
		if (currentAction == null)
		{
			// choose initial strategy here.
			return handleNoCurrentState(isDoublePassPossible, isGoalRedirectPossible);
		}
		return checkSwitchActions(currentAction, isSwitchAllowed, isDoublePassPossible,
				isGoalRedirectPossible, canOpponentCatchBallBeforeMe);
	}
	
	
	private ERedirectAction handleNoCurrentState(final boolean isDoublePassPossible,
			final boolean isGoalRedirectPossible)
	{
		if (isGoalRedirectPossible)
		{
			return ERedirectAction.REDIRECT_GOAL;
		} else if (isDoublePassPossible)
		{
			return ERedirectAction.REDIRECT_PASS;
		}
		return ERedirectAction.CATCH;
	}
	
	
	private boolean canOpponentCatchBallBeforeMe(ITrackedBot opponent, WorldFrame worldFrame,
			ITacticalField tacticalField, IVector2 myCatchPoint, double treshold)
	{
		if (!OffensiveConstants.isAllowRedirectorOvertake())
		{
			return false;
		}
		IVector2 opponentCatchPoint;
		ILine ballTravelLine = worldFrame.getBall().getTrajectory().getTravelLine();
		ILine botTravelLine = Line.fromDirection(opponent.getPos(), opponent.getVel());
		Optional<IVector2> intersection = ballTravelLine.intersectionWith(botTravelLine);
		opponentCatchPoint = intersection.orElseGet(() -> ballTravelLine.leadPointOf(opponent.getPos()));
		
		IDrawableShape dc = new DrawableCircle(Circle.createCircle(opponentCatchPoint, 180), Color.orange);
		tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_REDIRECT_INTERCEPT).add(dc);
		
		enemyCatchPos = opponentCatchPoint;
		if (!Geometry.getField().isPointInShape(enemyCatchPos))
		{
			// this position will not be used when returning false... just to be sure
			enemyCatchPos = Geometry.getField().nearestPointInside(enemyCatchPos, 200);
			return false;
		}
		if (myCatchPoint.distanceTo(worldFrame.getBall().getPos()) + treshold > opponentCatchPoint
				.distanceTo(worldFrame.getBall().getPos()))
		{
			tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_REDIRECT_INTERCEPT)
					.add(new DrawableCircle(Circle.createCircle(opponentCatchPoint, 190), Color.RED));
			return true;
		}
		return false;
	}
	
	
	private ERedirectAction checkSwitchActions(final ERedirectAction currentAction, final boolean isSwitchAllowed,
			final boolean isDoublePassPossible, final boolean isGoalRedirectPossible,
			final boolean canOpponentCatchBallBeforeMe)
	{
		ERedirectAction newAction;
		switch (currentAction)
		{
			case CATCH:
				newAction = handleCatch(isSwitchAllowed, isDoublePassPossible,
						isGoalRedirectPossible, canOpponentCatchBallBeforeMe);
				break;
			case REDIRECT_GOAL:
				newAction = handleRedirectGoal(isDoublePassPossible, isGoalRedirectPossible, canOpponentCatchBallBeforeMe);
				break;
			case REDIRECT_PASS:
				newAction = handlePass(isDoublePassPossible, isGoalRedirectPossible, canOpponentCatchBallBeforeMe);
				break;
			case INTERCEPT:
				newAction = handleIntercept(isDoublePassPossible, isGoalRedirectPossible, canOpponentCatchBallBeforeMe);
				break;
			default:
				throw new NotImplementedException();
		}
		if (newAction != null)
		{
			return newAction;
		}
		return currentAction;
	}
	
	
	private ERedirectAction handleIntercept(final boolean isDoublePassPossible, final boolean isGoalRedirectPossible,
			final boolean canOpponentCatchBallBeforeMe)
	{
		if (!canOpponentCatchBallBeforeMe)
			return handleNoCurrentState(isDoublePassPossible, isGoalRedirectPossible);
		return ERedirectAction.INTERCEPT;
	}
	
	
	private ERedirectAction handlePass(final boolean isDoublePassPossible, final boolean isGoalRedirectPossible,
			final boolean canOpponentCatchBallBeforeMe)
	{
		if (canOpponentCatchBallBeforeMe)
			return ERedirectAction.INTERCEPT;
		if (!isDoublePassPossible && isGoalRedirectPossible)
			return ERedirectAction.REDIRECT_GOAL;
		else if (!isDoublePassPossible)
			return ERedirectAction.CATCH;
		return null;
	}
	
	
	private ERedirectAction handleRedirectGoal(final boolean isDoublePassPossible,
			final boolean isGoalRedirectPossible, final boolean canOpponentCatchBallBeforeMe)
	{
		if (canOpponentCatchBallBeforeMe)
			return ERedirectAction.INTERCEPT;
		if (!isGoalRedirectPossible && isDoublePassPossible)
			return ERedirectAction.REDIRECT_PASS;
		else if (!isGoalRedirectPossible)
			return ERedirectAction.CATCH;
		return null;
	}
	
	
	private ERedirectAction handleCatch(final boolean isSwitchAllowed, final boolean isDoublePassPossible,
			final boolean isGoalRedirectPossible, final boolean canOpponentCatchBallBeforeMe)
	{
		if (canOpponentCatchBallBeforeMe)
			return ERedirectAction.INTERCEPT;
		if (isGoalRedirectPossible && isSwitchAllowed)
			return ERedirectAction.REDIRECT_GOAL;
		else if (isDoublePassPossible && isSwitchAllowed)
			return ERedirectAction.REDIRECT_PASS;
		return null;
	}
	
	
	private boolean isSwitchAllowed(final IVector2 myDestination, final ITrackedBall ball)
	{
		boolean isSwitchAllowed = true;
		if (myDestination != null)
		{
			double ballDistToMe = myDestination
					.distanceTo(ball.getPos());
			double timeBallToMe = ball.getTrajectory().getTimeByDist(ballDistToMe);
			
			// if theres not enough time to change the skill, keep the old one
			if (timeBallToMe < 0.4)
			{
				isSwitchAllowed = false;
			}
			
			// ball is really close 2 me, just for safety reason, the time check above should
			// already include this check
			if (ballDistToMe < STATE_SWITCH_MAX_BALL_DIST)
			{
				isSwitchAllowed = false;
			}
		}
		return isSwitchAllowed;
	}
	
	
	public IVector2 getEnemyCatchPos()
	{
		return enemyCatchPos;
	}
}
