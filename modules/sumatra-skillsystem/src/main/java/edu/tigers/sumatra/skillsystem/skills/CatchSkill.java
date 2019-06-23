/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;


import java.awt.*;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryMath;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * @author chris
 */
public class CatchSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger	log									= Logger.getLogger(CatchSkill.class.getName());
	
	@Configurable(comment = "The radius to try intercepting the chip-kicked ball within")
	private static double			maxChipInterceptDist				= 500;
	
	@Configurable(comment = "Max. Acceleration in Catch Skill")
	private static double			maxAcc								= 3;
	
	@Configurable(comment = "Prevent defenders from driving into the penalty area")
	private static double			keeperBlockPreventionMargin	= 300;
	
	@Configurable(comment = "Over Acceleration")
	private static boolean			isOverAccelerationActive		= false;
	
	private double						targetAngle							= 0;
	private boolean					penaltyAreaAllowed				= true;
	
	
	/**
	 * Default
	 */
	public CatchSkill()
	{
		super(ESkill.CATCH);
		setInitialState(new CatchState());
	}
	
	
	public void setPenaltyAreaAllowed(final boolean allowed)
	{
		
		penaltyAreaAllowed = allowed;
	}
	
	private class CatchState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			targetAngle = getAngle();
		}
		
		
		@SuppressWarnings("squid:MethodCyclomaticComplexity")
		@Override
		public void doUpdate()
		{
			ITrackedBall ball = getWorldFrame().getBall();
			getMoveCon().getMoveConstraints().setAccMax(maxAcc);
			
			if (ball.getVel().isZeroVector())
			{
				// skill is not designed for lying balls
				return;
			}
			
			IVector2 leadpoint = LineMath.leadPointOnLine(Line.fromDirection(ball.getPos(), ball.getVel()), getPos());
			if (ball.isChipped())
			{
				IVector2 nearestTouchdown = getNearestChipTouchdown();
				if ((nearestTouchdown != null) && (nearestTouchdown.distanceTo(getPos()) < maxChipInterceptDist))
				{
					// Get some distance between touchdown and bot
					double distance = Geometry.getBotRadius() + Geometry.getBallRadius();
					IVector2 direction = nearestTouchdown.subtractNew(getBall().getPos()).scaleTo(distance);
					
					leadpoint = nearestTouchdown.addNew(direction);
				}
			}
			
			IVector2 destination = leadpoint;
			
			if (!Geometry.getField().isPointInShape(destination))
			{
				List<IVector2> intersections = Geometry.getField()
						.lineIntersections(Line.fromPoints(ball.getPos(), leadpoint));
				for (IVector2 intersection : intersections)
				{
					IVector2 vec = intersection.subtractNew(ball.getPos());
					if (Math.abs(vec.getAngle() - ball.getVel().getAngle()) < 0.1)
					{
						destination = intersection;
						break;
					}
				}
			}
			if (isOverAccelerationActive)
			{
				destination = calcAcceleration(destination);
			}
			if (!penaltyAreaAllowed)
			{
				ILine velLine = Line.fromPoints(getPos(), destination);
				if (Geometry.getPenaltyAreaOur().isIntersectingWithLine(velLine))
				{
					IVector2 minVector = getNearestPenaltyAreaIntersection(velLine);
					
					destination = Geometry.getPenaltyAreaOur().withMargin(keeperBlockPreventionMargin)
							.nearestPointOutside(minVector);
				}
			}
			
			if (VectorMath.distancePP(destination, getTBot().getPos()) < (Geometry.getBotRadius() / 2))
			{
				targetAngle = getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			}
			
			setTargetPose(destination, targetAngle, getMoveCon().getMoveConstraints());
			
			getShapes().get(ESkillShapesLayer.PATH).add(new DrawableBot(destination, targetAngle, Color.YELLOW,
					Geometry.getBotRadius(), getBot().getCenter2DribblerDist()));
			
			if (getBot().getCurrentTrajectory().isPresent())
			{
				getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(new DrawableLine(Line.fromDirection(getPos(),
						getBot().getCurrentTrajectory().get().getAcceleration(0).getXYVector().multiplyNew(1000)),
						Color.BLACK));
			}
		}
		
		
		private IVector2 getNearestPenaltyAreaIntersection(final ILine velLine)
		{
			List<IVector2> intersections = Geometry.getPenaltyAreaOur().lineIntersections(velLine);
			double minDist = 0;
			IVector2 minVector = null;
			for (IVector2 vector : intersections)
			{
				double vectorDist = vector.distanceTo(getPos());
				if ((minVector == null) || (vectorDist < minDist))
				{
					minDist = vectorDist;
					minVector = vector;
				}
			}
			return minVector;
		}
		
		
		private IVector2 getNearestChipTouchdown()
		{
			
			ITrackedBall ball = getWorldFrame().getBall();
			List<IVector2> touchdowns = ball.getTrajectory().getTouchdownLocations();
			
			IVector2 nearestPoint = null;
			double min = -1;
			for (IVector2 td : touchdowns)
			{
				double dist = td.distanceTo(getPos());
				
				if ((min < 0) || ((dist < min) && (td.x() > Geometry.getGoalOur().getCenter().x())))
				{
					min = dist;
					nearestPoint = td;
				}
			}
			
			return nearestPoint;
		}
		
		
		private IVector2 calcAcceleration(final IVector2 destination)
		{
			double acc = getMoveCon().getMoveConstraints().getAccMax();
			double vel = getMoveCon().getMoveConstraints().getVelMax();
			return BangBangTrajectoryMath.getVirtualDestinationToReachPositionInTime(getPos(), getVel(),
					destination, acc, vel,
					getWorldFrame().getBall().getTrajectory().getTimeByPos(
							LineMath.stepAlongLine(destination, getWorldFrame().getBall().getPos(),
									Geometry.getBotRadius() / 2)),
					Geometry.getBotRadius());
		}
		
	}
	
}
