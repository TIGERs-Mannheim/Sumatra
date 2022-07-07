/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.RequiredArgsConstructor;

import java.util.Collection;


@RequiredArgsConstructor
public class FreeSpaceRater implements IPassRater
{
	@Configurable(comment = "[mm]", defValue = "6000.")
	private double maxDistance = 6000;

	static
	{
		ConfigRegistration.registerClass("metis", FreeSpaceRater.class);
	}

	private final Collection<ITrackedBot> consideredBots;


	@Override
	public double rate(Pass pass)
	{
		Double minDistance = consideredBots.stream()
				.map(ITrackedObject::getPos)
				.map(p -> p.distanceTo(pass.getKick().getTarget()))
				.min(Double::compareTo).orElse(0.);

		return SumatraMath.relative(minDistance, 0, maxDistance);
	}
}
