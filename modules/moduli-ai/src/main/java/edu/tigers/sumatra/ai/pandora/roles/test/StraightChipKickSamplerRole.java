/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author AndreR
 */
public class StraightChipKickSamplerRole extends AKickSamplerRole
{
	/**
	 * @param halfField
	 * @param chipFromSide
	 * @param minDurationMs
	 * @param maxDurationMs
	 * @param numSamples
	 * @param cont
	 * @param doChip
	 * @param doStraight
	 */
	@SuppressWarnings("squid:S00107") // constructor is used by UI to feed parameters into role
	public StraightChipKickSamplerRole(final boolean halfField, final boolean chipFromSide, final double minDurationMs,
			final double maxDurationMs, final int numSamples, final boolean cont, final boolean doChip,
			final boolean doStraight)
	{
		super(ERole.STRAIGHT_CHIP_KICK_SAMPLER, halfField);
		
		if (cont && !samples.isEmpty())
		{
			return;
		}
		
		samples.clear();
		
		double step = (maxDurationMs - minDurationMs) / numSamples;
		
		IVector2 kickTarget;
		if (halfField)
		{
			kickTarget = Vector2.fromXY(0, -Geometry.getFieldWidth() / 2);
		} else
		{
			kickTarget = Vector2.fromXY(Geometry.getFieldLength() / 2, -Geometry.getFieldWidth() / 2);
		}
		
		if (doStraight)
		{
			// create straight sample points
			for (double dur = minDurationMs; dur <= maxDurationMs; dur += step)
			{
				SamplePoint p = new SamplePoint();
				
				p.kickPos = Vector2.fromXY((-Geometry.getFieldLength() / 2) + 200, (Geometry.getFieldWidth() / 2) - 200);
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
			for (double dur = minDurationMs; dur <= maxDurationMs; dur += step)
			{
				SamplePoint p = new SamplePoint();
				
				if (chipFromSide)
				{
					p.kickPos = Vector2.fromXY(0, (-Geometry.getFieldWidth() / 2) + 200);
					p.targetAngle = AngleMath.PI_HALF;
				} else
				{
					p.kickPos = Vector2.fromXY((-Geometry.getFieldLength() / 2) + 200, 0);
					p.targetAngle = 0;
				}
				
				p.durationMs = dur;
				p.device = EKickerDevice.CHIP;
				p.rightOffset = 0;
				
				samples.add(p);
			}
		}
	}
	
	
	@Override
	protected String getFolderName()
	{
		return "ballKick";
	}
}
