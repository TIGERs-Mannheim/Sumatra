/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;


/**
 * Calculates a defense point which interrupts an pass path for the enemy team.
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class PassIntersectionPointCalc implements IPointOnLine
{
	
	@Override
	public DefensePoint getPointOnLine(final DefensePoint defPoint, final MetisAiFrame frame, final DefenderRole defender)
	{
		FoeBotData foeBotData = defPoint.getFoeBotData();
		if (foeBotData == null)
		{
			return defPoint;
		}
		List<IVector2> passIntersections = new ArrayList<IVector2>();
		for (IVector2 vec : foeBotData.getBot2goalIntersecsBot2bot())
		{
			passIntersections.add(vec);
		}
		IVector2 newDefPoint = null;
		
		if (passIntersections.isEmpty())
		{
			return defPoint;
		}
		
		newDefPoint = GeoMath.nearestPointInList(passIntersections, defPoint);
		return new DefensePoint(newDefPoint, foeBotData);
	}
}
