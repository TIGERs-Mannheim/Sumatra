/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.Getter;

import java.awt.Color;


/**
 * Role for RoboCup 2021 Hardware Challenge 2: Scoring From Contested Possession
 * https://robocup-ssl.github.io/ssl-hardware-challenge-rules/rules.html#_challenge_2_scoring_from_contested_possession
 * Steals the ball from the opponent robot closest to the ball, dodges the opponent, and scores on the goal.
 */
public class ContestedPossessionChallengeRole extends ARole
{
	@Configurable(comment = "Distance to pull back the ball from the opponent", defValue = "50.0")
	private static double pullbackDistance = 50.0;

	@Configurable(comment = "Rotation angle during pull back [deg]", defValue = "35.0")
	private static double pullbackRotation = 35.0;

	@Configurable(comment = "Dodge direction from the ball-opponent line [deg]", defValue = "90.0")
	private static double dodgeAngle = 90.0; // [deg]

	@Configurable(comment = "Target dodge distance to the side, the robot may kick before this is fully executed", defValue = "400.0")
	private static double dodgeDistanceSideways = 400.0;

	@Configurable(comment = "Target dodge distance forward, the robot may kick before this is fully executed", defValue = "1000.0")
	private static double dodgeDistanceForward = 1000.0;

	@Configurable(comment = "Desired kick speed", defValue = "6.0")
	private static double kickVel = 6.0;

	@Configurable(comment = "Dribble speed during pull back and dodge [rpm]", defValue = "10000.0")
	private static double dribbleSpeed = 10000.0;

	@Configurable(comment = "Maximum target orientation look ahead from current robot velocity angle [deg]", defValue = "45.0")
	private static double turnLookAheadAngle = 45.0; // [deg]

	@Configurable(comment = "Maximum acceleration during pull back and dodge state [m/s^2]", defValue = "2.0")
	private static double maxAcceleration = 2.0;

	private IVector2 dodgePos;
	private IVector2 targetPos;
	private IVector2 ballPickPos;
	private double dodgeDirection;


	public ContestedPossessionChallengeRole()
	{
		super(ERole.CONTESTED_POSSESSION_CHALLENGE_ROLE);

		targetPos = Geometry.getGoalTheir().getCenter();

		var prepareState = new PrepareState();
		var waitForRunningState = new WaitForRunningState();
		var getBallContactState = new GetContactState();
		var pullBackState = new PullBackState();
		var dodgeState = new DodgeState();
		var kickState = new TurnAndKickState();
		var stopState = new StopState();
		var approachBallState = new RoleState<>(ApproachAndStopBallSkill::new);
		var touchKickState = new RoleState<>(() -> new TouchKickSkill(targetPos, KickParams.straight(kickVel)));

		setInitialState(prepareState);

		prepareState.addTransition(prepareState::isAtTarget, waitForRunningState);
		waitForRunningState.addTransition(waitForRunningState::isRunning, getBallContactState);
		getBallContactState.addTransition(ESkillState.FAILURE, prepareState);
		getBallContactState.addTransition(ESkillState.SUCCESS, pullBackState);
		pullBackState.addTransition(pullBackState::isDone, dodgeState);
		dodgeState.addTransition(dodgeState::isDone, kickState);
		kickState.addTransition(kickState::hasKicked, stopState);
		kickState.addTransition(kickState::hasLostBall, approachBallState);
		approachBallState.addTransition(ESkillState.SUCCESS, touchKickState);
		approachBallState.addTransition(ESkillState.FAILURE, stopState);
		touchKickState.addTransition(ESkillState.SUCCESS, stopState);
		touchKickState.addTransition(ESkillState.FAILURE, stopState);
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

		if (ballPickPos != null)
		{
			shapes.add(new DrawableCircle(ballPickPos, 1000.0, Color.black));
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
			IVector2 preparePos = LineMath.stepAlongLine(getBall().getPos(), defenderPos, -300.0);

			skill.updateLookAtTarget(getBall());
			skill.updateDestination(preparePos);
		}


		public boolean isAtTarget()
		{
			return skill.getSkillState() == ESkillState.SUCCESS && getBall().getVel().getLength() < 0.1;
		}
	}

	private class GetContactState extends RoleState<ProtectiveGetBallSkill>
	{
		public GetContactState()
		{
			super(ProtectiveGetBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			IVector2 defenderPos = getWFrame().getBot(getTacticalField().getOpponentClosestToBall().getBotId()).getPos();
			IVector2 preparePos = LineMath.stepAlongLine(getBall().getPos(), defenderPos, -500.0);

			skill.setProtectionTarget(preparePos);
			skill.setProtectDribbleSpeed(dribbleSpeed);
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().setBotsObstacle(false);
		}
	}

	private class PullBackState extends RoleState<MoveToSkill>
	{
		private IVector2 startPos;


		public PullBackState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			dodgeDirection = Math.copySign(1.0, -getPos().y());
			ballPickPos = getBall().getPos();

			startPos = getPos();

			double targetOrient = getBot().getOrientation() + AngleMath.deg2rad(pullbackRotation) * dodgeDirection;
			IVector2 pullbackPos = getPos().subtractNew(Vector2.fromAngleLength(targetOrient, pullbackDistance * 2.0));

			skill.updateDestination(pullbackPos);
			skill.updateTargetAngle(targetOrient);
			skill.setKickParams(KickParams.disarm().withDribbleSpeed(dribbleSpeed));
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
		}


		public boolean isDone()
		{
			return getPos().distanceTo(startPos) > pullbackDistance;
		}
	}

	private class DodgeState extends RoleState<MoveToSkill>
	{
		public DodgeState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			dodgePos = getPos().addNew(Vector2.fromAngleLength(
					getBot().getOrientation() + AngleMath.deg2rad(dodgeAngle - pullbackRotation) * dodgeDirection,
					dodgeDistanceSideways));

			skill.updateDestination(dodgePos);
			skill.updateTargetAngle(getBot().getOrientation());
			skill.setKickParams(KickParams.disarm().withDribbleSpeed(dribbleSpeed));
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveConstraints().setAccMax(maxAcceleration);
		}


		public boolean isDone()
		{
			return getPos().distanceTo(dodgePos) < (dodgeDistanceSideways - 100.0);
		}
	}

	private class TurnAndKickState extends RoleState<MoveToSkill>
	{
		private double rotationDir;
		private boolean armed;


		public TurnAndKickState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			armed = false;

			double initialBotAngle = getBot().getOrientation();
			double toTargetAngle = Vector2.fromPoints(getPos(), targetPos).getAngle();

			rotationDir = Math.copySign(1.0, AngleMath.difference(toTargetAngle, initialBotAngle));

			skill.updateDestination(LineMath.stepAlongLine(dodgePos, targetPos, dodgeDistanceForward));
			skill.getMoveConstraints().setPrimaryDirection(Vector2.fromPoints(dodgePos, targetPos));
			skill.setKickParams(KickParams.disarm().withDribbleSpeed(dribbleSpeed));
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveConstraints().setAccMax(maxAcceleration);

			// compute desired orientation: rotate ahead of angle of current velocity and limit by orientation to final target
			double angleToTargetPos = Vector2.fromPoints(getPos(), targetPos).getAngle();
			double orientationTargetFromVel =
					getBot().getVel().getAngle() + AngleMath.deg2rad(turnLookAheadAngle) * rotationDir;

			double pastTargetAngle = AngleMath.difference(orientationTargetFromVel, angleToTargetPos);
			if (pastTargetAngle * rotationDir > AngleMath.deg2rad(20))
			{
				pastTargetAngle = AngleMath.deg2rad(20) * rotationDir;
			}

			skill.updateTargetAngle(angleToTargetPos + pastTargetAngle);

			// Find out when to arm kicker based on difference to target angle.
			// This also includes to check the final overlay of bot vel and kick vel and compensates it.
			double angleDiffToTarget = AngleMath.difference(getBot().getAngleByTime(0.05),
					Vector2.fromPoints(getBot().getPos(), targetPos).getAngle());

			IVector2 theoreticalBallVel = Vector2.fromAngleLength(getBot().getOrientation(), kickVel);
			double leadAngle = theoreticalBallVel.addNew(getBot().getVel()).angleToAbs(theoreticalBallVel).orElse(0.0);

			if (angleDiffToTarget * rotationDir > leadAngle)
			{
				skill.setKickParams(KickParams.straight(kickVel).withDribbleSpeed(dribbleSpeed));
				armed = true;
			}

			// debug shapes
			ILineSegment toTarget = Lines.segmentFromPoints(getPos(), targetPos);
			ILineSegment curOrient = Lines
					.segmentFromOffset(getPos(), Vector2.fromAngleLength(getBot().getOrientation(), toTarget.getLength()));
			ILineSegment setOrient = Lines
					.segmentFromOffset(getPos(), Vector2.fromAngleLength(skill.getTargetAngle(), toTarget.getLength()));

			getShapes(EAiShapesLayer.TEST_CONTESTED_POSSESSION_CHALLENGE).add(new DrawableLine(toTarget, Color.red));
			getShapes(EAiShapesLayer.TEST_CONTESTED_POSSESSION_CHALLENGE).add(new DrawableLine(curOrient, Color.magenta));
			getShapes(EAiShapesLayer.TEST_CONTESTED_POSSESSION_CHALLENGE).add(new DrawableLine(setOrient, Color.green));

			getShapes(EAiShapesLayer.TEST_CONTESTED_POSSESSION_CHALLENGE).add(new DrawableAnnotation(targetPos,
					String.format("Angle Diff: %.2fdeg", AngleMath.rad2deg(angleDiffToTarget))));
		}


		public boolean hasKicked()
		{
			double timeSinceKick = getWFrame().getKickEvent()
					.map(e -> (getWFrame().getTimestamp() - e.getTimestamp()) * 1e-9).orElse(0.0);

			boolean isBallMovingToTarget =
					Vector2.fromPoints(dodgePos, targetPos).angleToAbs(getBall().getVel()).orElse(0.0) < AngleMath
							.deg2rad(45.0);

			return (armed && timeSinceKick > 0 && timeSinceKick < 0.5 && isBallMovingToTarget);
		}


		public boolean hasLostBall()
		{
			return !getBot().getBallContact().hadContact(0.2);
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
