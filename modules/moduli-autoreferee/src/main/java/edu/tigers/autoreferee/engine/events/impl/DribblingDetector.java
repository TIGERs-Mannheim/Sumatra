/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.DistanceViolation;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class tries to detect ball dribbling
 * 
 * @author Lukas Magel
 */
public class DribblingDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	private static final Logger	log									= Logger.getLogger(DribblingDetector.class);
	
	@Configurable(comment = "[mm] Any dribbling distance above this value is considered a violation")
	private static double maxDribblingLength = 1000;
	
	@Configurable(comment = "[mm] Any distance to the ball closer than this value is considered dribbling")
	private static double dribblingBotBallDistance = 40;
	
	static
	{
		AGameEventDetector.registerClass(DribblingDetector.class);
	}
	
	/** The position where the currently dribbling bot first touched the ball */
	private BotPosition	firstContact;
	private long			resetTime;
	
	
	/**
	 * Default constructor
	 */
	public DribblingDetector()
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
		resetTime = frame.getTimestamp();
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		BotPosition curContact = frame.getLastBotCloseToBall();
		if (!isSane(firstContact))
		{
			if (isSane(curContact) && (curContact.getTimestamp() >= resetTime))
			{
				firstContact = curContact;
			} else
			{
				return Optional.empty();
			}
		}
		
		BotID dribblerID = firstContact.getBotID();
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		ITrackedBot dribblerBot = frame.getWorldFrame().getBot(dribblerID);
		if (dribblerBot == null)
		{
			log.warn("Bot that last touched the ball disappeard from the field: " + dribblerID);
			return Optional.empty();
		}
		
		if (!curContact.getBotID().equals(dribblerID))
		{
			resetRule(curContact.getTimestamp());
			return Optional.empty();
		}
		
		// The ball has not been touched since the last contact
		if (VectorMath.distancePP(dribblerBot.getPos(), ballPos) > (dribblingBotBallDistance + Geometry
				.getBotRadius() + Geometry.getBallRadius()))
		{
			resetRule(frame.getTimestamp());
			return Optional.empty();
		}
		
		double totalDistance = VectorMath.distancePP(firstContact.getPos(), ballPos);
		if (totalDistance > maxDribblingLength)
		{
			ETeamColor dribblerColor = dribblerID.getTeamColor();
			IVector2 kickPos = AutoRefMath.getClosestFreekickPos(ballPos, dribblerColor.opposite());
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, dribblerColor.opposite(), kickPos);
			GameEvent violation = new DistanceViolation(EGameEvent.BALL_DRIBBLING, frame.getTimestamp(),
					dribblerID, followUp, totalDistance);
			resetRule(frame.getTimestamp());
			return Optional.of(violation);
		}
		return Optional.empty();
	}
	
	
	@Override
	protected void doReset()
	{
		firstContact = null;
	}
	
	
	private void resetRule(final long ts)
	{
		resetTime = ts;
		doReset();
	}
	
	
	private boolean isSane(final BotPosition pos)
	{
		return pos != null && !pos.getBotID().isUninitializedID();
	}
	
}
