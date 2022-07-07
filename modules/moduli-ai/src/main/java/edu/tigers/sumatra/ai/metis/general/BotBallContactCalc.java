/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;
import lombok.Getter;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This calculator determines the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 */
public class BotBallContactCalc extends ACalculator
{
	private final BotLastTouchedBallCalculator botLastTouchedBallCalculator = new BotLastTouchedBallCalculator();

	@Getter
	private Set<BotID> botsLastTouchedBall;

	@Getter
	private Set<BotID> currentlyTouchingBots;


	@Override
	public void doCalc()
	{
		var touchingBots = botLastTouchedBallCalculator.currentlyTouchingBots(getWFrame()).stream()
				.map(b -> getWFrame().getBot(b))
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
		getWFrame().getTigerBotsAvailable().entrySet()
				.stream().filter(e -> e.getValue().getBallContact().hasContact())
				.map(Map.Entry::getKey)
				.forEach(touchingBots::add);
		currentlyTouchingBots = touchingBots.stream().collect(Collectors.toUnmodifiableSet());

		if (!currentlyTouchingBots.isEmpty())
		{
			botsLastTouchedBall = currentlyTouchingBots;
		}

		botsLastTouchedBall.stream()
				.filter(b -> !currentlyTouchingBots.contains(b))
				.map(b -> getWFrame().getBot(b))
				.filter(Objects::nonNull)
				.forEach(b -> getShapes(EAiShapesLayer.AI_BALL_CONTACT)
						.add(new DrawableCircle(b.getPos(), 120, Color.BLUE)));
		currentlyTouchingBots.stream()
				.map(b -> getWFrame().getBot(b))
				.filter(Objects::nonNull)
				.forEach(b -> getShapes(EAiShapesLayer.AI_BALL_CONTACT)
						.add(new DrawableCircle(b.getPos(), 100, Color.RED)));
	}


	@Override
	protected void reset()
	{
		botsLastTouchedBall = Collections.emptySet();
	}
}
