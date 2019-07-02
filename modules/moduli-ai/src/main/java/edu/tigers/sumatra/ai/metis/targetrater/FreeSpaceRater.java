/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.util.Collection;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;


public class FreeSpaceRater implements IPassRater
{
	
	@Configurable(comment = "[mm]", defValue = "6000.")
	private double maxDistance = 6000;
	
	
	private final Collection<ITrackedBot> consideredBots;
	
	
	public FreeSpaceRater(Collection<ITrackedBot> consideredBots)
	{
		this.consideredBots = consideredBots;
	}
	
	
	@Override
	public double rateStraightPass(final IVector2 passOrigin, final IVector2 passTarget)
	{
		return rate(passTarget);
	}
	
	
	private double rate(IVector2 passTarget)
	{
		Double minDistance = consideredBots.stream()
				.map(ITrackedObject::getPos)
				.map(p -> p.distanceTo(passTarget))
				.min(Double::compareTo).orElse(0.);
		
		return SumatraMath.relative(minDistance, 0, maxDistance);
	}
	
	
	@Override
	public double rateChippedPass(final IVector2 passOrigin, final IVector2 passTarget, final double maxChipSpeed)
	{
		return rate(passTarget);
	}
}
