/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.paramoptimizer.redirect.RedirectParamCalc;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Prepare for redirect
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectSkill extends AReceiveSkill
{
	private DynamicPosition	target;
	@Configurable(comment = "Additional time [s] to wait used as safety for the redirect")
	private static double	safetyTime	= 0.1;
	
	private EKickerDevice	device		= EKickerDevice.STRAIGHT;
	
	
	/**
	 * @param target
	 */
	public RedirectSkill(final DynamicPosition target)
	{
		super(ESkill.REDIRECT);
		this.target = target;
	}
	
	
	@Override
	protected IVector3 getPose()
	{
		target.update(getWorldFrame());
		final double angle;
		if (target.equals(getPos(), 1))
		{
			angle = getAngle();
		} else
		{
			angle = target.subtractNew(getPos()).getAngle();
		}
		
		List<IDrawableShape> shapes = new ArrayList<>(3);
		shapes.add(new DrawableLine(Line.newLine(getTBot().getBotKickerPos(), target), Color.blue));
		shapes.add(new DrawableLine(new Line(getPos(), new Vector2(getAngle()).scaleTo(200)),
				Color.black));
		
		double center2DribblerDist = getTBot().getCenter2DribblerDist();
		double center2BallHit = center2DribblerDist + Geometry.getBallRadius();
		IVector2 ballVel = GeoMath.getBotKickerPos(getPos(), getAngle(), center2BallHit)
				.subtractNew(getWorldFrame().getBall().getPos()).scaleTo(2);
		
		IVector3 pose = new Vector3(getDesiredDestination(), angle);
		
		// do redirect positioning
		RedirectParamCalc rpc = RedirectParamCalc.forBot(getBot());
		double kickSpeed = rpc.getKickSpeed(getWorldFrame(), getBotId(), target,
				getWorldFrame().getBall().getVel().getLength());
		kickSpeed = 8;
		
		pose = rpc.calcRedirectPose(getTBot(), getPos(), angle,
				getWorldFrame().getBall(),
				target,
				kickSpeed);
		
		if (Math.abs(AngleMath.difference(pose.z(), getAngle())) < 0.2)
		{
			getMatchCtrl().setKick(kickSpeed, device, EKickerMode.ARM);
			getMatchCtrl().setDribblerSpeed(3000);
		} else
		{
			getMatchCtrl().setKick(kickSpeed, device, EKickerMode.DISARM);
		}
		
		double requiredBallSpeed = rpc.getRequiredBallSpeed(getTBot().getBotKickerPos(), target,
				OffensiveConstants.getDefaultPassEndVel());
		IVector2 newKickerPos = pose.getXYVector()
				.addNew(new Vector2(pose.z()).scaleTo(getTBot().getCenter2DribblerDist()));
		IVector2 shootVector = target.subtractNew(newKickerPos).scaleTo(requiredBallSpeed);
		RedirectParamCalc.forBot(getBot()).setShootVector(getBotId(), shootVector);
		
		shapes.add(new DrawableLine(new Line(getWorldFrame().getBall().getPos(), ballVel.scaleToNew(2000)),
				Color.magenta));
		shapes.add(new DrawableLine(new Line(getPos(), new Vector2(pose.z()).scaleTo(200)),
				Color.red));
		getPathDriver().setShapes(EShapesLayer.REDIRECT_SKILL, shapes);
		
		return pose;
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
	
	
	/**
	 * @return the device
	 */
	public EKickerDevice getDevice()
	{
		return device;
	}
	
	
	/**
	 * @param device the device to set
	 */
	public void setDevice(final EKickerDevice device)
	{
		this.device = device;
	}
}
