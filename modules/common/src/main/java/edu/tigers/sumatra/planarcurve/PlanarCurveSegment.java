/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.planarcurve;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;


/**
 * A planar curve segment restricts a curve to a specific time frame.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class PlanarCurveSegment
{
	private final EPlanarCurveSegmentType type;

	private final IVector2 pos;         // [mm]
	private final IVector2 vel;         // [mm/s]
	private final IVector2 acc;         // [mm/s^2]

	private final double startTime;
	@Setter
	private double endTime;

	@Setter
	@Getter
	private double airtime;
	@Setter
	@Getter
	private double hopHeight;


	/**
	 * @param type
	 * @param pos       [mm]
	 * @param vel       [mm/s]
	 * @param acc       [mm/s^2]
	 * @param startTime [s]
	 * @param endTime   [s]
	 */
	private PlanarCurveSegment(final EPlanarCurveSegmentType type, final IVector2 pos, final IVector2 vel,
			final IVector2 acc,
			final double startTime,
			final double endTime)
	{
		this.type = type;
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.startTime = startTime;
		this.endTime = endTime;
		this.airtime = 0;
		this.hopHeight = 0;
	}


	/**
	 * Create planar curve segment from point.
	 *
	 * @param pos    [mm]
	 * @param tStart
	 * @param tEnd
	 * @return
	 */
	public static PlanarCurveSegment fromPoint(final IVector2 pos, final double tStart, final double tEnd)
	{
		return new PlanarCurveSegment(EPlanarCurveSegmentType.POINT, pos, Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR,
				tStart,
				tEnd);
	}


	/**
	 * Create planar curve segment from first order function.
	 *
	 * @param pos    [mm]
	 * @param vel    [mm/s]
	 * @param tStart
	 * @param tEnd
	 * @return
	 */
	public static PlanarCurveSegment fromFirstOrder(final IVector2 pos, final IVector2 vel, final double tStart,
			final double tEnd)
	{
		return new PlanarCurveSegment(EPlanarCurveSegmentType.FIRST_ORDER, pos, vel, Vector2f.ZERO_VECTOR, tStart,
				tEnd);
	}


	/**
	 * Create planar curve segment from second order function.
	 *
	 * @param pos    [mm]
	 * @param vel    [mm/s]
	 * @param acc    [mm/s^2]
	 * @param tStart
	 * @param tEnd
	 * @return
	 */
	public static PlanarCurveSegment fromSecondOrder(final IVector2 pos, final IVector2 vel, final IVector2 acc,
			final double tStart, final double tEnd)
	{
		return new PlanarCurveSegment(EPlanarCurveSegmentType.SECOND_ORDER, pos, vel, acc, tStart,
				tEnd);
	}


	/**
	 * Position in [mm] at time t.
	 *
	 * @param t
	 * @return
	 */
	public IVector2 getPosition(final double t)
	{
		return switch (type)
		{
			case FIRST_ORDER -> pos.addNew(vel.multiplyNew(t));
			case SECOND_ORDER -> pos.addNew(vel.multiplyNew(t)).add(acc.multiplyNew(0.5 * t * t));
			default -> pos;
		};
	}


	/**
	 * Velocity in [mm/s] at time t.
	 *
	 * @param t
	 * @return
	 */
	public IVector2 getVelocity(final double t)
	{
		if (type == EPlanarCurveSegmentType.SECOND_ORDER)
		{
			return vel.addNew(acc.multiplyNew(t));
		}

		return vel;
	}


	/**
	 * Split this curve segment at tSplit.<br>
	 * Returns two segments: first from original segment to tSplit, second from tSplit to orignal tEnd.<br>
	 * Splitting this segment beyond tEnd will return this and a new POINT segment from tEnd to tSplit.
	 *
	 * @param tSplit
	 * @return
	 */
	public Pair<PlanarCurveSegment, PlanarCurveSegment> split(final double tSplit)
	{
		if (tSplit >= endTime)
		{
			double t = endTime - startTime;
			final IVector2 posNow = switch (type)
			{
				case FIRST_ORDER -> pos.addNew(vel.multiplyNew(t));
				case SECOND_ORDER -> pos.addNew(vel.multiplyNew(t)).add(acc.multiplyNew(0.5 * t * t));
				default -> pos;
			};

			return new Pair<>(this, PlanarCurveSegment.fromPoint(posNow, endTime, tSplit));
		}

		double t = tSplit - startTime;

		PlanarCurveSegment first = new PlanarCurveSegment(type, pos, vel, acc, startTime, tSplit);

		IVector2 posNow;
		switch (type)
		{
			case FIRST_ORDER:
				posNow = pos.addNew(vel.multiplyNew(t));
				return new Pair<>(first, PlanarCurveSegment.fromFirstOrder(posNow, vel, tSplit, endTime));
			case SECOND_ORDER:
				posNow = pos.addNew(vel.multiplyNew(t)).add(acc.multiplyNew(0.5 * t * t));
				IVector2 velNow = vel.addNew(acc.multiplyNew(t));
				return new Pair<>(first, PlanarCurveSegment.fromSecondOrder(posNow, velNow, acc, tSplit, endTime));
			default:
				return new Pair<>(first, PlanarCurveSegment.fromPoint(pos, tSplit, endTime));
		}
	}


	public double getEndTime()
	{
		return endTime;
	}


	public double getStartTime()
	{
		return startTime;
	}


	public double getDuration()
	{
		return endTime - startTime;
	}


	public EPlanarCurveSegmentType getType()
	{
		return type;
	}


	/**
	 * Position at time 0.
	 *
	 * @return Position in [mm].
	 */
	public IVector2 getPos()
	{
		return pos;
	}


	/**
	 * Velocity in [mm/s].
	 *
	 * @return
	 */
	public IVector2 getVel()
	{
		return vel;
	}


	/**
	 * Acceleration in [mm/s^2].
	 *
	 * @return
	 */
	public IVector2 getAcc()
	{
		return acc;
	}
}
