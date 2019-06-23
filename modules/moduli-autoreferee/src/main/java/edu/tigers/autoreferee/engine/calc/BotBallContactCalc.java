/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;


/**
 * This calculator decides which robot has last touched the ball
 */
public class BotBallContactCalc implements IRefereeCalc
{
	private List<BotPosition> lastBotTouchedBall = Collections.emptyList();
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		BotLastTouchedBallCalculator calculator = new BotLastTouchedBallCalculator(frame.getWorldFrame(),
				frame.getPreviousFrame().getWorldFrame());
		
		List<BotPosition> currentlyTouchingBots = calculator.currentlyTouchingBots().stream()
				.map(b -> frame.getWorldFrame().getBot(b))
				.map(b -> new BotPosition(frame.getTimestamp(), b))
				.collect(Collectors.toList());
		
		frame.setBotsTouchingBall(currentlyTouchingBots);
		if (!currentlyTouchingBots.isEmpty())
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
	}
}
