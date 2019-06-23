/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import java.awt.Color;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.animated.AnimatedCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author MarkG
 */
public class DrawingsFeature extends AOffensiveStrategyFeature
{
	/**
	 * Default
	 */
	public DrawingsFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final TemporaryOffensiveInformation tempInfo, final OffensiveStrategy strategy)
	{
		// Fancy drawings
		drawCrucialDefenders(newTacticalField, baseAiFrame);
		IVector2 primaryMovePos = baseAiFrame.getPrevFrame().getAICom().getPrimaryOffensiveMovePos();
		IVector2 secondaryMovePos = newTacticalField.getSupportiveAttackerMovePos();
		drawMovePositions(newTacticalField, primaryMovePos, "primary");
		drawMovePositions(newTacticalField, secondaryMovePos, "secondary");
	}
	
	
	private void drawMovePositions(final TacticalField newTacticalField, final IVector2 movePos, final String text)
	{
		if (movePos == null)
		{
			return;
		}
		DrawableCircle drawableCircle = new DrawableCircle(Circle.createCircle(movePos, 120),
				new Color(0, 204, 255, 118));
		drawableCircle.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_MOVE_POSITIONS).add(drawableCircle);
		DrawableAnnotation drawableAnnotation = new DrawableAnnotation(movePos, text, Color.CYAN);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_MOVE_POSITIONS).add(drawableAnnotation);
	}
	
	
	private void drawCrucialDefenders(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		for (BotID id : baseAiFrame.getPrevFrame().getTacticalField().getCrucialDefender())
		{
			ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(id);
			if (bot != null)
			{
				newTacticalField.getDrawableShapes().get(EAiShapesLayer.DEFENSE_CRUCIAL_DEFENDERS).add(
						AnimatedCircle.aFilledCircleWithShrinkingSize(bot.getPos(), 100, 150, 1.0f, new Color(125, 255, 50),
								new Color(125, 255, 50, 100)));
			}
		}
	}
	
}
