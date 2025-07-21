/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Find a position for the supportive attacker.
 */
@RequiredArgsConstructor
public class SupportiveAttackerPosCalc extends ACalculator
{
	@Configurable(defValue = "1400.0")
	private static double defaultDistToBall = 1400.0;

	private final Supplier<RedirectorDetectionInformation> redirectorDetectionInformation;
	private final Supplier<BotDistance> opponentClosestToBall;

	private IVector2 supportiveReceiveEarlyPos;
	@Getter
	private IVector2 supportiveDefaultPos;


	@Override
	public void doCalc()
	{
		supportiveReceiveEarlyPos = bothTeamsAreReceiving() ? calcMovePosEarlyReceiver() : null;
		supportiveDefaultPos = calcBestSupportiveMovePos();
	}


	public Optional<IVector2> getSupportiveReceiveEarlyPos()
	{
		return Optional.ofNullable(supportiveReceiveEarlyPos);
	}


	private boolean bothTeamsAreReceiving()
	{
		var rInfo = redirectorDetectionInformation.get();
		return rInfo.isFriendlyBotReceiving() && rInfo.isOpponentReceiving();
	}


	private IVector2 calcMovePosEarlyReceiver()
	{
		IVector2 opponentRec = redirectorDetectionInformation.get().getOpponentReceiverPos();
		IVector2 opponent = getWFrame().getOpponentBots()
				.values()
				.stream()
				.min(Comparator.comparingDouble(e -> opponentRec.distanceTo(e.getPos())))
				.map(ITrackedObject::getPos)
				.orElse(opponentRec);

		IVector2 dir = getBall().getPos().subtractNew(opponent);
		return opponent.addNew(dir.scaleToNew(Geometry.getBotRadius() * 3));
	}


	private IVector2 calcBestSupportiveMovePos()
	{
		var ballPos = getBall().getPos();
		var opponentId = opponentClosestToBall.get().getBotId();
		var opponentBot = getWFrame().getBot(opponentId);

		final IVector2 dir;
		if (opponentBot == null)
		{
			// towards our goal line
			dir = Vector2.fromX(-1);
		} else if (opponentBot.getPos().x() > ballPos.x())
		{
			// opposite side of opponent
			dir = ballPos.subtractNew(opponentBot.getPos()).normalizeNew();
		} else
		{
			// between our goal and opponent
			IVector2 goal = Geometry.getGoalOur().bisection(ballPos);
			dir = goal.subtractNew(ballPos).normalizeNew();
		}

		return ballPos.addNew(dir.multiplyNew(defaultDistToBall));
	}
}
