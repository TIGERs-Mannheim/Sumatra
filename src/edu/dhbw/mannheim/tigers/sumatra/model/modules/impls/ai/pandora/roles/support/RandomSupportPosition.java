/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.02.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Chooses new support positions by random.
 * 
 * @author JulianT
 */
public class RandomSupportPosition extends ASupportPosition
{
	@Configurable
	private static float				minDistanceToBall	= 500;
	
	@Configurable(comment = "The upper part of our field where supporters are allowed to locate themselves")
	private static float				tigersFieldReach	= 0.5f;
	
	@Configurable(comment = "Maximum number of random generated positions")
	private static int				maxIterations		= 3000;
	
	private static final Logger	log					= Logger.getLogger(RandomSupportPosition.class.getName());
	
	
	@Override
	protected IVector2 estimateNewPosition(final BotID botID, final TacticalField newTacticalField,
			final BaseAiFrame aiFrame)
	{
		TrackedTigerBot bot = aiFrame.getWorldFrame().getTiger(botID);
		IVector2 newPosition;
		
		int i = 0;
		boolean closeToOurGoalline;
		boolean closeToOtherBots;
		boolean closeToBall;
		
		do
		{
			newPosition = AIConfig.getGeometry().getField().getRandomPointInShape();
			
			// Keep distance to our goal
			closeToOurGoalline = newPosition.x() < (tigersFieldReach * -0.5 * AIConfig.getGeometry().getFieldLength());
			
			// Keep distance to other bots
			closeToOtherBots = !AiMath.isShapeFreeOfBots(
					new Circle(newPosition, AIConfig.getGeometry().getBotRadius() * 8),
					aiFrame
							.getWorldFrame().getBots(), bot);
			
			// Keep distance to ball
			closeToBall = GeoMath.distancePP(newPosition, aiFrame.getWorldFrame().getBall().getPos()) < (minDistanceToBall + (2 * AIConfig
					.getGeometry().getBotRadius()));
			
			i++;
			
			if (i > maxIterations)
			{
				log.error("Couldn't find support pos!");
				return null;
			}
		} while (closeToOtherBots
				|| closeToOurGoalline
				|| closeToBall
				|| !AiMath.isBalancedSupportPosition(newPosition, aiFrame)
				|| !LegalPointChecker.checkPoint(newPosition, aiFrame, newTacticalField));
		
		return newPosition;
	}
}
