/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.data.TimeLimitedBuffer;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.PenaltyKickFailed;

import java.util.List;
import java.util.Optional;


public class PenaltyKickFailedDetector extends AGameEventDetector
{
	private static final double BALL_STOPPED_TOL = 1e-3;
	private final TimeLimitedBuffer<IVector2> ballVelBuffer = new TimeLimitedBuffer<>();


	public PenaltyKickFailedDetector()
	{
		super(EGameEventDetectorType.PENALTY_KICK_FAILED, EGameState.PENALTY);
		setDeactivateOnFirstGameEvent(true);
		ballVelBuffer.setMaxElements(10);
		ballVelBuffer.setMaxDuration(0.2);
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		ballVelBuffer.add(frame.getWorldFrame().getTimestamp(), frame.getWorldFrame().getBall().getVel());
		if (!frame.isBallInsideField())
		{
			return Optional.empty();
		}

		IVector2 ballVelNow = ballVelBuffer.getLatest().orElseThrow();
		IVector2 ballVelPre = ballVelBuffer.getOldest().orElseThrow();
		boolean ballStopped = ballVelPre.getLength2() > BALL_STOPPED_TOL && ballVelNow.getLength2() <= BALL_STOPPED_TOL;
		double ballVelDirChangeAngle = ballVelNow.angleToAbs(ballVelPre).orElse(0.0);
		boolean ballVelDirChanged = ballVelDirChangeAngle > AngleMath.DEG_090_IN_RAD;

		if (ballStopped || ballVelDirChanged)
		{
			List<BotPosition> lastBots = frame.getBotsLastTouchedBall();

			ETeamColor attackingTeam = frame.getGameState().getForTeam();
			ETeamColor defendingTeam = frame.getGameState().getForTeam().opposite();

			for (BotPosition bot : lastBots)
			{
				if (bot.getBotID().getTeamColor() == defendingTeam)
				{
					IVector2 pos = frame.getWorldFrame().getBall().getPos();
					return Optional.of(new PenaltyKickFailed(attackingTeam, pos));
				}
			}
		}

		return Optional.empty();
	}
}
