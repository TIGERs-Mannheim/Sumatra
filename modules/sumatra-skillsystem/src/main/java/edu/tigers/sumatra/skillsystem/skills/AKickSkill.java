/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.wp.ball.prediction.IChipBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Abstract base for all kick skills
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AKickSkill extends AMoveSkill
{
	protected final DynamicPosition receiver;
	protected EKickMode kickMode = EKickMode.FIXED_SPEED;
	protected EKickerDevice device = EKickerDevice.STRAIGHT;
	protected double kickSpeed = 0;
	protected MinMarginChargeValue minMarginChargeValue;
	private double passEndVel = 3.5;
	private double minPassTime = 0;
	private TargetAngleReachedChecker targetAngleReachedChecker;
	
	private boolean readyForKick = true;
	
	@Configurable(defValue = "true")
	private boolean respectSignForOrientation = true;
	
	@Configurable(defValue = "50", comment = "Bot will look towards ball when nearer than this to ball to ensure that the ball is not pushed away")
	private static double ball2BotOffsetForEarlyTargetOrientation = 50;
	
	@Configurable(comment = "The distance between kicker and ball to keep before kicking the ball", defValue = "15")
	protected static double minDistBeforeKick = 15;
	
	@Configurable(defValue = "false", comment = "Prefer to reach the final target orientation as early as possible (aka. always with few exceptions)")
	private static boolean earlyFinalTargetOrientation = false;
	
	@Configurable(defValue = "0.1", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.1;
	
	@Configurable(defValue = "1.0", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 1.0;
	
	@Configurable(defValue = "false")
	private static boolean adaptOrientationToBotVel = false;
	
	
	AKickSkill(final ESkill skillName, final DynamicPosition receiver)
	{
		super(skillName);
		this.receiver = receiver;
		if (receiver.getTrackedId().isBot())
		{
			setKickMode(EKickMode.PASS);
		} else
		{
			setKickMode(EKickMode.MAX);
		}
		targetAngleReachedChecker = new TargetAngleReachedChecker(roughAngleTolerance, maxTimeTargetAngleReached);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		targetAngleReachedChecker.setRespectSign(respectSignForOrientation);
		receiver.update(getWorldFrame());
		drawShapes();
	}
	
	
	@Override
	protected final void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		KickerParams kickerParams = calcKickerParams();
		updateKickerParams(kickerParams);
		double speed = Math.max(0, Math.min(8, kickerParams.getSpeed()));
		kickerDribblerOutput.setKick(speed, kickerParams.getDevice(), kickerParams.getMode());
		kickerDribblerOutput.setDribblerSpeed(kickerParams.getDribbleSpeed());
	}
	
	
	private double calcKickSpeed()
	{
		return Math.max(0, Math.min(8, calcKickerParams().getSpeed()));
	}
	
	
	protected double getMinMargin(final IVector2 dest)
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
	
	
	protected boolean isReadyAndFocussed()
	{
		return isReadyForKick() && isOrientationReached();
	}
	
	
	private boolean isOrientationReached()
	{
		double targetOrientation = receiver.subtractNew(getBall().getPos()).getAngle(getAngle());
		targetAngleReachedChecker.update(targetOrientation, getAngle(), getWorldFrame().getTimestamp());
		return targetAngleReachedChecker.isReached();
	}
	
	
	protected boolean isReadyForKick()
	{
		return readyForKick;
	}
	
	
	public void setReadyForKick(final boolean readyForKick)
	{
		this.readyForKick = readyForKick;
	}
	
	
	protected double getTargetOrientation()
	{
		double finalTargetOrientation = receiver.subtractNew(getBall().getPos()).getAngle(0);
		
		if (adaptOrientationToBotVel)
		{
			IVector2 kickVector = Vector2.fromAngle(finalTargetOrientation).scaleTo(calcKickSpeed());
			double angleDiff = kickVector.addNew(getVel()).angleTo(kickVector).orElse(0.0);
			finalTargetOrientation += angleDiff;
		}
		
		double currentDirection = getBall().getPos().subtractNew(getPos()).getAngle(0);
		double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
		double alteredDiff = Math.signum(diff) * Math.max(0, Math.abs(diff) - 0.3);
		
		double ball2BotDist = getBall().getPos().distanceTo(getPos());
		
		double relDist = 0;
		if (earlyFinalTargetOrientation)
		{
			relDist = SumatraMath.relative(ball2BotDist,
					getTBot().getCenter2DribblerDist() + Geometry.getBallRadius(),
					Geometry.getBotRadius() + Geometry.getBallRadius() + ball2BotOffsetForEarlyTargetOrientation);
		}
		
		return finalTargetOrientation - ((1 - relDist) * alteredDiff);
	}
	
	
	private void drawShapes()
	{
		getShapes().get(ESkillShapesLayer.KICK_SKILL)
				.add(new DrawableLine(Line.fromPoints(getBall().getPos(), receiver),
						getBotId().getTeamColor().getColor()));
		getShapes().get(ESkillShapesLayer.KICK_SKILL)
				.add(new DrawableLine(Line.fromDirection(getPos(), Vector2.fromAngle(getAngle()).scaleTo(5000)),
						Color.black));
	}
	
	
	private KickerParams calcKickerParams()
	{
		KickerParams kickerParams = new KickerParams();
		kickerParams.setDevice(device);
		kickerParams.setMode(EKickerMode.ARM);
		
		double kickLength = receiver.subtractNew(getWorldFrame().getBall().getPos()).getLength2();
		
		if ((device == EKickerDevice.STRAIGHT)
				&& (getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.WORKING))
		{
			// kicker is broken, lets pass with chip
			kickerParams.setDevice(EKickerDevice.CHIP);
			kickMode = EKickMode.PASS;
		} else if ((device == EKickerDevice.CHIP)
				&& (getBot().getBotFeatures().get(EFeature.CHIP_KICKER) != EFeatureState.WORKING))
		{
			// good luck
			kickerParams.setDevice(EKickerDevice.STRAIGHT);
		}
		
		switch (kickerParams.getDevice())
		{
			case CHIP:
				calcChipParams(kickLength, kickerParams);
				break;
			case STRAIGHT:
				calcStraightParams(kickLength, kickerParams);
				break;
			default:
				throw new IllegalStateException();
		}
		
		return kickerParams;
	}
	
	
	private void calcStraightParams(final double length, final KickerParams kickerParams)
	{
		switch (kickMode)
		{
			case MAX:
				kickerParams.setSpeed(8);
				break;
			case PASS:
				kickerParams.setSpeed(
						SkillUtil.passKickSpeed(getBall().getStraightConsultant(), length, passEndVel, minPassTime));
				break;
			case POINT:
			case STOP:
				kickerParams.setSpeed(getBall().getStraightConsultant().getInitVelForDist(length, 0));
				break;
			case FIXED_SPEED:
				kickerParams.setSpeed(kickSpeed);
				break;
			default:
				throw new IllegalStateException();
		}
		
		adaptStraightKickSpeedToBallVel(kickerParams);
	}
	
	
	private void adaptStraightKickSpeedToBallVel(final KickerParams kickerParams)
	{
		IVector2 targetVel = receiver.subtractNew(getTBot().getBotKickerPos()).scaleTo(kickerParams.getSpeed());
		IVector2 ballVel = getWorldFrame().getBall().getVel();
		
		IVector2 vel = targetVel.subtractNew(ballVel);
		kickerParams.setSpeed(vel.getLength2());
	}
	
	
	private void calcChipParams(final double kickLength, final KickerParams kickerParams)
	{
		IChipBallConsultant consultant = BallFactory.createChipConsultant();
		switch (kickMode)
		{
			case PASS:
			case STOP:
				kickerParams.setSpeed(consultant.getInitVelForDistAtTouchdown(kickLength, 4));
				break;
			case MAX:
				kickerParams.setSpeed(8);
				break;
			case FIXED_SPEED:
				kickerParams.setSpeed(kickSpeed);
				break;
			case POINT:
				kickerParams.setSpeed(consultant.getInitVelForDistAtTouchdown(kickLength, 0));
				break;
			default:
				throw new IllegalStateException();
		}
	}
	
	
	protected void updateKickerParams(final KickerParams kickerParams)
	{
		// no default implementation
	}
	
	
	/**
	 * @param device the device to set
	 */
	public final void setDevice(final EKickerDevice device)
	{
		this.device = device;
	}
	
	
	/**
	 * Update the receiver target
	 *
	 * @param recv
	 */
	public final void setReceiver(final DynamicPosition recv)
	{
		receiver.update(recv);
	}
	
	
	/**
	 * @return the receiver
	 */
	protected final IVector2 getReceiver()
	{
		return receiver;
	}
	
	
	/**
	 * @param kickMode the kickMode to set
	 */
	public final void setKickMode(final EKickMode kickMode)
	{
		this.kickMode = kickMode;
	}
	
	
	/**
	 * @return the kickSpeed
	 */
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * @param kickSpeed the kickSpeed to set
	 */
	public void setKickSpeed(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
		kickMode = EKickMode.FIXED_SPEED;
	}
	
	
	public void setPassEndVel(final double passEndVel)
	{
		this.passEndVel = passEndVel;
	}
	
	
	public void setMinPassTime(final double minPassTime)
	{
		this.minPassTime = minPassTime;
	}
	
	/**
	 * Mode for determining the kick speed
	 */
	public enum EKickMode
	{
		/** Calculate kickSpeed for passing to receiver */
		PASS,
		/** Full speed */
		MAX,
		/** kick such that ball will come to a stop at receiver */
		POINT,
		STOP,
		/** use given kickSpeed */
		FIXED_SPEED,
	}
}
