/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter2D;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class ChipInterceptRole extends ARole
{
	
	@Configurable(comment = "Distance (in mm) needed to destination to switch state", defValue = "150.0")
	private static double distanceThreshold = 150;
	
	@Configurable(comment = "Initial distance to enemy bot", defValue = "3000.0")
	private static double initDistance = 3000.0;
	
	@Configurable(comment = "The max kick speed", defValue = "8.0")
	private static double maxKickSpeed = 8.0;
	
	@Configurable(comment = "Move to predicted area in prepare state", defValue = "true")
	private static boolean moveToPredictedTouchdownArea = true;
	
	@Configurable(comment = "Kick the ball back to the shooter instead of redirecting it to the goal", defValue = "false")
	private static boolean justRedirectBall = false;
	
	@Configurable(comment = "Just receive the ball instead of kicking it", defValue = "true")
	private static boolean justReceiveBall = true;
	
	@Configurable(comment = "ID of the touchdown point to set as target", defValue = "1")
	private static int touchdownId = 1;
	
	@Configurable(comment = "The distance between touchdown point and destination (in origin direction)", defValue = "200.0")
	private static double distanceTouchdownToDest = 200.0;
	
	@Configurable(comment = "Filter threshold", defValue = "0.99")
	private static double positionFilterAlpha = 0.99;
	
	@Configurable(comment = "Time until role resets to wait state", defValue = "5")
	private static double timeUntilReset = 5;
	
	private Goal bestGoal = null;
	private IVector2 ballOrigin = null;
	
	
	/**
	 * Creates a new ChipInterceptRole
	 */
	public ChipInterceptRole()
	{
		super(ERole.CHIP_INTERCEPT);
		
		initStates();
	}
	
	private enum EChipInterceptEvent implements IEvent
	{
		WAIT,
		MOVE_TO_TOUCHDOWN,
		KICK
	}
	
	
	private void initStates()
	{
		IState waitState = new WaitState();
		IState moveToTouchdownState = new MoveToTouchdownState();
		IState kickState = new KickState();
		
		setInitialState(waitState);
		
		addTransition(EChipInterceptEvent.WAIT, waitState);
		addTransition(EChipInterceptEvent.MOVE_TO_TOUCHDOWN, moveToTouchdownState);
		addTransition(EChipInterceptEvent.KICK, kickState);
	}
	
	
	private Goal getBestGoal()
	{
		if (bestGoal == null)
		{
			
			IVector2 ballVel = getBall().getVel();
			if (ballVel.x() < 0)
			{
				bestGoal = Geometry.getGoalTheir();
			} else
			{
				
				bestGoal = Geometry.getGoalOur();
			}
		}
		
		return bestGoal;
	}
	
	private class WaitState implements IState
	{
		AMoveToSkill skill;
		ExponentialMovingAverageFilter2D filter;
		IVector2 dest;
		
		
		@Override
		public void doEntryActions()
		{
			// Delete best goal to calculate it on every run
			bestGoal = null;
			filter = new ExponentialMovingAverageFilter2D(positionFilterAlpha,
					getNormalVector(getBotClosestToBall()));
			
			skill = AMoveToSkill.createMoveToSkill();
			dest = getPos();
			
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(dest);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().isChipped())
			{
				triggerEvent(EChipInterceptEvent.MOVE_TO_TOUCHDOWN);
				return;
			}
			
			skill.getMoveCon().updateLookAtTarget(calcRedirectTarget(dest));
			
			if (moveToPredictedTouchdownArea)
			{
				ITrackedBot enemyBot = getAiFrame().getWorldFrame()
						.getBot(getBotClosestToBall().getBotId());
				
				IVector2 ballPos = getBall().getPos();
				filter.update(getNormalVector(enemyBot));
				dest = getOppositePos(ballPos);
				
				skill.getMoveCon().updateDestination(dest);
				skill.getMoveCon().setFastPosMode(true);
			}
		}
		
		
		private IVector2 getNormalVector(final ITrackedBot enemyBot)
		{
			return Vector2.fromPoints(enemyBot.getPos(), getBall().getPos()).scaleTo(1);
		}
		
		
		private IVector2 getOppositePos(final IVector2 ballPos)
		{
			IVector2 oppositeDest = ballPos.addNew(filter.getState().scaleToNew(initDistance));
			oppositeDest = Geometry.getField().nearestPointInside(oppositeDest, -2 * Geometry.getBotRadius());
			
			return oppositeDest;
		}
	}
	
	private class MoveToTouchdownState implements IState
	{
		AMoveToSkill skill;
		IVector2 formerDest;
		IVector2 dest;
		IVector2 formerLocalDest;
		IVector2 chipOrigin;
		long startTime;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			
			startTime = getAiFrame().getWorldFrame().getTimestamp();
			formerDest = getPos();
			formerLocalDest = getPos();
			chipOrigin = getAiFrame().getWorldFrame().getBot(getBotClosestToBall().getBotId())
					.getBotKickerPos();
			
			dest = calcDestination();
			skill.getMoveCon().updateDestination(dest);
			
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getAiFrame().getWorldFrame().getTimestamp() - startTime) * 1e-9 > timeUntilReset)
			{
				triggerEvent(EChipInterceptEvent.WAIT);
				return;
			}
			
			if (getPos() == null || dest == null)
			{
				return;
			}
			
			if (getPos().distanceTo(dest) < distanceThreshold && !justReceiveBall)
			{
				triggerEvent(EChipInterceptEvent.KICK);
				return;
			}
			
			if (getBall().getTrajectory().getTouchdownLocations().isEmpty())
			{
				return;
			}
			
			dest = calcDestination();
			skill.getMoveCon().updateDestination(dest);
			
			skill.getMoveCon().updateLookAtTarget(calcRedirectTarget(dest));
		}
		
		
		private IVector2 calcDestination()
		{
			List<IVector2> touchdownLocations = getBall().getTrajectory().getTouchdownLocations();
			IVector2 localDest;
			if (touchdownLocations.isEmpty())
			{
				localDest = formerDest;
			} else
			{
				IVector2 kickPos = ((ABallTrajectory) getBall().getTrajectory()).getKickPos().getXYVector();
				if (kickPos.distanceTo(chipOrigin) < 500)
				{
					localDest = touchdownLocations.get(touchdownId);
				} else
				{
					localDest = formerLocalDest;
				}
			}
			
			IVector2 redirectTarget = calcRedirectTarget(localDest);
			
			double dist = Geometry.getBallRadius() + getBot().getCenter2DribblerDist();
			if (Math.abs(distanceTouchdownToDest) > 0.5)
			{
				dist = -distanceTouchdownToDest;
			}
			
			formerLocalDest = localDest;
			localDest = LineMath.stepAlongLine(localDest, redirectTarget, dist);
			return localDest;
		}
	}
	
	private class KickState implements IState
	{
		RedirectSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new RedirectSkill(new DynamicPosition(calcRedirectTarget(getPos())));
			
			skill.setFixedKickSpeed(maxKickSpeed);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getAiFrame().getRefereeMsg() != null
					&& getAiFrame().getRefereeMsg().getCommand() == Referee.SSL_Referee.Command.STOP)
			{
				triggerEvent(EChipInterceptEvent.WAIT);
			}
		}
	}
	
	
	private IVector2 calcRedirectTarget(final IVector2 dest)
	{
		if (ballOrigin == null)
		{
			ballOrigin = getBall().getPos();
		}
		
		if (justRedirectBall)
		{
			return ballOrigin;
		} else if (justReceiveBall)
		{
			return LineMath.stepAlongLine(dest, getBall().getPos(), -100);
		} else
		{
			return getBestGoal().getCenter();
		}
	}
	
	
	private ITrackedBot getBotClosestToBall()
	{
		List<BotDistance> botDistances = new ArrayList<>();
		botDistances.add(getAiFrame().getTacticalField().getEnemyClosestToBall());
		botDistances.add(getAiFrame().getTacticalField().getTigerClosestToBall());
		
		botDistances = botDistances.stream()
				.filter(b -> b.getBot() != null)
				.filter(b -> !b.getBot().getBotId().equals(getBot().getBotId()))
				.sorted(Comparator.comparing(BotDistance::getDist)).limit(1)
				.collect(Collectors.toList());
		
		return botDistances.get(0).getBot();
	}
}