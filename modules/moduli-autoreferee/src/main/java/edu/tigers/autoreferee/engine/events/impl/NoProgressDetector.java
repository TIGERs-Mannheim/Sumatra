/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * Check if there is no progress in a running game and trigger a stop -> force start
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class NoProgressDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	private static final double DISTANCE_TOLERANCE = 100;
	private static final double TIMEOUT = 10.0;
	
	private long lastTime;
	private IVector2 lastBallPos = null;
	
	
	/**
	 * Default
	 */
	public NoProgressDetector()
	{
		super(EnumSet.of(EGameState.RUNNING));
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		lastTime = 0;
		lastBallPos = frame.getWorldFrame().getBall().getPos();
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		
		if (frame.getWorldFrame().getBall().isOnCam() && ballPos.distanceTo(lastBallPos) > DISTANCE_TOLERANCE)
		{
			lastTime = 0;
			lastBallPos = ballPos;
			return Optional.empty();
		}
		if (lastTime == 0)
		{
			lastTime = frame.getTimestamp();
		}
		
		if ((frame.getTimestamp() - lastTime) / 1e9 > TIMEOUT)
		{
			FollowUpAction followUp = new FollowUpAction(FollowUpAction.EActionType.FORCE_START, ETeamColor.NEUTRAL,
					ballPos);
			GameEvent violation = new GameEvent(EGameEvent.NO_PROGRESS_IN_GAME, frame.getTimestamp(), ETeamColor.NEUTRAL,
					followUp);
			lastTime = 0;
			return Optional.of(violation);
		}
		
		return Optional.empty();
	}
	
}
