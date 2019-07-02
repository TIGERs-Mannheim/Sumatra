/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.geometry.Geometry;


/**
 * This class makes some tactical analysis to create a table with the number of bots
 * assigned to defend our goal.
 * It uses the algorithm described in
 * <a href=
 * "https://web.archive.org/web/20190513115938/https://ssl.robocup.org/wp-content/uploads/2019/01/2016_ETDP_CMDragons.pdf">
 * CMDragons' 2016 TDP</a>.
 */
public class NumDefenderCalc extends ACalculator
{
	@Configurable(comment = "Amount of minimum defenders to keep at all times", defValue = "1")
	private static int minDefendersAtAllTimes = 1;

	@Configurable(comment = "Maximal velocity of the ball in the penalty area if the keeper will chip, to free defenders", defValue = "0.1")
	private static double maxBallVelocityOfBallInPenaltyAreaToFreeDefenders = 0.1;


	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final int nAvailableBots = availableBots();
		int nDefender = defaultNumDefenders(nAvailableBots);

		// clamp nDefender between minDefendersAtAllTimes and nAvailableBots
		nDefender = Math.max(minDefenders(), nDefender);
		nDefender = Math.min(nAvailableBots, nDefender);

		// if offense has ball responsibility but we used all defenders we release one here
		if (nDefender == nAvailableBots
				&& nAvailableBots > 0
				&& getNewTacticalField().getBallResponsibility() == EBallResponsibility.OFFENSE)
		{
			nDefender = nAvailableBots - 1;
		}

		newTacticalField.setAvailableAttackers(Math.max(0, nAvailableBots - nDefender));
		newTacticalField.setNumDefender(nDefender);
	}


	private int minDefenders()
	{
		boolean insaneKeeper = getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(b -> getAiFrame().getKeeperId() == b).orElse(false);
		if (insaneKeeper)
		{
			return 0;
		}
		return minDefendersAtAllTimes;
	}


	private int availableBots()
	{
		// number of available TIGER bots minus the keeper and interchange bots
		int nKeeper = getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId()) ? 1 : 0;
		return getWFrame().getTigerBotsVisible().size()
				- nKeeper
				- getNewTacticalField().getBotInterchange().getNumInterchangeBots();
	}


	private int defaultNumDefenders(final int nAvailableBots)
	{
		if (getAiFrame().getGamestate().isStandardSituationForUs()
				|| getAiFrame().getGamestate().isNextStandardSituationForUs())
		{
			return nDefenderStandardWe();
		}

		if (getAiFrame().getGamestate().isStoppedGame()
				|| getAiFrame().getGamestate().isStandardSituationForThem())
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
			return getNewTacticalField().getNumDefenderForBall();
		}

		int nThreats = getNewTacticalField().getDefenseBotThreats().size();
		return ((nThreats + 1) / 2)
				+ getNewTacticalField().getNumDefenderForBall();
	}


	private boolean isBallSafeAtOurKeeper()
	{
		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), -Geometry.getBotRadius())
				&& (getBall().getVel().getLength() < maxBallVelocityOfBallInPenaltyAreaToFreeDefenders)
				&& (getNewTacticalField().getKeeperState() == EKeeperState.CHIP_FAST);
	}
}
