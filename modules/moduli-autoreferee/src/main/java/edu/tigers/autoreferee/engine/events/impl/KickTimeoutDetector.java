/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * The kick timeout will stop the game and initiate a {@link Command#FORCE_START} command if the ball is not kicked
 * after {@link KickTimeoutDetector#FREEKICK_TIMEOUT_MS} seconds.
 * 
 * @author Lukas Magel
 */
public class KickTimeoutDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	private static final long FREEKICK_TIMEOUT_MS = 10_000;
	
	private long entryTime;
	private boolean kickTimedOut;
	
	
	/**
	 * Default
	 */
	public KickTimeoutDetector()
	{
		super(EGameEventDetectorType.KICK_TIMEOUT, EnumSet.of(
				EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE, EGameState.KICKOFF));
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		entryTime = frame.getTimestamp();
		kickTimedOut = false;
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate(final IAutoRefFrame frame)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		ETeamColor attackingColor = frame.getGameState().getForTeam();
		
		long curTime = frame.getTimestamp();
		if (((curTime - entryTime) > TimeUnit.MILLISECONDS.toNanos(FREEKICK_TIMEOUT_MS)) && !kickTimedOut)
		{
			kickTimedOut = true;
			FollowUpAction followUp = new FollowUpAction(EActionType.FORCE_START, ETeamColor.NEUTRAL, ballPos);
			GameEvent violation = new GameEvent(EGameEvent.KICK_TIMEOUT, frame.getTimestamp(), attackingColor,
					followUp);
			return Optional.of(violation);
		}
		return Optional.empty();
	}
	
}
