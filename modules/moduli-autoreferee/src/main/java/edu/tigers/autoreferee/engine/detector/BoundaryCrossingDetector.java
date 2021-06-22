/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BoundaryCrossing;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import lombok.extern.log4j.Log4j2;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;


/**
 * This rule detects when the ball leaves the playing area by crossing the
 * filed boundary
 */
@Log4j2
public class BoundaryCrossingDetector extends AGameEventDetector
{
	@Configurable(defValue = "2.0", comment = "Delay until the Game Event is thrown [s]")
	private static double delay = 2.0;
	private ETeamColor lastTouchedBy = ETeamColor.NEUTRAL;
	private long ballLastSeen = 0;
	private IVector2 lastKnownPosition = Vector2.zero();


	public BoundaryCrossingDetector()
	{
		super(EGameEventDetectorType.BOUNDARY_CROSSING,
				EnumSet.of(
						EGameState.RUNNING,
						EGameState.INDIRECT_FREE,
						EGameState.DIRECT_FREE,
						EGameState.BALL_PLACEMENT,
						EGameState.PREPARE_PENALTY,
						EGameState.PREPARE_KICKOFF,
						EGameState.PENALTY));

		setDeactivateOnFirstGameEvent(true);
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		boolean ballOnField = isBallOnField();
		ETeamColor ballLastTouchedBy = getLastTouchedByTeam();

		if (ballOnField)
		{
			lastTouchedBy = ballLastTouchedBy;
			ballLastSeen = frame.getTimestamp();
			lastKnownPosition = frame.getWorldFrame().getBall().getPos();
		} else if (getBall().isChipped() && timeElapsed())
		{
			if (lastTouchedBy != ETeamColor.NEUTRAL)
			{
				IVector2 pos = frame.getBallLeftFieldPos()
						.map(ballLeftFieldPosition -> ballLeftFieldPosition.getPosition().getPos()).orElse(lastKnownPosition);

				return Optional.of(new BoundaryCrossing(lastTouchedBy, pos));
			} else
			{
				log.warn("Boundary crossing was detected but causing team was unclear and thus no event was submitted");
			}
		}

		return Optional.empty();
	}


	private boolean timeElapsed()
	{
		double time = (frame.getTimestamp() - ballLastSeen) / 1e9;
		return time > delay && ballLastSeen != 0;
	}


	@Override
	protected void doReset()
	{
		lastTouchedBy = ETeamColor.NEUTRAL;
		ballLastSeen = 0;
	}


	private ETeamColor getLastTouchedByTeam()
	{
		List<BotPosition> bots = frame.getBotsLastTouchedBall();

		ETeamColor color = ETeamColor.NEUTRAL;
		for (BotPosition botPosition : bots)
		{
			if (color == ETeamColor.NEUTRAL || color == botPosition.getBotID().getTeamColor())
			{
				color = botPosition.getBotID().getTeamColor();
			} else
			{
				// Last Touched Robots are from a different team and thus no guilty team could be identified
				return ETeamColor.NEUTRAL;
			}
		}
		return color;
	}


	private boolean isBallOnField()
	{
		boolean ballVisible = frame.getWorldFrame().getBall().isOnCam(0.1);
		boolean ballInsideField = Geometry.getField().withMargin(700)
				.isPointInShape(frame.getWorldFrame().getBall().getPos());

		return ballInsideField && ballVisible;
	}
}
