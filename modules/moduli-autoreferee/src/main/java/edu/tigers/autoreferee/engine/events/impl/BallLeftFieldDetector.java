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
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * This rule initiates throw-ins/goalkicks/corner kicks when the ball leaves the field. This rule also handles icing.
 * 
 * @author "Lukas Magel"
 */
public class BallLeftFieldDetector extends AGameEventDetector
{
	private static final Logger	log								= Logger.getLogger(BallLeftFieldDetector.class);
	
	private static final int		PRIORITY							= 1;
	
	@Configurable(comment = "[mm] The goal line threshold")
	private static double			goalLineThreshold				= 10;
	
	@Configurable(comment = "[mm] A goalline off is only considered icing if the bot was located more than this value behind the kickoff line")
	private static double			icingKickoffLineThreshold	= 200;
	
	static
	{
		AGameEventDetector.registerClass(BallLeftFieldDetector.class);
	}
	
	private boolean eventReported = false;
	
	
	/**
	 * Create new instance of the BallLeftFieldDetector
	 */
	public BallLeftFieldDetector()
	{
		super(EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		if (frame.isBallInsideField())
		{
			// The ball is inside the field or inside the goal
			eventReported = false;
		} else if (!eventReported)
		{
			eventReported = true;
			TimedPosition leftFieldPos = frame.getBallLeftFieldPos();
			
			if (leftFieldPos.getPos().isZeroVector())
			{
				// Maybe the game was started while the ball was still outside the field
				log.warn("Ball left the field but no valid exit position present");
				return Optional.empty();
			}
			
			BotPosition lastTouched = frame.getBotLastTouchedBall();
			long ts = frame.getTimestamp();
			boolean exitGoallineInX = ((Geometry.getFieldLength() / 2)
					- Math.abs(leftFieldPos.getPos().x())) < goalLineThreshold;
			boolean exitGoallineInY = ((Geometry.getFieldWidth() / 2)
					- Math.abs(leftFieldPos.getPos().y())) > goalLineThreshold;
			if (exitGoallineInX && exitGoallineInY)
			{
				// The ball exited the field over the goal line
				return handleGoalLineOff(leftFieldPos.getPos(), lastTouched, ts);
			}
			return handleSideLineOff(leftFieldPos.getPos(), lastTouched, ts);
		}
		return Optional.empty();
	}
	
	
	private Optional<IGameEvent> handleSideLineOff(final IVector2 ballPos,
			final BotPosition lastTouched, final long ts)
	{
		int ySide = ballPos.y() > 0 ? 1 : -1;
		IVector2 kickPos = ballPos.addNew(Vector2.fromXY(0, -100.0 * ySide));
		return Optional.of(buildThrowInResult(lastTouched.getBotID(), kickPos, ts));
	}
	
	
	private Optional<IGameEvent> handleGoalLineOff(final IVector2 intersection,
			final BotPosition lastTouched, final long ts)
	{
		BotID lastTouchedID = lastTouched.getBotID();
		
		if (isIcing(lastTouched, intersection))
		{
			return Optional.of(buildIcingResult(lastTouchedID, lastTouched.getPos(), ts));
		}
		
		ETeamColor goalLineColor = NGeometry.getTeamOfClosestGoalLine(intersection);
		
		if (lastTouchedID.getTeamColor() == goalLineColor)
		{
			// This is a corner kick
			IVector2 kickPos = AutoRefMath.getClosestCornerKickPos(intersection);
			return Optional.of(buildCornerKickResult(lastTouchedID, kickPos, ts));
		}
		// This is a goal kick
		IVector2 kickPos = AutoRefMath.getClosestGoalKickPos(intersection);
		
		return Optional.of(buildGoalKickResult(lastTouchedID, kickPos, ts));
	}
	
	
	private boolean isIcing(final BotPosition lastTouched, final IVector2 intersection)
	{
		ETeamColor colorOfGoalLine = NGeometry.getTeamOfClosestGoalLine(intersection);
		ETeamColor kickerColor = lastTouched.getBotID().getTeamColor();
		
		boolean kickerWasInHisHalf = NGeometry.getFieldSide(kickerColor).isPointInShape(lastTouched.getPos())
				&& (Math.abs(lastTouched.getPos().x()) > icingKickoffLineThreshold);
		boolean crossedOppositeGoalLine = kickerColor != colorOfGoalLine;
		return kickerWasInHisHalf && crossedOppositeGoalLine;
	}
	
	
	private IGameEvent buildCornerKickResult(final BotID lastTouched, final IVector2 kickPos, final long ts)
	{
		FollowUpAction action = new FollowUpAction(EActionType.DIRECT_FREE, lastTouched.getTeamColor().opposite(),
				kickPos);
		return new GameEvent(EGameEvent.BALL_LEFT_FIELD, ts, lastTouched, action);
	}
	
	
	private IGameEvent buildGoalKickResult(final BotID lastTouched, final IVector2 kickPos, final long ts)
	{
		FollowUpAction action = new FollowUpAction(EActionType.DIRECT_FREE, lastTouched.getTeamColor().opposite(),
				kickPos);
		return new GameEvent(EGameEvent.BALL_LEFT_FIELD, ts, lastTouched, action);
	}
	
	
	private IGameEvent buildIcingResult(final BotID lastTouched, final IVector2 lastTouchedPos, final long ts)
	{
		IVector2 kickPos = AutoRefMath.getClosestFreekickPos(lastTouchedPos, lastTouched.getTeamColor().opposite());
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, lastTouched.getTeamColor().opposite(),
				kickPos);
		return new GameEvent(EGameEvent.ICING, ts, lastTouched, action);
	}
	
	
	private IGameEvent buildThrowInResult(final BotID lastTouched, final IVector2 kickPos, final long ts)
	{
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, lastTouched
				.getTeamColor().opposite(), kickPos);
		return new GameEvent(EGameEvent.BALL_LEFT_FIELD, ts, lastTouched, action);
	}
	
	
	@Override
	public void reset()
	{
		eventReported = false;
	}
}
