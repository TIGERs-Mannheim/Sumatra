/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import java.util.Random;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.shootout.PenaltyPlacementTargetGroup;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class OneOnOneShooter extends ARole
{
	@Configurable(comment = "Distance to destination", defValue = "100.0")
	private static double distanceToTarget = 100;
	
	@Configurable(comment = "The placement kick speed (if fixed_speed)", defValue = "2.0")
	private static double placementKickSpeed = 2.0;
	
	@Configurable(comment = "Time to predict enemy position (in seconds)", defValue = "1.0")
	private static double enemyTimeToBallPrediction = 1.0;
	
	@Configurable(comment = "Max predicted enemy distance to the ball necessary to force kick", defValue = "500.0")
	private static double maxEnemyDistanceToBall = 500;
	
	@Configurable(comment = "Bot is allowed to drive into their penalty area", defValue = "false")
	private static boolean penaltyAllowedTheir = false;
	
	@Configurable(comment = "Bot is allowed to drive into our penalty area", defValue = "true")
	private static boolean penaltyAllowedOur = true;
	
	@Configurable(comment = "Whether to use chip kicks", defValue = "true")
	private static boolean chipKickEnabled = true;
	
	@Configurable(comment = "Minimal ball speed percentage", defValue = "0.75")
	private static double minEndSpeedPercent = 0.75;
	
	@Configurable(comment = "Move speed of bot in placement state (relative to ball speed)", defValue = "0.8")
	private static double moveSpeedPercent = 0.8;
	
	private PenaltyPlacementTargetGroup penaltyPlacementTargetGroup;
	private boolean scoreProcessed = false;
	
	private Random rnd = null;
	
	
	/**
	 * The penalty attacker role
	 */
	public OneOnOneShooter()
	{
		super(ERole.PENALTY_ATTACKER);
		
		IState prepareState = new PrepareState();
		IState ballPlacementState = new BallPlacementState();
		IState shootState = new ShootState();
		IState fastShootState = new FastShootState();
		
		setInitialState(prepareState);
		addTransition(prepareState, EEvent.PREPARATION_COMPLETE, ballPlacementState);
		addTransition(ballPlacementState, EEvent.BALL_PLACEMENT_COMPLETE, shootState);
		addTransition(ballPlacementState, EEvent.FAST_SHOT_NECESSARY, fastShootState);
		
		addTransition(EEvent.SHOT_CONFIRMED, prepareState);
	}
	
	private enum EEvent implements IEvent
	{
		PREPARATION_COMPLETE,
		BALL_PLACEMENT_COMPLETE,
		FAST_SHOT_NECESSARY,
		SHOT_CONFIRMED
	}
	
	
	private class PrepareState extends AState
	{
		AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			if (rnd == null)
			{
				rnd = new Random(getAiFrame().getWorldFrame().getTimestamp());
			}
			
			skill = AMoveToSkill.createMoveToSkill();
			
			IVector2 standbyPos = getBall().getPos().addNew(Vector2.fromXY(-500, 0));
			
			skill.getMoveCon().updateDestination(standbyPos);
			skill.getMoveCon().updateLookAtTarget(new DynamicPosition(getWFrame().getBall()));
			setNewSkill(skill);
			
			scoreProcessed = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 standbyPos = getBall().getPos()
					.subtractNew(Vector2.fromXY(RuleConstraints.getStopRadius() + Geometry.getBotRadius(), 0));
			
			skill.getMoveCon().updateDestination(standbyPos);
			skill.getMoveCon().updateLookAtTarget(new DynamicPosition(getWFrame().getBall()));
			
			if (getAiFrame().getRefereeMsg() != null
					&& getAiFrame().getRefereeMsg().getCommand().equals(Command.NORMAL_START))
			{
				triggerEvent(EEvent.PREPARATION_COMPLETE);
			}
			
			scoreProcessed = checkGoal();
		}
	}
	
	
	private class BallPlacementState extends AState
	{
		IVector2 finalTarget;
		IVector2 startPos;
		KickParams kickParams;
		TouchKickSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			penaltyPlacementTargetGroup = getAiFrame().getTacticalField().getFilteredPenaltyPlacementTargetGroup();
			penaltyPlacementTargetGroup.setAttempts(penaltyPlacementTargetGroup.getAttempts() + 1);
			
			double xDiff = (rnd.nextDouble() - 0.5) * (penaltyPlacementTargetGroup.radius() * 2);
			double yDiff = (rnd.nextDouble() - 0.5) * (penaltyPlacementTargetGroup.radius() * 2);
			
			finalTarget = Vector2.fromXY(penaltyPlacementTargetGroup.center().x() + xDiff,
					penaltyPlacementTargetGroup.center().y() + yDiff);
			finalTarget = Geometry.getPenaltyAreaTheir().withMargin(2 * Geometry.getBotRadius())
					.nearestPointOutside(finalTarget);
			finalTarget = Geometry.getField().nearestPointInside(finalTarget, 3 * Geometry.getBotRadius());
			startPos = getBall().getPos();
			
			kickParams = KickParams.straight(placementKickSpeed);
			skill = new TouchKickSkill(new DynamicPosition(finalTarget), kickParams);
			skill.getMoveCon().setPenaltyAreaAllowedOur(penaltyAllowedOur);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(penaltyAllowedTheir);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 ballPos = getBall().getPos();
			
			if (getBall().getTrajectory().getPosByTime(1).getXYVector().distanceTo(finalTarget) < distanceToTarget)
			{
				triggerEvent(EEvent.BALL_PLACEMENT_COMPLETE);
			}
			
			if (!getAiFrame().getTacticalField()
					.getEnemyClosestToBall().equals(BotDistance.NULL_BOT_DISTANCE)
					&& getAiFrame().getTacticalField().getEnemyClosestToBall().getBot()
							.getPosByTime(enemyTimeToBallPrediction).distanceTo(getBall().getPos()) < maxEnemyDistanceToBall)
			{
				triggerEvent(EEvent.FAST_SHOT_NECESSARY);
			}
			
			double distancePercent = finalTarget.distanceTo(ballPos) / finalTarget.distanceTo(startPos);
			double desiredKickSpeed = placementKickSpeed * Math.max(minEndSpeedPercent, distancePercent);
			if (getBall().getTrajectory().getAbsVelByTime(1.0) > desiredKickSpeed)
			{
				skill.getMoveCon().getMoveConstraints()
						.setVelMax(Math.min(getBot().getRobotInfo().getBotParams().getMovementLimits().getVelMax(),
								getBall().getTrajectory().getAbsVelByTime(1.0) * moveSpeedPercent));
				kickParams.setKickSpeed(0);
			} else
			{
				kickParams.setKickSpeed(desiredKickSpeed);
				skill.getMoveCon().getMoveConstraints()
						.setVelMax(getBot().getRobotInfo().getBotParams().getMovementLimits().getVelMax());
			}
		}
	}
	
	
	private class ShootState extends AState
	{
		@Override
		public void doEntryActions()
		{
			IVector2 target = calculateBestShotTarget();
			
			AMoveSkill skill = new TouchKickSkill(new DynamicPosition(target), KickParams.maxStraight());
			skill.getMoveCon().setPenaltyAreaAllowedTheir(penaltyAllowedTheir);
			skill.getMoveCon().setPenaltyAreaAllowedOur(penaltyAllowedOur);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			calculateShotEvent();
			scoreProcessed = checkGoal();
		}
	}
	
	
	private class FastShootState extends AState
	{
		IVector2 shotTarget;
		KickParams kickParams;
		
		
		@Override
		public void doEntryActions()
		{
			shotTarget = calculateBestShotTarget();
			
			kickParams = KickParams.maxStraight();
			AMoveSkill skill = new TouchKickSkill(new DynamicPosition(shotTarget), KickParams.maxStraight());
			skill.getMoveCon().setPenaltyAreaAllowedTheir(penaltyAllowedTheir);
			skill.getMoveCon().setPenaltyAreaAllowedOur(penaltyAllowedOur);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			final BotDistance enemyClosestToBall = getAiFrame().getTacticalField().getEnemyClosestToBall();
			
			if (!enemyClosestToBall.equals(BotDistance.NULL_BOT_DISTANCE))
			{
				IVector2 enemyPos = enemyClosestToBall.getBot().getPos();
				Line lineToGoal = Line.fromPoints(getPos(), shotTarget);
				
				double enemyDistanceToLine = lineToGoal.nearestPointOnLine(enemyPos).distanceTo(enemyPos);
				
				if (enemyDistanceToLine < maxEnemyDistanceToBall)
				{
					if (chipKickEnabled)
					{
						kickParams.setDevice(EKickerDevice.CHIP);
					}
					
					final double distance = getPos().distanceTo(shotTarget);
					double initVel = getBall().getChipConsultant().getInitVelForDistAtTouchdown(distance, 6);
					kickParams.setKickSpeed(initVel);
				}
			}
			
			calculateShotEvent();
			scoreProcessed = checkGoal();
		}
	}
	
	
	private void calculateShotEvent()
	{
		if ((getAiFrame().getRefereeMsg() != null && getAiFrame().getRefereeMsg().getCommand().equals(Command.STOP))
				|| getAiFrame().getTacticalField().isGoalScored())
		{
			triggerEvent(EEvent.SHOT_CONFIRMED);
		}
	}
	
	
	private boolean checkGoal()
	{
		if (!scoreProcessed && getAiFrame().getTacticalField().isGoalScored() && penaltyPlacementTargetGroup != null)
		{
			penaltyPlacementTargetGroup.setSuccessfulAttempts(penaltyPlacementTargetGroup.getSuccessfulAttempts() + 1);
			return true;
		}
		
		return false;
	}
	
	
	private IVector2 calculateBestShotTarget()
	{
		return getAiFrame().getTacticalField().getBestGoalKickTarget().map(IRatedTarget::getTarget)
				.orElse(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
	}
}
