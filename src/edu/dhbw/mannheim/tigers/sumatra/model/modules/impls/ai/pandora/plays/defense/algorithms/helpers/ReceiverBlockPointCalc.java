/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 19, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.DriveOnLinePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ReceiverBlockPointCalc implements IPointOnLine
{
	@Configurable(comment = "Distance of the defense point to the enemies bot")
	private static float	defendDistance	= 3 * AIConfig.getGeometry().getBotRadius();
	
	
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
