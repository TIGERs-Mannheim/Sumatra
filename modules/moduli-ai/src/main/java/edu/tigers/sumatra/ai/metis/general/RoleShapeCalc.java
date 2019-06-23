/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Generates a shape, which represent the roles as colors on the bot
 */
public class RoleShapeCalc extends ACalculator
{
	
	private EnumMap<EPlay, Color> colorMap;
	
	
	/**
	 * Default
	 */
	public RoleShapeCalc()
	{
		int alpha = 200;
		colorMap = new EnumMap<>(EPlay.class);
		colorMap.put(EPlay.DEFENSIVE, new Color(108, 209, 255, alpha));
		colorMap.put(EPlay.KEEPER, new Color(255, 0, 218, alpha));
		colorMap.put(EPlay.OFFENSIVE, new Color(255, 50, 8, alpha));
		colorMap.put(EPlay.SUPPORT, new Color(80, 80, 80, alpha));
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> roleColorShapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.ROLE_COLOR);
		for (EPlay play : newTacticalField.getDesiredBotMap().keySet())
		{
			Set<BotID> botIDS = newTacticalField.getDesiredBotMap().get(play);
			if (colorMap.containsKey(play))
			{
				for (BotID bot : botIDS)
				{
					ITrackedBot tBot = getWFrame().getBot(bot);
					DrawableCircle botShape = new DrawableCircle(
							Circle.createCircle(tBot.getPos(), Geometry.getBotRadius() + 50), colorMap.get(play));
					botShape.setFill(true);
					roleColorShapes.add(botShape);
				}
			}
		}
	}
}
