/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.support.passtarget.EScoreMode;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.RatedPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.RatedPassTargetNoScore;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class selects the best Pass Targets and chooses the right score mode
 */
public class PassTargetSelectionCalc extends ACalculator
{
	@Configurable(defValue = "5", comment = "How many pass targets to select per bot")
	private static int maxPassTargetsPerBot = 5;
	
	@Configurable(defValue = "0.4", comment = "PassScore threshold in which selection mode is changed to pressure")
	private static double pressureScoreThreshold = 0.4;
	
	@Configurable(defValue = "0.15", comment = "Reflector threshold above which reflector scores are accepted")
	private static double reflectorScoreThreshold = 0.15;
	
	@Configurable(defValue = "15")
	private static int minPressureScoreTargetFilter = 15;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final List<RatedPassTargetNoScore> positiveGoalScorePassTargets = getNewTacticalField()
				.getAllRatedPassTargetsNoScore()
				.stream()
				.filter(e -> e.getPassTargetRating().getGoalKickScore() > reflectorScoreThreshold)
				.sorted(byGoalKickScore())
				.collect(Collectors.toList());
		
		if (positiveGoalScorePassTargets.isEmpty())
		{
			final List<RatedPassTargetNoScore> passTargets = getNewTacticalField().getAllRatedPassTargetsNoScore().stream()
					.sorted(byPassScore())
					.collect(Collectors.toList());
			
			final List<RatedPassTargetNoScore> selectedTargetsSortedByPassScore = bestPassTargetsForEachBot(passTargets)
					.stream()
					.sorted(byPassScore())
					.collect(Collectors.toList());
			
			if (!selectedTargetsSortedByPassScore.isEmpty()
					&& selectedTargetsSortedByPassScore.get(0).getPassTargetRating().getPassScore() > pressureScoreThreshold)
			{
				// sort by pressure score
				final List<RatedPassTargetNoScore> lowerThresh = selectedTargetsSortedByPassScore.stream()
						.filter(e -> e.getPassTargetRating().getPassScore() <= pressureScoreThreshold)
						.sorted(byPressureScore())
						.collect(Collectors.toList());
				
				final List<RatedPassTargetNoScore> sortedByPressureScoreFiltered = selectedTargetsSortedByPassScore.stream()
						.filter(e -> e.getPassTargetRating().getPassScore() > pressureScoreThreshold)
						.sorted(byPressureScore())
						.collect(Collectors.toList());
				
				while (sortedByPressureScoreFiltered.size() < minPressureScoreTargetFilter && !lowerThresh.isEmpty())
				{
					sortedByPressureScoreFiltered.add(lowerThresh.remove(0));
				}
				
				final List<IRatedPassTarget> finalPassTargets = sortedByPressureScoreFiltered.stream()
						.map(e -> new RatedPassTarget(e, EScoreMode.SCORE_BY_PASS))
						.collect(Collectors.toList());
				getNewTacticalField().setRatedPassTargetsRanked(finalPassTargets);
				drawPassTargets(finalPassTargets, 125, 125);
			} else
			{
				// sort by Pass Score
				final List<IRatedPassTarget> finalPassTargets = selectedTargetsSortedByPassScore.stream()
						.map(e -> new RatedPassTarget(e, EScoreMode.SCORE_BY_PASS))
						.collect(Collectors.toList());
				getNewTacticalField().setRatedPassTargetsRanked(finalPassTargets);
				drawPassTargets(finalPassTargets, 0, 0);
			}
		} else
		{
			// sort by goal Score
			final List<RatedPassTargetNoScore> selectedPassTargets = bestPassTargetsForEachBot(
					positiveGoalScorePassTargets)
							.stream()
							.sorted(byGoalKickScore())
							.collect(Collectors.toList());
			
			final List<IRatedPassTarget> finalPassTargets = selectedPassTargets.stream()
					.map(e -> new RatedPassTarget(e, EScoreMode.SCORE_BY_GOAL_KICK))
					.collect(Collectors.toList());
			getNewTacticalField().setRatedPassTargetsRanked(finalPassTargets);
			drawPassTargets(finalPassTargets, 255, 0);
		}
	}
	
	
	private Comparator<RatedPassTargetNoScore> byPressureScore()
	{
		return Comparator.comparingDouble((RatedPassTargetNoScore rt) -> rt.getPassTargetRating().getPressureScore())
				.reversed();
	}
	
	
	private Comparator<RatedPassTargetNoScore> byPassScore()
	{
		return Comparator.comparingDouble((RatedPassTargetNoScore rt) -> rt.getPassTargetRating().getPassScore())
				.reversed();
	}
	
	
	private Comparator<RatedPassTargetNoScore> byGoalKickScore()
	{
		return Comparator.comparingDouble((RatedPassTargetNoScore rt) -> rt.getPassTargetRating().getGoalKickScore())
				.reversed();
	}
	
	
	private List<RatedPassTargetNoScore> bestPassTargetsForEachBot(List<RatedPassTargetNoScore> filteredPassTargets)
	{
		Map<BotID, List<RatedPassTargetNoScore>> passTargetBotMap = new HashMap<>();
		for (RatedPassTargetNoScore pt : filteredPassTargets)
		{
			final List<RatedPassTargetNoScore> botPassTargets = passTargetBotMap.computeIfAbsent(pt.getBotId(),
					id -> new ArrayList<>(maxPassTargetsPerBot));
			if (botPassTargets.size() < maxPassTargetsPerBot)
			{
				botPassTargets.add(pt);
			}
		}
		return passTargetBotMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
	}
	
	
	private void drawPassTargets(final List<IRatedPassTarget> passTargets, final int colorRed, final int colorGreen)
	{
		Color pink = new Color(colorRed, colorGreen, 170, 130);
		Color magenta = new Color(colorRed, colorGreen + 120, 100, 150);
		Color blue = new Color(colorRed, colorGreen + 20, 220, 180);
		
		passTargets.forEach(this::drawLinesToPassTargets);
		
		Set<BotID> seenBots = new HashSet<>();
		int i = 1;
		for (IRatedPassTarget target : passTargets)
		{
			BotID botId = target.getBotId();
			
			final Color color;
			if (i == 1)
			{
				color = blue;
			} else if (!seenBots.contains(botId))
			{
				color = pink;
			} else
			{
				color = magenta;
			}
			seenBots.add(botId);
			
			DrawableCircle dTargetCircle = new DrawableCircle(target.getPos(), 30, color);
			dTargetCircle.setFill(true);
			getShapes().add(dTargetCircle);
			
			DrawableAnnotation dNumber = new DrawableAnnotation(target.getPos(), Integer.toString(i), Color.black);
			dNumber.withFontHeight(30);
			dNumber.withCenterHorizontally(true);
			getShapes().add(dNumber);
			
			DrawableAnnotation dScore = new DrawableAnnotation(target.getPos(),
					"s:" + scoreToStr(target.getScore()), Color.black);
			dScore.withFontHeight(10);
			dScore.withCenterHorizontally(true);
			dScore.withOffset(Vector2.fromXY(0, -20));
			getShapes().add(dScore);
			
			i++;
		}
	}
	
	
	private String scoreToStr(final double passScore)
	{
		return Long.toString(Math.round(passScore * 100));
	}
	
	
	private List<IDrawableShape> getShapes()
	{
		return getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS);
	}
	
	
	private void drawLinesToPassTargets(final IPassTarget target)
	{
		ITrackedBot bot = getWFrame().getTiger(target.getBotId());
		IVector2 kickerPos = bot.getBotKickerPos();
		
		if (!kickerPos.equals(target.getPos()))
		{
			getShapes().add(
					new DrawableLine(Line.fromPoints(kickerPos, target.getPos()), new Color(55, 55, 55, 70)));
		}
	}
}
