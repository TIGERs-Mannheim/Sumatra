/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;

import java.util.ArrayList;
import java.util.List;


/**
 * Factory for creating planar curves
 */
public class PlanarCurveFactory
{
	/**
	 * Create a planar curve from a 2d trajectory
	 *
	 * @param trajectory the trajectory
	 * @return the planar curve
	 */
	public PlanarCurve getPlanarCurve(final ITrajectory<IVector2> trajectory)
	{
		List<PlanarCurveSegment> segments = new ArrayList<>();

		List<Double> tQuery = trajectory.getTimeSections();
		tQuery.sort(Double::compare);

		PosVelAcc<IVector2> state = trajectory.getValuesAtTime(0);

		double tLast = 0;

		for (Double t : tQuery)
		{
			IVector2 pos = state.getPos().multiplyNew(1e3);
			IVector2 vel = state.getVel().multiplyNew(1e3);
			IVector2 acc = state.getAcc().multiplyNew(1e3);
			if (!SumatraMath.isZero(t - tLast))
			{
				if (SumatraMath.isZero(acc.getLength2()))
				{
					segments.add(PlanarCurveSegment.fromFirstOrder(pos, vel, tLast, t));
				} else
				{
					segments.add(PlanarCurveSegment.fromSecondOrder(pos, vel, acc, tLast, t));
				}
			}

			state = trajectory.getValuesAtTime(t);
			tLast = t;
		}

		if (segments.isEmpty())
		{
			Vector2 initPos = trajectory.getPosition(0).multiplyNew(1e3);
			segments.add(PlanarCurveSegment.fromPoint(initPos, 0, 1.0));
		}

		return new PlanarCurve(segments);
	}
}
