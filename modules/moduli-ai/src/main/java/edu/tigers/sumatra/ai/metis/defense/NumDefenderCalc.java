/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Supplier;


/**
 * This class makes some tactical analysis to create a table with the number of bots
 * assigned to defend our goal.
 * It uses the algorithm described in
 * <a href=
 * "https://web.archive.org/web/20190513115938/https://ssl.robocup.org/wp-content/uploads/2019/01/2016_ETDP_CMDragons.pdf">
 * CMDragons' 2016 TDP</a>.
 */
@RequiredArgsConstructor
public class NumDefenderCalc extends ACalculator
{
	@Configurable(comment = "Amount of minimum defenders to keep at all times", defValue = "1")
	private static int minDefendersAtAllTimes = 1;

	@Configurable(defValue = "1.5")
	private static double threatsToNumDefenderDivider = 1.5;

	private final Supplier<EBallResponsibility> ballResponsibility;
	private final Supplier<Integer> numDefenderForBall;
	private final Supplier<List<BotID>> botsToInterchange;
	private final Supplier<List<DefenseBotThreat>> defenseBotThreats;

	@Getter
	private int availableAttackers;
	@Getter
	private int numDefender;


	@Override
	public void doCalc()
	{
		int nAvailableBots = availableBots();
		numDefender = defaultNumDefenders(nAvailableBots);

		// clamp numDefender between minDefendersAtAllTimes and nAvailableBots
		numDefender = Math.max(minDefendersAtAllTimes, numDefender);
		numDefender = Math.min(nAvailableBots, numDefender);

		// if offense has ball responsibility but we used all defenders we release one here
		if (numDefender == nAvailableBots
				&& nAvailableBots > 0
				&& ballResponsibility.get() == EBallResponsibility.OFFENSE)
		{
			numDefender = nAvailableBots - 1;
		}

		availableAttackers = Math.max(0, nAvailableBots - numDefender);
	}


	private int availableBots()
	{
		// number of available TIGER bots minus the keeper and interchange bots
		int nKeeper = getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId()) ? 1 : 0;
		return getWFrame().getTigerBotsVisible().size()
				- nKeeper
				- botsToInterchange.get().size();
	}


	private int defaultNumDefenders(final int nAvailableBots)
	{
		if (getAiFrame().getGameState().isStandardSituationForUs()
				|| getAiFrame().getGameState().isNextStandardSituationForUs())
		{
			return nDefenderStandardWe();
		}

		if (getAiFrame().getGameState().isStoppedGame()
				|| getAiFrame().getGameState().isStandardSituationForThem())
		{
			return nAvailableBots;
		}
		return nDefenderRunning();
	}


	/**
	 * We need supporters in own standard
	 *
	 * @return
	 */
	private int nDefenderStandardWe()
	{
		double ballXPos = getWFrame().getBall().getPos().x() + (Geometry.getFieldLength() / 2);
		double percentageToTheirGoal = ballXPos / Geometry.getFieldLength();

		if (percentageToTheirGoal > 0.66)
		{
			return 0;
		} else if (percentageToTheirGoal > 0.33)
		{
			return 1;
		} else
		{
			return 2;
		}
	}


	private int nDefenderRunning()
	{
		if (isBallSafeAtOurKeeper())
		{
			return numDefenderForBall.get();
		}

		int nThreats = defenseBotThreats.get().size();
		return ((int) Math.ceil(nThreats / threatsToNumDefenderDivider)
				+ numDefenderForBall.get());
	}


	private boolean isBallSafeAtOurKeeper()
	{
		IVector2 ballStopPos = getBall().getTrajectory().getPosByVel(0.0).getXYVector();
		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), -Geometry.getBotRadius())
				&& Geometry.getPenaltyAreaOur().isPointInShape(ballStopPos, -Geometry.getBotRadius());
	}
}
