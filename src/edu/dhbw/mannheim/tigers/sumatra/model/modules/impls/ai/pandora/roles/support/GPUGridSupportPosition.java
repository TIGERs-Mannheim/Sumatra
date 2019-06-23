/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.04.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Uses Nicolai's GPU grid to find new support positions.
 * 
 * @author JulianT
 */
public class GPUGridSupportPosition extends RandomSupportPosition
{
	
	@Override
	protected IVector2 estimateNewPosition(final BotID bot, final TacticalField newTacticalField,
			final BaseAiFrame aiFrame)
	{
		// TODO JulianT
		List<IVector2> top100 = newTacticalField.getTopGpuGridPositions();
		
		for (IVector2 newPosition : top100)
		{
			boolean isFree = (!supportPositions.containsValue(newPosition))
					&& AiMath.isShapeFreeOfBots(new Circle(newPosition, 180), aiFrame.getWorldFrame()
							.getTigerBotsAvailable(), aiFrame.getWorldFrame().getTiger(bot));
			boolean isLegal = LegalPointChecker.checkPoint(newPosition, aiFrame, newTacticalField);
			boolean balanced = AiMath.isBalancedSupportPosition(newPosition, aiFrame);
			
			if (isFree && balanced && isLegal)
			{
				return newPosition;
			}
		}
		
		// fall back to random if GPU grid doesn't deliver a good point...
		// TODO this isn't a very good solution, find something more useful
		return super.estimateNewPosition(bot, newTacticalField, aiFrame);
	}
}
