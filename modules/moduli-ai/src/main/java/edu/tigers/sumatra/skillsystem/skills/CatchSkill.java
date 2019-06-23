/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 29, 2015
 * Author(s): chris
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;


import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.defense.KeeperRole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.PositionDriver;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryMath;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author chris
 */
public class CatchSkill extends AMoveSkill
{
	
	
	// private static final Logger log = Logger.getLogger(CatchSkill.class.getName());
	@Configurable(comment = "Angle Wheel1")
	private static double	angleWheel1		= Math.PI / 4;
	
	@Configurable(comment = "Angle Wheel2")
	private static double	angleWheel2		= Math.PI / 3;
	
	@Configurable(comment = "Max Iterations for Overacceleration")
	private static int		MAX_ITERATIONS	= 20;
	
	@Configurable(comment = "Stepsize for Overaccelerationcalculation")
	private static double	stepSize			= Geometry.getBotRadius();
	
	@Configurable(comment = "Time to lookahead")
	private static double	timeLookAhead	= 0.5;
	
	private PositionDriver	driver;
	private IVector2			destination		= Geometry.getGoalOur().getGoalCenter();
	
	
	/**
	 * @param skillName
	 */
	public CatchSkill(final ESkill skillName)
	{
		super(skillName);
		driver = new PositionDriver();
		
		setPathDriver(driver);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		TrackedBall ball = getWorldFrame().getBall();
		
		IVector2 leadpoint;
		if (ball.getVel().getLength() != 0)
		{
			leadpoint = GeoMath.leadPointOnLine(getPos(), new Line(ball.getPos(), ball.getVel()));
		} else
		{
			leadpoint = GeoMath.stepAlongLine(getPos(), ball.getPos(), KeeperRole.getDistToGoalCenter());
		}
		
		destination = calcAcceleration(leadpoint);
		destination = checkReachable(destination);
		driver.setDestination(destination);
		if (GeoMath.distancePP(destination, getTBot().getPos()) < (Geometry.getBotRadius() / 2))
		{
			driver.setOrientation(getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle());
		}
		
		
	}
	
	
	private IVector2 checkReachable(final IVector2 destination)
	{
		IVector2 newDestination = destination;
		try
		{
			boolean isKeeperDrivingIntoTheGoal = Math
					.abs(GeoMath.intersectionPoint(getPos(), getPos().subtractNew(destination),
							Geometry.getGoalOur().getGoalCenter(), Geometry.getGoalLineOur().directionVector()).y()) < Math
									.abs(Geometry.getGoalOur().getGoalPostLeft().y());
			boolean isDestinationBetweenGoalPosts = Math.abs(destination.y()) < Math
					.abs(Geometry.getGoalOur().getGoalPostLeft().y());
			if (isKeeperDrivingIntoTheGoal && isDestinationBetweenGoalPosts)
			{
				double stepSize = Geometry.getBotRadius()
						/ AngleMath.sin(GeoMath.angleBetweenVectorAndVector(destination, getWorldFrame().getBall().getPos()));// d/sin(a)
				IVector2 intersection;
				
				intersection = GeoMath.intersectionPoint(destination,
						destination.subtractNew(getWorldFrame().getBall().getPos()), Geometry.getGoalOur().getGoalCenter(),
						Geometry.getGoalOur().getGoalCenter().subtractNew(Geometry.getGoalOur().getGoalPostLeft()));
				
				newDestination = GeoMath.stepAlongLine(intersection, getWorldFrame().getBall().getPos(), stepSize);
				
			}
		} catch (MathException e1)
		{//
			// log.error("", e1);
		}
		
		
		return newDestination;
	}
	
	
	private IVector2 calcAcceleration(final IVector2 destination)
	{
		return BangBangTrajectoryMath.getVirtualDestinationToReachPositionInTime(getPos(), getVel(),
				destination, getBot().getDefaultAcceleration(), getBot().getDefaultAcceleration(),
				getBot().getDefaultVelocity(), getWorldFrame().getBall().getTimeByPos(
						GeoMath.stepAlongLine(destination, getWorldFrame().getBall().getPos(), Geometry.getBotRadius() / 2)));
		
	}
	
	
}
