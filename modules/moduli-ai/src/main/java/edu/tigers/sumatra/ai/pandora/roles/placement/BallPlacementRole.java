/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
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


/**
 * The primary ball placement role.
 */
public class BallPlacementRole extends ARole
{
	@Configurable(defValue = "PULL_PUSH")
	private static EBallHandling ballHandling = EBallHandling.PULL_PUSH;

	@Configurable(defValue = "150.0", comment = "Distance [mm] to pull the ball out of the goal")
	private static double goalPullOutDistance = 150;

	@Configurable(defValue = "3.0")
	private static double passEndSpeed = 3.0;

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
		var receiveState = new RoleState<>(ReceiveBallSkill::new);
		var prepareState = new PrepareState();
		var stopBallState = new RoleState<>(() -> new ApproachAndStopBallSkill().ignorePenAreas());
		var getBallContactState = new RoleState<>(GetBallContactSkill::new);
		var moveWithBallState = new MoveWithBallState();
		var passState = new PassState();

		setInitialState(clearBallState);

		clearBallState.addTransition(this::ballNeedsToBePlaced, receiveState);
		receiveState.addTransition(ESkillState.SUCCESS, prepareState);
		receiveState.addTransition(ESkillState.FAILURE, stopBallState);
		prepareState.addTransition(this::ballIsPlaced, clearBallState);
		prepareState.addTransition(prepareState::success, getBallContactState);
		prepareState.addTransition(prepareState::ballMoving, receiveState);
		prepareState.addTransition(this::ballNeedsToBePassed, passState);
		prepareState.addTransition(prepareState::skipPrepare, getBallContactState);
		stopBallState.addTransition(ESkillState.SUCCESS, prepareState);
		stopBallState.addTransition(ESkillState.FAILURE, prepareState);
		getBallContactState.addTransition(ESkillState.SUCCESS, moveWithBallState);
		getBallContactState.addTransition(ESkillState.FAILURE, clearBallState);
		moveWithBallState.addTransition(ESkillState.SUCCESS, clearBallState);
		moveWithBallState.addTransition(ESkillState.FAILURE, receiveState);
		passState.addTransition(ESkillState.FAILURE, clearBallState);
		passState.addTransition(ESkillState.SUCCESS, clearBallState);
		passState.addTransition(() -> passMode == EPassMode.NONE, clearBallState);
	}


	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();
		getShapes(EAiShapesLayer.AI_BALL_PLACEMENT).add(
				new DrawableCircle(Circle.createCircle(getCurrentBallTarget(), RuleConstraints.getBallPlacementTolerance()),
						Color.magenta)
		);
		getShapes(EAiShapesLayer.AI_BALL_PLACEMENT).add(
				new DrawableCircle(Circle.createCircle(ballTargetPos, RuleConstraints.getBallPlacementTolerance()),
						Color.cyan)
		);
	}


	private boolean ballNeedsToBePlaced()
	{
		return getBall().isOnCam(0.5) && ballNotOnTargetYet();
	}


	private boolean ballIsPlaced()
	{
		return !ballNeedsToBePlaced();
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
		return target2BallDist > RuleConstraints.getBallPlacementTolerance();
	}


	private IVector2 getCurrentBallTarget()
	{
		if (ballInGoal(0))
		{
			return getDestinationOutsideGoal();
		}

		var ballPos = getBall().getPos();
		var nearestInField = Geometry.getField().withMargin(-200).nearestPointInside(ballPos);
		if (nearestInField.distanceTo(ballPos) < RuleConstraints.getBallPlacementTolerance())
		{
			return ballTargetPos;
		}
		return nearestInField;
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


	private class PrepareState extends RoleState<MoveToSkill>
	{
		private final GenericHysteresis forcePullHysteresis = new GenericHysteresis(
				// lower <=> ball outside or in goal <=> force pull
				() -> !Geometry.getField().withMargin(-200).isPointInShape(getBall().getPos())
						|| ballInGoal(0),
				// upper <=> ball inside and not in goal <=> no force
				() -> Geometry.getField().withMargin(-300).isPointInShape(getBall().getPos())
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
			skill.updateLookAtTarget(getBall());
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);

			if (getBot().getBallContact().hadContact(0.2))
			{
				calmDownTimer.setDuration(0.3);
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
				skill.updateDestination(getDest());
				skill.getMoveCon().setBallObstacle(true);
				calmedDown = true;
			} else
			{
				skill.updateDestination(getPos());
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

			switch (ballHandling)
			{
				case PULL:
					return pullDest;
				case PUSH:
					return pushDest;
				default:
					return getPos().nearestTo(pullDest, pushDest);
			}
		}
	}

	private class MoveWithBallState extends RoleState<MoveWithBallSkill>
	{
		private IVector2 currentPlacementPos;


		public MoveWithBallState()
		{
			super(MoveWithBallSkill::new);
		}


		@Override
		protected void onInit()
		{
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

	private class ClearBallState extends RoleState<MoveToSkill>
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
			skill.getMoveCon().physicalObstaclesOnly();
			// ignore the ball while calming down
			skill.getMoveCon().setBallObstacle(false);

			if (getBot().getBallContact().hadContact(0.2))
			{
				calmDownTimer.setDuration(0.5);
			} else
			{
				calmDownTimer.setDuration(0.0);
			}
			calmDownTimer.start(getWFrame().getTimestamp());
		}


		@Override
		protected void onUpdate()
		{
			if (!calmDownTimer.isTimeUp(getWFrame().getTimestamp()))
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

				ballPlacedAndCleared =
						ballTargetPos.distanceTo(getBall().getPos()) < RuleConstraints.getBallPlacementTolerance();

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


		public boolean isCleared()
		{
			return getBall().getPos().distanceTo(getPos()) > Geometry.getBotRadius() + 50;
		}
	}

	private class PassState extends RoleState<SingleTouchKickSkill>
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
