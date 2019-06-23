/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
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
	@Configurable(defValue = "5", comment = "How many pass targets to select per bot")
	private static int maxPassTargetsPerBot = 5;
	
	
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
						.count() < maxPassTargetsPerBot)
				.forEach(selectedPassTargets::add);
		selectedPassTargets.sort(Comparable::compareTo);
		newTacticalField.setPassTargetsRanked(selectedPassTargets);
		drawPassTargets(selectedPassTargets);
	}
	
	
	private void drawPassTargets(final List<IPassTarget> passTargets)
	{
		Color pink = new Color(255, 0, 170, 100);
		Color magenta = new Color(255, 120, 100, 120);
		Color blue = new Color(20, 20, 220, 150);
		
		passTargets.forEach(this::drawLinesToPassTargets);
		
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
			getShapes().add(dTargetCircle);
			
			DrawableAnnotation dNumber = new DrawableAnnotation(target.getKickerPos(), Integer.toString(i), Color.black);
			dNumber.withFontHeight(30);
			dNumber.withCenterHorizontally(true);
			getShapes().add(dNumber);
			
			DrawableAnnotation dScore = new DrawableAnnotation(target.getKickerPos(),
					scoreToStr(target.getScore()),
					Color.black);
			dScore.withFontHeight(10);
			dScore.withCenterHorizontally(true);
			dScore.withOffset(Vector2.fromXY(0, -20));
			getShapes().add(dScore);
			
			DrawableAnnotation dPassGoalKickScore = new DrawableAnnotation(target.getKickerPos(),
					scoreToStr(target.getPassScore()) + "|" + scoreToStr(target.getGoalKickScore()),
					Color.black);
			dPassGoalKickScore.withFontHeight(10);
			dPassGoalKickScore.withCenterHorizontally(true);
			dPassGoalKickScore.withOffset(Vector2.fromXY(0, 20));
			getShapes().add(dPassGoalKickScore);
			
			i++;
		}
	}
	
	
	private String scoreToStr(final double passScore)
	{
		return Long.toString(Math.round(passScore * 1000));
	}
	
	
	private List<IDrawableShape> getShapes()
	{
		return getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS);
	}
	
	
	private void drawLinesToPassTargets(final IPassTarget target)
	{
		ITrackedBot bot = getWFrame().getTiger(target.getBotId());
		IVector2 kickerPos = bot.getBotKickerPos();
		
		if (!kickerPos.equals(target.getKickerPos()))
		{
			getShapes().add(
					new DrawableLine(Line.fromPoints(kickerPos, target.getKickerPos()), new Color(55, 55, 55, 70)));
		}
	}
}
