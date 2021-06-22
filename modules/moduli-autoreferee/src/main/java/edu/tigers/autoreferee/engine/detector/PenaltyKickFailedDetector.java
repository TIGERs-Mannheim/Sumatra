/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.PenaltyKickFailed;

import java.util.List;
import java.util.Optional;


public class PenaltyKickFailedDetector extends AGameEventDetector
{

	public PenaltyKickFailedDetector()
	{

		super(EGameEventDetectorType.PENALTY_KICK_FAILED, EGameState.PENALTY);

	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		Optional<Double> angle = frame.getWorldFrame().getBall().getVel()
				.angleTo(frame.getPreviousFrame().getWorldFrame().getBall().getVel());

		if (angle.isPresent() && Math.abs(angle.get()) >= Math.PI * 0.5)
		{
			boolean failed = false;

			List<BotPosition> lastBots = frame.getBotsLastTouchedBall();

			ETeamColor attackingTeam = frame.getGameState().getForTeam();
			ETeamColor defendingTeam = frame.getGameState().getForTeam().opposite();

			for (BotPosition bot : lastBots)
			{
				if (bot.getBotID().getTeamColor() == defendingTeam)
				{
					failed = true;
					break;
				}
			}

			if (failed && frame.isBallInsideField())
			{
				IVector2 pos = frame.getWorldFrame().getBall().getPos();
				return Optional.of(new PenaltyKickFailed(attackingTeam, pos));
			}
		}

		return Optional.empty();
	}
}
