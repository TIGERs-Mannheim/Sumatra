/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;


/**
 * This calculator determines the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotBallContactCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BotLastTouchedBallCalculator calculator = new BotLastTouchedBallCalculator(getWFrame(),
				baseAiFrame.getPrevFrame().getWorldFrame());
		
		Set<BotID> currentlyTouchingBots = calculator.currentlyTouchingBots().stream()
				.map(b -> getWFrame().getBot(b))
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
		
		newTacticalField.setBotsTouchingBall(currentlyTouchingBots);
		
		final Set<BotID> botsLastTouchedBall = new HashSet<>();
		if (!currentlyTouchingBots.isEmpty())
		{
			botsLastTouchedBall.addAll(currentlyTouchingBots);
		} else
		{
			// note: creating a new hash set here to ensure that we do not wrap infinite number of
			// unmodifiable collections in tactical field
			botsLastTouchedBall.addAll(baseAiFrame.getPrevFrame().getTacticalField().getBotsLastTouchedBall());
		}
		newTacticalField.setBotsLastTouchedBall(botsLastTouchedBall);
		
		newTacticalField.getBotsLastTouchedBall().stream()
				.filter(b -> !currentlyTouchingBots.contains(b))
				.map(b -> getWFrame().getBot(b))
				.forEach(b -> newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_BALL_CONTACT)
						.add(new DrawableCircle(b.getPos(), 100, Color.BLUE)));
		currentlyTouchingBots.stream()
				.map(b -> getWFrame().getBot(b))
				.forEach(b -> newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_BALL_CONTACT)
						.add(new DrawableCircle(b.getPos(), 100, Color.RED)));
	}
}
