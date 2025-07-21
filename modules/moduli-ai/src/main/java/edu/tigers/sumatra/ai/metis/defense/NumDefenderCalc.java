/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
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
	@Configurable(comment = "Minimum number of defenders to keep at all times", defValue = "2")
	private static int minDefendersAtAllTimes = 2;

	@Configurable(comment = "Minimum number of free bots available if ball responsibility is offense", defValue = "3")
	private static int minFreeBotsAtBallResponsibilityOffense = 3;

	@Configurable(defValue = "1.5")
	private static double threatsToNumDefenderDivider = 1.5;

	private final Supplier<EBallResponsibility> ballResponsibility;
	private final Supplier<Integer> numDefenderForBall;
	private final Supplier<List<BotID>> botsToInterchange;
	private final Supplier<List<DefenseBotThreatDefData>> defenseBotThreats;

	private final Supplier<DefensePassDisruptionAssignment> passDisruptionAssignment;

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

		// if offense has ball responsibility, but we used all supporters as defenders we release one here
		if (ballResponsibility.get() == EBallResponsibility.OFFENSE)
		{
			numDefender = SumatraMath.cap(numDefender, 0, nAvailableBots - minFreeBotsAtBallResponsibilityOffense);
		}

		numDefender = SumatraMath.cap(numDefender, 0, nAvailableBots - numMinFreeBots());
		availableAttackers = nAvailableBots - numDefender;
	}


	private int availableBots()
	{
		// number of available TIGER bots minus the keeper and interchange bots
		int nKeeper = getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId()) ? 1 : 0;
		return getWFrame().getTigerBotsAvailable().size()
				- nKeeper
				- botsToInterchange.get().size();
	}


	private int defaultNumDefenders(final int nAvailableBots)
	{
		var gameState = getAiFrame().getGameState();

		if (gameState.isStandardSituationForUs() || gameState.isNextStandardSituationForUs())
		{
			return nDefenderStandardWe();
		} else if (gameState.isNextStandardSituationForThem() || gameState.isStandardSituationForThem())
		{
			return nDefenderStandardThem(nAvailableBots);
		} else if (gameState.isStoppedGame() && getBall().getPos().x() < 0)
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


	private int nDefenderStandardThem(int nAvailableBots)
	{
		var pos = getAiFrame().getGameState().getBallPlacementPositionForUs();
		if (pos != null)
		{
			if (pos.x() > 0)
			{
				return nDefenderRunning();
			}
		} else
		{
			if (getBall().getPos().x() > 0)
			{
				return nDefenderRunning();
			}
		}

		return nAvailableBots;
	}


	private int nDefenderRunning()
	{
		int nBotThreats = defenseBotThreats.get().size();
		int nBotThreatDefender = ((int) Math.ceil(nBotThreats / threatsToNumDefenderDivider));
		int nDisrupt = passDisruptionAssignment.get() != null ? 1 : 0;
		int nBallDefender = numDefenderForBall.get();

		if (ballResponsibility.get() == EBallResponsibility.KEEPER)
		{
			return 2 * nBallDefender;
		}
		return nBallDefender + nDisrupt + nBotThreatDefender;
	}


	private int numMinFreeBots()
	{
		if (getAiFrame().getGameState().isNextStandardSituationForThem()
				|| getAiFrame().getGameState().isStandardSituationForThem())
		{
			return Math.min(minFreeBotsAtBallResponsibilityOffense, 1);
		}

		if (ballResponsibility.get() == EBallResponsibility.OFFENSE)
		{
			return minFreeBotsAtBallResponsibilityOffense;
		}

		return 0;

	}
}
