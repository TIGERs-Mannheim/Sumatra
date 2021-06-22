/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ball.trajectory.chipped;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;


/**
 * Ball trajectory for chipped kicks. Uses fixed Z damping and one damping in XY for first hop
 * and one damping in XY for all following hops.
 */
@Persistent
public class ChipBallTrajectory extends ABallTrajectory
{
	private static final double G = 9810;

	private final double tInAir;
	private final IVector2 kickPos;
	private final IVector3 kickVel;


	/**
	 * Create an empty default state. Required for {@link Persistent}.
	 */
	private ChipBallTrajectory()
	{
		super();

		tInAir = 0;
		kickPos = Vector2f.ZERO_VECTOR;
		kickVel = Vector3f.ZERO_VECTOR;
	}


	private ChipBallTrajectory(
			final BallParameters parameters,
			final IVector3 initialPos,
			final IVector3 initialVel,
			final IVector2 initialSpin,
			final double tIntAir,
			final IVector2 kickPos,
			final IVector3 kickVel)
	{
		this.parameters = parameters;
		this.initialPos = initialPos;
		this.initialVel = initialVel;
		this.initialSpin = initialSpin;
		this.tInAir = tIntAir;
		this.kickPos = kickPos;
		this.kickVel = kickVel;
	}


	/**
	 * Create from kick
	 *
	 * @param parameters
	 * @param kickPos in [mm]
	 * @param kickVel in [mm/s]
	 * @param kickSpin in [rad/s]
	 * @return
	 */
	public static ChipBallTrajectory fromKick(
			final BallParameters parameters, final IVector2 kickPos, final IVector3 kickVel, final IVector2 kickSpin)
	{
		return new ChipBallTrajectory(parameters, kickPos.getXYZVector(), kickVel, kickSpin, 0, kickPos, kickVel);
	}


	/**
	 * Create from state
	 *
	 * @param parameters
	 * @param posNow in [mm]
	 * @param velNow in [mm/s]
	 * @param spin in [rad/s]
	 * @return
	 */
	public static ChipBallTrajectory fromState(
			final BallParameters parameters, final IVector3 posNow, final IVector3 velNow, final IVector2 spin)
	{
		double pz1 = posNow.z();
		double vz1 = velNow.z();

		IVector3 a = Vector3.fromXYZ(0, 0, -G);

		// tInAir will be negative
		double tInAir = -(SumatraMath.sqrt((vz1 * vz1) + (2.0 * G * pz1)) - vz1) / G;
		double vKickZ = SumatraMath.sqrt((vz1 * vz1) + (2.0 * G * pz1));

		IVector3 kickPos = posNow.addNew(velNow.multiplyNew(tInAir)).addNew(a.multiplyNew(0.5 * tInAir * tInAir));
		IVector3 kickVel = Vector3.from2d(velNow.getXYVector(), vKickZ);

		return new ChipBallTrajectory(parameters, posNow, velNow, spin, -tInAir, kickPos.getXYVector(), kickVel);
	}


	@Override
	public IBallTrajectory withAdjustedInitialPos(final IVector3 posNow, final double time)
	{
		IVector3 deltaPos = posNow.subtractNew(getMilliStateAtTime(time).getPos());

		return new ChipBallTrajectory(parameters, initialPos.addNew(deltaPos), initialVel, initialSpin, tInAir,
				kickPos.addNew(
						deltaPos.getXYVector()), kickVel);
	}


	@Override
	public IBallTrajectory withBallParameters(BallParameters ballParameters)
	{
		return new ChipBallTrajectory(ballParameters, initialPos, initialVel, initialSpin, tInAir, kickPos, kickVel);
	}


	@Override
	public BallState getMilliStateAtTime(final double time)
	{
		if (time < 0)
		{
			return BallState.builder()
					.withPos(initialPos)
					.withVel(initialVel)
					.withAcc(Vector3f.ZERO_VECTOR)
					.withSpin(initialSpin)
					.build();
		}

		double tQuery = time + tInAir;

		Vector3 posNow = Vector3.copy(kickPos.getXYZVector());
		Vector3 velNow = Vector3.copy(kickVel);
		Vector3 accNow = Vector3.fromXYZ(0, 0, -G);
		double tNow = 0;
		IVector2 spin = initialSpin;

		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * G)) > parameters.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / G;

			if ((tNow + tFly) > tQuery)
			{
				double t = tQuery - tNow;
				posNow.add(velNow.multiplyNew(t)).add(Vector3.fromXYZ(0, 0, -0.5 * G * t * t));
				velNow.add(Vector3.fromXYZ(0, 0, -G * t));

				return BallState.builder()
						.withPos(posNow)
						.withVel(velNow)
						.withAcc(accNow)
						.withSpin(spin)
						.build();
			}

			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(getDamping(spin));
			tNow += tFly;

			// set spin to rolling after first hop
			spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());
		}

		velNow.set(2, 0);

		// ball is below 10mm and assumed to be rolling
		double t = tQuery - tNow;
		double tStop = -velNow.getLength2() / parameters.getAccRoll();
		if (t > tStop)
		{
			t = tStop;
		}

		accNow = velNow.normalizeNew().multiply(parameters.getAccRoll());
		posNow.add(velNow.multiplyNew(t))
				.add(velNow.normalizeNew().multiply(0.5 * parameters.getAccRoll() * t * t));
		velNow.add(velNow.normalizeNew().multiply(parameters.getAccRoll() * t));
		spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());

		return BallState.builder()
				.withPos(posNow)
				.withVel(velNow)
				.withAcc(accNow)
				.withSpin(spin)
				.build();
	}


	@Override
	public PlanarCurve getPlanarCurve()
	{
		List<PlanarCurveSegment> segments = new ArrayList<>();

		double tRest = getTimeAtRest();
		if (tInAir > tRest)
		{
			segments.add(PlanarCurveSegment.fromPoint(initialPos.getXYVector(), 0, 1.0));
			return new PlanarCurve(segments);
		}

		Vector3 posNow = Vector3.copy(kickPos.getXYZVector());
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = -tInAir;
		IVector2 spin = initialSpin;

		// go through hops while max. height is above minHopHeight
		while (((velNow.z() * velNow.z()) / (2.0 * G)) > parameters.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / G;

			PlanarCurveSegment fly = PlanarCurveSegment.fromFirstOrder(posNow.getXYVector(),
					velNow.getXYVector(), tNow, tNow + tFly);

			segments.add(fly);

			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(getDamping(spin));
			tNow += tFly;

			spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());
		}

		velNow.set(2, 0);

		// ball is below 10mm and assumed to be rolling
		Vector3 accNow = velNow.normalizeNew().multiply(parameters.getAccRoll());
		double tStop = -velNow.getLength2() / parameters.getAccRoll();
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
		double tNow = -tInAir;
		IVector2 spin = initialSpin;

		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * G)) > parameters.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / G;

			velNow = velNow.multiplyNew(getDamping(spin));
			tNow += tFly;

			spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());
		}

		velNow.set(2, 0);

		// ball is below 10mm and assumed to be rolling
		double tStop = -velNow.getLength2() / parameters.getAccRoll();
		tNow += tStop;

		return tNow;
	}


	@Override
	protected double getTimeByDistanceInMillimeters(final double distance)
	{
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = -tInAir;
		double distNow = -initialPos.getXYVector().distanceTo(kickPos.getXYVector());
		IVector2 spin = initialSpin;

		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * G)) > parameters.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / G;

			double partLength = velNow.multiplyNew(tFly).getLength2();

			if ((distNow + partLength) > distance)
			{
				double tPart = (distance - distNow) / velNow.getLength2();
				return tNow + tPart;
			}

			distNow += partLength;
			velNow = velNow.multiplyNew(getDamping(spin));
			tNow += tFly;

			spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());
		}

		// ball is below 10mm and assumed to be rolling
		double p = distance - distNow;
		double v = velNow.getLength2();
		double a = parameters.getAccRoll();

		double tStop = -v / a;
		double distStop = distNow + (v * tStop) + (0.5 * a * tStop * tStop);
		if (distance > distStop)
		{
			// cannot reach the requested distance!
			return tNow + tStop;
		}

		double timeToDist = ((SumatraMath.sqrt((v * v) + (2.0 * a * p) + 1e-6) - v) / a) + 1e-6;
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
		double tNow = -tInAir;
		IVector2 spin = initialSpin;

		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * G)) > parameters.getMinHopHeight())
		{
			if (velNow.getLength2() < velocity)
			{
				return tNow;
			}

			double tFly = (2 * velNow.z()) / G;
			velNow = velNow.multiplyNew(getDamping(spin));
			tNow += tFly;

			spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());
		}

		// ball is below 10mm and assumed to be rolling
		double v = velNow.getLength2();
		double a = parameters.getAccRoll();

		// v = v0 + a*t
		double tToVel = -(v - velocity) / a;
		Validate.isTrue(tToVel >= 0);

		return tNow + tToVel;
	}


	@Override
	public ABallTrajectory mirrored()
	{
		IVector2 kickPosMir = kickPos.multiplyNew(-1);
		IVector3 kickVelMir = Vector3.from2d(kickVel.getXYVector().multiplyNew(-1), kickVel.getXYZVector().z());
		IVector3 pos = Vector3.from2d(initialPos.getXYVector().multiplyNew(-1), initialPos.z());
		IVector3 vel = Vector3.from2d(initialVel.getXYVector().multiplyNew(-1), initialVel.z());
		IVector2 spin = initialSpin.multiplyNew(-1);

		return new ChipBallTrajectory(parameters, pos, vel, spin, tInAir, kickPosMir, kickVelMir);
	}


	@Override
	public List<IVector2> getTouchdownLocations()
	{
		List<IVector2> locations = new ArrayList<>();

		Vector3 posNow = Vector3.copy(kickPos.getXYZVector());
		Vector3 velNow = Vector3.copy(kickVel);

		IVector2 spin = initialSpin;

		// go through hops while max. height is above minHeight
		while (((velNow.z() * velNow.z()) / (2.0 * G)) > parameters.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / G;

			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(getDamping(spin));

			locations.add(posNow.getXYVector());

			spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());
		}

		return locations;
	}


	@Override
	public ILineSegment getTravelLineRolling()
	{
		List<IVector2> locs = getTouchdownLocations();
		if (locs.isEmpty())
		{
			return getTravelLineSegment();
		}

		IVector2 finalPos = getPosByVel(0).getXYVector();
		return Lines.segmentFromPoints(locs.get(locs.size() - 1), finalPos);
	}


	@Override
	public List<ILineSegment> getTravelLinesInterceptable()
	{
		final double g = G;
		final double h = parameters.getMaxInterceptableHeight();
		List<ILineSegment> lines = new ArrayList<>();
		Vector3 posNow = Vector3.copy(kickPos.getXYZVector());
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = -tInAir;
		IVector2 spin = initialSpin;

		double t1;
		double t2;
		IVector2 p2 = getPosByTime(0).getXYVector();

		// go through hops while max. height is above 150mm
		while (((velNow.z() * velNow.z()) / (2.0 * g)) > h)
		{
			double vz = velNow.z();
			double tFly = (2 * vz) / g;

			t1 = -(SumatraMath.sqrt((vz * vz) - (2 * g * h)) - vz) / g;

			IVector2 p1 = posNow.addNew(velNow.multiplyNew(t1)).add(Vector3.fromXYZ(0, 0, -0.5 * g * t1 * t1))
					.getXYVector();

			if ((tNow + t1) > 0)
			{
				lines.add(Lines.segmentFromPoints(p2, p1));
			}

			t2 = (SumatraMath.sqrt((vz * vz) - (2 * g * h)) + vz) / g;

			if ((tNow + t2) < 0)
			{
				t2 = -tNow;
			}

			p2 = posNow.addNew(velNow.multiplyNew(t2)).add(Vector3.fromXYZ(0, 0, -0.5 * g * t2 * t2)).getXYVector();

			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(getDamping(spin));
			tNow += tFly;

			spin = velNow.getXYVector().multiplyNew(1.0 / parameters.getBallRadius());
		}

		IVector2 p1 = getPosByVel(0).getXYVector();
		lines.add(Lines.segmentFromPoints(p2, p1));

		return lines;
	}


	private IVector3 getDamping(final IVector2 spin)
	{
		if (spin.getLength2() > 0)
		{
			return Vector3.fromXYZ(parameters.getChipDampingXYOtherHops(),
					parameters.getChipDampingXYOtherHops(), parameters.getChipDampingZ());
		}

		return Vector3.fromXYZ(parameters.getChipDampingXYFirstHop(),
				parameters.getChipDampingXYFirstHop(), parameters.getChipDampingZ());
	}
}
