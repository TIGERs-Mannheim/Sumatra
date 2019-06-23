/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class detects a violation of the Double Touch Rule which can occur if the bot who performs a
 * kickoff/direct/indirect touches the ball a second time before any other bot touched it
 * -> according to rules from 2017: the bot is allowed to touch the ball more than ones before the ball moved 50mm
 * 
 * @author "Simon Sander"
 */
public class DoubleTouchDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	private static final Set<EGameState> VALID_PREVIOUS_STATES = Collections.unmodifiableSet(EnumSet.of(
			EGameState.KICKOFF, EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE));
	private static final double ALLOWED_BALL_MOVE_DISTANCE_WHILE_TOUCHING = 50;
	@Configurable(comment = "[mm] additional offset to allowed 50mm movement (considering accuracy of position = 10mm)", defValue = "20")
	private static double distanceErrorOffset = 20;
	
	
	static
	{
		AGameEventDetector.registerClass(DoubleTouchDetector.class);
	}
	
	private BotID kickerID = null;
	private IVector2 ballKickPos = null;
	private boolean justKickedFirstTime = false;
	
	
	/**
	 * Default
	 */
	public DoubleTouchDetector()
	{
		super(EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		List<GameState> stateHistory = frame.getStateHistory();
		if ((stateHistory.size() > 1) && VALID_PREVIOUS_STATES.contains(stateHistory.get(1).getState()))
		{
			IAutoRefFrame lastFrame = frame.getPreviousFrame();
			ballKickPos = lastFrame.getLastStopBallPosition();
			kickerID = frame.getBotLastTouchedBall().getBotID();
			justKickedFirstTime = true;
		}
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		if (justKickedFirstTime)
		{
			justKickedFirstTime = false;
			return Optional.empty();
		}
		if ((kickerID == null) || kickerID.equals(BotID.noBot()))
		{
			return Optional.empty();
		}
		if (!kickerID.equals(frame.getBotLastTouchedBall().getBotID()))
		{
			// The ball has been touched by another robot
			doReset();
			return Optional.empty();
		}
		
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		
		if ((ballPos.distanceTo(ballKickPos) > ALLOWED_BALL_MOVE_DISTANCE_WHILE_TOUCHING + distanceErrorOffset)
				&& frame.getBotTouchedBall().isPresent())
		{
			BotID justTouched = frame.getBotTouchedBall().get().getBotID();
			if (justTouched.equals(kickerID))
			{
				ITrackedBot botJustTouched = frame.getPreviousFrame().getWorldFrame().getBot(justTouched);
				if (botJustTouched != null && botJustTouched.getBotKickerPos()
						.distanceTo(ballKickPos) > ALLOWED_BALL_MOVE_DISTANCE_WHILE_TOUCHING + distanceErrorOffset)
				{
					ETeamColor kickerColor = kickerID.getTeamColor();
					
					IVector2 kickPos = AutoRefMath.getClosestFreekickPos(ballPos, kickerColor.opposite());
					FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, kickerColor.opposite(),
							kickPos);
					GameEvent violation = new GameEvent(EGameEvent.DOUBLE_TOUCH, frame.getTimestamp(),
							kickerID, followUp);
					doReset();
					return Optional.of(violation);
				}
			}
		}
		return Optional.empty();
	}
	
	
	@Override
	protected void doReset()
	{
		kickerID = null;
		ballKickPos = null;
	}
}
