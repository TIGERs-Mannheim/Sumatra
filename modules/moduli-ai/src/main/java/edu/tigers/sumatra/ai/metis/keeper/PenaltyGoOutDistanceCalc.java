/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command.NORMAL_START;
import static edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Stage.PENALTY_SHOOTOUT;


/**
 * This calculator calculate distance where the keeper should go out during a penalty kick.
 * The distance is initially set to a configured value.
 * The keeper tries this value for defending the goal.
 * If the keeper fails, the calculator remembers this distance and searches for new values.
 * A new distance is set to the center of the biggest interval between failed memorized distances and bounds (0, maxKeeperRamboDistance)
 * for causing the biggest effect for the next penalty.
 */
@Log4j2
public class PenaltyGoOutDistanceCalc extends ACalculator
{
	@Configurable(comment = "Keeper rambo distance", defValue = "2000.0")
	private double defaultKeeperRamboDistance = 2000;
	@Configurable(comment = "Maximal keeper rambo distance", defValue = "5000.0")
	private double maxKeeperRamboDistance = 5000;

	private List<ShootoutRound> lastShootoutRoundResults = new LinkedList<>();
	private int lastStartTheirGoals = -1;

	@Getter
	private double keeperRamboDistance = defaultKeeperRamboDistance;


	@Override
	public boolean isCalculationNecessary()
	{
		ETeamColor opponentTeamColor = ETeamColor.opposite(getAiFrame().getTeamColor());

		SslGcRefereeMessage.Referee.Stage stage = getAiFrame().getRefereeMsg().getStage();
		ETeamColor shooterTeam = getAiFrame().getGameState().getForTeam();

		return (stage == PENALTY_SHOOTOUT && shooterTeam == opponentTeamColor &&
				getAiFrame().isNewRefereeMsg() && getAiFrame().getRefereeMsg().getCommand() == NORMAL_START);
	}


	@Override
	public void doCalc()
	{
		ETeamColor opponentTeamColor = ETeamColor.opposite(getAiFrame().getTeamColor());

		int theirGoals = getAiFrame().getRefereeMsg().getGoals().get(opponentTeamColor);

		if (lastStartTheirGoals >= 0)
		{
			// Update ShootoutResults
			ShootoutRound lastShootoutRoundResult = new ShootoutRound(defaultKeeperRamboDistance,
					lastStartTheirGoals < theirGoals);
			List<ShootoutRound> overrideShootoutRounds = lastShootoutRoundResults.stream()
					.filter(shootoutRound -> Math.abs(shootoutRound.ramboDistance - defaultKeeperRamboDistance) < 10e-10)
					.collect(Collectors.toList());
			lastShootoutRoundResults.removeAll(overrideShootoutRounds);
			lastShootoutRoundResults.add(lastShootoutRoundResult);

			// Setting new RamboDistance
			keeperRamboDistance = getNewRamboDistance();
		}

		lastStartTheirGoals = theirGoals;
	}


	private double getNewRamboDistance()
	{
		if (lastShootoutRoundResults.isEmpty())
		{
			return defaultKeeperRamboDistance;
		}

		Optional<ShootoutRound> shootoutWithoutGoal = lastShootoutRoundResults.stream()
				.filter(shootoutRound -> !shootoutRound.isGoal()).findFirst();


		if (shootoutWithoutGoal.isPresent())
		{
			// If there is a positive Result in ShootoutResults
			double ramboDistance = shootoutWithoutGoal.get().getRamboDistance();
			log.info("Taking RamboDistance from positive memorized Result (" + ramboDistance + ")");
			return shootoutWithoutGoal.get().getRamboDistance();
		} else
		{
			// Searches for biggest Interval between ramboDistances from lastShootoutRoundResults
			// and takes the value of the center as next RamboDistance
			lastShootoutRoundResults.sort(Comparator.comparingDouble((ShootoutRound s) -> s.ramboDistance));

			double lengthBiggestInterval = lastShootoutRoundResults.get(0).getRamboDistance();
			double nextRamboDistance = lengthBiggestInterval * 0.5;
			for (int i = 1; i < lastShootoutRoundResults.size(); i++)
			{
				double ramboDistance0 = lastShootoutRoundResults.get(i - 1).getRamboDistance();
				double ramboDistance1 = lastShootoutRoundResults.get(i).getRamboDistance();
				double lengthInterval = ramboDistance1 - ramboDistance0;
				if (lengthInterval > lengthBiggestInterval)
				{
					lengthBiggestInterval = lengthInterval;
					nextRamboDistance = ramboDistance0 + lengthInterval * 0.5;
				}
			}
			double lastRamboDistance = lastShootoutRoundResults.get(lastShootoutRoundResults.size() - 1)
					.getRamboDistance();
			double lengthInterval = maxKeeperRamboDistance - lastRamboDistance;
			if (lengthInterval > lengthBiggestInterval)
			{
				nextRamboDistance = lastRamboDistance + lengthInterval * 0.5;
			}

			log.info("Trying new RamboDistance (" + nextRamboDistance + ")");

			return nextRamboDistance;
		}
	}


	/**
	 * Data structure for memorising last Shootouts
	 */
	private static class ShootoutRound
	{
		double ramboDistance;
		boolean goal;


		public ShootoutRound(double ramboDistance, boolean goal)
		{
			this.ramboDistance = ramboDistance;
			this.goal = goal;
		}


		public double getRamboDistance()
		{
			return this.ramboDistance;
		}


		public boolean isGoal()
		{
			return this.goal;
		}


		public void setGoal(boolean result)
		{
			this.goal = result;
		}
	}
}
