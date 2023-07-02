/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.OffensiveZones;
import edu.tigers.sumatra.geometry.Geometry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OffensiveZonesCalc extends ACalculator
{

	private OffensiveZones.OffensiveZoneGeometry oldGeometry;

	@Getter
	private OffensiveZones offensiveZones;


	@Override
	protected void reset()
	{
		oldGeometry = null;
	}


	@Override
	protected void doCalc()
	{
		OffensiveZones.OffensiveZoneGeometry geometry = new OffensiveZones.OffensiveZoneGeometry(Geometry.getFieldWidth(),
				Geometry.getFieldLength(), Geometry.getPenaltyAreaTheir().getPosCorner(),
				Geometry.getPenaltyAreaTheir().getNegCorner());
		if (oldGeometry == null || !oldGeometry.equals(geometry))
		{
			offensiveZones = OffensiveZones.generateDefaultOffensiveZones(geometry);
			oldGeometry = geometry;
		}
	}
}
