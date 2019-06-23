/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
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
 * Kick with a single touch (taking care to not double touching the ball)
 */
public class SingleTouchKickSkill extends ATouchKickSkill
{
	@Configurable(comment = "Speed in chill mode")
	private static double chillVel = 1;
	
	@Configurable(comment = "Acceleration in chill model")
	private static double chillAcc = 2;
	
	@Configurable(comment = "The distance between kicker and ball to keep before kicking the ball", defValue = "15")
	private static double minDistBeforeKick = 15;
	
	@Configurable
	private static double maxAroundBallMargin = 120;
	
	@Configurable(defValue = "0.1", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.1;
	
	@Configurable(defValue = "1.0", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 1.0;
	
	private final TargetAngleReachedChecker targetAngleReachedChecker;
	private final PositionValidator positionValidator = new PositionValidator();
	private final BallStabilizer ballStabilizer = new BallStabilizer();
	
	private boolean readyForKick = true;
	
	
	/**
	 * UI constructor
	 *
	 * @param target
	 * @param device
	 * @param kickSpeed
	 */
	@SuppressWarnings("unused") // used by UI
	public SingleTouchKickSkill(
			final DynamicPosition target,
			final EKickerDevice device,
			final double kickSpeed)
	{
		this(target, KickParams.of(device, kickSpeed));
	}
	
	
	public SingleTouchKickSkill(final DynamicPosition target, final KickParams kickParams)
	{
		super(ESkill.SINGLE_TOUCH_KICK, target, kickParams);
		targetAngleReachedChecker = new TargetAngleReachedChecker(roughAngleTolerance, maxTimeTargetAngleReached);
		setInitialState(new KickState());
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		ballStabilizer.update(getBall(), getTBot());
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawablePoint(getBallPos(), Color.green));
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		
		if (!isReadyAndFocussed())
		{
			kickerDribblerOutput.setKick(kickParams.getKickSpeed(), kickParams.getDevice(), EKickerMode.DISARM);
		} else
		{
			kickerDribblerOutput.setKick(kickParams.getKickSpeed(), kickParams.getDevice(), EKickerMode.ARM);
		}
	}
	
	
	private IVector2 getBallPos()
	{
		return ballStabilizer.getBallPos();
	}
	
	
	private boolean isReadyAndFocussed()
	{
		double targetOrientation = target.subtractNew(getBallPos()).getAngle(getAngle());
		return isReadyForKick()
				&& isOrientationReached(targetOrientation);
	}
	
	
	private boolean isOrientationReached(double targetOrientation)
	{
		targetAngleReachedChecker.update(targetOrientation, getOrientationFromFilter(), getWorldFrame().getTimestamp());
		return targetAngleReachedChecker.isReached();
	}
	
	
	private double getOrientationFromFilter()
	{
		return getTBot().getFilteredState().map(State::getOrientation).orElseGet(this::getAngle);
	}
	
	
	private boolean isReadyForKick()
	{
		return readyForKick;
	}
	
	
	public void setReadyForKick(final boolean readyForKick)
	{
		this.readyForKick = readyForKick;
	}
	
	
	private class KickState extends MoveToState
	{
		private double dist2Ball = 0;
		protected MinMarginChargeValue minMarginChargeValue;
		
		
		private KickState()
		{
			super(SingleTouchKickSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			dist2Ball = 20;
			getMoveCon().getMoveConstraints().setVelMax(chillVel);
			getMoveCon().setBallObstacle(false);
			
			minMarginChargeValue = MinMarginChargeValue.aMinMargin()
					.withDefaultValue(10)
					.withChargeRate(-50)
					.withLowerThreshold(50)
					.withUpperThreshold(70)
					.withLimit(-100)
					.build();
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getVel().getLength2() <= getMoveCon().getMoveConstraints().getVelMax())
			{
				getMoveCon().getMoveConstraints().setAccMax(chillAcc);
			}
			
			IVector2 dest = AroundBallCalc
					.aroundBall()
					.withBallPos(getBallPos())
					.withTBot(getTBot())
					.withDestination(getDestination(0))
					.withMaxMargin(maxAroundBallMargin)
					.withMinMargin(dist2Ball)
					.build()
					.getAroundBallDest();
			dist2Ball = getMinMargin(dest);
			
			positionValidator.update(getWorldFrame(), getMoveCon(), getTBot());
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			
			getMoveCon().updateDestination(dest);
			double targetOrientation = getTargetOrientation();
			getMoveCon().updateTargetAngle(targetOrientation);
			super.doUpdate();
			
			getShapes().get(ESkillShapesLayer.KICK_SKILL)
					.add(new DrawableLine(Line.fromPoints(getBall().getPos(), target),
							getBotId().getTeamColor().getColor()));
			getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableLine(
					Line.fromDirection(dest, Vector2.fromAngle(targetOrientation).scaleTo(5000)), Color.RED));
		}
		
		
		protected double getTargetOrientation()
		{
			double finalTargetOrientation = target.subtractNew(getBallPos()).getAngle(0);
			
			double currentDirection = getBallPos().subtractNew(getPos()).getAngle(0);
			double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
			double alteredDiff = Math.signum(diff) * Math.max(0, Math.abs(diff) - 0.4);
			
			return finalTargetOrientation - alteredDiff;
		}
		
		
		private double getMinMargin(final IVector2 dest)
		{
			if (isReadyForKick())
			{
				double dist = dest.distanceTo(getPos());
				minMarginChargeValue.updateMinMargin(dist, getWorldFrame().getTimestamp());
				return minMarginChargeValue.getMinMargin();
			}
			minMarginChargeValue.reset();
			return minDistBeforeKick;
		}
		
		
		private IVector2 getDestination(double margin)
		{
			return LineMath.stepAlongLine(getBallPos(), target, -getDistance(margin));
		}
		
		
		private double getDistance(double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + dist2Ball + margin;
		}
	}
}
