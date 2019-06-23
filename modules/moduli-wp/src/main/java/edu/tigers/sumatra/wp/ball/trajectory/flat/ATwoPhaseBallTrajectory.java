/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory.flat;

import static edu.tigers.sumatra.wp.data.BallTrajectoryState.aBallState;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * @author AndreR <andre@ryll.cc>
 */
public abstract class ATwoPhaseBallTrajectory extends ABallTrajectory
{
	private final double tSwitch;
	private final IVector2 posSwitch;
	private final IVector2 velSwitch;
	private final double accSlide;
	private final double accRoll;
	
	
	protected ATwoPhaseBallTrajectory(final IVector kickPos, final IVector kickVel, final double tSwitch,
			final double accSlide, final double accRoll, final double tKickToNow)
	{
		super(kickPos, kickVel, tKickToNow);
		
		this.tSwitch = tSwitch;
		this.accSlide = accSlide;
		this.accRoll = accRoll;
		
		Vector2 intAcc = kickVel.getXYVector().scaleToNew(accSlide * tSwitch);
		velSwitch = kickVel.getXYVector().addNew(intAcc);
		posSwitch = kickVel.getXYVector().multiplyNew(tSwitch)
				.add(kickPos)
				.add(intAcc.multiply(0.5 * tSwitch));
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
			IVector2 accNow = kickVel.getXYVector().scaleToNew(accSlide);
			Vector2 intAcc = accNow.multiplyNew(time);
			IVector velNow = kickVel.getXYVector().addNew(intAcc);
			IVector posNow = kickVel.getXYVector()
					.multiplyNew(time)
					.add(intAcc.multiplyNew(0.5 * time))
					.add(kickPos);
			
			return aBallState().withPos(posNow).withVel(velNow).withAcc(accNow)
					.withVSwitchToRoll(velSwitch.getLength2()).withChipped(false).build();
		}
		
		double t2 = time - tSwitch;
		if (time > getTimeAtRest())
		{
			t2 = getTimeAtRest() - tSwitch;
		}
		
		IVector2 acc = kickVel.getXYVector().scaleToNew(accRoll);
		Vector2 intAcc = acc.multiplyNew(t2);
		IVector velNow = velSwitch.addNew(intAcc);
		IVector posNow = velSwitch.multiplyNew(t2)
				.add(intAcc.multiplyNew(0.5 * t2))
				.add(posSwitch);
		
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
			return (SumatraMath.sqrt((v * v) + (2.0 * a * distance)) - v) / a;
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
		double timeToDist = ((SumatraMath.sqrt((v * v) + (2.0 * a * p) + 1e-6) - v) / a) + 1e-6;
		if (timeToDist < 1e-3)
		{
			timeToDist = 0.0; // numerical issues...
		}
		assert timeToDist >= 0 : timeToDist;
		
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
