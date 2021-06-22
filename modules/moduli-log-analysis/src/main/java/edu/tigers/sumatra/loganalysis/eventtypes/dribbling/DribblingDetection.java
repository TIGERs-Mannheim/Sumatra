/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.dribbling;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.loganalysis.eventtypes.IEventTypeDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class DribblingDetection implements IEventTypeDetection<Dribbling>
{

	@Configurable(comment = "max distance between ball and bot kicker to detect dribbling", defValue = "35.0")
	private double dribblingDistance = 35d;

	private boolean dribblingDistanceHysteresis = true;
	private double additionalDistanceForHysteresis = 20d;

	private Map<BotID, Boolean> botHasHysteresis = new HashMap<>();


	private List<IDrawableShape> dribblingHistoryDraw = new LinkedList<>();

	private Dribbling detectedDribbling = null;


	private boolean isBotDribbling(final ITrackedBot trackedBot, final ITrackedBall ball,
			final List<IDrawableShape> shapes)
	{
		double distanceBallKicker = trackedBot.getBotKickerPos().distanceTo(ball.getPos());
		double currentDribblingDistance = dribblingDistance;

		if (dribblingDistanceHysteresis && botHasHysteresis.containsKey(trackedBot.getBotId())
				&& botHasHysteresis.get(trackedBot.getBotId()))
		{
			currentDribblingDistance += additionalDistanceForHysteresis;
		}

		final double finalDrawDribblingDistance = currentDribblingDistance;
		shapes.add(new DrawableCircle(Circle.createCircle(trackedBot.getBotKickerPos(), finalDrawDribblingDistance),
				Color.GRAY));

		return distanceBallKicker < currentDribblingDistance;
	}


	@Override
	public void nextFrameForDetection(TypeDetectionFrame frame)
	{
		dribblingDistance = 35d;
		ITrackedBot nextBotToBall = frame.getClosestBotToBall();
		if (nextBotToBall == null)
		{
			detectedDribbling = new Dribbling(false, null, ETeamColor.NEUTRAL);
			return;
		}
		Optional<ITrackedBot> secondClosestBotToBall = frame.getSecondClosestBotToBall();
		ITrackedBall ball = frame.getWorldFrameWrapper().getSimpleWorldFrame().getBall();

		GameState gameState = frame.getWorldFrameWrapper().getGameState();

		List<IDrawableShape> shapes = frame.getShapeMap().get(ELogAnalysisShapesLayer.DRIBBLING);

		boolean nextBotToBallIsDribbling = isBotDribbling(nextBotToBall, ball, shapes);
		boolean secondClosestBotToBallIsDribbling = secondClosestBotToBall.isPresent()
				&& isBotDribbling(secondClosestBotToBall.get(), ball, shapes);

		if (!gameState.isRunning())
		{
			detectedDribbling = new Dribbling(false, null, ETeamColor.NEUTRAL);
		} else if (nextBotToBallIsDribbling)
		{
			if (secondClosestBotToBallIsDribbling
					&& !secondClosestBotToBall.get().getTeamColor().equals(nextBotToBall.getTeamColor()))
			{
				// second opponent Bot on ball
				detectedDribbling = new Dribbling(false, null, ETeamColor.NEUTRAL);
			} else
			{
				detectedDribbling = new Dribbling(true, nextBotToBall, nextBotToBall.getTeamColor());
			}

		} else
		{
			detectedDribbling = new Dribbling(false, null, ETeamColor.NEUTRAL);
		}

		dribblingHistoryDraw.addAll(detectedDribbling.getDrawableShape());

		// Draw last passes in the visualizer
		for (IDrawableShape dribbling : dribblingHistoryDraw)
		{
			frame.getShapeMap().get(ELogAnalysisShapesLayer.DRIBBLING).add(dribbling);
		}

		botHasHysteresis.clear();
		if (nextBotToBallIsDribbling)
		{
			botHasHysteresis.put(nextBotToBall.getBotId(), true);
		}
		if (secondClosestBotToBallIsDribbling)
		{
			botHasHysteresis.put(secondClosestBotToBall.get().getBotId(), true);
		}
	}


	@Override
	public void resetDetection()
	{
		dribblingHistoryDraw.clear();
		botHasHysteresis.clear();
		detectedDribbling = new Dribbling(false, null, ETeamColor.NEUTRAL);
	}


	@Override
	public Dribbling getDetectedEventType()
	{
		return detectedDribbling;
	}


}
