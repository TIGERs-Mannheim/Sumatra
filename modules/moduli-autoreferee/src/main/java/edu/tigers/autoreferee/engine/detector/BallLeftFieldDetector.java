/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.AimlessKick;
import edu.tigers.sumatra.referee.gameevent.BallLeftFieldGoalLine;
import edu.tigers.sumatra.referee.gameevent.BallLeftFieldTouchLine;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.data.TimedPosition;


/**
 * This rule detects when the ball leaves the field. This rule also handles icing (aimless kick).
 */
public class BallLeftFieldDetector extends AGameEventDetector
{
	@Configurable(comment = "[mm] A goalline off is only considered icing if the bot was located more than this value behind the kickoff line", defValue = "200.0")
	private static double icingKickoffLineThreshold = 200.0;
	
	
	private TimedPosition lastBallLeftFieldPos = null;
	
	
	/**
	 * Create new instance of the BallLeftFieldDetector
	 */
	public BallLeftFieldDetector()
	{
		super(EGameEventDetectorType.BALL_LEFT_FIELD, EGameState.RUNNING);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		return frame.getBallLeftFieldPos()
				.filter(b -> !b.getPosition().similarTo(lastBallLeftFieldPos))
				.map(this::processBallLeftField);
	}
	
	
	@Override
	protected void doReset()
	{
		lastBallLeftFieldPos = null;
	}
	
	
	private IGameEvent processBallLeftField(final BallLeftFieldPosition ballLeftFieldPosition)
	{
		lastBallLeftFieldPos = ballLeftFieldPosition.getPosition();
		switch (ballLeftFieldPosition.getType())
		{
			case TOUCH_LINE:
				return handleSideLineOff(ballLeftFieldPosition.getPosition().getPos(), botThatLastTouchedBall());
			case GOAL_LINE:
			case GOAL_OVER:
				return handleGoalLineOff(ballLeftFieldPosition.getPosition().getPos(), botThatLastTouchedBall());
			default:
				return null;
		}
	}
	
	
	private BotPosition botThatLastTouchedBall()
	{
		final List<BotPosition> botLastTouchedBall = frame.getBotsLastTouchedBall();
		if (botLastTouchedBall.size() == 1)
		{
			return botLastTouchedBall.get(0);
		}
		return null;
	}
	
	
	private IGameEvent handleSideLineOff(final IVector2 ballPos, final BotPosition lastTouched)
	{
		if (lastTouched == null)
		{
			// we do not know who last touched the ball
			// let's just flip a coin
			return new BallLeftFieldTouchLine(randomTeam(), ballPos);
		}
		return new BallLeftFieldTouchLine(lastTouched.getBotID(), ballPos);
	}
	
	
	private IGameEvent handleGoalLineOff(final IVector2 ballPos, final BotPosition lastTouched)
	{
		if (lastTouched == null)
		{
			// we do not know who last touched the ball
			// let's just flip a coin
			return new BallLeftFieldGoalLine(randomTeam(), ballPos);
		}
		
		if (isIcing(lastTouched, ballPos))
		{
			return new AimlessKick(lastTouched.getBotID(), ballPos, lastTouched.getPos());
		}
		
		return new BallLeftFieldGoalLine(lastTouched.getBotID(), ballPos);
	}
	
	
	private ETeamColor randomTeam()
	{
		return new Random(frame.getTimestamp()).nextInt(2) == 0
				? ETeamColor.YELLOW
				: ETeamColor.BLUE;
	}
	
	
	private boolean isIcing(final BotPosition lastTouched, final IVector2 ballPos)
	{
		ETeamColor colorOfGoalLine = NGeometry.getTeamOfClosestGoalLine(ballPos);
		ETeamColor kickerColor = lastTouched.getBotID().getTeamColor();
		
		boolean kickerWasInHisHalf = NGeometry.getFieldSide(kickerColor).isPointInShape(lastTouched.getPos())
				&& (Math.abs(lastTouched.getPos().x()) > icingKickoffLineThreshold);
		boolean crossedOppositeGoalLine = kickerColor != colorOfGoalLine;
		return kickerWasInHisHalf && crossedOppositeGoalLine;
	}
}
