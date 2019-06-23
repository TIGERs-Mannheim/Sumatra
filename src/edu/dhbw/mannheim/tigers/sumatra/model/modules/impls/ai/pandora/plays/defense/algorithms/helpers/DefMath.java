/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class DefMath
{
	
	private static final Logger	log	= Logger.getLogger(DefMath.class.getName());
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param curFoeBot
	 * @param ourBotPos
	 * @return
	 */
	public static IVector2 calcNearestDefPoint(final FoeBotData curFoeBot, final IVector2 ourBotPos)
	{
		IVector2 nearestPotentialDefPoint = null;
		try
		{
			nearestPotentialDefPoint = GeoMath.nearestPointOnLineSegment(
					curFoeBot.getBot2goalNearestToGoal(),
					curFoeBot.getBot2goalNearestToBot(), ourBotPos);
		} catch (MathException err1)
		{
			log.error("Math exception", err1);
			return curFoeBot.getBot2goalNearestToGoal(); // use a backup solution
		}
		
		return moveDefPointInValidInterval(curFoeBot, nearestPotentialDefPoint);
	}
	
	
	private static IVector2 moveDefPointInValidInterval(final FoeBotData foeBotData, final IVector2 defPoint)
	{
		
		if (GeoMath.isVectorBetween(defPoint, foeBotData.getBot2goalNearestToGoal(),
				foeBotData.getBot2goalNearestToBot()))
		{
			return defPoint;
		}
		
		if (defPoint.subtractNew(foeBotData.getBot2goalNearestToGoal()).getLength2() < defPoint
				.subtractNew(foeBotData.getBot2goalNearestToBot()).getLength2())
		{
			return foeBotData.getBot2goalNearestToGoal();
		}
		
		return foeBotData.getBot2goalNearestToBot();
	}
	
}
