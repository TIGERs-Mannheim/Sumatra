/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.GameStateCalculator;


/**
 * <p>
 * This class detects a violation of the Double Touch Rule which can occur if the bot who performs a
 * kickoff/direct/indirect touches the ball a second time before any other bot touched it
 * -> according to rules from 2017: the bot is allowed to touch the ball more than ones before the ball moved 50mm
 * </p>
 * <p>
 * From the rules (as of 2018):
 * For all restarts where the Laws stipulate that the ball is in play when it is kicked and moves,
 * the robot must clearly tap or kick the ball to make it move. It is understood that the ball
 * may remain in contact with the robot or be bumped by the robot multiple times over a short
 * distance while the kick is being taken, but under no circumstances should the robot remain
 * in contact or touch the ball after it has traveled 50 mm, unless the ball has previously touched
 * another robot. Robots may use dribbling and kicking devices in taking the free kick.
 * </p>
 * <p>
 * This detector assumes that the ball has moved by 50mm, because it is activated when the game switched to running.
 * The {@link GameStateCalculator} uses the same 50mm distance to switch to running.
 * </p>
 */
public class DoubleTouchDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	
	private static final Set<EGameState> VALID_PREVIOUS_STATES = Collections.unmodifiableSet(EnumSet.of(
			EGameState.KICKOFF, EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE));
	
	static
	{
		AGameEventDetector.registerClass(DoubleTouchDetector.class);
	}
	
	private BotID kickerID = null;
	
	
	public DoubleTouchDetector()
	{
		super(EGameEventDetectorType.DOUBLE_TOUCH, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		kickerID = null;
		
		List<GameState> stateHistory = frame.getStateHistory();
		if ((stateHistory.size() > 1) && VALID_PREVIOUS_STATES.contains(stateHistory.get(1).getState()))
		{
			kickerID = frame.getWorldFrame().getBots().values().stream()
					.min(Comparator.comparingDouble(b -> b.getPos().distanceTo(frame.getWorldFrame().getBall().getPos())))
					.map(ITrackedBot::getBotId)
					.orElse(null);
		}
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame)
	{
		if (frame.getBotsLastTouchedBall().stream().noneMatch(b -> b.getBotID().equals(kickerID)))
		{
			// The ball has been touched by another robot
			kickerID = null;
			return Optional.empty();
		}
		
		if (frame.getBotsTouchingBall().stream().anyMatch(b -> b.getBotID().equals(kickerID)))
		{
			// kicker touched the ball again
			GameEvent violation = createViolation(frame);
			kickerID = null;
			return Optional.of(violation);
		}
		
		// situation is not decided yet
		return Optional.empty();
	}
	
	
	private GameEvent createViolation(final IAutoRefFrame frame)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		ETeamColor attackingTeam = kickerID.getTeamColor();
		IVector2 kickPos = AutoRefMath.getClosestFreekickPos(ballPos, attackingTeam.opposite());
		
		FollowUpAction followUp = new FollowUpAction(
				EActionType.INDIRECT_FREE,
				attackingTeam.opposite(),
				kickPos);
		
		return new GameEvent(
				EGameEvent.DOUBLE_TOUCH,
				frame.getTimestamp(),
				kickerID,
				followUp);
	}
}
