/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.wp.data.KickedBall;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * This calculator decides which robot has last touched the ball
 */
public class BotBallContactAutoRefCalc implements IAutoRefereeCalc
{
	private final BotLastTouchedBallCalculator botLastTouchedBallCalculator = new BotLastTouchedBallCalculator();
	private List<BotPosition> lastBotTouchedBall = Collections.emptyList();
	private KickedBall lastKickEvent;


	@Override
	public void process(final AutoRefFrame frame)
	{
		List<BotPosition> currentlyTouchingBots = botLastTouchedBallCalculator
				.currentlyTouchingBots(frame.getWorldFrame()).stream()
				.map(b -> frame.getWorldFrame().getBot(b))
				.map(b -> new BotPosition(frame.getTimestamp(), b))
				.collect(Collectors.toList());
		frame.setBotsTouchingBall(currentlyTouchingBots);

		if (currentlyTouchingBots.isEmpty())
		{
			var newKickEvent = frame.getWorldFrame().getKickedBall()
					.filter(k -> lastKickEvent == null || k.getTimestamp() != lastKickEvent.getTimestamp());
			lastBotTouchedBall = newKickEvent
					.map(k -> new BotPosition(k.getTimestamp(), k.getPosition(), k.getKickingBot()))
					.map(List::of)
					.orElse(lastBotTouchedBall);
		} else
		{
			lastBotTouchedBall = currentlyTouchingBots;
		}
		frame.setBotsLastTouchedBall(lastBotTouchedBall);

		lastBotTouchedBall.stream()
				.filter(b -> currentlyTouchingBots.stream().noneMatch(p -> p.getBotID().equals(b.getBotID())))
				.map(b -> frame.getWorldFrame().getBot(b.getBotID()))
				.filter(Objects::nonNull)
				.forEach(b -> frame.getShapes().get(EAutoRefShapesLayer.LAST_BALL_CONTACT)
						.add(new DrawableCircle(b.getPos(), 100, Color.BLUE)));
		currentlyTouchingBots.forEach(b -> frame.getShapes().get(EAutoRefShapesLayer.LAST_BALL_CONTACT)
				.add(new DrawableCircle(b.getPos(), 100, Color.RED)));

		lastKickEvent = frame.getWorldFrame().getKickedBall().orElse(null);
	}
}
