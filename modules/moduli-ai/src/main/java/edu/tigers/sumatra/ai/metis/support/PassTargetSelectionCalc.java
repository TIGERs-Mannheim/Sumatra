/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.ai.metis.support.PassTargetGenerationCalc.getMaxPassTargetsPerBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class select the best Pass Targets, which are prior rated by PassTargetRatingCalc
 */
public class PassTargetSelectionCalc extends ACalculator
{
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IPassTarget> selectedPassTargets = new ArrayList<>();
		newTacticalField.getAllPassTargets().stream()
				.sorted()
				.filter(passTarget -> selectedPassTargets.stream()
						.noneMatch(pt -> pt.isSimilarTo(passTarget) && pt.getBotId() == passTarget.getBotId()))
				.filter(passTarget -> selectedPassTargets.stream()
						.filter(pt -> pt.getBotId().equals(passTarget.getBotId()))
						.count() < getMaxPassTargetsPerBot())
				.forEach(selectedPassTargets::add);
		selectedPassTargets.sort(Comparable::compareTo);
		newTacticalField.setPassTargetsRanked(selectedPassTargets);
		drawPassTargets(selectedPassTargets, newTacticalField);
	}
	
	
	private void drawPassTargets(final List<IPassTarget> passTargets, TacticalField newTacticalField)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS);
		List<IDrawableShape> shapesDebug = newTacticalField.getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS_DEBUG);
		
		Color pink = new Color(255, 0, 170, 100);
		Color magenta = new Color(255, 120, 100, 120);
		Color blue = new Color(20, 20, 220, 150);
		
		for (IPassTarget target : passTargets)
		{
			BotID botId = target.getBotId();
			ITrackedBot bot = getWFrame().getTiger(botId);
			if (bot != null)
			{
				IVector2 kickerPos = bot.getBotKickerPos();
				
				if (!kickerPos.equals(target.getKickerPos()))
				{
					shapes.add(
							new DrawableLine(Line.fromPoints(kickerPos, target.getKickerPos()), new Color(55, 55, 55, 70)));
				}
			}
		}
		
		List<BotID> seenBots = new ArrayList<>();
		int i = 1;
		for (IPassTarget target : passTargets)
		{
			BotID botId = target.getBotId();
			
			final Color color;
			if (i == 1)
			{
				seenBots.add(botId);
				color = blue;
			} else
			{
				if (!seenBots.contains(botId))
				{
					seenBots.add(botId);
					color = pink;
				} else
				{
					color = magenta;
				}
			}
			
			DrawableCircle dTargetCircle = new DrawableCircle(target.getKickerPos(), 30, color);
			dTargetCircle.setFill(true);
			shapes.add(dTargetCircle);
			
			DrawableAnnotation dTxti = new DrawableAnnotation(target.getKickerPos(), Integer.toString(i), Color.black);
			dTxti.setFontHeight(30);
			dTxti.setCenterHorizontally(true);
			shapes.add(dTxti);
			
			DrawableAnnotation dTxtValue = new DrawableAnnotation(target.getKickerPos(),
					Long.toString(Math.round(target.getScore() * 1000)),
					Color.black);
			dTxtValue.setFontHeight(10);
			dTxtValue.setCenterHorizontally(true);
			dTxtValue.setOffset(Vector2.fromXY(0, 20));
			shapes.add(dTxtValue);
			
			String scores = target.getIntermediateScores().stream()
					.map(score -> String.valueOf(Math.round(score * 100.0)))
					.collect(Collectors.joining("|"));
			DrawableAnnotation dTxtScores = new DrawableAnnotation(target.getKickerPos(),
					scores,
					Color.black);
			dTxtScores.setFontHeight(10);
			dTxtScores.setCenterHorizontally(true);
			dTxtScores.setOffset(Vector2.fromXY(0, 35));
			shapesDebug.add(dTxtScores);
			
			i++;
		}
	}
}
