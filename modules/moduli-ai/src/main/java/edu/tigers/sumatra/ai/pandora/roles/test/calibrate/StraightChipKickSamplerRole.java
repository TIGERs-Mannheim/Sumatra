/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.calibrate;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Sample straight and chip kicks for Ball Calibration.
 */
public class StraightChipKickSamplerRole extends AKickSamplerRole
{

	@SuppressWarnings({ "squid:S00107", "unused" }) // constructor is used by UI to feed parameters into role
	public StraightChipKickSamplerRole(
			final boolean onlyOurHalf,
			final boolean chipFromSide,
			final IVector2 kickCorner,
			final double minDurationMs,
			final double maxDurationMs,
			final double stepSize,
			final boolean continueSampling,
			final boolean doChip,
			final boolean doStraight)
	{
		super(ERole.STRAIGHT_CHIP_KICK_SAMPLER, onlyOurHalf);

		if (continueSampling && !samples.isEmpty())
		{
			return;
		}

		samples.clear();

		if (doStraight)
		{
			IVector2 kickTarget;
			if (onlyOurHalf)
			{
				kickTarget = Vector2.fromXY(
						-Geometry.getFieldWidth() / 2,
						-Geometry.getFieldLength() / 2
				);
			} else
			{
				kickTarget = Vector2.fromXY(
						-Math.signum(kickCorner.x()) * Geometry.getFieldLength() / 2,
						-Math.signum(kickCorner.y()) * Geometry.getFieldWidth() / 2
				);
			}

			// create straight sample points
			for (double dur = minDurationMs; dur <= maxDurationMs; dur += stepSize)
			{
				SamplePoint p = new SamplePoint();

				p.kickPos = Vector2.fromXY(
						Math.signum(kickCorner.x()) * (Geometry.getFieldLength() / 2 - 200),
						Math.signum(kickCorner.y()) * (Geometry.getFieldWidth() / 2 - 200)
				);
				p.targetAngle = kickTarget.subtractNew(p.kickPos).getAngle();
				p.durationMs = dur;
				p.device = EKickerDevice.STRAIGHT;
				p.rightOffset = 0;

				samples.add(p);
			}
		}

		if (doChip)
		{
			// create chip sample points
			for (double dur = minDurationMs; dur <= maxDurationMs; dur += stepSize)
			{
				SamplePoint p = new SamplePoint();

				if (chipFromSide)
				{
					p.kickPos = Vector2.fromXY(0, (-Geometry.getFieldWidth() / 2) + 600);
					p.targetAngle = AngleMath.PI_HALF;
				} else
				{
					p.kickPos = Vector2.fromXY((-Geometry.getFieldLength() / 2) + 600, 0);
					p.targetAngle = 0;
				}

				p.durationMs = dur;
				p.device = EKickerDevice.CHIP;
				p.rightOffset = 0;

				samples.add(p);
			}
		}
	}


	public StraightChipKickSamplerRole(
			boolean onlyOurHalf,
			EKickerDevice kickerDevice,
			IVector2 kickPos,
			IVector2 kickTarget,
			double shootDurationMs)
	{
		super(ERole.STRAIGHT_CHIP_KICK_SAMPLER, onlyOurHalf);

		var sample = new SamplePoint();
		sample.device = kickerDevice;
		sample.kickPos = kickPos;
		sample.targetAngle = Vector2.fromPoints(kickPos, kickTarget).getAngle();
		sample.durationMs = shootDurationMs;
		sample.rightOffset = 0;

		samples.add(sample);
	}


	@Override
	protected String getFolderName()
	{
		return "ballKick";
	}
}
