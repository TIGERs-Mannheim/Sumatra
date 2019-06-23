/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 19, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.DriveOnLinePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ReceiverBlockPointCalc implements IPointOnLine
{
	
	
	@Configurable(comment = "Distance of the defense point to the enemies bot")
	private static double defendDistance = 3 * Geometry.getBotRadius();
	
	
	static
	{
		ConfigRegistration.registerClass("defensive", ReceiverBlockPointCalc.class);
	}
	
	
	@Override
	public DefensePoint getPointOnLine(final DefensePoint defPoint, final MetisAiFrame frame,
			final DefenderRole curDefender)
	{
		FoeBotData foeBotData = defPoint.getFoeBotData();
		
		if (Circle.getNewCircle(defPoint.getFoeBotData().getFoeBot().getPos(), DriveOnLinePointCalc.nearEnemyBotDist)
				.isPointInShape(curDefender.getPos()))
		{
			
			return new DefensePoint(foeBotData.getBall2botNearestToBot(), foeBotData);
		}
		
		return new DefensePoint(foeBotData.getBot2goalNearestToBot(), foeBotData);
	}
	
}
