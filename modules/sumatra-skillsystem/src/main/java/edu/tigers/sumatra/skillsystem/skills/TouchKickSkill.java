/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * The 'normal' kick skill for in-game kicks.
 */
public class TouchKickSkill extends ATouchKickSkill
{
	@Configurable(comment = "The max margin to the ball for destinations", defValue = "20.0")
	private static double maxMarginToBall = 20.0;

	@Configurable(defValue = "0.1", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.1;

	@Configurable(defValue = "1.0", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 1.0;

	@Configurable(defValue = "3000", comment = "Constant dribble speed during kicks")
	private static int dribbleSpeed = 3000;

	private final TargetAngleReachedChecker targetAngleReachedChecker;
	private final PositionValidator positionValidator = new PositionValidator();
	private final BallStabilizer ballStabilizer = new BallStabilizer();
	private double marginToTheirPenArea = 0;


	/**
	 * UI constructor
	 *
	 * @param target
	 * @param device
	 * @param kickSpeed
	 */
	@SuppressWarnings("unused") // used by UI
	public TouchKickSkill(
			final DynamicPosition target,
			final EKickerDevice device,
			final double kickSpeed)
	{
		this(target, KickParams.of(device, kickSpeed));
	}


	public TouchKickSkill(final DynamicPosition target, final KickParams kickParams)
	{
		super(ESkill.TOUCH_KICK, target, kickParams);
		targetAngleReachedChecker = new TargetAngleReachedChecker(roughAngleTolerance, maxTimeTargetAngleReached);
		setInitialState(new KickState());
	}


	public void setMarginToTheirPenArea(final double marginToTheirPenArea)
	{
		this.marginToTheirPenArea = marginToTheirPenArea;
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();

		ballStabilizer.update(getBall(), getTBot());

		double passRangeTolerance = target.getPassRange() / 2;
		// try being a bit more precise than the pass range, but have a minimum tolerance
		double angleTolerance = Math.max(roughAngleTolerance, passRangeTolerance - roughAngleTolerance);
		targetAngleReachedChecker.setOuterAngleDiffTolerance(angleTolerance);

		final double targetOrientation = target.getPos().subtractNew(getBall().getPos()).getAngle(getAngle());
		final double angleDiff = targetAngleReachedChecker.update(targetOrientation, getOrientationFromFilter(),
				getWorldFrame().getTimestamp());
		final IVector2 ballPos = getBallPos();
		final IVector2 kickDir = target.getPos().subtractNew(ballPos);
		final double currentTolerance = targetAngleReachedChecker.getCurrentTolerance();
		final ILineSegment toleranceLine1 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(angleTolerance));
		final ILineSegment toleranceLine2 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(-angleTolerance));
		final ILineSegment focusLine1 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(currentTolerance));
		final ILineSegment focusLine2 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(-currentTolerance));
		final Line targetLine = Line.fromPoints(getBall().getPos(), target.getPos());
		final Color teamColor = getBotId().getTeamColor().getColor();
		final String angleDiffTxt = String.format("%.2f -> %s", angleDiff, isFocused() ? "focused" : "unfocused");
		final IVector2 txtOffset = Vector2f.fromY(200);

		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawablePoint(ballPos, Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(toleranceLine1, Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(toleranceLine2, Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(focusLine1, Color.magenta));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(focusLine2, Color.magenta));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(targetLine, teamColor));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(ballPos, angleDiffTxt, txtOffset));
	}


	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);

		double kickSpeed = adaptKickSpeed(target.getPos(), kickParams.getKickSpeed());
		if (isFocused())
		{
			kickerDribblerOutput.setKick(kickSpeed, kickParams.getDevice(), EKickerMode.ARM);
		} else
		{
			kickerDribblerOutput.setKick(kickSpeed, kickParams.getDevice(), EKickerMode.DISARM);
		}

		if (kickParams.getDribbleSpeed() > 0)
		{
			kickerDribblerOutput.setDribblerSpeed(kickParams.getDribbleSpeed());
		} else
		{
			kickerDribblerOutput.setDribblerSpeed(dribbleSpeed);
		}
	}


	private IVector2 getBallPos()
	{
		return ballStabilizer.getBallPos();
	}


	private boolean isFocused()
	{
		return targetAngleReachedChecker.isReached();
	}


	private double getOrientationFromFilter()
	{
		return getTBot().getFilteredState().map(State::getOrientation).orElseGet(this::getAngle);
	}


	private class KickState extends MoveToState
	{
		private double dist2Ball = 0;
		protected MinMarginChargeValue minMarginChargeValue;


		private KickState()
		{
			super(TouchKickSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			dist2Ball = 20;
			getMoveCon().setBallObstacle(false);

			minMarginChargeValue = MinMarginChargeValue.aMinMargin()
					.withDefaultValue(10)
					.withInitValue(isNearBall() ? -10 : 10)
					.withLimit(-50)
					.withChargeRate(-200)
					.withLowerThreshold(70)
					.withUpperThreshold(90)
					.build();
		}


		private boolean isNearBall()
		{
			return getBallPos().distanceTo(getTBot().getBotKickerPos()) < (Geometry.getBallRadius() + 10);
		}


		@Override
		public void doUpdate()
		{
			IVector2 dest = AroundBallCalc
					.aroundBall()
					.withBallPos(getBallPosByTime(0.5))
					.withTBot(getTBot())
					.withDestination(getDestination(0))
					.withMaxMargin(maxMarginToBall)
					.withMinMargin(dist2Ball)
					.build()
					.getAroundBallDest();
			dist2Ball = getMinMargin(dest);

			positionValidator.update(getWorldFrame(), getMoveCon());
			positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
			dest = positionValidator.movePosInsideField(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);

			getMoveCon().updateDestination(dest);
			final double targetOrientation = getTargetOrientation();
			getMoveCon().updateTargetAngle(targetOrientation);

			super.doUpdate();

			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(getPos(),
					String.format("%.2f", dist2Ball)).withOffset(Vector2.fromY(300)));
		}


		private IVector2 getBallPosByTime(final double lookahead)
		{
			return ballStabilizer.getBallPos(lookahead);
		}


		private double getTargetOrientation()
		{
			double finalTargetOrientation = target.getPos().subtractNew(getBall().getPos()).getAngle(0);

			double currentDirection = getBall().getPos().subtractNew(getPos()).getAngle(0);
			double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
			double alteredDiff = Math.signum(diff) * Math.max(0, Math.abs(diff) - 0.4);

			return finalTargetOrientation - alteredDiff;
		}


		private double getMinMargin(final IVector2 dest)
		{
			double dist = dest.distanceTo(getPos());
			minMarginChargeValue.updateMinMargin(dist, getWorldFrame().getTimestamp());
			return minMarginChargeValue.getMinMargin();
		}


		private IVector2 getDestination(final double margin)
		{
			return LineMath.stepAlongLine(getBall().getPos(), target.getPos(), -getDistance(margin));
		}


		private double getDistance(final double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + dist2Ball + margin;
		}
	}
}
