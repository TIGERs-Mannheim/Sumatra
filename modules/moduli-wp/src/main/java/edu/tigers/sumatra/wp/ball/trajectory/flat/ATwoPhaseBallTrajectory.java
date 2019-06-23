/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory.flat;

import static edu.tigers.sumatra.wp.data.BallTrajectoryState.aBallState;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * @author AndreR <andre@ryll.cc>
 */
public abstract class ATwoPhaseBallTrajectory extends ABallTrajectory
{
	protected final double		tSwitch;
	protected final IVector3	posSwitch;
	protected final IVector3	velSwitch;
	protected final double		accSlide;
	protected final double		accRoll;
	
	
	protected ATwoPhaseBallTrajectory(final IVector3 kickPos, final IVector3 kickVel, final double tSwitch,
			final double accSlide, final double accRoll, final double tKickToNow)
	{
		super(kickPos, kickVel, tKickToNow);
		
		this.tSwitch = tSwitch;
		this.accSlide = accSlide;
		this.accRoll = accRoll;
		
		IVector3 acc = kickVel.normalizeNew().multiply(accSlide);
		posSwitch = kickPos.addNew(kickVel.multiplyNew(tSwitch))
				.add(acc.multiplyNew(0.5 * tSwitch * tSwitch));
		velSwitch = kickVel.addNew(acc.multiplyNew(tSwitch));
	}
	
	
	@Override
	public BallTrajectoryState getMilliStateAtTime(final double time)
	{
		if (time < 0)
		{
			return aBallState().withPos(kickPos).withVel(kickVel)
					.withVSwitchToRoll(kickVel.getLength2()).withChipped(false).build();
		}
		
		if (time < tSwitch)
		{
			IVector3 accNow = kickVel.normalizeNew().multiply(accSlide);
			IVector3 posNow = kickPos.addNew(kickVel.multiplyNew(time)).add(accNow.multiplyNew(0.5 * time * time));
			IVector3 velNow = kickVel.addNew(accNow.multiplyNew(time));
			
			return aBallState().withPos(posNow).withVel(velNow).withAcc(accNow)
					.withVSwitchToRoll(velSwitch.getLength2()).withChipped(false).build();
		}
		
		double t2 = time - tSwitch;
		if (time > getTimeAtRest())
		{
			t2 = getTimeAtRest() - tSwitch;
		}
		
		IVector3 acc = kickVel.normalizeNew().multiply(accRoll);
		IVector3 posNow = posSwitch.addNew(velSwitch.multiplyNew(t2)).add(acc.multiplyNew(0.5 * t2 * t2));
		IVector3 velNow = velSwitch.addNew(acc.multiplyNew(t2));
		
		return aBallState().withPos(posNow).withVel(velNow).withAcc(acc)
				.withVSwitchToRoll(velSwitch.getLength2()).withChipped(false).build();
	}
	
	
	@Override
	public PlanarCurve getPlanarCurve()
	{
		List<PlanarCurveSegment> segments = new ArrayList<>();
		BallTrajectoryState state = getMilliStateAtTime(tKickToNow);
		
		double tRest = getTimeAtRest();
		
		if (tKickToNow > tRest)
		{
			segments.add(PlanarCurveSegment.fromPoint(state.getPos().getXYVector(), 0, 1.0));
			return new PlanarCurve(segments);
		}
		
		if (tKickToNow < tSwitch)
		{
			// add sliding phase
			PlanarCurveSegment slide = PlanarCurveSegment.fromSecondOrder(state.getPos().getXYVector(),
					state.getVel().getXYVector(),
					state.getAcc().getXYVector(),
					0, tSwitch - tKickToNow);
			segments.add(slide);
		}
		
		PlanarCurveSegment roll = PlanarCurveSegment.fromSecondOrder(posSwitch.getXYVector(),
				velSwitch.getXYVector(),
				kickVel.getXYVector().normalizeNew().multiply(accRoll),
				tSwitch - tKickToNow, tRest - tKickToNow);
		segments.add(roll);
		
		return new PlanarCurve(segments);
	}
	
	
	@Override
	public double getTimeAtRest()
	{
		double tStop = -velSwitch.getLength2() / accRoll;
		return tSwitch + tStop;
	}
	
	
	@Override
	protected double getTimeByDistanceInMillimeters(final double distance)
	{
		double distToSwitch = ((kickVel.getLength2() + velSwitch.getLength2()) / 2.0) * tSwitch;
		
		if (distance < distToSwitch)
		{
			// queried distance is in sliding phase
			double v = kickVel.getLength2();
			double a = accSlide;
			return (Math.sqrt((v * v) + (2.0 * a * distance)) - v) / a;
		}
		
		double v = velSwitch.getLength2();
		double a = accRoll;
		double tRoll = -v / a;
		double distRoll = (v / 2.0) * tRoll;
		
		if (distance > (distToSwitch + distRoll))
		{
			// queried distance is beyond total distance
			return Double.POSITIVE_INFINITY;
		}
		
		// distance is in rolling phase
		double p = distance - distToSwitch;
		double timeToDist = ((Math.sqrt((v * v) + (2.0 * a * p) + 1e-6) - v) / a) + 1e-6;
		if (timeToDist < 1e-3)
		{
			timeToDist = 0.0; // numerical issues...
		}
		Validate.isTrue(timeToDist >= 0, String.valueOf(timeToDist));
		
		return tSwitch + timeToDist;
	}
	
	
	@Override
	protected double getTimeByVelocityInMillimetersPerSec(final double velocity)
	{
		if (velocity > kickVel.getLength2())
		{
			return 0;
		}
		
		if (velocity > velSwitch.getLength2())
		{
			// requested velocity is during sliding phase
			double tToVel = -(kickVel.getLength2() - velocity) / accSlide;
			Validate.isTrue(tToVel >= 0);
			
			return tToVel;
		}
		
		// requested velocity is during rolling phase
		double tToVel = -(velSwitch.getLength2() - velocity) / accRoll;
		Validate.isTrue(tToVel >= 0);
		
		return tSwitch + tToVel;
	}
}
