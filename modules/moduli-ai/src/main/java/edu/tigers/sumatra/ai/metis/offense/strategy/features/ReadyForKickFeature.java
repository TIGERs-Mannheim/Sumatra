/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;


/**
 * MarkG
 */
public class ReadyForKickFeature extends AOffensiveStrategyFeature
{
	/**
	 * checks if offensive is ready for kick
	 */
	public ReadyForKickFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		ITrackedBot primaryBot = tempInfo.getPrimaryBot();
		if (primaryBot == null)
		{
			return;
		}
		
		DrawableAnnotation dt = new DrawableAnnotation(primaryBot.getPos().addNew(Vector2.fromXY(170, 150)), "Ready!",
				Color.RED);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dt);
		if (newTacticalField.getOffensiveActions().get(primaryBot.getBotId()) != null)
		{
			newTacticalField.getOffensiveActions().get(primaryBot.getBotId()).setRoleReadyToKick(true);
		}
	}
}
