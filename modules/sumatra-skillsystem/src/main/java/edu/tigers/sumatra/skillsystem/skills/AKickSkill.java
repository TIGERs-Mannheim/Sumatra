/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.BotVelDirectedToTargetChecker;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Abstract base for all kick skills
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AKickSkill extends AMoveSkill
{
	protected final DynamicPosition target;
	protected final KickParams kickParams;
	
	protected MinMarginChargeValue minMarginChargeValue;
	private TargetAngleReachedChecker targetAngleReachedChecker;
	private final BotVelDirectedToTargetChecker botVelDirectedToTargetChecker = new BotVelDirectedToTargetChecker();
	
	private boolean readyForKick = true;
	
	@Configurable(defValue = "true")
	private static boolean respectSignForOrientation = true;
	
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
	
	
	/**
	 * UI constructor
	 * 
	 * @param skillName
	 * @param target
	 * @param device
	 * @param kickSpeed
	 */
	AKickSkill(final ESkill skillName, final DynamicPosition target, final EKickerDevice device, final double kickSpeed)
	{
		this(skillName, target, KickParams.of(device, kickSpeed));
	}
	
	
	AKickSkill(final ESkill skillName, final DynamicPosition target, final KickParams kickParams)
	{
		super(skillName);
		this.target = target;
		this.kickParams = kickParams;
		targetAngleReachedChecker = new TargetAngleReachedChecker(roughAngleTolerance, maxTimeTargetAngleReached);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		targetAngleReachedChecker.setRespectSign(respectSignForOrientation);
		targetAngleReachedChecker.setOuterAngleDiffTolerance(roughAngleTolerance + target.getPassRange() / 2);
		target.update(getWorldFrame());
		drawShapes();
	}
	
	
	protected double adaptKickSpeed(final double kickSpeed)
	{
		IVector2 targetVel = target.subtractNew(getTBot().getBotKickerPos()).scaleTo(kickSpeed);
		double adaptedKickSpeed = targetVel.subtractNew(getBall().getVel()).getLength2();
		return Math.max(0, Math.min(RuleConstraints.getMaxBallSpeed(), adaptedKickSpeed));
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
		double targetOrientation = target.subtractNew(getBall().getPos()).getAngle(getAngle());
		return isReadyForKick()
				&& isOrientationReached(targetOrientation)
				&& botVelDirectedToTargetChecker.check(targetOrientation, getVel());
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
	
	
	protected double getTargetOrientation()
	{
		double finalTargetOrientation = target.subtractNew(getBall().getPos()).getAngle(0);
		
		if (adaptOrientationToBotVel)
		{
			IVector2 kickVector = Vector2.fromAngle(finalTargetOrientation).scaleTo(kickParams.getKickSpeed());
			double angleDiff = kickVector.addNew(getVel()).angleTo(kickVector).orElse(0.0);
			finalTargetOrientation += angleDiff;
		}
		
		double currentDirection = getBall().getPos().subtractNew(getPos()).getAngle(0);
		double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
		double alteredDiff = Math.signum(diff) * Math.max(0, Math.abs(diff) - 0.4);
		
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
				.add(new DrawableLine(Line.fromPoints(getBall().getPos(), target),
						getBotId().getTeamColor().getColor()));
		getShapes().get(ESkillShapesLayer.KICK_SKILL)
				.add(new DrawableLine(
						Line.fromDirection(getPos(), Vector2.fromAngle(getOrientationFromFilter()).scaleTo(5000)),
						Color.black));
	}
	
	
	/**
	 * @param device the device to set
	 */
	public final void setDevice(final EKickerDevice device)
	{
		kickParams.setDevice(device);
	}
	
	
	/**
	 * Update the receiver target
	 *
	 * @param recv
	 */
	public final void setTarget(final DynamicPosition recv)
	{
		target.update(recv);
	}
	
	
	/**
	 * @return the receiver
	 */
	protected final IVector2 getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param kickSpeed the kickSpeed to set
	 */
	public void setKickSpeed(final double kickSpeed)
	{
		kickParams.setKickSpeed(kickSpeed);
	}
}
