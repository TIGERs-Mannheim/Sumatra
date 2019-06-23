/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;


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
