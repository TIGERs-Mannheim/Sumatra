/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
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
	private static final int PRIORITY = 1;
	private static final Logger log = Logger.getLogger(BallLeftFieldDetector.class);
	
	@Configurable(comment = "[mm] The goal line threshold", defValue = "10.0")
	private static double goalLineThreshold = 10;
	
	@Configurable(comment = "[mm] A goalline off is only considered icing if the bot was located more than this value behind the kickoff line", defValue = "200.0")
	private static double icingKickoffLineThreshold = 200;
	
	@Configurable(comment = "[mm] Ball from sideline distance", defValue = "200.0")
	private static double sideLineDistance = 200;
	
	static
	{
		AGameEventDetector.registerClass(BallLeftFieldDetector.class);
	}
	
	private TimedPosition lastBallLeftFieldPos = null;
	private Random rnd = new Random();
	
	
	/**
	 * Create new instance of the BallLeftFieldDetector
	 */
	public BallLeftFieldDetector()
	{
		super(EGameEventDetectorType.BALL_LEFT_FIELD_ICING, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		if (rnd == null)
		{
			// get a deterministic random generator in simulation
			rnd = new Random(frame.getTimestamp());
		}
		
		if (frame.getBallLeftFieldPos().isPresent()
				&& !frame.getBallLeftFieldPos().get().similarTo(lastBallLeftFieldPos))
		{
			lastBallLeftFieldPos = frame.getBallLeftFieldPos().get();
			
			BotPosition lastTouched = botThatLastTouchedBall(frame);
			long ts = frame.getTimestamp();
			boolean exitGoallineInX = ((Geometry.getFieldLength() / 2)
					- Math.abs(lastBallLeftFieldPos.getPos().x())) < goalLineThreshold;
			boolean exitGoallineInY = ((Geometry.getFieldWidth() / 2)
					- Math.abs(lastBallLeftFieldPos.getPos().y())) > goalLineThreshold;
			boolean enteredGoalInY = Geometry.getGoalOur().getWidth() / 2
					- Math.abs(lastBallLeftFieldPos.getPos().y()) > goalLineThreshold;
			if (exitGoallineInX && exitGoallineInY)
			{
				// The ball exited the field over the goal line
				if (enteredGoalInY)
				{
					// a potential goal
					return Optional.empty();
				}
				return handleGoalLineOff(lastBallLeftFieldPos.getPos(), lastTouched, ts);
			}
			return handleSideLineOff(lastBallLeftFieldPos.getPos(), lastTouched, ts);
		}
		return Optional.empty();
	}
	
	
	private BotPosition botThatLastTouchedBall(IAutoRefFrame frame)
	{
		final List<BotPosition> botLastTouchedBall = frame.getBotsLastTouchedBall();
		if (botLastTouchedBall.isEmpty())
		{
			log.info("No last touched bot detected, choosing random one");
			ETeamColor teamColor = rnd.nextInt(2) == 0 ? ETeamColor.YELLOW : ETeamColor.BLUE;
			BotID id = BotID.createBotId(-1, teamColor);
			return new BotPosition(frame.getTimestamp(), frame.getWorldFrame().getBall().getPos(), id);
		} else if (botLastTouchedBall.size() == 1)
		{
			return botLastTouchedBall.get(0);
		}
		int randomId = rnd.nextInt(botLastTouchedBall.size());
		return botLastTouchedBall.get(randomId);
	}
	
	
	private Optional<IGameEvent> handleSideLineOff(final IVector2 ballPos,
			final BotPosition lastTouched, final long ts)
	{
		int ySide = ballPos.y() > 0 ? 1 : -1;
		IVector2 kickPos = ballPos.addNew(Vector2.fromXY(0, -sideLineDistance * ySide));
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
		lastBallLeftFieldPos = null;
		rnd = null;
	}
}
