/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class HedgehogPointCalc implements IPointOnLine
{
	
	@Override
	public DefensePoint getPointOnLine(final DefensePoint defPoint, final MetisAiFrame frame, final DefenderRole defender)
	{
		FoeBotData foeBotData = defPoint.getFoeBotData();
		
		if (null == foeBotData)
		{
			return defPoint;
		}
		
		return new DefensePoint(foeBotData.getBot2goalNearestToGoal(), foeBotData);
	}
	
}
