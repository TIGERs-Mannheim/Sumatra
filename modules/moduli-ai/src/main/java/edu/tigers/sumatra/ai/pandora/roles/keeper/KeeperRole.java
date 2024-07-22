/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperActionType;
import edu.tigers.sumatra.ai.metis.keeper.IKeeperPenAreaMarginProvider;
import edu.tigers.sumatra.ai.metis.keeper.KeeperBehaviorCalc;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RamboKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.statemachine.TransitionableState;
import edu.tigers.sumatra.time.TimestampTimer;

import java.awt.Color;


/**
 * The Keeper protect the Goal and tries to catch every shoot into the goal.
 */
public class KeeperRole extends ARole
{
	@Configurable(comment = "[s] lock target this much time prior to panicking", defValue = "1.0")
	private static double passTargetLockTime = 1.0;
	@Configurable(comment = "[s] prior to held-ball foul, do not aim at all anymore", defValue = "0.5")
	private static double passPanicTime = 0.5;


	@Configurable(comment = "[rad/s] maximal angular velocity while handling the ball", defValue = "3")
	private static double ballHandlingMaxVelW = 3;
	@Configurable(comment = "[m/s] maximal velocity while handling the ball", defValue = "1")
	private static double ballHandlingMaxVel = 1;
	@Configurable(comment = "[rad/s2] maximal angular acceleration while handling the ball", defValue = "2")
	private static double ballHandlingMaxAccW = 2;
	@Configurable(comment = "[m/s^2] maximal acceleration while handling the ball", defValue = "1.5")
	private static double ballHandlingMaxAcc = 1.5;
	@Configurable(comment = "Dribbling Mode while receiving the ball", defValue = "DEFAULT")
	private static EDribblerMode receiveDribblerMode = EDribblerMode.DEFAULT;
	@Configurable(comment = "Dribbling Mode while handling the ball", defValue = "HIGH_POWER")
	private static EDribblerMode ballHandlingDribblerMode = EDribblerMode.HIGH_POWER;

	@Configurable(comment = "[rad] The approximate tolerance when the angle is considered to be reached", defValue = "0.15")
	private static double roughAngleTolerance = 0.15;

	@Configurable(defValue = "0.4", comment = "[s] The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 0.4;

	@Configurable(defValue = "100", comment = "[mm] Distance between bot and ball during MoveInFrontOfBall state")
	private static double moveInFrontDistance = 100;


	private final TimestampTimer heldBallTimer = new TimestampTimer(RuleConstraints.getKeeperHeldBallPeriod());
	private final DefendState defendState;

	private Pass keeperPass = null;
	private boolean spinUpFinished = false;

	private boolean movedInFront = false;


	public KeeperRole()
	{
		super(ERole.KEEPER);

		// Default States
		defendState = new DefendState();
		var moveToPenAreaState = new MoveToPenaltyAreaState();
		var stopState = new KeeperStoppedState();

		// One on One Penalty States
		var prepPenaltyState = new PreparePenaltyState();
		var ramboState = new RoleState<>(RamboKeeperSkill::new);

		// Get Ball Contact States
		var moveInFrontOfBallState = new MoveInFrontOfBallState();
		var getBallContactState = new RoleState<>(GetBallContactSkill::new);

		var interceptState = new InterceptRollingBallState();

		// Ball Handling States
		var moveWithBallPrepState = new SpinUpState();
		var moveWithBallState = new MoveWithBallState();
		var passPrepState = new SpinUpState();
		var passState = new PassState();

		setInitialState(defendState);

		bidirectionalTransition(defendState, stopState, EKeeperActionType.STOP);
		bidirectionalTransition(defendState, prepPenaltyState, EKeeperActionType.PREP_PENALTY);
		bidirectionalTransition(defendState, ramboState, EKeeperActionType.RAMBO);
		bidirectionalTransition(defendState, moveToPenAreaState, EKeeperActionType.MOVE_TO_PEN_AREA);


		setupStandardExitTransitions(defendState);
		transition(defendState, interceptState, EKeeperActionType.INTERCEPT_PASS);
		transitionAtStoppedBall(defendState, moveInFrontOfBallState, EKeeperActionType.HANDLE_BALL);
		transitionAtStoppedBall(defendState, moveInFrontOfBallState, EKeeperActionType.MOVE_BETWEEN_GOAL_AND_BALL);

		bidirectionalTransition(defendState, interceptState, EKeeperActionType.INTERCEPT_PASS);
		interceptState.addTransition("isBallDribblerContact", this::isDribblingBallOrHasContact, moveWithBallPrepState);

		setupStandardExitTransitions(moveInFrontOfBallState);
		moveInFrontOfBallState.addTransition("isBallDribblerContact", this::isDribblingBallOrHasContact,
				moveWithBallPrepState);
		moveInFrontOfBallState.addTransition("isBallMoving", this::isBallMoving, defendState);
		moveInFrontOfBallState.addTransition("movedInFront", () -> movedInFront, getBallContactState);

		setupStandardExitTransitions(getBallContactState);
		transition(getBallContactState, defendState, EKeeperActionType.MOVE_BETWEEN_GOAL_AND_BALL);
		getBallContactState.addTransition(ESkillState.SUCCESS, moveWithBallPrepState);
		getBallContactState.addTransition(ESkillState.FAILURE, defendState);

		setupStandardExitTransitions(moveWithBallPrepState);
		moveWithBallPrepState.addTransition("placementCanBeSkipped", this::placementCanBeSkipped, passState);
		moveWithBallPrepState.addTransition("isReadyToMoveBall", this::isReadyToMoveBall, moveWithBallState);
		moveWithBallPrepState.addTransition("isBallControlLost", this::isBallControlLost, defendState);


		setupStandardExitTransitions(moveWithBallState);
		moveWithBallState.addTransition(ESkillState.SUCCESS, passPrepState);
		moveWithBallState.addTransition(ESkillState.FAILURE, moveInFrontOfBallState);

		setupStandardExitTransitions(passPrepState);
		passPrepState.addTransition("isSpinUpFinished", () -> spinUpFinished, passState);
		passPrepState.addTransition("isBallControlLost", this::isBallControlLost, defendState);

		setupStandardExitTransitions(passState);
		passState.addTransition("isBallControlLost", this::isBallControlLost, defendState);
	}


	private EKeeperActionType getBehavior()
	{
		return getAiFrame().getTacticalField().getKeeperBehavior();
	}


	private void transition(TransitionableState from, TransitionableState to, EKeeperActionType condition)
	{
		if (from != to)
		{
			from.addTransition(condition.toString(), () -> getBehavior() == condition, to);
		}
	}


	private void transitionAtStoppedBall(TransitionableState from, TransitionableState to, EKeeperActionType condition)
	{
		if (from != to)
		{
			from.addTransition(condition.toString(), () -> (getBehavior() == condition && !isBallMoving()), to);
		}
	}


	private void bidirectionalTransition(TransitionableState from, TransitionableState to, EKeeperActionType condition)
	{
		if (from != to)
		{
			from.addTransition(condition.toString(), () -> getBehavior() == condition, to);
			to.addTransition(String.format("!%s", condition), () -> getBehavior() != condition, from);
		}
	}


	private void setupStandardExitTransitions(TransitionableState state)
	{
		transition(state, defendState, EKeeperActionType.IMPORTANT_DEFEND);
		transition(state, defendState, EKeeperActionType.MOVE_TO_PEN_AREA);
		transition(state, defendState, EKeeperActionType.STOP);
		transition(state, defendState, EKeeperActionType.PREP_PENALTY);
		transition(state, defendState, EKeeperActionType.RAMBO);
	}


	@Override
	protected void beforeUpdate()
	{
		if (getAiFrame().getGameState().isRunning()
				&& Geometry.getPenaltyAreaOur().withMargin(2 * Geometry.getBallRadius()).isPointInShape(getBall().getPos()))
		{
			heldBallTimer.update(getWFrame().getTimestamp());
			var remain = heldBallTimer.getRemainingTime(getWFrame().getTimestamp());
			Color color;
			if (remain > passTargetLockTime + passPanicTime)
			{
				color = Color.GREEN;
				keeperPass = getAiFrame().getTacticalField().getKeeperPass();
			} else if (remain > passPanicTime)
			{
				color = Color.YELLOW;
			} else
			{
				color = Color.RED;
			}
			getShapes(EAiShapesLayer.KEEPER_BEHAVIOR).add(
					new DrawableBorderText(Vector2.fromXY(1, 7), String.format("%.2f", remain)).setColor(color));
		} else
		{
			keeperPass = getAiFrame().getTacticalField().getKeeperPass();
			heldBallTimer.reset();
		}
	}


	private boolean isDribblingBallOrHasContact()
	{
		boolean isBallContact =
				getBot().getBotShape().getKickerLine().distanceTo(getBall().getPos()) <= Geometry.getBallRadius() + 5;
		boolean isBarrierInterrupted = getBot().getBallContact().hasContact();
		return (isBallContact && isBarrierInterrupted) || isDribblingBall();
	}


	private boolean isDribblingBall()
	{
		if (ballHandlingDribblerMode == EDribblerMode.DEFAULT)
		{
			return getBot().getRobotInfo().getDribbleTraction().getId() >= EDribbleTractionState.LIGHT.getId();
		} else
		{
			return getBot().getRobotInfo().getDribbleTraction().getId() >= EDribbleTractionState.STRONG.getId();
		}
	}


	private boolean isBallMoving()
	{
		return getBall().getVel().getLength() > 0.3;
	}


	private boolean isBallControlLost()
	{
		return !isDribblingBallOrHasContact()
				&& getBall().getPos().distanceTo(getPos()) > Geometry.getBallRadius() + Geometry.getBotRadius() + 50;
	}


	private IVector2 getPlacementPos(IKeeperPenAreaMarginProvider marginProvider)
	{
		var posInside = Geometry.getPenaltyAreaOur()
				.withMargin(marginProvider.atBorder())
				.nearestPointInside(getBall().getPos());

		var goalLine = Geometry.getGoalOur().getLineSegment().withMargin(-1.5 * Geometry.getBotRadius());
		if (goalLine.distanceTo(posInside) > Math.abs(marginProvider.atGoal()))
		{
			return posInside;
		}
		var startPoint = goalLine.closestPointOnPath(posInside);
		return LineMath.stepAlongLine(startPoint, getBall().getPos(), Math.abs(marginProvider.atGoal()));
	}


	private boolean placementCanBeSkipped()
	{
		var placementPos = getPlacementPos(KeeperBehaviorCalc.ballMarginLower());
		getShapes(EAiShapesLayer.KEEPER_BEHAVIOR).add(
				new DrawableCircle(Circle.createCircle(placementPos, 30), Color.RED));
		return spinUpFinished && placementPos.distanceTo(getBall().getPos()) < 1;
	}


	private boolean isReadyToMoveBall()
	{
		var ballToBotDir = getPos().subtractNew(getBall().getPos());
		var ballToPosDir = getPlacementPos(KeeperBehaviorCalc.ballMarginUpper()).subtractNew(getBall().getPos());
		var needToPush = AngleMath.diffAbs(ballToBotDir.getAngle(), ballToPosDir.getAngle()) > AngleMath.PI_HALF;
		return spinUpFinished && (!needToPush || MoveWithBallSkill.isAllowPushingTheBall());
	}


	private void setupBallHandlingSkill(AMoveToSkill skill)
	{
		skill.getMoveCon().physicalObstaclesOnly();
		skill.getMoveCon().setBallObstacle(false);
		skill.getMoveCon().setBotsObstacle(false);

		skill.getMoveConstraints().setVelMaxW(ballHandlingMaxVelW);
		skill.getMoveConstraints().setAccMaxW(ballHandlingMaxAccW);
		skill.getMoveConstraints().setVelMax(ballHandlingMaxVel);
		skill.getMoveConstraints().setAccMax(ballHandlingMaxAcc);
	}


	private boolean startPanicking()
	{
		var lookingForward = Math.abs(getBot().getAngleByTime(0)) < 1.3;
		var distPenAreaBoundaryToGoal = (Geometry.getPenaltyAreaWidth() - Geometry.getGoalOur().getWidth()) / 2;
		var penArea = Geometry.getPenaltyAreaOur().withMargin(-distPenAreaBoundaryToGoal);
		var lookingDirection = Lines.halfLineFromAngle(getPos(), getBot().getAngleByTime(0));
		var lookingOutside = penArea.intersectPerimeterPath(lookingDirection).isEmpty();

		return heldBallTimer.getRemainingTime(getWFrame().getTimestamp()) < passPanicTime
				&& (lookingForward || lookingOutside);
	}


	private KickParams createBallHandlingKickParams(EDribblerMode dribblerMode)
	{
		if (startPanicking())
		{
			return panicKick(dribblerMode);
		}
		return KickParams.disarm().withDribblerMode(dribblerMode);
	}


	private KickParams panicKick(EDribblerMode dribblerMode)
	{
		var ballTravel = Lines.halfLineFromDirection(getBot().getPos(), Vector2.fromAngle(getBot().getAngleByTime(0)));
		var intersections = Geometry.getField().intersectPerimeterPath(ballTravel);
		if (intersections.size() != 1)
		{
			// Should never happen, but if it does yeet the ball away
			return KickParams.maxChip().withDribblerMode(dribblerMode);
		}
		var distance = intersections.get(0).distanceTo(getPos());
		var kickSpeed = getBall().getChipConsultant().getInitVelForDistAtTouchdown(distance, 3);
		return KickParams.chip(SumatraMath.min(kickSpeed, RuleConstraints.getMaxKickSpeed()))
				.withDribblerMode(dribblerMode);
	}


	private class DefendState extends RoleState<CriticalKeeperSkill>
	{

		public DefendState()
		{
			super(CriticalKeeperSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.setBallHandlingKickParams(createBallHandlingKickParams(receiveDribblerMode));
			skill.getMoveCon().setBallObstacle(false);
		}
	}

	private class KeeperStoppedState extends RoleState<MoveToSkill>
	{
		private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall(new PointChecker()
				.checkBallDistances()
				.checkInsideField()
				.checkPointFreeOfBots());


		public KeeperStoppedState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();

			var dest = LineMath.stepAlongLine(Geometry.getGoalOur().bisection(getBall().getPos()), getBall().getPos(),
							Geometry.getGoalOur().getWidth() / 3)
					.addNew(Vector2.fromX(Geometry.getPenaltyAreaDepth() / 5));
			skill.updateDestination(keepDistanceToBall.findNextFreeDest(getAiFrame(), dest, getBotID()));
			skill.updateLookAtTarget(getWFrame().getBall());
		}
	}

	private class MoveToPenaltyAreaState extends RoleState<MoveToSkill>
	{
		public MoveToPenaltyAreaState()
		{
			super(MoveToSkill::new);
		}


		@Override
		public void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			// if game is running, get into penArea as fast as possible (in STOP, we are limited to stop speed anyway)
			skill.getMoveConstraints().setFastMove(true);
			skill.updateDestination(Geometry.getPenaltyAreaOur().getRectangle().center());
			skill.updateLookAtTarget(Geometry.getCenter());
		}
	}

	private class PreparePenaltyState extends RoleState<MoveToSkill>
	{
		PreparePenaltyState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.updateDestination(Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(Geometry.getBotRadius() - 20)));
		}
	}

	private class InterceptRollingBallState extends RoleState<MoveToSkill>
	{
		public InterceptRollingBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.updateLookAtTarget(getBall());
			skill.setKickParams(createBallHandlingKickParams(receiveDribblerMode));
			if (getBall().getPos().distanceTo(getInterceptDestination()) > 4 * Geometry.getBotRadius())
			{
				skill.updateDestination(getInterceptDestination());
			}
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.updateDestination(getInterceptDestination());
			skill.updateLookAtTarget(getBall());
		}


		private IVector2 getInterceptDestination()
		{
			return getAiFrame().getTacticalField().getKeeperInterceptPos()
					.addNew(getBall().getVel().scaleToNew(getBot().getCenter2DribblerDist()));
		}
	}

	private class MoveInFrontOfBallState extends RoleState<MoveToSkill>
	{
		private TimestampTimer ballStoppedTimer = new TimestampTimer(0.1);


		public MoveInFrontOfBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			var placementPos = getPlacementPos(KeeperBehaviorCalc.ballMarginUpper());
			var destination = getApproachDestination();
			var stage = getApproachStage(destination);
			configureMovementConstraints(stage);
			getShapes(EAiShapesLayer.KEEPER_BEHAVIOR)
					.add(new DrawableAnnotation(getPos().addNew(Vector2.fromY(200)), stage.toString(), true));


			skill.updateDestination(destination);
			getShapes(EAiShapesLayer.KEEPER_BEHAVIOR).add(new DrawableLine(destination, placementPos, Color.RED));
			skill.updateLookAtTarget(getBall());

			if (getBall().getVel().getLength2() < 0.1)
			{
				ballStoppedTimer.update(getWFrame().getTimestamp());
			} else
			{
				ballStoppedTimer.reset();
			}

			movedInFront = skill.getSkillState() == ESkillState.SUCCESS && ballStoppedTimer.isTimeUp(
					getWFrame().getTimestamp());
		}


		@Override
		protected void onExit()
		{
			movedInFront = false;
		}


		private IVector2 getApproachDestination()
		{
			var target = getBall().getTrajectory().getPosByTime(0.3).getXYVector();
			IVector2 posToBuildLine;
			if (Geometry.getPenaltyAreaOur().withMargin(KeeperBehaviorCalc.ballMarginMiddle().atBorder())
					.isPointInShapeOrBehind(target))
			{
				var pointOnGoal = Geometry.getGoalOur().getLineSegment()
						.withMargin(-Geometry.getBotRadius())
						.closestPointOnPath(target);
				if (pointOnGoal.distanceTo(getBall().getPos()) > Geometry.getBotRadius())
				{
					posToBuildLine = pointOnGoal.addNew(Vector2.fromX(Geometry.getBotRadius()));
				} else
				{
					posToBuildLine = pointOnGoal.addNew(Vector2.fromX(-2 * Geometry.getBotRadius()));
				}
			} else
			{
				posToBuildLine = Geometry.getPenaltyAreaOur().withMargin(KeeperBehaviorCalc.ballMarginUpper().atBorder())
						.nearestPointInside(target);
			}
			getShapes(EAiShapesLayer.KEEPER_BEHAVIOR).add(new DrawablePoint(posToBuildLine, Color.PINK));
			return LineMath.stepAlongLine(target, posToBuildLine, calcWantedPositionDistanceToBall());
		}


		private void configureMovementConstraints(EApproachStage stage)
		{
			skill.getMoveCon().physicalObstaclesOnly();
			switch (stage)
			{
				case FAR ->
				{
					skill.getMoveCon().setGoalPostsObstacle(true);
					skill.getMoveCon().setBallObstacle(true);
					skill.getMoveCon().setDistanceToBall(moveInFrontDistance / 2);
					skill.getMoveConstraints().setVelMax(SumatraMath.max(0.1, getBot().getMoveConstraints().getVelMax()));
				}
				case MEDIUM ->
				{
					skill.getMoveCon().setGoalPostsObstacle(false);
					skill.getMoveCon().setBallObstacle(true);
					skill.getMoveCon().setDistanceToBall(moveInFrontDistance / 2);
					skill.getMoveConstraints().setVelMax(ballHandlingMaxVel / 2);
				}
				case CLOSE ->
				{
					skill.getMoveCon().setGoalPostsObstacle(false);
					var margin = calcWantedPerimeterDistanceToBall() - (Geometry.getBotRadius()
							- getBot().getCenter2DribblerDist());
					skill.getMoveCon().setBallObstacle(margin > 0);
					skill.getMoveCon().setDistanceToBall(margin > 0 ? margin / 2 : null);
					skill.getMoveConstraints().setVelMax(ballHandlingMaxVel / 2);
				}
			}
		}


		private double calcWantedPositionDistanceToBall()
		{
			return calcWantedPerimeterDistanceToBall() + Geometry.getBallRadius() + getBot().getCenter2DribblerDist();
		}


		private double calcWantedPerimeterDistanceToBall()
		{
			var distToGaol = Geometry.getGoalOur().getLineSegment().distanceTo(getBall().getPos())
					- Geometry.getBallRadius();
			return SumatraMath.cap(distToGaol, 0, moveInFrontDistance);
		}


		private EApproachStage getApproachStage(IVector2 destination)
		{
			if (Geometry.getGoalOur().getLineSegment().distanceTo(getBall().getPos()) > 3 * Geometry.getBotRadius())
			{
				// Ball is rather far away from the goal -> always stay in far approach stage as the slower ones are not necessary
				return EApproachStage.FAR;
			}
			var distance = calcWantedPositionDistanceToBall();
			var ball2bot = Vector2.fromPoints(getBall().getPos(), getPos());
			if (ball2bot.getLength() > 3 * distance)
			{
				return EApproachStage.FAR;
			}
			if (ball2bot.getLength() < 1.5 * distance || destination.distanceTo(getPos()) < Geometry.getBotRadius())
			{
				return EApproachStage.CLOSE;
			} else
			{
				return EApproachStage.MEDIUM;
			}
		}


		private enum EApproachStage
		{
			FAR,
			MEDIUM,
			CLOSE
		}
	}

	private class SpinUpState extends RoleState<MoveToSkill>
	{
		IVector2 targetPos;
		double targetAngle;

		TimestampTimer stuckTimer = new TimestampTimer(0.3);


		SpinUpState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			targetPos = getTargetPos();
			targetAngle = getBot().getAngleByTime(0);
			skill.setMinTimeAtDestForSuccess(0.1);
			stuckTimer.reset();
		}


		@Override
		protected void onUpdate()
		{
			setupBallHandlingSkill(skill);
			skill.updateDestination(targetPos);
			skill.updateTargetAngle(targetAngle);

			if (skill.getSkillState() != ESkillState.SUCCESS)
			{
				spinUpFinished = false;
			} else
			{
				spinUpFinished = isDribblingBall();
			}

			if (spinUpFinished)
			{
				stuckTimer.update(getWFrame().getTimestamp());
			} else
			{
				stuckTimer.reset();
			}

			if (stuckTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				skill.setKickParams(panicKick(ballHandlingDribblerMode));
			} else
			{
				skill.setKickParams(createBallHandlingKickParams(ballHandlingDribblerMode));
			}
		}


		@Override
		protected void onExit()
		{
			spinUpFinished = false;
		}


		private IVector2 getTargetPos()
		{
			var vel = getBot().getVel();
			var velLength = vel.getLength2();
			var breakDistance = 1000 * (0.5f * velLength * velLength / ballHandlingMaxAcc);

			return Geometry.getField().nearestPointInside(getPos().addNew(vel.scaleToNew(breakDistance)));
		}
	}

	private class MoveWithBallState extends RoleState<MoveWithBallSkill>
	{
		public MoveWithBallState()
		{
			super(MoveWithBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			updateTargetPose(getPlacementPos(KeeperBehaviorCalc.ballMarginUpper()));
		}


		@Override
		protected void onUpdate()
		{
			IVector2 placementPos = getPlacementPos(KeeperBehaviorCalc.ballMarginUpper());
			if (placementPos.distanceTo(getBall().getPos()) > 300)
			{
				updateTargetPose(placementPos);
			}
			skill.setForcedChipSpeed(createBallHandlingKickParams(ballHandlingDribblerMode).getKickSpeed());
		}


		private void updateTargetPose(final IVector2 placementPos)
		{
			double dist2Ball = Geometry.getBallRadius() + getBot().getCenter2DribblerDist();
			skill.setFinalDest(LineMath.stepAlongLine(placementPos, getBall().getPos(), -dist2Ball));
		}
	}


	private class PassState extends RoleState<MoveToSkill>
	{

		IVector2 targetDestination;
		private TargetAngleReachedChecker targetAngleReachedChecker;


		PassState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			targetAngleReachedChecker = new TargetAngleReachedChecker(roughAngleTolerance, maxTimeTargetAngleReached);
			targetDestination = getPos();
		}


		@Override
		protected void onUpdate()
		{
			setupBallHandlingSkill(skill);
			skill.updateDestination(targetDestination);
			setTargetAndKickParams();
		}


		private void setTargetAndKickParams()
		{
			double tolerance;
			IVector2 target;
			KickParams paramsIfAimed;
			if (keeperPass == null)
			{
				target = Geometry.getGoalTheir().getCenter();
				tolerance = 0.1;
				paramsIfAimed = KickParams.maxChip().withDribblerMode(ballHandlingDribblerMode);
			} else
			{
				target = keeperPass.getKick().getTarget();
				tolerance = keeperPass.getKick().getAimingTolerance();
				paramsIfAimed = keeperPass.getKick().getKickParams().withDribblerMode(ballHandlingDribblerMode);
			}

			skill.updateLookAtTarget(target);
			var currentAngle = getBot().getAngleByTime(0);
			var targetAngle = Vector2.fromPoints(getPos(), target).getAngle();

			var passRangeTolerance = tolerance / 2;
			// try being a bit more precise than the pass range, but have a minimum tolerance
			var angleTolerance = Math.max(roughAngleTolerance, passRangeTolerance - roughAngleTolerance);
			targetAngleReachedChecker.setOuterAngleDiffTolerance(angleTolerance);

			targetAngleReachedChecker.update(targetAngle, currentAngle, getWFrame().getTimestamp());

			if (targetAngleReachedChecker.isReached())
			{
				skill.setKickParams(paramsIfAimed);
			} else
			{
				skill.setKickParams(createBallHandlingKickParams(ballHandlingDribblerMode));
			}
		}
	}
}
