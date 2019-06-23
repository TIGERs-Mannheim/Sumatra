/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class detects a violation of the Double Touch Rule which can occur if the bot who performs a
 * kickoff/direct/indirect touches the ball a second time.
 * 
 * @author "Simon Sander"
 */
public class DoubleTouchDetector extends APreparingGameEventDetector
{
	private static final int					priority						= 1;
	private static final Logger				log							= Logger.getLogger(DoubleTouchDetector.class);
	
	@Configurable(comment = "[mm] The bot may only once approach the ball by less than this value")
	private static double						MIN_BOT_BALL_DISTANCE	= 150;
	
	private BotPosition							firstTouchedPos;
	private boolean								hasMovedAwayFromBall		= false;
	private long									hasMovedAwayFromBallTs;
	
	private static Set<EGameStateNeutral>	VALID_PREVIOUS_STATES;
	
	static
	{
		AGameEventDetector.registerClass(DoubleTouchDetector.class);
		
		Set<EGameStateNeutral> states = EnumSet.of(
				EGameStateNeutral.KICKOFF_BLUE, EGameStateNeutral.KICKOFF_YELLOW,
				EGameStateNeutral.DIRECT_KICK_BLUE, EGameStateNeutral.DIRECT_KICK_YELLOW,
				EGameStateNeutral.INDIRECT_KICK_BLUE, EGameStateNeutral.INDIRECT_KICK_YELLOW);
		VALID_PREVIOUS_STATES = Collections.unmodifiableSet(states);
	}
	
	
	/**
	 * 
	 */
	public DoubleTouchDetector()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		List<EGameStateNeutral> stateHistory = frame.getStateHistory();
		if ((stateHistory.size() > 1) && VALID_PREVIOUS_STATES.contains(stateHistory.get(1)))
		{
			BotPosition lastTouched = frame.getBotLastTouchedBall();
			if ((lastTouched != null) && (!lastTouched.getId().isUninitializedID()))
			{
				firstTouchedPos = lastTouched;
				hasMovedAwayFromBall = false;
				hasMovedAwayFromBallTs = 0;
			}
		}
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		if ((firstTouchedPos == null) || firstTouchedPos.getId().isUninitializedID())
		{
			return Optional.empty();
		}
		BotID firstTouchedId = firstTouchedPos.getId();
		
		BotPosition curLastTouchedPos = frame.getBotLastTouchedBall();
		if (!firstTouchedId.equals(curLastTouchedPos.getId()))
		{
			// The ball has been touched by another robot
			doReset();
			return Optional.empty();
		}
		
		ITrackedBot firstTouchedBot = frame.getWorldFrame().getBot(firstTouchedId);
		if (firstTouchedBot == null)
		{
			log.warn("Tracked bot disappeard from the field: " + firstTouchedPos.getId());
			return Optional.empty();
		}
		
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		double botBallDist = GeoMath.distancePP(ballPos, firstTouchedBot.getPos());
		
		if (hasMovedAwayFromBall == false)
		{
			if (botBallDist > (MIN_BOT_BALL_DISTANCE + Geometry.getBotRadius()))
			{
				hasMovedAwayFromBall = true;
				hasMovedAwayFromBallTs = frame.getTimestamp();
				return Optional.empty();
			}
		} else
		{
			if ((curLastTouchedPos.getTs() > hasMovedAwayFromBallTs) && curLastTouchedPos.getId().equals(firstTouchedId))
			{
				ETeamColor firstTouchedColor = firstTouchedId.getTeamColor();
				
				IVector2 kickPos = AutoRefMath.getClosestFreekickPos(ballPos, firstTouchedColor.opposite());
				FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, firstTouchedColor.opposite(),
						kickPos);
				GameEvent violation = new GameEvent(EGameEvent.DOUBLE_TOUCH, frame.getTimestamp(),
						firstTouchedId, followUp);
				doReset();
				return Optional.of(violation);
			}
		}
		
		return Optional.empty();
	}
	
	
	@Override
	public void doReset()
	{
		firstTouchedPos = null;
		hasMovedAwayFromBall = false;
		hasMovedAwayFromBallTs = 0;
	}
	
	
}
