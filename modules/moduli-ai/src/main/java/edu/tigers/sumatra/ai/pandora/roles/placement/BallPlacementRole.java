/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.placement;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.DropBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;


/**
 * The primary ball placement role.
 */
public class BallPlacementRole extends ARole
{
	@Configurable(defValue = "PULL_PUSH")
	private static EBallHandling ballHandling = EBallHandling.PULL_PUSH;

	@Configurable(defValue = "2.0")
	private static double passEndSpeed = 2.0;

	@Configurable(defValue = "0.5", comment = "Time in s to wait at the end")
	private static double clearanceWaitTime = 0.5;

	@Configurable(defValue = "50", comment = "Extra tolerance to subtract from the rule-defined placement tolerance")
	private static double extraPlacementTolerance = 50;

	@Configurable(defValue = "180", comment = "Angle [deg] that the robot can turn with the ball on dribbler while pulling")
	private static double maxRotateWhilePullAngle = 180;

	@Setter
	private IVector2 ballTargetPos = Vector2.zero();
	@Setter
	private EPassMode passMode = EPassMode.NONE;
	@Getter
	private boolean ballPlacedAndCleared = false;
	@Setter
	private double placementTolerance = RuleConstraints.getBallPlacementTolerance() - extraPlacementTolerance;

	private final PullAwayDestCalc pullAwayDestCalc = new PullAwayDestCalc();
	private IVector2 currentBallTarget;


	public BallPlacementRole()
	{
		super(ERole.BALL_PLACEMENT);

		var clearBallState = new ClearBallState();
		var dropBallState = new BaseState<>(DropBallSkill::new);
		var receiveState = new BaseState<>(ReceiveBallSkill::new);
		var prepareState = new PrepareState();
		var decisionState = new RoleState<>(IdleSkill::new);
		var stopBallState = new BaseState<>(ApproachAndStopBallSkill::new);
		var getBallContactState = new BaseState<>(GetBallContactSkill::new);
		var moveWithBallState = new MoveWithBallState();
		var passState = new PassState();

		setInitialState(clearBallState);

		clearBallState.addTransition(
				"ball cleared, but not yet on target",
				() -> clearBallState.isCalmedDown() && clearBallState.isCleared() && ballNotOnTargetYet()
						&& !ballPlacedAndCleared,
				receiveState
		);
		receiveState.addTransition(ESkillState.SUCCESS, prepareState);
		receiveState.addTransition(ESkillState.FAILURE, stopBallState);
		prepareState.addTransition("ballIsPlaced", this::ballIsPlaced, dropBallState);
		prepareState.addTransition("success", prepareState::success, decisionState);
		prepareState.addTransition("ballMoving", prepareState::ballMoving, receiveState);
		decisionState.addTransition("ballNeedsToBePassed", this::ballNeedsToBePassed, passState);
		decisionState.addTransition("move ball", () -> !ballNeedsToBePassed(), getBallContactState);
		stopBallState.addTransition(ESkillState.SUCCESS, prepareState);
		stopBallState.addTransition(ESkillState.FAILURE, prepareState);
		getBallContactState.addTransition(ESkillState.SUCCESS, moveWithBallState);
		getBallContactState.addTransition(ESkillState.FAILURE, dropBallState);
		moveWithBallState.addTransition(ESkillState.SUCCESS, dropBallState);
		moveWithBallState.addTransition(ESkillState.FAILURE, receiveState);
		passState.addTransition(ESkillState.FAILURE, clearBallState);
		passState.addTransition(ESkillState.SUCCESS, clearBallState);
		passState.addTransition("pass mode is NONE", () -> passMode == EPassMode.NONE, clearBallState);
		dropBallState.addTransition(ESkillState.SUCCESS, clearBallState);
		dropBallState.addTransition(ESkillState.FAILURE, clearBallState);
	}


	@Override
	protected void beforeUpdate()
	{
		if (currentBallTarget == null)
		{
			updateCurrentBallTarget();
		}
	}


	private void updateCurrentBallTarget()
	{
		var ballInsideFieldPos = Geometry.getFieldWBorders().nearestPointInside(getBall().getPos(), getPos());
		currentBallTarget = pullAwayDestCalc.getPullAwayBallTarget(ballInsideFieldPos).orElse(ballTargetPos);
	}


	@Override
	protected void afterUpdate()
	{
		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.AI_BALL_PLACEMENT);
		shapes.add(
				new DrawableCircle(
						Circle.createCircle(currentBallTarget, placementTolerance),
						Color.magenta
				)
		);
		shapes.add(
				new DrawableCircle(
						Circle.createCircle(ballTargetPos, placementTolerance),
						Color.cyan
				)
		);
		shapes.addAll(pullAwayDestCalc.getShapes());
	}


	private boolean ballIsPlaced()
	{
		return !ballNotOnTargetYet();
	}


	private boolean ballNeedsToBePassed()
	{
		return passMode != EPassMode.NONE
				&& currentBallTarget.equals(ballTargetPos);
	}


	private boolean ballNotOnTargetYet()
	{
		if (!Geometry.getField().isPointInShape(getBall().getPos()))
		{
			// Never when outside of field, even if within placement tolerance
			return true;
		}
		double target2BallDist = currentBallTarget
				.distanceTo(getBall().getTrajectory().getPosByVel(0.0).getXYVector());
		return target2BallDist > placementTolerance;
	}


	private class PrepareState extends BaseState<MoveToSkill>
	{
		private final TimestampTimer calmDownTimer = new TimestampTimer(0.0);
		private boolean calmedDown;


		public PrepareState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			super.onInit();
			skill.setMinTimeAtDestForSuccess(0.3);
			skill.updateLookAtTarget(getBall());

			if (getBot().getBallContact().hadContact(0.2))
			{
				calmDownTimer.setDuration(clearanceWaitTime);
				skill.setComeToAStop(true);
			} else
			{
				calmDownTimer.setDuration(0.0);
			}
			calmDownTimer.start(getWFrame().getTimestamp());
			calmedDown = false;
		}


		@Override
		protected void onUpdate()
		{
			updateCurrentBallTarget();
			if (calmDownTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				skill.setComeToAStop(false);
				skill.updateDestination(getDest());
				skill.getMoveCon().setBallObstacle(true);
				calmedDown = true;
			}
		}


		public boolean success()
		{
			return calmedDown && skill.getSkillState() == ESkillState.SUCCESS;
		}


		public boolean ballMoving()
		{
			return getBall().getVel().getLength2() > 0.5;
		}


		private IVector2 getDest()
		{
			double margin = 210;
			IVector2 pushDest = LineMath.stepAlongLine(getBall().getPos(), currentBallTarget, -margin);
			IVector2 pullDest = LineMath.stepAlongLine(getBall().getPos(), currentBallTarget, margin);

			if (!currentBallTarget.equals(ballTargetPos))
			{
				// force pull
				return pullDest;
			}

			if (ballNeedsToBePassed())
			{
				return pushDest;
			}

			return switch (ballHandling)
			{
				case PULL -> pullDest;
				case PUSH -> pushDest;
				default -> getPos().nearestTo(pullDest, pushDest);
			};
		}
	}

	private class MoveWithBallState extends BaseState<MoveWithBallSkill>
	{
		private IVector2 currentPlacementPos;


		public MoveWithBallState()
		{
			super(MoveWithBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			super.onInit();
			currentPlacementPos = currentBallTarget;
			updateTargetPose();
		}


		@Override
		protected void onUpdate()
		{
			if (passMode == EPassMode.NONE)
			{
				updateCurrentBallTarget();

				double turnWhilePullAngle = AngleMath.diffAbs(
						getBall().getPos().subtractNew(currentBallTarget).getAngle(),
						getBot().getOrientation()
				);
				if (turnWhilePullAngle < AngleMath.deg2rad(maxRotateWhilePullAngle))
				{
					currentPlacementPos = currentBallTarget;
				}
			}
			if (currentPlacementPos.distanceTo(getBall().getPos()) > 300)
			{
				updateTargetPose();
			}
		}


		private void updateTargetPose()
		{
			double dist2Ball = Geometry.getBallRadius() + getBot().getCenter2DribblerDist();
			double ball2Target = currentPlacementPos.subtractNew(getBall().getPos()).getAngle();
			IVector2 offset = AngleMath.diffAbs(getBot().getOrientation(), ball2Target) < AngleMath.DEG_090_IN_RAD
					? Vector2.fromAngleLength(ball2Target + AngleMath.DEG_180_IN_RAD, dist2Ball)
					: Vector2.fromAngleLength(ball2Target, dist2Ball);
			skill.setFinalDest(currentPlacementPos.addNew(offset));
			skill.setFinalOrientation(offset.getAngle() + AngleMath.DEG_180_IN_RAD);
		}
	}

	private class ClearBallState extends BaseState<MoveToSkill>
	{
		private final KeepDistanceToBall awayFromBallMover = new KeepDistanceToBall(
				new PointChecker()
						.checkBallDistanceStatic()
						.checkInsideField()
						.checkNotInPenaltyAreas()
						.checkPointFreeOfBots());
		private final TimestampTimer calmDownTimer = new TimestampTimer(0.0);
		private final TimestampTimer clearedTimer = new TimestampTimer(2);


		public ClearBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			super.onInit();
			if (getBot().getBallContact().hadContact(0.2))
			{
				calmDownTimer.setDuration(clearanceWaitTime);
			} else
			{
				calmDownTimer.setDuration(0.0);
			}
			calmDownTimer.start(getWFrame().getTimestamp());
		}


		@Override
		protected void onUpdate()
		{
			// refresh current ball target
			updateCurrentBallTarget();

			if (!isCalmedDown())
			{
				return;
			}

			double clearanceRadius =
					RuleConstraints.getStopRadius() + Geometry.getBotRadius() + Geometry.getBallRadius() + 20;
			IVector2 dest = LineMath.stepAlongLine(getBall().getPos(), getPos(), clearanceRadius);
			if (isCleared())
			{
				skill.updateLookAtTarget(getBall());
				skill.getMoveCon().setBallObstacle(true);

				if (getBall().getVel().getLength2() < 0.1
						&& ballTargetPos.distanceTo(getBall().getPos()) < placementTolerance + extraPlacementTolerance)
				{
					clearedTimer.update(getWFrame().getTimestamp());
				} else
				{
					clearedTimer.reset();
				}

				double tolerance;
				if (clearedTimer.isTimeUp(getWFrame().getTimestamp()))
				{
					tolerance = placementTolerance;
				} else
				{
					tolerance = placementTolerance + extraPlacementTolerance;
				}

				ballPlacedAndCleared = getBall().getVel().getLength2() < 0.1
						&& ballTargetPos.distanceTo(getBall().getPos()) < tolerance;

				// find a valid destination, after having sufficient distance to the ball
				// This is required to clear the stop radius in cases where the game is not continued
				// immediately (e.g. ball placement before force start)
				dest = awayFromBallMover.findNextFreeDest(getAiFrame(), dest, getBotID());
			}
			skill.updateDestination(dest);
		}


		@Override
		protected void onExit()
		{
			ballPlacedAndCleared = false;
			clearedTimer.reset();
		}


		public boolean isCalmedDown()
		{
			return calmDownTimer.isTimeUp(getWFrame().getTimestamp());
		}


		public boolean isCleared()
		{
			return getBall().getPos().distanceTo(getPos()) > Geometry.getBotRadius() + 50;
		}
	}

	private class PassState extends BaseState<SingleTouchKickSkill>
	{
		public PassState()
		{
			super(() -> new SingleTouchKickSkill(ballTargetPos, KickParams.disarm()));
		}


		@Override
		protected void onUpdate()
		{
			// Wait for receiver to be at target destination
			skill.setReadyForKick(passMode == EPassMode.READY);
			double passDistance = getBall().getPos().distanceTo(ballTargetPos);
			double passSpeed = getBall().getStraightConsultant().getInitVelForDist(passDistance, passEndSpeed);
			skill.setKickSpeed(passSpeed);
		}
	}

	private class BaseState<T extends AMoveToSkill> extends RoleState<T>
	{
		public BaseState(Supplier<T> supplier)
		{
			super(supplier);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
		}
	}

	private enum EBallHandling
	{
		PULL_PUSH,
		PULL,
		PUSH,
	}

	public enum EPassMode
	{
		NONE,
		WAIT,
		READY,
	}
}
