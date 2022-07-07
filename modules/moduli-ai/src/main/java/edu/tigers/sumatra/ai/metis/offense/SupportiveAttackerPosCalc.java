/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.general.ESkirmishStrategy;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


/**
 * Find a position for the supportive attacker.
 */
@RequiredArgsConstructor
public class SupportiveAttackerPosCalc extends ACalculator
{
	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<RedirectorDetectionInformation> redirectorDetectionInformation;
	private final Supplier<BotDistance> opponentClosestToBall;

	@Configurable(defValue = "1400.0")
	private static double defaultDistToBall = 1400.0;

	@Getter
	private IVector2 supportiveAttackerMovePos;


	@Override
	public void doCalc()
	{
		supportiveAttackerMovePos = calcMovePos();
	}


	private IVector2 calcMovePos()
	{
		if (bothTeamsAreReceiving())
		{
			return calcMovePosBetweenOurGoalAndReceiver();
		} else if (skirmishInformation.get().getStrategy() == ESkirmishStrategy.FREE_BALL)
		{
			// Add a supportive attacker that helps the primary attacker in a skirmish
			return skirmishInformation.get().getSupportiveCircleCatchPos();
		}
		return calcBestSupportiveMovePos();
	}


	private boolean bothTeamsAreReceiving()
	{
		var rInfo = redirectorDetectionInformation.get();
		return rInfo.isFriendlyBotReceiving() && rInfo.isOpponentReceiving();
	}


	private IVector2 calcMovePosBetweenOurGoalAndReceiver()
	{
		IVector2 opponent = redirectorDetectionInformation.get().getOpponentReceiverPos();
		IVector2 dir = Geometry.getGoalOur().getCenter().subtractNew(opponent);
		return opponent.addNew(dir.scaleToNew(Geometry.getBotRadius() * 2.5));
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
			dir = Vector2f.fromX(-1);
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
