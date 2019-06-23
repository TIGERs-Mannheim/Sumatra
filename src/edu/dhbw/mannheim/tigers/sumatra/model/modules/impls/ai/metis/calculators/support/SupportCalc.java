/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.04.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support.ASupportPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support.ESupportPosition;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Calls the configured implementation of ASupportPosition to estimate new support positions
 * 
 * @author JulianT
 */
public class SupportCalc extends ACalculator
{
	@Configurable(comment = "Implementation of ASupportPosition to use")
	private static ESupportPosition	supportPositionConfig	= ESupportPosition.RANDOM;
	
	private ASupportPosition			supportPosition;
	
	
	/**
	 * Use the selected implementation of ASupportPosition
	 */
	public SupportCalc()
	{
		super();
		supportPosition = supportPositionConfig.getSupportPosition();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		Map<BotID, IVector2> newSupportPositions = new HashMap<BotID, IVector2>();
		
		for (TrackedTigerBot bot : baseAiFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			newSupportPositions.put(bot.getId(),
					supportPosition.getNewPosition(bot.getId(), newTacticalField, baseAiFrame));
		}
		
		newTacticalField.setSupportPositions(newSupportPositions);
	}
}
