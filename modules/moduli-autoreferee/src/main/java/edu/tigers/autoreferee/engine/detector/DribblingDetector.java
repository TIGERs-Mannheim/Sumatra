/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotDribbledBallTooFar;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Detect if bots dribble the ball over a large distance.
 */
public class DribblingDetector extends AGameEventDetector
{
	@Configurable(comment = "[mm] Any dribbling distance above this value is considered a violation", defValue = "1000.0")
	private static double maxDribblingLength = 1000.0;

	private final Map<BotID, IVector2> currentContacts = new HashMap<>();


	public DribblingDetector()
	{
		super(EGameEventDetectorType.DRIBBLING, EGameState.RUNNING);
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		IVector2 ballPos = getBall().getPos();
		List<BotPosition> botsTouchingBall = frame.getBotsTouchingBall();

		// add new touching bots
		botsTouchingBall.stream()
				// bots are considered touching ball quite early, so only accept them here if ball is close
				// This is fine here, because we do not need to consider ball reflection
				.filter(bp -> bp.getPos().distanceTo(ballPos) < Geometry.getBotRadius() + Geometry.getBallRadius() + 10)
				.forEach(bp -> currentContacts.putIfAbsent(bp.getBotID(), ballPos));
		// remove vanished touching bots
		currentContacts.keySet().removeIf(k -> botsTouchingBall.stream().noneMatch(b -> b.getBotID().equals(k)));

		currentContacts.values().forEach(this::drawCircle);

		Map<BotID, Double> dribbleDistances = currentContacts.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> dribbleDistance(e.getValue())));

		Optional<BotID> violatingBot = dribbleDistances.entrySet().stream()
				.filter(e -> e.getValue() > maxDribblingLength)
				.min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey);

		return violatingBot.map(botId -> new BotDribbledBallTooFar(botId, currentContacts.remove(botId), ballPos));
	}


	private void drawCircle(IVector2 pos)
	{
		frame.getShapes().get(EAutoRefShapesLayer.ALLOWED_DRIBBLING_DISTANCE).add(
				new DrawableCircle(pos, maxDribblingLength).setColor(Color.red)
		);
		frame.getShapes().get(EAutoRefShapesLayer.ALLOWED_DRIBBLING_DISTANCE).add(
				new DrawableCircle(pos, 10).setColor(Color.red)
		);
	}


	private double dribbleDistance(IVector2 startPos)
	{
		return startPos.distanceTo(getBall().getPos());
	}


	@Override
	protected void doReset()
	{
		currentContacts.clear();
	}
}
