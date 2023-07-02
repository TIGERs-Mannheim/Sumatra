/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.placement;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GenericHysteresis;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.DropBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.function.Supplier;


/**
 * The primary ball placement role.
 */
public class BallPlacementRole extends ARole
{
	@Configurable(defValue = "PULL_PUSH")
	private static EBallHandling ballHandling = EBallHandling.PULL_PUSH;

	@Configurable(defValue = "150.0", comment = "Distance [mm] to pull the ball out of the goal")
	private static double goalPullOutDistance = 150;

	@Configurable(defValue = "2.0")
	private static double passEndSpeed = 2.0;

	@Configurable(defValue = "0.5", comment = "Time in s to wait at the end")
	private static double clearanceWaitTime = 0.5;

	@Configurable(defValue = "50", comment = "Extra tolerance to subtract from the rule-defined placement tolerance")
	private static double extraPlacementTolerance = 50;

	@Setter
	private IVector2 ballTargetPos = Vector2.zero();
	@Setter
	private EPassMode passMode = EPassMode.NONE;
	@Getter
	private boolean ballPlacedAndCleared = false;


	public BallPlacementRole()
	{
		super(ERole.BALL_PLACEMENT);

		var clearBallState = new ClearBallState();
		var dropBallState = new BaseState<>(DropBallSkill::new);
		var receiveState = new BaseState<>(ReceiveBallSkill::new);
		var prepareState = new PrepareState();
		var stopBallState = new BaseState<>(ApproachAndStopBallSkill::new);
		var getBallContactState = new BaseState<>(GetBallContactSkill::new);
		var moveWithBallState = new MoveWithBallState();
		var passState = new PassState();

		setInitialState(clearBallState);

		clearBallState.addTransition(
				"ball cleared, but not yet on target",
				() -> clearBallState.isCalmedDown() && clearBallState.isCleared() && ballNotOnTargetYet(),
				receiveState
		);
		receiveState.addTransition(ESkillState.SUCCESS, prepareState);
		receiveState.addTransition(ESkillState.FAILURE, stopBallState);
		prepareState.addTransition("ballIsPlaced", this::ballIsPlaced, dropBallState);
		prepareState.addTransition("success", prepareState::success, getBallContactState);
		prepareState.addTransition("ballMoving", prepareState::ballMoving, receiveState);
		prepareState.addTransition("ballNeedsToBePassed", this::ballNeedsToBePassed, passState);
		prepareState.addTransition("skipPrepare", prepareState::skipPrepare, getBallContactState);
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
	protected void afterUpdate()
	{
		super.afterUpdate();
		getShapes(EAiShapesLayer.AI_BALL_PLACEMENT).add(
				new DrawableCircle(Circle.createCircle(getCurrentBallTarget(), getBallPlacementTolerance()),
						Color.magenta)
		);
		getShapes(EAiShapesLayer.AI_BALL_PLACEMENT).add(
				new DrawableCircle(Circle.createCircle(ballTargetPos, getBallPlacementTolerance()),
						Color.cyan)
		);
	}


	private boolean ballIsPlaced()
	{
		return !ballNotOnTargetYet();
	}


	private boolean ballNeedsToBePassed()
	{
		return passMode != EPassMode.NONE
				&& getCurrentBallTarget().equals(ballTargetPos);
	}


	private boolean ballNotOnTargetYet()
	{
		double target2BallDist = getCurrentBallTarget()
				.distanceTo(getBall().getTrajectory().getPosByVel(0.0).getXYVector());
		return target2BallDist > getBallPlacementTolerance();
	}


	private IVector2 getCurrentBallTarget()
	{
		if (ballInGoal(0))
		{
			return getDestinationOutsideGoal();
		}

		var ballPos = getBall().getPos();
		var nearestInField = Geometry.getField().withMargin(-100).nearestPointInside(ballPos);
		if (nearestInField.distanceTo(ballPos) < getBallPlacementTolerance())
		{
			return ballTargetPos;
		}
		return Geometry.getField().withMargin(-300).nearestPointInside(ballPos);
	}


	private boolean ballInGoal(double margin)
	{
		double fullMargin = goalPullOutDistance + margin;
		return Geometry.getGoalOur().getRectangle().withMargin(fullMargin).isPointInShape(getBall().getPos())
				|| Geometry.getGoalTheir().getRectangle().withMargin(fullMargin).isPointInShape(getBall().getPos());
	}


	private IVector2 getDestinationOutsideGoal()
	{
		double margin = goalPullOutDistance + 100;
		double y = 0;
		if (Math.abs(getBall().getPos().y()) > Geometry.getGoalTheir().getWidth() / 2)
		{
			// outside of goal
			y = Math.signum(getBall().getPos().y()) * (Geometry.getGoalTheir().getWidth() / 2 + 300);
		}
		return Vector2.fromXY(Math.signum(getBall().getPos().x()) * (Geometry.getFieldLength() / 2 - margin), y);
	}


	private double getBallPlacementTolerance()
	{
		return RuleConstraints.getBallPlacementTolerance() - extraPlacementTolerance;
	}


	private class PrepareState extends BaseState<MoveToSkill>
	{
		private final GenericHysteresis forcePullHysteresis = new GenericHysteresis(
				// lower <=> ball outside or in goal <=> force pull
				() -> !Geometry.getFieldWBorders().withMargin(-300).isPointInShape(getBall().getPos())
						|| ballInGoal(0),
				// upper <=> ball inside and not in goal <=> no force
				() -> Geometry.getFieldWBorders().withMargin(-400).isPointInShape(getBall().getPos())
						&& !ballInGoal(100)
		);
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


		public boolean skipPrepare()
		{
			return ballHandling == EBallHandling.PULL_PUSH_ROTATE;
		}


		public boolean ballMoving()
		{
			return getBall().getVel().getLength2() > 0.5;
		}


		private IVector2 getDest()
		{
			double margin = 210;
			IVector2 pushDest = LineMath.stepAlongLine(getBall().getPos(), getCurrentBallTarget(), -margin);
			IVector2 pullDest = LineMath.stepAlongLine(getBall().getPos(), getCurrentBallTarget(), margin);

			forcePullHysteresis.update();
			if (forcePullHysteresis.isLower())
			{
				// force pull
				return pullDest;
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
			currentPlacementPos = getCurrentBallTarget();
			updateTargetPose();
		}


		@Override
		protected void onUpdate()
		{
			if (ballHandling == EBallHandling.PULL_PUSH_ROTATE
					&& passMode == EPassMode.NONE)
			{
				currentPlacementPos = getCurrentBallTarget();
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

				ballPlacedAndCleared = ballTargetPos.distanceTo(getBall().getPos()) < getBallPlacementTolerance();

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
			skill.setReadyForKick(passMode != EPassMode.WAIT);
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
		PULL_PUSH_ROTATE,
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
