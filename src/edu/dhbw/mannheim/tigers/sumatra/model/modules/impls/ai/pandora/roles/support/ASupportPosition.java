/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.02.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * Interface to inject position estimation methods for SupportRole
 * 
 * @author JulianT
 */
public abstract class ASupportPosition
{
	protected Map<BotID, IVector2>	supportPositions;
	
	
	protected ASupportPosition()
	{
		supportPositions = new HashMap<BotID, IVector2>();
	}
	
	
	/**
	 * Calls estimateNewPosition to estimate a new position
	 * 
	 * @param bot The bot in need of a new position
	 * @param newTacticalField The new TacticalField from Metis
	 * @param aiFrame Current AthenaAiFrame
	 * @return A new position as IVector2 != null
	 */
	public IVector2 getNewPosition(final BotID bot, final TacticalField newTacticalField, final BaseAiFrame aiFrame)
	{
		IVector2 newPosition;
		
		if (isSupporter(bot, aiFrame))
		{
			newPosition = estimateNewPosition(bot, newTacticalField, aiFrame);
		} else
		{
			newPosition = aiFrame.getWorldFrame().getTiger(bot).getPos();
		}
		
		if (newPosition == null)
		{
			newPosition = aiFrame.getWorldFrame().getTiger(bot).getPos();
		}
		
		supportPositions.put(bot, newPosition);
		
		return newPosition;
	}
	
	
	/**
	 * Implement this method to return a new position for the support bot
	 * 
	 * @param bot The bot in need of a new position
	 * @param aiFrame Current AthenaAiFrame
	 * @return A new position as IVector2
	 */
	protected abstract IVector2 estimateNewPosition(final BotID bot, final TacticalField newTacticalField,
			final BaseAiFrame aiFrame);
	
	
	protected boolean isSupporter(final BotID bot, final BaseAiFrame aiFrame)
	{
		return aiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORT).stream()
				.anyMatch(role -> bot.equals(role.getBotID()));
	}
	
	
	// protected boolean isBalanced(final IVector2 position, final BaseAiFrame aiFrame)
	// {
	// int yBalance = 0;
	//
	// for (ARole role : aiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORT))
	// {
	// if (((SupportRole) role).getDestination() != null)
	// {
	// yBalance += ((SupportRole) role).getDestination().y() < 0 ? -1 : 1;
	// }
	// }
	//
	// boolean positiveNeeded = yBalance <= 0;
	//
	// return (yBalance == 0) || (positiveNeeded && (position.y() >= 0)) || (!positiveNeeded && (position.y() < 0));
	// }
}
