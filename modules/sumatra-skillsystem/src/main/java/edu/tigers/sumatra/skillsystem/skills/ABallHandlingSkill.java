/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.statemachine.TransitionableState;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;


public class ABallHandlingSkill extends AMoveToSkill
{

	@Configurable(defValue = "-100")
	private static double maxPushDistance = -100;

	@Configurable(defValue = "30")
	private static double ballContactPushPullSpeed = 30;

	@Configurable(defValue = "-20")
	private static double nearBallPushDistance = -20;

	@Configurable(defValue = "0.3")
	private static double aroundBallLookahead = 0.3;

	@Configurable(defValue = "0.4")
	private static double maxTargetOrientationStep = 0.4;

	@Configurable(defValue = "1.3")
	private static double maxTargetOrientationStepStrong = 1.3;

	@Configurable(comment = "The max margin to the ball for destinations", defValue = "70.0")
	private static double maxMarginToBall = 70.0;

	@Configurable(comment = "Tolerance [mm] to determine if bot is near ball", defValue = "20.0")
	private static double nearBallTolerance = 20.0;

	@Getter
	@Configurable(defValue = "0.10", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.10;

	@Configurable(defValue = "0.4", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 0.4;

	@Configurable(defValue = "1.1", comment = "[s] Reset timer time limit")
	private static double resetTimerTime = 1.1;

	@Configurable(defValue = "0.3", comment = "[s] Reset duration time limit")
	private static double resetDurationTime = 0.3;

	@Configurable(defValue = "5.7", comment = "[deg] orientation target overstep")
	private static double orientationTargetOverstep = 5.7;

	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);

	@Setter
	private double marginToTheirPenArea = 0;

	@Setter
	private EBallHandlingSkillTurnAdvise turnAdvise = EBallHandlingSkillTurnAdvise.NONE;

	@Setter
	private EBallHandlingSkillMoveAdvise moveAdvise = EBallHandlingSkillMoveAdvise.NONE;

	private DoubleChargingValue chargingValue;

	protected final PositionValidator positionValidator = new PositionValidator();

	protected double targetOrientation;

	protected double finalTargetOrientation;

	@Setter
	protected IVector2 target;
	@Setter
	protected double passRange;
	@Setter
	private boolean stopOnContact = false;
	@Getter
	private boolean isStopped = false;

	private IVector2 contactPos = null;
	private double contactAngle = 0;

	private TimestampTimer resetTimer = new TimestampTimer(resetTimerTime);


	ABallHandlingSkill()
	{
		var defaultState = new DefaultState();
		var resetState = new ResetState();

		defaultState.addTransition(this::doReset, resetState);
		resetState.addTransition(resetState::isDone, defaultState);

		setInitialState(defaultState);
	}


	private boolean doReset()
	{
		if (getWorldFrame().getBall().getPos().distanceTo(getTBot().getBotKickerPos()) < Geometry.getBotRadius() * 2
				&& !getTBot().getBallContact().hadRecentContact())
		{
			resetTimer.update(getWorldFrame().getTimestamp());
			return resetTimer.isTimeUp(getWorldFrame().getTimestamp());
		} else
		{
			resetTimer.reset();
		}
		return false;
	}


	class DefaultState extends TransitionableState
	{

		public DefaultState()
		{
			super(ABallHandlingSkill.this::changeState);
		}


		@Override
		public void doEntryActions()
		{
			var dist2Ball = isNearBall() ? nearBallPushDistance : 0;

			chargingValue = new DoubleChargingValue(
					dist2Ball,
					200,
					-2000,
					maxPushDistance,
					0 // must be zero to make isNearBall() work
			);
		}


		@Override
		protected void onUpdate()
		{
			finalTargetOrientation = target.subtractNew(getBall().getPos()).getAngle(0);
			targetOrientation = getTargetOrientation(finalTargetOrientation);
			var passRangeTolerance = passRange / 2;
			// try being a bit more precise than the pass range, but have a minimum tolerance
			var angleTolerance = Math.max(roughAngleTolerance, passRangeTolerance - roughAngleTolerance);
			targetAngleReachedChecker.setOuterAngleDiffTolerance(angleTolerance);
			var angleDiff = targetAngleReachedChecker.update(
					finalTargetOrientation,
					getAngle(),
					getWorldFrame().getTimestamp()
			);

			chargingValue.setChargeMode(
					isRoughlyFocussed() ? DoubleChargingValue.ChargeMode.DECREASE : DoubleChargingValue.ChargeMode.INCREASE);
			chargingValue.update(getWorldFrame().getTimestamp());
			var dist2Ball = chargingValue.getValue();

			IVector2 dest = findDest(dist2Ball);

			positionValidator.update(getWorldFrame(), getMoveCon());
			positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
			dest = positionValidator.movePosInsideField(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);

			if (getTBot().getBallContact().hadContact(0.2) && stopOnContact)
			{
				isStopped = true;
				if (contactPos == null)
				{
					contactPos = getPos();
					contactAngle = getAngle();
				}
				dest = contactPos;
				targetOrientation = contactAngle;
			} else
			{
				contactPos = null;
				isStopped = false;
			}

			updateDestination(dest);
			updateTargetAngle(targetOrientation);

			ABallHandlingSkill.super.doUpdate();

			drawShapes(getAngle(), angleDiff, angleTolerance);
		}


		private IVector2 findDest(double dist2Ball)
		{
			if (getTBot().getBallContact().hasContact())
			{
				double distance =
						moveAdvise == EBallHandlingSkillMoveAdvise.PULL ?
								-ballContactPushPullSpeed :
								ballContactPushPullSpeed;
				return getPos().addNew(Vector2.fromAngle(getTBot().getOrientation()).scaleToNew(distance));
			}
			return AroundBallCalc
					.aroundBall()
					.withBallPos(getBallPosByTime(aroundBallLookahead))
					.withTBot(getTBot())
					.withDestination(getDestination(dist2Ball))
					.withMaxMargin(maxMarginToBall)
					.withMinMargin(dist2Ball)
					.build()
					.getAroundBallDest();
		}


		private void drawShapes(double orientation, double angleDiff, double angleTolerance)
		{
			var ballPos = getBallPos();
			var kickDir = target.subtractNew(ballPos);
			var currentTolerance = targetAngleReachedChecker.getCurrentTolerance();
			var toleranceLine1 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(angleTolerance));
			var toleranceLine2 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(-angleTolerance));
			var focusLine1 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(currentTolerance));
			var focusLine2 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(-currentTolerance));
			var targetLine = Lines.segmentFromPoints(getBall().getPos(), target);
			var orientationOffset = Vector2.fromAngleLength(orientation, targetLine.getLength());
			var currentOrientationLine = Lines.segmentFromOffset(getPos(), orientationOffset);
			var teamColor = getBotId().getTeamColor().getColor();
			var focusedTxt = createFocusedText();
			var angleDiffTxt = String.format("%.2f -> %s", angleDiff, focusedTxt);
			var txtOffset = Vector2f.fromY(200);

			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(getPos(),
					String.format("dist2Ball: %.2f", chargingValue.getValue())).withOffset(Vector2.fromY(300)));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawablePoint(ballPos, Color.green));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(toleranceLine1, Color.green));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(toleranceLine2, Color.green));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(focusLine1, Color.magenta));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(focusLine2, Color.magenta));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(targetLine, teamColor));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(currentOrientationLine, Color.orange));
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(getPos(), angleDiffTxt, txtOffset));
		}


		private String createFocusedText()
		{
			if (isFocused())
			{
				return "focused";
			}
			return isRoughlyFocussed() ? "roughly focussed" : "unfocused";
		}


		private IVector2 getBallPosByTime(final double lookahead)
		{
			return getBall().getTrajectory().getPosByTime(lookahead).getXYVector();
		}


		private double getTargetOrientation(double finalTargetOrientation)
		{
			double currentDirection = getBall().getPos().subtractNew(getPos()).getAngle(0);
			double diff = AngleMath.difference(finalTargetOrientation, currentDirection);

			double stepSize = maxTargetOrientationStep;
			if (getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
			{
				stepSize = maxTargetOrientationStepStrong;
			}

			if (getTBot().getBallContact().hasContact())
			{
				if (diff > 0 && turnAdvise == EBallHandlingSkillTurnAdvise.RIGHT)
				{
					diff = diff - AngleMath.PI_TWO;
				} else if (diff < 0 && turnAdvise == EBallHandlingSkillTurnAdvise.LEFT)
				{
					diff = diff + AngleMath.PI_TWO;
				}
				diff += Math.signum(diff) * AngleMath.deg2rad(orientationTargetOverstep);
			}

			return currentDirection + Math.signum(diff) * Math.min(stepSize,
					Math.abs(diff));
		}


		private IVector2 getDestination(final double margin)
		{
			return LineMath.stepAlongLine(getBall().getPos(), target, -getDistance(margin));
		}


		private double getDistance(final double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + margin;
		}


		private boolean isRoughlyFocussed()
		{
			return isNearBall() && targetAngleReachedChecker.isRoughlyFocussed();
		}


		private boolean isNearBall()
		{
			return getBallPos().distanceTo(getTBot().getBotKickerPos()) < (Geometry.getBallRadius() + nearBallTolerance);
		}
	}


	class ResetState extends TransitionableState
	{
		private TimestampTimer timer = new TimestampTimer(resetDurationTime);


		public ResetState()
		{
			super(ABallHandlingSkill.this::changeState);
		}


		@Override
		protected void onInit()
		{
			super.onInit();
			timer.start(getWorldFrame().getTimestamp());
			resetTimer.reset();

			setMotorsOff();
		}


		public boolean isDone()
		{
			return timer.isTimeUp(getWorldFrame().getTimestamp());
		}
	}


	protected boolean isFocused()
	{
		return targetAngleReachedChecker.isReached();
	}


	protected IVector2 getBallPos()
	{
		return getBall().getPos();
	}

}
