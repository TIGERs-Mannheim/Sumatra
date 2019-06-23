/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.redirect.ARedirectBallConsultant;
import edu.tigers.sumatra.skillsystem.skills.util.redirect.RedirectBallConsultantFactory;
import edu.tigers.sumatra.wp.data.DynamicPosition;

import java.awt.Color;


/**
 * Prepare for redirect
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectSkill extends AReceiveSkill
{
	private static final double MAX_KICK_SPEED = 8.0;
	private DynamicPosition target;
	private ARedirectBallConsultant consultant;
	
	private double passReceivingSpeed;
	private Double fixedKickSpeed = null;
	
	private AKickSkill.EKickMode kickMode = AKickSkill.EKickMode.MAX;
	
	
	/**
	 * @param target the target to redirect
	 */
	public RedirectSkill(final DynamicPosition target)
	{
		this(target, 3);
	}
	
	
	/**
	 * @param target the target to redirect
	 * @param passReceivingSpeed pass receiving speed
	 */
	public RedirectSkill(final DynamicPosition target, final double passReceivingSpeed)
	{
		super(ESkill.REDIRECT);
		this.target = target;
		this.passReceivingSpeed = passReceivingSpeed;
		setInitialState(new RedirectState());
	}
	
	
	private void updateConsultant()
	{
		Double desiredBallSpeed = getRequiredBallSpeed(getPos(), target);
		
		IVector2 ballVelAtCollision = getBall().getTrajectory()
				.getVelByTime(getBall().getTrajectory().getTimeByPos(getTBot().getBotKickerPos()));
		if (ballVelAtCollision.isZeroVector() || getBall().getVel().isZeroVector())
		{
			ballVelAtCollision = getPos().subtractNew(getBall().getPos()).scaleTo(passReceivingSpeed);
		}
		
		double redirectAngle = target.subtractNew(getPos()).angleTo(ballVelAtCollision).orElse(0.);
		
		consultant = new RedirectBallConsultantFactory()
				.setBallRedirectAngle(redirectAngle)
				.setBallVelAtCollision(ballVelAtCollision)
				.setDesiredVelocity(desiredBallSpeed)
				.create();
	}
	
	
	/**
	 * Calculate the required ball speed that is needed, if the ball should be received with a given speed
	 *
	 * @param from kick position
	 * @param to destination
	 * @return required ball speed
	 */
	private double getRequiredBallSpeed(final IVector2 from, final DynamicPosition to)
	{
		if (!to.getTrackedId().isBot() && kickMode != AKickSkill.EKickMode.PASS)
		{
			return MAX_KICK_SPEED;
		}
		
		double desiredBallSpeed;
		double dist = VectorMath.distancePP(from, to);
		desiredBallSpeed = getBall().getStraightConsultant().getInitVelForDist(dist, passReceivingSpeed);
		
		return desiredBallSpeed;
	}
	
	
	@Override
	protected double calcTargetOrientation(final IVector2 kickerPos)
	{
		updateConsultant();
		if (consultant != null)
		{
			return consultant.getBotTargetAngle();
		}
		
		return getBall().getPos().subtractNew(getPos()).getAngle();
	}
	
	
	/**
	 * @return the target
	 */
	public final DynamicPosition getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param target the target to set
	 */
	public final void setTarget(final DynamicPosition target)
	{
		this.target = target;
	}
	
	
	public void setPassReceivingSpeed(final double passReceivingSpeed)
	{
		this.passReceivingSpeed = passReceivingSpeed;
	}
	
	
	public void setFixedKickSpeed(final Double fixedKickSpeed)
	{
		this.fixedKickSpeed = fixedKickSpeed;
	}
	
	private class RedirectState extends AReceiveState
	{
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			target.update(getWorldFrame());
			
		}
		
		
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			updateConsultant();
			// arm kicker if target angle reached
			if (getBall().getVel().getLength2() > 0.1 &&
					Math.abs(AngleMath.difference(getLastTargetOrientation(), getAngle())) < 0.2)
			{
				setKickSpeed(calcKickSpeed());
			} else
			{
				setKickSpeed(0);
			}
			
			// bot to target
			getShapes().get(ESkillShapesLayer.RECEIVER_SKILL)
					.add(new DrawableLine(Line.fromPoints(getTBot().getBotKickerPos(), target),
							getBotId().getTeamColor().getColor()));
			
			// current angle
			getShapes().get(ESkillShapesLayer.RECEIVER_SKILL)
					.add(new DrawableLine(Line.fromDirection(getPos(), Vector2.fromAngle(getAngle()).scaleTo(200)),
							Color.black));
			
			// desired angle
			getShapes().get(ESkillShapesLayer.RECEIVER_SKILL)
					.add(new DrawableLine(
							Line.fromDirection(getPos(), Vector2.fromAngle(getLastTargetOrientation()).scaleTo(200)),
							Color.magenta));
			
			double bot2Target = Line.fromPoints(getTBot().getBotKickerPos(), target).getAngle().orElse(0.0);
			getShapes().get(ESkillShapesLayer.RECEIVER_SKILL)
					.add(new DrawableAnnotation(getPos(),
							String.format("angle diff: %.3f", getLastTargetOrientation() - bot2Target)));
		}
		
		
		private double calcKickSpeed()
		{
			double kickSpeed;
			if (fixedKickSpeed != null)
			{
				kickSpeed = fixedKickSpeed;
			} else if (consultant != null)
			{
				kickSpeed = consultant.getRedirectKickSpeed();
			} else
			{
				// Just for redirect preparation
				kickSpeed = 6.;
			}
			return Math.max(0, Math.min(8, kickSpeed));
		}
	}
	
	
	public void setKickMode(final AKickSkill.EKickMode kickMode)
	{
		this.kickMode = kickMode;
	}
	
}
