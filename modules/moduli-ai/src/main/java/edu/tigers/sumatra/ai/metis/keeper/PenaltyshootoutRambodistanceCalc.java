/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ids.ETeamColor;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Logger;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.Referee.SSL_Referee.Command.NORMAL_START;
import static edu.tigers.sumatra.Referee.SSL_Referee.Stage.PENALTY_SHOOTOUT;


/**
 * This calculator calculate RamboDistance
 * for RamboState of KeeperOneOnOneRole at PenaltyShootout
 * The value of RamboDistance determines whether the KeeperState is changing to RamboState at PenaltyShootout
 * The RamboDistance is initially set to a configured Value. The keeper tries this value for defending the goal.
 * If the keeper fails, the Calculator remembers this distance and searches for new Values. A new RamboDistance is set
 * to the center of the biggest interval between failed memorized distances and bounds (0, maxKeeperRamboDistance)
 * for causing the biggest effect for next shootout round.
 *
 * @author StefanSch
 */
public class PenaltyshootoutRambodistanceCalc extends ACalculator
{
	private static Logger logger = Logger.getLogger(PenaltyshootoutRambodistanceCalc.class);
	
	@Configurable(comment = "Keeper rambo distance", defValue = "2000.0")
	private double keeperRamboDistance = 2000;
	@Configurable(comment = "Maximal keeper rambo distance", defValue = "5000.0")
	private double maxKeeperRamboDistance = 5000;
	
	private List<ShootoutRound> lastShootoutRoundResults = new LinkedList<>();
	private int lastStartTheirGoals = -1;
	
	
	/**
	 * Tracks FoeGoals during PenaltyShootout to learn from it
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		ETeamColor foeTeamColor = ETeamColor.opposite(baseAiFrame.getTeamColor());
		
		int theirGoals = baseAiFrame.getRefereeMsg().getGoals().get(foeTeamColor);
		
		if (lastStartTheirGoals >= 0)
		{
			// Update ShootoutResults
			ShootoutRound lastShootoutRoundResult = new ShootoutRound(keeperRamboDistance,
					lastStartTheirGoals < theirGoals);
			List<ShootoutRound> overrideShootoutRounds = lastShootoutRoundResults.stream()
					.filter(shootoutRound -> Math.abs(shootoutRound.ramboDistance - keeperRamboDistance) < 10e-10)
					.collect(Collectors.toList());
			lastShootoutRoundResults.removeAll(overrideShootoutRounds);
			lastShootoutRoundResults.add(lastShootoutRoundResult);
			
			// Setting new RamboDistance
			keeperRamboDistance = getNewRamboDistance();
			newTacticalField.setKeeperRamboDistance(keeperRamboDistance);
		}
		
		lastStartTheirGoals = theirGoals;
		
	}
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame baseAiFrame)
	{
		ETeamColor foeTeamColor = ETeamColor.opposite(baseAiFrame.getTeamColor());
		
		Referee.SSL_Referee.Stage stage = baseAiFrame.getRefereeMsg().getStage();
		ETeamColor shooterTeam = baseAiFrame.getGamestate().getForTeam();
		
		
		return (stage == PENALTY_SHOOTOUT && shooterTeam == foeTeamColor &&
				baseAiFrame.isNewRefereeMsg() && baseAiFrame.getRefereeMsg().getCommand() == NORMAL_START);
	}
	
	
	private double getNewRamboDistance()
	{
		if (lastShootoutRoundResults.isEmpty())
		{
			return keeperRamboDistance;
		}
		
		Optional<ShootoutRound> shootoutWithoutGoal = lastShootoutRoundResults.stream()
				.filter(shootoutRound -> !shootoutRound.isGoal()).findFirst();
		
		
		if (shootoutWithoutGoal.isPresent())
		{
			// If there is a positive Result in ShootoutResults
			double ramboDistance = shootoutWithoutGoal.get().getRamboDistance();
			logger.info("Taking RamboDistance from positive memorized Result ("  + ramboDistance + ")");
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

			logger.info("Trying new RamboDistance (" + nextRamboDistance + ")");

			return nextRamboDistance;
		}
	}
	
	/**
	 * Data structure for memorising last Shootouts
	 */
	private class ShootoutRound
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
