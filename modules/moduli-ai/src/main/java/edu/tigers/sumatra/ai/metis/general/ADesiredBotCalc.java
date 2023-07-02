/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Abstract base class for calculators that determine the desired robots per play.
 */
@RequiredArgsConstructor
public abstract class ADesiredBotCalc extends ACalculator
{
	private final EnumMap<EPlay, Color> colorMap = new EnumMap<>(EPlay.class);

	protected final Supplier<Map<EPlay, Set<BotID>>> desiredBotMap;


	@Override
	protected void start()
	{
		int alpha = 200;
		colorMap.put(EPlay.DEFENSIVE, new Color(108, 209, 255, alpha));
		colorMap.put(EPlay.KEEPER, new Color(255, 0, 218, alpha));
		colorMap.put(EPlay.OFFENSIVE, new Color(255, 50, 8, alpha));
		colorMap.put(EPlay.SUPPORT, new Color(80, 80, 80, alpha));
		colorMap.put(EPlay.INTERCHANGE, new Color(255, 150, 0, alpha));
	}


	/**
	 * Returns a list of available bots not already assigned to another play
	 *
	 * @return List of bot ids
	 */
	protected Set<BotID> getUnassignedBots()
	{
		return getUnassignedBots(Integer.MAX_VALUE);
	}


	/**
	 * Returns a list of available bots not already assigned to another play
	 *
	 * @return List of bot ids
	 */
	protected Set<BotID> getUnassignedBots(int limit)
	{
		return getAiFrame().getWorldFrame().getTigerBotsAvailable().keySet().stream()
				.filter(bot -> desiredBotMap.get().values().stream()
						.noneMatch(set -> set.contains(bot)))
				.limit(limit)
				.collect(Collectors.toSet());
	}


	/**
	 * @param botID
	 * @return true, if the bot id is not yet assigned and available
	 */
	protected boolean isAssignable(BotID botID)
	{
		return getUnassignedBots().stream().anyMatch(b -> b.equals(botID));
	}


	/**
	 * Add desired bots to the given play
	 *
	 * @param play      The play to add desired bots to
	 * @param botsToAdd A set of BotIDs
	 */
	protected void addDesiredBots(EPlay play, Set<BotID> botsToAdd)
	{
		desiredBotMap.get().put(play, Collections.unmodifiableSet(botsToAdd));
		drawShapes(play, botsToAdd);
	}


	private void drawShapes(EPlay play, Set<BotID> bots)
	{
		if (!colorMap.containsKey(play))
		{
			return;
		}
		var shapes = getShapes(EAiShapesLayer.AI_ROLE_COLOR);
		var radius = Geometry.getBotRadius() + 50;
		bots.stream()
				.map(id -> getWFrame().getBot(id))
				.filter(Objects::nonNull)
				.map(ITrackedBot::getPos)
				.map(pos -> Circle.createCircle(pos, radius))
				.map(circle -> new DrawableCircle(circle, colorMap.get(play)).setFill(true))
				.forEach(shapes::add);
	}
}
