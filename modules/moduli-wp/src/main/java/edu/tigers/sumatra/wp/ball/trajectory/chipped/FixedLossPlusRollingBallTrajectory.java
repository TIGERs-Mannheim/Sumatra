/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.chipped;

import static edu.tigers.sumatra.wp.data.BallTrajectoryState.aBallState;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class FixedLossPlusRollingBallTrajectory extends ABallTrajectory
{
	private final FixedLossPlusRollingParameters params;
	
	
	protected FixedLossPlusRollingBallTrajectory(final IVector3 kickPos, final IVector3 kickVel, final double tKickToNow,
			final FixedLossPlusRollingParameters params)
	{
		super(kickPos, kickVel, tKickToNow);
		this.params = params;
	}
	
	
	/**
	 * Create for kick
	 * 
	 * @param kickPos
	 * @param kickVel
	 * @param params
	 * @return
	 */
	public static FixedLossPlusRollingBallTrajectory fromKick(final IVector2 kickPos, final IVector3 kickVel,
			final FixedLossPlusRollingParameters params)
	{
		return new FixedLossPlusRollingBallTrajectory(Vector3.from2d(kickPos, 0), kickVel, 0, params);
	}
	
	
	/**
	 * Create from state
	 * 
	 * @param posNow
	 * @param velNow
	 * @param params
	 * @return
	 */
	public static FixedLossPlusRollingBallTrajectory fromState(final IVector3 posNow, final IVector3 velNow,
			final FixedLossPlusRollingParameters params)
	{
		double pz1 = posNow.z();
		double vz1 = velNow.z();
		double g = 9810;
		
		IVector3 a = Vector3.fromXYZ(0, 0, -g);
		
		double tInAir = -(Math.sqrt((vz1 * vz1) + (2.0 * g * pz1)) - vz1) / g;
		double vKickZ = Math.sqrt((vz1 * vz1) + (2.0 * g * pz1));
		
		IVector3 kickPos = posNow.addNew(velNow.multiplyNew(tInAir)).addNew(a.multiplyNew(0.5 * tInAir * tInAir));
		IVector3 kickVel = Vector3.from2d(velNow.getXYVector(), vKickZ);
		
		return new FixedLossPlusRollingBallTrajectory(kickPos, kickVel, -tInAir, params);
	}
	
	
	@Override
	public BallTrajectoryState getMilliStateAtTime(final double time)
	{
		Vector3 accNow = Vector3.fromXYZ(0, 0, -9810);
		
		if (time < 0)
		{
			return aBallState().withPos(kickPos).withVel(kickVel).withAcc(accNow)
					.withVSwitchToRoll(kickVel.getLength2()).withChipped(true).build();
		}
		
		Vector3 posNow = Vector3.copy(kickPos);
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = 0;
		
		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > params.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / 9810;
			
			if ((tNow + tFly) > time)
			{
				double t = time - tNow;
				posNow.add(velNow.multiplyNew(t)).add(Vector3.fromXYZ(0, 0, -0.5 * 9810 * t * t));
				velNow.add(Vector3.fromXYZ(0, 0, -9810 * t));
				
				return aBallState().withPos(posNow).withVel(velNow).withAcc(accNow)
						.withVSwitchToRoll(velNow.getLength2()).withChipped(true).build();
			}
			
			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(Vector3.fromXYZ(params.getChipDampingXY(),
					params.getChipDampingXY(), params.getChipDampingZ()));
			tNow += tFly;
		}
		
		velNow.set(2, 0);
		
		// ball is below 10mm and assumed to be rolling
		double t = time - tNow;
		double tStop = -velNow.getLength2() / params.getAccRoll();
		if (t > tStop)
		{
			t = tStop;
		}
		
		accNow = velNow.normalizeNew().multiply(params.getAccRoll());
		posNow.add(velNow.multiplyNew(t))
				.add(velNow.normalizeNew().multiply(0.5 * params.getAccRoll() * t * t));
		velNow.add(velNow.normalizeNew().multiply(params.getAccRoll() * t));
		
		
		return aBallState().withPos(posNow).withVel(velNow).withAcc(accNow)
				.withVSwitchToRoll(velNow.getLength2()).withChipped(true).build();
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
		
		Vector3 posNow = Vector3.copy(state.getPos());
		Vector3 velNow = Vector3.copy(state.getVel());
		double tNow = 0;
		
		// go through hops while max. height is above minHopHeight
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > params.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / 9810;
			
			PlanarCurveSegment fly = PlanarCurveSegment.fromFirstOrder(posNow.getXYVector(),
					velNow.getXYVector(), tNow, tNow + tFly);
			
			segments.add(fly);
			
			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(Vector3.fromXYZ(params.getChipDampingXY(),
					params.getChipDampingXY(), params.getChipDampingZ()));
			tNow += tFly;
		}
		
		velNow.set(2, 0);
		
		// ball is below 10mm and assumed to be rolling
		Vector3 accNow = velNow.normalizeNew().multiply(params.getAccRoll());
		double tStop = -velNow.getLength2() / params.getAccRoll();
		PlanarCurveSegment roll = PlanarCurveSegment.fromSecondOrder(posNow.getXYVector(),
				velNow.getXYVector(),
				accNow.getXYVector(),
				tNow, tNow + tStop);
		segments.add(roll);
		
		return new PlanarCurve(segments);
	}
	
	
	@Override
	public double getTimeAtRest()
	{
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = 0;
		
		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > params.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / 9810;
			
			velNow = velNow.multiplyNew(Vector3.fromXYZ(params.getChipDampingXY(),
					params.getChipDampingXY(), params.getChipDampingZ()));
			tNow += tFly;
		}
		
		velNow.set(2, 0);
		
		// ball is below 10mm and assumed to be rolling
		double tStop = -velNow.getLength2() / params.getAccRoll();
		tNow += tStop;
		
		return tNow;
	}
	
	
	@Override
	protected double getTimeByDistanceInMillimeters(final double distance)
	{
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = 0;
		double distNow = 0;
		
		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > params.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / 9810;
			
			double partLength = velNow.multiplyNew(tFly).getLength2();
			
			if ((distNow + partLength) > distance)
			{
				double tPart = (distance - distNow) / velNow.getLength2();
				return tNow + tPart;
			}
			
			distNow += partLength;
			velNow = velNow.multiplyNew(Vector3.fromXYZ(params.getChipDampingXY(),
					params.getChipDampingXY(), params.getChipDampingZ()));
			tNow += tFly;
		}
		
		// ball is below 10mm and assumed to be rolling
		double p = distance - distNow;
		double v = velNow.getLength2();
		double a = params.getAccRoll();
		
		double tStop = -v / a;
		double distStop = distNow + (v * tStop) + (0.5 * a * tStop * tStop);
		if (distance > distStop)
		{
			// cannot reach the requested distance!
			return tNow + tStop;
		}
		
		double timeToDist = ((Math.sqrt((v * v) + (2.0 * a * p) + 1e-6) - v) / a) + 1e-6;
		if (timeToDist < 1e-3)
		{
			timeToDist = 0.0; // numerical issues...
		}
		Validate.isTrue(timeToDist >= 0, String.valueOf(timeToDist));
		
		return tNow + timeToDist;
	}
	
	
	@Override
	protected double getTimeByVelocityInMillimetersPerSec(final double velocity)
	{
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = 0;
		
		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > params.getMinHopHeight())
		{
			if (velNow.getLength2() < velocity)
			{
				return tNow;
			}
			
			double tFly = (2 * velNow.z()) / 9810;
			velNow = velNow.multiplyNew(Vector3.fromXYZ(params.getChipDampingXY(),
					params.getChipDampingXY(), params.getChipDampingZ()));
			tNow += tFly;
		}
		
		// ball is below 10mm and assumed to be rolling
		double v = velNow.getLength2();
		double a = params.getAccRoll();
		
		// v = v0 + a*t
		double tToVel = -(v - velocity) / a;
		Validate.isTrue(tToVel >= 0);
		
		return tNow + tToVel;
	}
	
	
	@Override
	public ABallTrajectory mirrored()
	{
		IVector3 vel = Vector3.from2d(kickVel.getXYVector().multiplyNew(-1), kickVel.z());
		IVector2 pos = kickPos.getXYVector().multiplyNew(-1);
		
		return FixedLossPlusRollingBallTrajectory.fromKick(pos, vel, params);
	}
	
	
	@Override
	public List<IVector2> getTouchdownLocations()
	{
		List<IVector2> locations = new ArrayList<>();
		
		Vector3 posNow = Vector3.copy(kickPos);
		Vector3 velNow = Vector3.copy(kickVel);
		
		// go through hops while max. height is above minHeight
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > params.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / 9810;
			
			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(Vector3.fromXYZ(params.getChipDampingXY(),
					params.getChipDampingXY(), params.getChipDampingZ()));
			
			locations.add(posNow.getXYVector());
		}
		
		return locations;
	}
	
	
	@Override
	public ILine getTravelLineRolling()
	{
		List<IVector2> locs = getTouchdownLocations();
		if (locs.isEmpty())
		{
			return getTravelLine();
		}
		
		IVector2 finalPos = getPosByVel(0);
		return Line.fromPoints(locs.get(locs.size() - 1), finalPos);
	}
	
	
	@Override
	public List<ILine> getTravelLinesInterceptable()
	{
		final double g = 9810;
		final double h = params.getMaxInterceptableHeight();
		List<ILine> lines = new ArrayList<>();
		Vector3 posNow = Vector3.copy(kickPos);
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = 0;
		
		double t1;
		double t2;
		IVector2 p2 = getPosByTime(0);
		// go through hops while max. height is above 150mm
		while (((velNow.z() * velNow.z()) / (2.0 * g)) > h)
		{
			double vz = velNow.z();
			double tFly = (2 * vz) / g;
			
			t1 = -(Math.sqrt((vz * vz) - (2 * g * h)) - vz) / g;
			
			IVector2 p1 = posNow.addNew(velNow.multiplyNew(t1)).add(Vector3.fromXYZ(0, 0, -0.5 * g * t1 * t1))
					.getXYVector();
			
			if ((tNow + t1) > tKickToNow)
			{
				lines.add(Line.fromPoints(p2, p1));
			}
			
			t2 = (Math.sqrt((vz * vz) - (2 * g * h)) + vz) / g;
			
			if ((tNow + t2) < tKickToNow)
			{
				t2 = tKickToNow - tNow;
			}
			
			p2 = posNow.addNew(velNow.multiplyNew(t2)).add(Vector3.fromXYZ(0, 0, -0.5 * g * t2 * t2)).getXYVector();
			
			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(Vector3.fromXYZ(params.getChipDampingXY(),
					params.getChipDampingXY(), params.getChipDampingZ()));
			tNow += tFly;
		}
		
		IVector2 p1 = getPosByVel(0);
		lines.add(Line.fromPoints(p2, p1));
		
		return lines;
	}
	
	
	/**
	 * Parameter class
	 */
	public static class FixedLossPlusRollingParameters
	{
		private final double chipDampingXY;
		private final double chipDampingZ;
		private final double accRoll;
		private final double minHopHeight;
		private final double maxInterceptableHeight;
		
		
		/**
		 * Constructor with global parameters.
		 */
		public FixedLossPlusRollingParameters()
		{
			BallParameters ballParams = Geometry.getBallParameters();
			chipDampingXY = ballParams.getChipDampingXY();
			chipDampingZ = ballParams.getChipDampingZ();
			accRoll = ballParams.getAccRoll();
			minHopHeight = ballParams.getMinHopHeight();
			maxInterceptableHeight = ballParams.getMaxInterceptableHeight();
		}
		
		
		/**
		 * @param chipDampingXY
		 * @param chipDampingZ
		 * @param accRoll
		 * @param minHopHeight
		 * @param maxInterceptableHeight
		 */
		public FixedLossPlusRollingParameters(final double chipDampingXY, final double chipDampingZ, final double accRoll,
				final double minHopHeight, final double maxInterceptableHeight)
		{
			super();
			this.chipDampingXY = chipDampingXY;
			this.chipDampingZ = chipDampingZ;
			this.accRoll = accRoll;
			this.minHopHeight = minHopHeight;
			this.maxInterceptableHeight = maxInterceptableHeight;
		}
		
		
		public double getChipDampingXY()
		{
			return chipDampingXY;
		}
		
		
		public double getChipDampingZ()
		{
			return chipDampingZ;
		}
		
		
		public double getAccRoll()
		{
			return accRoll;
		}
		
		
		public double getMinHopHeight()
		{
			return minHopHeight;
		}
		
		
		public double getMaxInterceptableHeight()
		{
			return maxInterceptableHeight;
		}
	}
}
