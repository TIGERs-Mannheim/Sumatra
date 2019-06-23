/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.DistanceViolation;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * This class tries to detect ball dribbling
 * 
 * @author Lukas Magel
 */
public class DribblingDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	
	@Configurable(comment = "[mm] Any dribbling distance above this value is considered a violation", defValue = "1000.0")
	private static double maxDribblingLength = 1000;
	
	static
	{
		AGameEventDetector.registerClass(DribblingDetector.class);
	}
	
	private final Map<BotID, BotPosition> currentContacts = new HashMap<>();
	
	
	/**
	 * Default constructor
	 */
	public DribblingDetector()
	{
		super(EGameEventDetectorType.DRIBBLING, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame)
	{
		List<BotPosition> botsTouchingBall = frame.getBotsTouchingBall();
		
		// add new touching bots
		botsTouchingBall.forEach(b -> currentContacts.putIfAbsent(b.getBotID(), b));
		// remove vanished touching bots
		currentContacts.keySet().removeIf(k -> botsTouchingBall.stream().noneMatch(b -> b.getBotID().equals(k)));
		
		Optional<IGameEvent> gameEvent = botsTouchingBall.stream()
				.filter(b -> dribbleDistance(b) > maxDribblingLength)
				.findFirst()
				.map(b -> createViolation(frame, b));
		
		gameEvent.ifPresent(g -> doReset());
		return gameEvent;
	}
	
	
	private IVector2 dribbleStartPosition(final BotPosition b)
	{
		return currentContacts.get(b.getBotID()).getPos();
	}
	
	
	private double dribbleDistance(final BotPosition b)
	{
		return b.getPos().distanceTo(dribbleStartPosition(b));
	}
	
	
	private GameEvent createViolation(final IAutoRefFrame frame, final BotPosition finalBotPosition)
	{
		final BotID violatorId = finalBotPosition.getBotID();
		final ETeamColor teamInFavor = violatorId.getTeamColor().opposite();
		final IVector2 kickPos = AutoRefMath.getClosestFreekickPos(
				dribbleStartPosition(finalBotPosition),
				teamInFavor);
		
		FollowUpAction followUp = new FollowUpAction(
				EActionType.INDIRECT_FREE,
				teamInFavor,
				kickPos);
		
		return new DistanceViolation(
				EGameEvent.BALL_DRIBBLING,
				frame.getTimestamp(),
				violatorId,
				followUp,
				dribbleDistance(finalBotPosition));
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		// nothing to prepare
	}
	
	
	@Override
	protected void doReset()
	{
		currentContacts.clear();
	}
}
