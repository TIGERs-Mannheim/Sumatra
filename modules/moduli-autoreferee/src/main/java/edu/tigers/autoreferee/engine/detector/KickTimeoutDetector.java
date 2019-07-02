/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.EnumSet;
import java.util.Optional;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.KickTimeout;


/**
 * The kick timeout will stop the game if the ball is not kicked
 */
public class KickTimeoutDetector extends AGameEventDetector
{
	private long entryTime;
	private boolean kickTimedOut;
	
	
	public KickTimeoutDetector()
	{
		super(EGameEventDetectorType.KICK_TIMEOUT, EnumSet.of(
				EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE, EGameState.KICKOFF));
	}
	
	
	@Override
	protected void doPrepare()
	{
		entryTime = frame.getTimestamp();
		kickTimedOut = false;
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		if (frame.getRefereeMsg().getCurrentActionTimeRemaining() < 0 && !kickTimedOut)
		{
			ETeamColor attackingColor = frame.getGameState().getForTeam();
			long curTime = frame.getTimestamp();

			kickTimedOut = true;
			IGameEvent violation = new KickTimeout(attackingColor,
					getBall().getPos(), (curTime - entryTime) / 1e9);
			return Optional.of(violation);
		}
		return Optional.empty();
	}
}
