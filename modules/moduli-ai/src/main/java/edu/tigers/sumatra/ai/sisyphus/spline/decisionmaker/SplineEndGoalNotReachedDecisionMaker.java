/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.spline.decisionmaker;

import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.spline.EDecision;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * checks if the bot has reached the end of the spline but the target is not reached
 * Tigerv2 bot should automtically drive to the target, for the other bots a new spline is needed
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class SplineEndGoalNotReachedDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	
	private static final Logger	log				= Logger
																		.getLogger(SplineEndGoalNotReachedDecisionMaker.class.getName());
																		
	@Configurable(comment = "Dist [mm] - max allowed distance to destination when pos is considered reached.")
	private static double			positionTol		= 40;
																
	@Configurable(comment = "Dist [rad] - max allowed distance to target orientation when orientation is considered reached.")
	private static double			orientationTol	= 0.17;
																
																
	static
	{
		ConfigRegistration.registerClass("sisyphus", SplineEndGoalNotReachedDecisionMaker.class);
	}
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ITrajectory<IVector3> oldSpline,
			final ITrajectory<IVector3> newSpline,
			final List<IDrawableShape> shapes, final double curTime)
	{
		final SimpleWorldFrame wFrame = localPathFinderInput.getFieldInfo().getwFrame();
		final ITrackedBot bot = wFrame.getBot(localPathFinderInput.getBotId());
		final MovementCon moveCon = localPathFinderInput.getMoveCon();
		final IVector2 destination = moveCon.getDestination();
		final double targetOrientation = moveCon.getTargetAngle();
		double curTimeOnSpline = curTime;
		
		boolean isGoalReached = destination.equals(bot.getPos(), positionTol);
		boolean isTargetAngleReached = (Math
				.abs(AngleMath.getShortestRotation(targetOrientation, bot.getAngle())) < orientationTol);
		boolean isSplineCompleted = curTimeOnSpline > oldSpline.getTotalTime();
		boolean isTargetInPenArea = Geometry.getPenaltyAreaOur()
				.isPointInShape(destination, Geometry.getBotRadius());
				
		if (isSplineCompleted && (!isGoalReached || !isTargetAngleReached) && !isTargetInPenArea)
		{
			log.trace("New Spline - Spline end but goal is not reached yet: Goal not reached: "
					+ oldSpline.getPositionMM(oldSpline.getTotalTime()).getXYVector()
					+ " != " + bot.getPos());
			return EDecision.ENFORCE;
		}
		return EDecision.NO_VIOLATION;
	}
}
