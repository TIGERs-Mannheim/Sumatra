/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.SumatraMath;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


/**
 * Bang Bang Trajectory for one dimension.
 */
@ToString
class BangBangTrajectory1D implements ITrajectory<Double>
{
	static final int MAX_PARTS = 3;
	final BBTrajectoryPart[] parts = new BBTrajectoryPart[MAX_PARTS];
	int numParts;


	BangBangTrajectory1D()
	{
		for (int i = 0; i < MAX_PARTS; i++)
		{
			parts[i] = new BBTrajectoryPart();
		}
	}


	@Override
	public Double getPosition(final double tt)
	{
		float trajTime = Math.max(0, (float) tt);

		if (trajTime >= getTotalTime())
		{
			// requested time beyond final element
			BBTrajectoryPart lastPart = parts[numParts - 1];
			final float t = lastPart.tEnd - parts[numParts - 2].tEnd;
			return (double) (lastPart.s0 + (lastPart.v0 * t) + (0.5f * lastPart.acc * t * t));
		}

		var pieceIdx = findPartIdx(trajTime);
		var piece = parts[pieceIdx];
		var tPieceStart = pieceIdx < 1 ? 0 : parts[pieceIdx - 1].tEnd;
		var t = trajTime - tPieceStart;
		return (double) (piece.s0 + (piece.v0 * t) + (0.5f * piece.acc * t * t));
	}


	@Override
	public Double getPositionMM(final double t)
	{
		return getPosition(t) * 1000.0f;
	}


	@Override
	public Double getVelocity(final double tt)
	{
		var trajTime = Math.max(0, (float) tt);

		if (trajTime >= getTotalTime())
		{
			// requested time beyond final element
			return 0.0;
		}

		var pieceIdx = findPartIdx(trajTime);
		var piece = parts[pieceIdx];
		var tPieceStart = pieceIdx < 1 ? 0 : parts[pieceIdx - 1].tEnd;
		var t = trajTime - tPieceStart;
		return (double) (piece.v0 + (piece.acc * t));
	}


	@Override
	public Double getAcceleration(final double tt)
	{
		float trajTime = Math.max(0, (float) tt);

		if (trajTime >= getTotalTime())
		{
			// requested time beyond final element
			return 0.0;
		}

		return (double) findPart(trajTime).acc;
	}


	@Override
	public double getTotalTime()
	{
		return parts[numParts - 1].tEnd;
	}


	@Override
	public BangBangTrajectory1D mirrored()
	{
		BangBangTrajectory1D mirrored = new BangBangTrajectory1D();
		mirrored.numParts = numParts;
		for (int i = 0; i < numParts; i++)
		{
			mirrored.parts[i].tEnd = parts[i].tEnd;
			mirrored.parts[i].acc = -parts[i].acc;
			mirrored.parts[i].v0 = -parts[i].v0;
			mirrored.parts[i].s0 = -parts[i].s0;
		}
		return mirrored;
	}


	@Override
	public PosVelAcc<Double> getValuesAtTime(final double tt)
	{
		float trajTime = Math.max(0, (float) tt);

		if (trajTime >= getTotalTime())
		{
			// requested time beyond final element
			return new PosVelAcc<>(getPosition(tt), 0.0, 0.0);
		}

		var pieceIdx = findPartIdx(trajTime);
		var piece = parts[pieceIdx];
		var tPieceStart = pieceIdx < 1 ? 0 : parts[pieceIdx - 1].tEnd;
		var t = trajTime - tPieceStart;
		return new PosVelAcc<>(
				(double) (piece.s0 + (piece.v0 * t) + (0.5f * piece.acc * t * t)),
				(double) (piece.v0 + (piece.acc * t)),
				(double) piece.acc
		);
	}


	@Override
	public List<Double> getTimeSections()
	{
		List<Double> sections = new ArrayList<>(numParts);
		for (int i = 0; i < numParts; i++)
		{
			sections.add((double) parts[i].tEnd);
		}
		return sections;
	}


	private int findPartIdx(double t)
	{
		for (int i = 0; i < numParts; i++)
		{
			if (t < parts[i].tEnd)
			{
				return i;
			}
		}
		return numParts - 1;
	}


	private BBTrajectoryPart findPart(double t)
	{
		return parts[findPartIdx(t)];
	}


	BangBangTrajectory1D generate(
			final float initialPos,
			final float finalPos,
			final float initialVel,
			final float maxVel,
			final float maxAcc
	)
	{
		float x0 = initialPos;
		float xd0 = initialVel;
		float xTrg = finalPos;
		float xdMax = maxVel;
		float xddMax = maxAcc;
		float sAtZeroAcc = velChangeToZero(x0, xd0, xddMax);

		if (sAtZeroAcc <= xTrg)
		{
			float sEnd = velTriToZero(x0, xd0, xdMax, xddMax);

			if (sEnd >= xTrg)
			{
				// Triangular profile
				calcTri(x0, xd0, xTrg, xddMax);
			} else
			{
				// Trapezoidal profile
				calcTrapz(x0, xd0, xdMax, xTrg, xddMax);
			}
		} else
		{
			// even with a full brake we miss xTrg
			float sEnd = velTriToZero(x0, xd0, -xdMax, xddMax);

			if (sEnd <= xTrg)
			{
				// Triangular profile
				calcTri(x0, xd0, xTrg, -xddMax);
			} else
			{
				// Trapezoidal profile
				calcTrapz(x0, xd0, -xdMax, xTrg, xddMax);
			}
		}
		return this;
	}


	private float velChangeToZero(final float s0, final float v0, final float aMax)
	{
		final float a;

		if (0 >= v0)
		{
			a = aMax;
		} else
		{
			a = -aMax;
		}

		final float t = -v0 / a;
		return s0 + (0.5f * v0 * t);
	}


	private float velTriToZero(final float s0, final float v0, final float v1, final float aMax)
	{
		final float a1;
		final float a2;

		if (v1 >= v0)
		{
			a1 = aMax;
			a2 = -aMax;
		} else
		{
			a1 = -aMax;
			a2 = aMax;
		}

		final float t1 = (v1 - v0) / a1;
		final float s1 = s0 + (0.5f * (v0 + v1) * t1);

		final float t2 = -v1 / a2;
		return s1 + (0.5f * v1 * t2);
	}


	private void calcTri(
			final float s0,
			final float v0,
			final float s2,
			final float a
	)
	{
		final float t2;
		final float v1;
		final float t1;
		final float s1;
		final float sq;

		if (a > 0)
		{
			// + -
			sq = ((a * (s2 - s0)) + (0.5f * v0 * v0)) / (a * a);
		} else
		{
			// - +
			sq = ((-a * (s0 - s2)) + (0.5f * v0 * v0)) / (a * a);
		}

		if (sq > 0.0f)
		{
			t2 = (float) SumatraMath.sqrt(sq);
		} else
		{
			t2 = 0;
		}
		v1 = a * t2;
		t1 = (v1 - v0) / a;
		s1 = s0 + ((v0 + v1) * 0.5f * t1);

		parts[0].tEnd = t1;
		parts[0].acc = a;
		parts[0].v0 = v0;
		parts[0].s0 = s0;
		parts[1].tEnd = t1 + t2;
		parts[1].acc = -a;
		parts[1].v0 = v1;
		parts[1].s0 = s1;
		numParts = 2;
	}


	private void calcTrapz(
			final float s0,
			final float v0,
			final float v1,
			final float s3,
			final float aMax
	)
	{
		float a1;
		float a3;
		float t1;
		float t2;
		float t3;
		float v2;
		float s1;
		float s2;

		if (v0 > v1)
		{
			a1 = -aMax;
		} else
		{
			a1 = aMax;
		}

		if (v1 > 0)
		{
			a3 = -aMax;
		} else
		{
			a3 = aMax;
		}

		t1 = (v1 - v0) / a1;
		v2 = v1;
		t3 = -v2 / a3;

		s1 = s0 + (0.5f * (v0 + v1) * t1);
		s2 = s3 - (0.5f * v2 * t3);
		t2 = (s2 - s1) / v1;

		parts[0].tEnd = t1;
		parts[0].acc = a1;
		parts[0].v0 = v0;
		parts[0].s0 = s0;
		parts[1].tEnd = t1 + t2;
		parts[1].acc = 0;
		parts[1].v0 = v1;
		parts[1].s0 = s1;
		parts[2].tEnd = t1 + t2 + t3;
		parts[2].acc = a3;
		parts[2].v0 = v2;
		parts[2].s0 = s2;
		numParts = 3;
	}
}
