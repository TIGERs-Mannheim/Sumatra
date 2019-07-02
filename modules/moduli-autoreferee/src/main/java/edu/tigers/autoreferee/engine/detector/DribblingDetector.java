/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotDribbledBallTooFar;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;


/**
 * Detect if bots dribble the ball over a large distance.
 */
public class DribblingDetector extends AGameEventDetector
{
	@Configurable(comment = "[mm] Any dribbling distance above this value is considered a violation", defValue = "1000.0")
	private static double maxDribblingLength = 1000.0;
	
	private final Map<BotID, BotPosition> currentContacts = new HashMap<>();
	
	
	public DribblingDetector()
	{
		super(EGameEventDetectorType.DRIBBLING, EGameState.RUNNING);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		List<BotPosition> botsTouchingBall = frame.getBotsTouchingBall();
		
		// add new touching bots
		botsTouchingBall.forEach(b -> currentContacts.putIfAbsent(b.getBotID(), b));
		// remove vanished touching bots
		currentContacts.keySet().removeIf(k -> botsTouchingBall.stream().noneMatch(b -> b.getBotID().equals(k)));
		
		Optional<IGameEvent> gameEvent = botsTouchingBall.stream()
				.filter(b -> dribbleDistance(b) > maxDribblingLength)
				.findFirst()
				.map(this::createViolation);
		
		gameEvent.ifPresent(g -> doReset());
		return gameEvent;
	}
	
	
	private IVector2 dribbleStartPosition(final BotID botID)
	{
		return currentContacts.get(botID).getPos();
	}
	
	
	private double dribbleDistance(final BotPosition b)
	{
		return b.getPos().distanceTo(dribbleStartPosition(b.getBotID()));
	}
	
	
	private BotDribbledBallTooFar createViolation(final BotPosition finalBotPosition)
	{
		final BotID violatorId = finalBotPosition.getBotID();
		final IVector2 kickPos = dribbleStartPosition(finalBotPosition.getBotID());
		
		return new BotDribbledBallTooFar(violatorId, kickPos, finalBotPosition.getPos());
	}
	
	
	@Override
	protected void doReset()
	{
		currentContacts.clear();
	}
}
