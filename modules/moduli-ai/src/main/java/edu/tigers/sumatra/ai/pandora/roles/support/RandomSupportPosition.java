/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.02.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.support;

import java.util.Random;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Chooses new support positions by random.
 * 
 * @author JulianT
 */
public class RandomSupportPosition extends ASupportPosition
{
	
	
	@Configurable
	private static double			minDistanceToBall	= 500;
																	
	@Configurable(comment = "The upper part of our field where supporters are allowed to locate themselves")
	private static double			tigersFieldReach	= 0.5;
																	
	@Configurable(comment = "Maximum number of random generated positions")
	private static int				maxIterations		= 3000;
																	
	private static final Logger	log					= Logger.getLogger(RandomSupportPosition.class.getName());
																	
	private Random						rnd					= null;
																	
																	
	static
	{
		ConfigRegistration.registerClass("roles", RandomSupportPosition.class);
	}
	
	
	@Override
	protected IVector2 estimateNewPosition(final BotID botID, final TacticalField newTacticalField,
			final BaseAiFrame aiFrame)
	{
		if (rnd == null)
		{
			rnd = new Random(aiFrame.getSimpleWorldFrame().getTimestamp());
		}
		
		ITrackedBot bot = aiFrame.getWorldFrame().getTiger(botID);
		IVector2 newPosition;
		
		int i = 0;
		boolean closeToOurGoalline;
		boolean closeToOtherBots;
		boolean closeToBall;
		
		do
		{
			newPosition = Geometry.getField().getRandomPointInShape(rnd);
			
			// Keep distance to our goal
			closeToOurGoalline = newPosition.x() < (tigersFieldReach * -0.5 * Geometry.getFieldLength());
			
			// Keep distance to other bots
			closeToOtherBots = !AiMath.isShapeFreeOfBots(
					new Circle(newPosition, Geometry.getBotRadius() * 8),
					aiFrame
							.getWorldFrame().getBots(),
					bot);
					
			// Keep distance to ball
			closeToBall = GeoMath.distancePP(newPosition,
					aiFrame.getWorldFrame().getBall().getPos()) < (minDistanceToBall + (2 * Geometry.getBotRadius()));
					
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
