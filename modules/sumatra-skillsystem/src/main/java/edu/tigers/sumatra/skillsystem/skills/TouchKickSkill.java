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
import edu.tigers.sumatra.drawable.DrawableAnnotation;
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
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		ballStabilizer.update(getBall(), getTBot());
		targetAngleReachedChecker.setOuterAngleDiffTolerance(roughAngleTolerance + target.getPassRange() / 2);
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawablePoint(getBallPos(), Color.green));
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		
		double kickSpeed = adaptKickSpeed(kickParams.getKickSpeed());
		if (!isFocussed())
		{
			kickerDribblerOutput.setKick(kickSpeed, kickParams.getDevice(), EKickerMode.DISARM);
		} else
		{
			kickerDribblerOutput.setKick(kickSpeed, kickParams.getDevice(), EKickerMode.ARM);
		}
		
		kickerDribblerOutput.setDribblerSpeed(dribbleSpeed);
	}
	
	
	private double adaptKickSpeed(final double kickSpeed)
	{
		IVector2 targetVel = target.subtractNew(getTBot().getBotKickerPos()).scaleTo(kickSpeed);
		double adaptedKickSpeed = targetVel.subtractNew(getBall().getVel()).getLength2();
		return KickParams.limitKickSpeed(adaptedKickSpeed);
	}
	
	
	private IVector2 getBallPos()
	{
		return ballStabilizer.getBallPos();
	}
	
	
	private boolean isFocussed()
	{
		double targetOrientation = target.subtractNew(getBall().getPos()).getAngle(getAngle());
		return isOrientationReached(targetOrientation);
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
			return getBallPos().distanceTo(getTBot().getBotKickerPos()) < Geometry.getBallRadius() + 10;
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
			
			positionValidator.update(getWorldFrame(), getMoveCon(), getTBot());
			dest = positionValidator.movePosInsideField(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			
			getMoveCon().updateDestination(dest);
			final double targetOrientation = getTargetOrientation();
			getMoveCon().updateTargetAngle(targetOrientation);
			
			super.doUpdate();
			
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(getPos(),
					String.format("%.2f", dist2Ball)).withOffset(Vector2.fromY(300)));
			getShapes().get(ESkillShapesLayer.KICK_SKILL)
					.add(new DrawableLine(Line.fromPoints(getBall().getPos(), target),
							getBotId().getTeamColor().getColor()));
		}
		
		
		private IVector2 getBallPosByTime(double lookahead)
		{
			return ballStabilizer.getBallPos(lookahead);
		}
		
		
		private double getTargetOrientation()
		{
			double finalTargetOrientation = target.subtractNew(getBall().getPos()).getAngle(0);
			
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
		
		
		private IVector2 getDestination(double margin)
		{
			return LineMath.stepAlongLine(getBall().getPos(), target, -getDistance(margin));
		}
		
		
		private double getDistance(double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + dist2Ball + margin;
		}
	}
}
