/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author AndreR
 */
public class CrookedKickSamplerRole extends AKickSamplerRole
{
	/**
	 * @param kickPos
	 * @param kickOrientation
	 * @param durationMs
	 * @param offset
	 * @param numSamples
	 * @param cont
	 */
	public CrookedKickSamplerRole(final IVector2 kickPos, final double kickOrientation, final double durationMs,
			final double offset, final int numSamples, final boolean cont)
	{
		super(ERole.CROOKED_KICK_SAMPLER, false);
		
		if (cont && !samples.isEmpty())
		{
			return;
		}
		
		samples.clear();
		
		double step = (2 * offset) / (numSamples - 1);
		
		// create sample points
		for (double off = -offset; off <= offset; off += step)
		{
			SamplePoint p = new SamplePoint();
			
			p.kickPos = kickPos;
			p.targetAngle = kickOrientation;
			p.durationMs = durationMs;
			p.device = EKickerDevice.STRAIGHT;
			p.rightOffset = off;
			
			samples.add(p);
		}
	}
	
	
	@Override
	protected String getFolderName()
	{
		return "crookedKick";
	}
}
