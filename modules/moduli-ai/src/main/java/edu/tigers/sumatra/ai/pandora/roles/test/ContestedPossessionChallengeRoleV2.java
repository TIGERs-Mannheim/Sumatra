/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.Getter;

import java.awt.Color;


/**
 * Role for RoboCup 2021 Hardware Challenge 2: Scoring From Contested Possession
 * https://robocup-ssl.github.io/ssl-hardware-challenge-rules/rules.html#_challenge_2_scoring_from_contested_possession
 * Steals the ball from the opponent robot closest to the ball, dodges the opponent, and scores on the goal.
 * This role simply dodges along in front of the opponent robot and picks up the ball on the go. It stops and aims
 * afterwards, hence it is precise but slow.
 */
public class ContestedPossessionChallengeRoleV2 extends ARole
{
	@Configurable(comment = "Distance to keep from the ball before stealing it", defValue = "100.0")
	private static double prepareDistance = 100.0;

	@Configurable(comment = "Rotation angle during dodge [deg]", defValue = "60.0")
	private static double dodgeRotation = 60.0;

	@Configurable(comment = "Target dodge distance to the side", defValue = "250.0")
	private static double dodgeDistance = 250.0;

	@Configurable(comment = "Desired kick speed", defValue = "6.0")
	private static double kickVel = 6.0;

	@Configurable(comment = "Maximum acceleration during dodge state [m/s^2]", defValue = "1.5")
	private static double maxAcceleration = 1.5;

	private IVector2 dodgePos;
	private IVector2 targetPos;


	public ContestedPossessionChallengeRoleV2()
	{
		super(ERole.CONTESTED_POSSESSION_CHALLENGE_ROLE_V2);

		targetPos = Geometry.getGoalTheir().getCenter();

		var prepareState = new PrepareState();
		var waitForRunningState = new WaitForRunningState();
		var getBallContactState = new GetContactState();
		var dodgeState = new DodgeState();
		var stopState = new StopState();
		var approachBallState = new RoleState<>(ApproachAndStopBallSkill::new);
		var touchKickState = new RoleState<>(
				() -> new TouchKickSkill(targetPos,
						KickParams.straight(kickVel).withDribblerMode(EDribblerMode.HIGH_POWER)));

		setInitialState(prepareState);

		prepareState.addTransition(prepareState::isAtTarget, waitForRunningState);
		waitForRunningState.addTransition(waitForRunningState::isRunning, getBallContactState);
		getBallContactState.addTransition(ESkillState.FAILURE, prepareState);
		getBallContactState.addTransition(ESkillState.SUCCESS, dodgeState);
		dodgeState.addTransition(dodgeState::isDoneWithBallAtDribbler, touchKickState);
		dodgeState.addTransition(dodgeState::isDoneAndLostBall, approachBallState);
		approachBallState.addTransition(ESkillState.SUCCESS, touchKickState);
		approachBallState.addTransition(ESkillState.FAILURE, touchKickState);
		touchKickState.addTransition(ESkillState.SUCCESS, stopState);
		touchKickState.addTransition(ESkillState.FAILURE, approachBallState);
	}


	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();

		var shapes = getShapes(EAiShapesLayer.TEST_CONTESTED_POSSESSION_CHALLENGE);

		if (targetPos != null)
		{
			shapes.add(new DrawableCircle(targetPos, 20.0, Color.red));
		}

		if (dodgePos != null)
		{
			shapes.add(new DrawableCircle(dodgePos, 100.0, Color.white));
		}
	}


	private class WaitForRunningState extends RoleState<IdleSkill>
	{
		@Getter
		private boolean isRunning = false;


		public WaitForRunningState()
		{
			super(IdleSkill::new);
		}


		@Override
		public void onUpdate()
		{
			isRunning = getAiFrame().getGameState().isGameRunning();
		}
	}

	private class PrepareState extends RoleState<MoveToSkill>
	{
		public PrepareState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().setGameStateObstacle(false);
			skill.getMoveCon().setBallObstacle(false);

			IVector2 defenderPos = getWFrame().getBot(getTacticalField().getOpponentClosestToBall().getBotId()).getPos();
			IVector2 preparePos = LineMath.stepAlongLine(getBall().getPos(), defenderPos, -380.0);

			skill.updateLookAtTarget(getBall());
			skill.updateDestination(preparePos);
		}


		public boolean isAtTarget()
		{
			return skill.getSkillState() == ESkillState.SUCCESS && getBall().getVel().getLength() < 0.1;
		}
	}

	private class GetContactState extends RoleState<MoveToSkill>
	{
		public GetContactState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			double dodgeDirection = Math.copySign(1.0, -getPos().y());

			IVector2 defenderPos = getWFrame().getBot(getTacticalField().getOpponentClosestToBall().getBotId()).getPos();

			IVector2 preparePos = Vector2.fromPoints(getBall().getPos(), defenderPos)
					.turn(AngleMath.PI_HALF * -dodgeDirection)
					.scaleTo(prepareDistance)
					.add(getBall().getPos())
					.add(Vector2.fromPoints(getBall().getPos(), defenderPos).scaleTo(-90.0));

			dodgePos = Vector2.fromPoints(getBall().getPos(), defenderPos)
					.turn(AngleMath.PI_HALF * dodgeDirection)
					.scaleTo(dodgeDistance)
					.add(getBall().getPos())
					.add(Vector2.fromPoints(getBall().getPos(), defenderPos).scaleTo(-80.0));

			skill.updateDestination(preparePos);
			skill.updateTargetAngle(Vector2.fromPoints(getBall().getPos(), defenderPos)
					.turn(AngleMath.deg2rad(dodgeRotation) * dodgeDirection).getAngle());
			skill.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
		}
	}

	private class DodgeState extends RoleState<MoveToSkill>
	{
		private long successTime = 0;


		public DodgeState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			successTime = 0;

			skill.updateDestination(dodgePos);
			skill.updateTargetAngle(getBot().getOrientation());
			skill.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveConstraints().setAccMax(maxAcceleration);

			if (successTime == 0 && skill.getSkillState() == ESkillState.SUCCESS)
			{
				successTime = getWFrame().getTimestamp();
			}
		}


		public boolean isDone()
		{
			return successTime > 0 && (getWFrame().getTimestamp() - successTime) * 1e-9 > 0.05;
		}


		public boolean isDoneWithBallAtDribbler()
		{
			return isDone() && getBot().getBallContact().hasContact();
		}


		public boolean isDoneAndLostBall()
		{
			return isDone() && !getBot().getBallContact().hasContact();
		}
	}

	private class StopState extends RoleState<MoveToSkill>
	{
		public StopState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.updateDestination(getPos());
		}


		@Override
		protected void onUpdate()
		{
			if (getBot().getVel().getLength() < 0.1)
				setCompleted();
		}
	}
}
