/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ball.trajectory.flat;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import lombok.Getter;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;


/**
 * Ball trajectory for flat kicks with two phases, separated by a dynamically calculated switch velocity.
 */
@Persistent
public class FlatBallTrajectory extends ABallTrajectory
{
	@Getter
	private final double tSwitch;
	@Getter
	private final IVector2 posSwitch;
	@Getter
	private final IVector2 velSwitch;
	private final IVector2 accSlide;
	private final IVector2 accSlideSpin;
	private final IVector2 accRoll;


	/**
	 * Create an empty default state. Required for {@link Persistent}.
	 */
	private FlatBallTrajectory()
	{
		super();

		tSwitch = 0;
		posSwitch = Vector2f.ZERO_VECTOR;
		velSwitch = Vector2f.ZERO_VECTOR;
		accSlide = Vector2f.ZERO_VECTOR;
		accSlideSpin = Vector2f.ZERO_VECTOR;
		accRoll = Vector2f.ZERO_VECTOR;
	}


	private FlatBallTrajectory(
			final BallParameters parameters,
			final IVector2 initialPos,
			final IVector2 initialVel,
			final IVector2 initialSpin)
	{
		this.parameters = parameters;
		this.initialPos = initialPos.getXYZVector();
		this.initialVel = initialVel.getXYZVector();
		this.initialSpin = initialSpin;

		// compute relative velocity of ball to ground surface, if ball is rolling this is close to zero
		IVector2 contactVelocity = initialVel.subtractNew(initialSpin.multiplyNew(parameters.getBallRadius()));

		if (contactVelocity.getLength2() < 0.01)
		{
			// ball is rolling
			accSlide = initialVel.scaleToNew(parameters.getAccRoll());
			accSlideSpin = accSlide.multiplyNew(1.0 / parameters.getBallRadius());
			tSwitch = 0.0;
		} else
		{
			// ball is sliding
			accSlide = contactVelocity.scaleToNew(parameters.getAccSlide());
			accSlideSpin = accSlide
					.multiplyNew(1.0 / (parameters.getBallRadius() * parameters.getInertiaDistribution()));
			double f = 1.0 / (1.0 + 1.0 / parameters.getInertiaDistribution());
			IVector2 slideVel = initialSpin.multiplyNew(parameters.getBallRadius()).subtract(initialVel).multiply(f);

			if (Math.abs(accSlide.x()) > Math.abs(accSlide.y()))
			{
				tSwitch = slideVel.x() / accSlide.x();
			} else
			{
				tSwitch = slideVel.y() / accSlide.y();
			}
		}

		velSwitch = initialVel.addNew(accSlide.multiplyNew(tSwitch));
		posSwitch = initialPos.addNew(initialVel.multiplyNew(tSwitch)).add(accSlide.multiplyNew(0.5 * tSwitch * tSwitch));

		accRoll = velSwitch.scaleToNew(parameters.getAccRoll());
	}


	/**
	 * Create a flat ball trajectory from a ball where kick position/velocity is known.
	 *
	 * @param parameters
	 * @param kickPos    in [mm]
	 * @param kickVel    in [mm/s]
	 * @param kickSpin   in [rad/s]
	 * @return
	 */
	public static FlatBallTrajectory fromKick(
			final BallParameters parameters, final IVector2 kickPos, final IVector2 kickVel, final IVector2 kickSpin)
	{
		return new FlatBallTrajectory(parameters, kickPos, kickVel, kickSpin);
	}


	/**
	 * Create from state.
	 *
	 * @param parameters
	 * @param posNow in [mm]
	 * @param velNow in [mm/s]
	 * @param spin in [rad/s]
	 * @return
	 */
	public static FlatBallTrajectory fromState(
			final BallParameters parameters, final IVector2 posNow, final IVector2 velNow, final IVector2 spin)
	{
		return new FlatBallTrajectory(parameters, posNow, velNow, spin);
	}


	@Override
	public IBallTrajectory withAdjustedInitialPos(final IVector3 posNow, final double time)
	{
		IVector3 deltaPos = posNow.subtractNew(getMilliStateAtTime(time).getPos());

		return new FlatBallTrajectory(parameters, initialPos.addNew(deltaPos).getXYVector(), initialVel.getXYVector(),
				initialSpin);
	}


	@Override
	public IBallTrajectory withBallParameters(BallParameters ballParameters)
	{
		return new FlatBallTrajectory(ballParameters, initialPos.getXYVector(), initialVel.getXYVector(), initialSpin);
	}


	@Override
	public ABallTrajectory mirrored()
	{
		IVector2 pos = initialPos.getXYVector().multiplyNew(-1);
		IVector2 vel = initialVel.getXYVector().multiplyNew(-1);
		IVector2 spin = initialSpin.multiplyNew(-1);

		return new FlatBallTrajectory(parameters, pos, vel, spin);
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

		if (time < tSwitch)
		{
			IVector2 posNow = initialPos.getXYVector().addNew(initialVel.getXYVector().multiplyNew(time))
					.add(accSlide.multiplyNew(0.5 * time * time));
			IVector2 velNow = initialVel.getXYVector().addNew(accSlide.multiplyNew(time));
			IVector2 spinNow = initialSpin.subtractNew(accSlideSpin.multiplyNew(time));

			return BallState.builder()
					.withPos(posNow.getXYZVector())
					.withVel(velNow.getXYZVector())
					.withAcc(accSlide.getXYZVector())
					.withSpin(spinNow)
					.build();
		}

		double t2 = time - tSwitch;
		if (time > getTimeAtRest())
		{
			t2 = getTimeAtRest() - tSwitch;
		}

		IVector2 posNow = posSwitch.addNew(velSwitch.getXYVector().multiplyNew(t2))
				.add(accRoll.multiplyNew(0.5 * t2 * t2));
		IVector2 velNow = velSwitch.getXYVector().addNew(accRoll.multiplyNew(t2));
		IVector2 spinNow = velNow.multiplyNew(1.0 / parameters.getBallRadius());

		return BallState.builder()
				.withPos(posNow.getXYZVector())
				.withVel(velNow.getXYZVector())
				.withAcc(accRoll.getXYZVector())
				.withSpin(spinNow)
				.build();
	}


	@Override
	public PlanarCurve getPlanarCurve()
	{
		List<PlanarCurveSegment> segments = new ArrayList<>();

		double tRest = getTimeAtRest();

		if (tRest <= 0)
		{
			segments.add(PlanarCurveSegment.fromPoint(initialPos.getXYVector(), 0, 1.0));
			return new PlanarCurve(segments);
		}

		if (tSwitch > 0)
		{
			// add sliding phase
			PlanarCurveSegment slide = PlanarCurveSegment.fromSecondOrder(initialPos.getXYVector(),
					initialVel.getXYVector(),
					accSlide,
					0, tSwitch);
			segments.add(slide);
		}

		PlanarCurveSegment roll = PlanarCurveSegment.fromSecondOrder(posSwitch.getXYVector(),
				velSwitch.getXYVector(),
				accRoll,
				tSwitch, tRest);
		segments.add(roll);

		return new PlanarCurve(segments);
	}


	@Override
	public double getTimeAtRest()
	{
		double tStop = -velSwitch.getLength2() / parameters.getAccRoll();
		return tSwitch + tStop;
	}


	@Override
	protected double getTimeByDistanceInMillimeters(final double distance)
	{
		double distToSwitch = ((initialVel.getLength2() + velSwitch.getLength2()) / 2.0) * tSwitch;

		if (distance < distToSwitch)
		{
			// queried distance is in sliding phase
			double v = initialVel.getLength2();
			double a = parameters.getAccSlide();
			return (SumatraMath.sqrt((v * v) + (2.0 * a * distance)) - v) / a;
		}

		double v = velSwitch.getLength2();
		double a = parameters.getAccRoll();
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
		if (velocity > initialVel.getLength2())
		{
			return 0;
		}

		if (velocity > velSwitch.getLength2())
		{
			// requested velocity is during sliding phase
			double tToVel = -(initialVel.getLength2() - velocity) / parameters.getAccSlide();
			Validate.isTrue(tToVel >= 0);

			return tToVel;
		}

		// requested velocity is during rolling phase
		double tToVel = -(velSwitch.getLength2() - velocity) / parameters.getAccRoll();
		Validate.isTrue(tToVel >= 0);

		return tSwitch + tToVel;
	}
}
