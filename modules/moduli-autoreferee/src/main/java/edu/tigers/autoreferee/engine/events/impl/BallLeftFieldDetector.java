/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * This rule initiates throw-ins/goalkicks/corner kicks when the ball leaves the field. This rule also handles icing.
 * 
 * @author "Lukas Magel"
 */
public class BallLeftFieldDetector extends AGameEventDetector
{
	private static final int	priority							= 1;
	
	@Configurable(comment = "[mm] The goal line threshold")
	private static double		goalLineThreshold				= 10;
	
	@Configurable(comment = "[mm] A goalline off is only considered icing if the bot was located more than this value behind the kickoff line")
	private static double		icingKickoffLineThreshold	= 200;
	
	@Configurable(comment = "[mm] Area behind the goal that is still considered to be part of the goal for better goal detection")
	private static double		goalDepthMargin				= 100;
	
	@Configurable(comment = "[ms] Cooldown before an event is reported again")
	private static long			cooldownTime					= 2_000;
	
	private boolean				ballOutsideField				= false;
	private long					ballLeftFieldStamp			= 0;
	
	static
	{
		AGameEventDetector.registerClass(BallLeftFieldDetector.class);
	}
	
	
	/**
	 * 
	 */
	public BallLeftFieldDetector()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		long curTimestamp = frame.getTimestamp();
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		if (Geometry.getField().isPointInShape(ballPos) || ballInsideGoal(ballPos))
		{
			// The ball is inside the field or inside the goal
			ballOutsideField = false;
			return Optional.empty();
		}
		
		boolean cooldownElapsed = (curTimestamp - ballLeftFieldStamp) > TimeUnit.MILLISECONDS.toNanos(cooldownTime);
		if ((ballOutsideField == false) && cooldownElapsed)
		{
			ballOutsideField = true;
			ballLeftFieldStamp = curTimestamp;
			
			IVector2 intersection = frame.getBallLeftFieldPos();
			if (intersection == null)
			{
				// Maybe the game was started while the ball was still outside the field
				return Optional.empty();
			}
			BotPosition lastTouched = frame.getBotLastTouchedBall();
			long ts = frame.getTimestamp();
			boolean exitGoallineInX = ((Geometry.getFieldLength() / 2) - Math.abs(intersection.x())) < goalLineThreshold;
			boolean exitGoallineInY = ((Geometry.getFieldWidth() / 2) - Math.abs(intersection.y())) > goalLineThreshold;
			if (exitGoallineInX && exitGoallineInY)
			{
				// The ball exited the field over the goal line
				return handleGoalLineOff(intersection, lastTouched, ts);
			}
			return handleSideLineOff(intersection, lastTouched, ts);
		}
		return Optional.empty();
	}
	
	
	private Optional<IGameEvent> handleSideLineOff(final IVector2 ballPos,
			final BotPosition lastTouched, final long ts)
	{
		int ySide = ballPos.y() > 0 ? 1 : -1;
		IVector2 kickPos = ballPos.addNew(new Vector2(0, -100 * ySide));
		return Optional.of(buildThrowInResult(lastTouched.getId(), kickPos, ts));
	}
	
	
	protected Optional<IGameEvent> handleGoalLineOff(final IVector2 intersection,
			final BotPosition lastTouched, final long ts)
	{
		BotID lastTouchedID = lastTouched.getId();
		
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
		ETeamColor kickerColor = lastTouched.getId().getTeamColor();
		
		IVector2 lastTouchedPos = lastTouched.getPos();
		boolean kickerWasInHisHalf = NGeometry.getFieldSide(kickerColor).isPointInShape(lastTouchedPos)
				&& (Math.abs(lastTouchedPos.x()) > icingKickoffLineThreshold);
		boolean crossedOppositeGoalLine = kickerColor != colorOfGoalLine;
		if (kickerWasInHisHalf && crossedOppositeGoalLine)
		{
			return true;
		}
		return false;
	}
	
	
	private IGameEvent buildCornerKickResult(final BotID lastTouched, final IVector2 kickPos, final long ts)
	{
		FollowUpAction action = new FollowUpAction(EActionType.DIRECT_FREE, lastTouched.getTeamColor().opposite(),
				kickPos);
		GameEvent violation = new GameEvent(EGameEvent.BALL_LEFT_FIELD, ts, lastTouched, action);
		return violation;
	}
	
	
	private IGameEvent buildGoalKickResult(final BotID lastTouched, final IVector2 kickPos, final long ts)
	{
		FollowUpAction action = new FollowUpAction(EActionType.DIRECT_FREE, lastTouched.getTeamColor().opposite(),
				kickPos);
		GameEvent violation = new GameEvent(EGameEvent.BALL_LEFT_FIELD, ts, lastTouched, action);
		return violation;
	}
	
	
	private IGameEvent buildIcingResult(final BotID lastTouched, final IVector2 lastTouchedPos, final long ts)
	{
		IVector2 kickPos = AutoRefMath.getClosestFreekickPos(lastTouchedPos, lastTouched.getTeamColor().opposite());
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, lastTouched.getTeamColor().opposite(),
				kickPos);
		GameEvent violation = new GameEvent(EGameEvent.ICING, ts, lastTouched, action);
		return violation;
	}
	
	
	private IGameEvent buildThrowInResult(final BotID lastTouched, final IVector2 kickPos, final long ts)
	{
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, lastTouched
				.getTeamColor().opposite(), kickPos);
		GameEvent violation = new GameEvent(EGameEvent.BALL_LEFT_FIELD, ts, lastTouched, action);
		return violation;
	}
	
	
	@Override
	public void reset()
	{
		ballOutsideField = false;
		ballLeftFieldStamp = 0;
	}
	
	
	private boolean ballInsideGoal(final IVector2 ballPos)
	{
		double margin = 0.0d;
		if (!ballOutsideField)
		{
			margin = goalDepthMargin;
		}
		return NGeometry.ballInsideGoal(ballPos, margin);
	}
}
