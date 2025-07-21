/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.calibrate;

import edu.tigers.sumatra.ai.pandora.plays.test.ballmodel.EFieldSide;
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
			EFieldSide fieldSide,
			EKickerDevice kickerDevice,
			double shootDurationMs,
			EKickMode kickMode,
			IVector2 kickCorner,
			boolean chipFromSide
			)
	{
		super(ERole.STRAIGHT_CHIP_KICK_SAMPLER, kickMode);

		samples.clear();

		SamplePoint p = new SamplePoint();
		p.rightOffset = 0;
		p.device = kickerDevice;
		p.durationMs = shootDurationMs;

		if (kickerDevice == EKickerDevice.STRAIGHT)
		{
			IVector2 kickTarget;
			if (fieldSide != EFieldSide.BOTH)
			{
				kickTarget = Vector2.fromXY(
						fieldSide.getSign() * Geometry.getFieldWidth() / 2,
						-Geometry.getFieldLength() / 2
				);
			} else
			{
				kickTarget = Vector2.fromXY(
						-Math.signum(kickCorner.x()) * Geometry.getFieldLength() / 2,
						-Math.signum(kickCorner.y()) * Geometry.getFieldWidth() / 2
				);
			}

			p.kickPos = Vector2.fromXY(
					Math.signum(kickCorner.x()) * (Geometry.getFieldLength() / 2 - 200),
					Math.signum(kickCorner.y()) * (Geometry.getFieldWidth() / 2 - 200)
			);
			p.targetAngle = kickTarget.subtractNew(p.kickPos).getAngle();

		} else
		{

			if (chipFromSide)
			{
				p.kickPos = Vector2.fromXY(0, (-Geometry.getFieldWidth() / 2) + 600);
				p.targetAngle = AngleMath.PI_HALF;
			} else
			{
				p.kickPos = Vector2.fromXY((-Geometry.getFieldLength() / 2) + 600, 0);
				p.targetAngle = 0;
			}
		}

		samples.add(p);
	}

	public StraightChipKickSamplerRole(
			EKickerDevice kickerDevice,
			IVector2 kickPos,
			IVector2 kickTarget,
			double shootDurationMs,
			EKickMode kickMode
	)
	{
		super(ERole.STRAIGHT_CHIP_KICK_SAMPLER, kickMode);

		var sample = new SamplePoint();
		sample.device = kickerDevice;
		sample.kickPos = kickPos;
		sample.targetAngle = Vector2.fromPoints(kickPos, kickTarget).getAngle();
		sample.durationMs = shootDurationMs;
		sample.rightOffset = 0;

		samples.add(sample);
	}
}
