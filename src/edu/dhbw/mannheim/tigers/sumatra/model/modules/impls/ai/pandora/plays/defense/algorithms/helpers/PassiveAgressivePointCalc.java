/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;


/**
 * Calculates a defense point which is as near to the covered bot as possible.
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class PassiveAgressivePointCalc implements IPointOnLine
{
	
	@Override
	public DefensePoint getPointOnLine(final DefensePoint defPoint, final MetisAiFrame frame, final DefenderRole defender)
	{
		FoeBotData foeBotData = defPoint.getFoeBotData();
		
		if (null == foeBotData)
		{
			return defPoint;
		}
		
		return new DefensePoint(foeBotData.getBot2goalNearestToBot(), foeBotData);
	}
	
}
