/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.statistics.calculators.PenaltyStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.StatisticData;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.statistics.MarkovChain;


/**
 * Data holder for StatisticsCalc elements for persisting them.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@Persistent(version = 4)
public class MatchStatistics
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger							log							= Logger.getLogger(MatchStatistics.class
																											.getName());
	
	private final Map<EBallPossession, Percentage>	ballPossessionGeneral;
	private Map<Integer, Percentage>						ballPossessionTigers;
	private final Map<BotID, Percentage>				ballPossessionOpponents;
	
	private Map<BotID, Percentage>						tackleWon;
	private Map<BotID, Percentage>						tackleLost;
	private Percentage										tackleGeneralWon;
	private Percentage										tackleGeneralLost;
	
	private int													possibleTigersGoals		= 0;
	private int													possibleOpponentsGoals	= 0;
	private Map<BotID, Percentage>						possibleBotGoals;
	
	private List<PenaltyStats>								bestPenaltyShooters;
	
	private BotIDMap<Integer>								countFramesAsDefender;
	private BotIDMap<Integer>								countFramesAsOffensive;
	private BotIDMap<Integer>								countFramesAsSupporter;
	
	private BotIDMap<MarkovChain<ERole>>				roleTransitions;
	
	private Map<Integer, Percentage>						passAccuracy;
	
	/**
	 * This enum is giving an overview of the available statistics
	 * 
	 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
	 */
	public enum EAvailableStatistic
	{
		/** This is the statistic for ball possession */
		BallPossession("Ballbesitz"),
		/** This is the statistic for transitions from defensive to offensive */
		DefensiveToOffensive("D->O"),
		/** This is the statistic for transitions from defensive to support */
		DefensiveToSupport("D->S"),
		/** This are the frames as defender */
		FramesAsDefender("Defender"),
		/** Shows the frames as Defender */
		FramesAsOffensive("Offensive"),
		/** Shows the frames as Supporter */
		FramesAsSupport("Supporter"),
		/** This is the statistic for scored goals */
		GoalsScored("Tore"),
		/** This is the statistic for transitions from offensive to support */
		OffensiveToSupport("O->S"),
		/** This is the statistic for transitions from offensive to defensive */
		OffensiveToDefensive("O->D"),
		/** This is the statistic for a count how many times it was an active pass target */
		PassTarget("Passziel"),
		/** This is the statistic for transitions from support to defensive */
		SupportToDefensive("S->D"),
		/** This is the statistic for transitions from support to offensive */
		SupportToOffensive("S->O"),
		/** This is the statistic for won tackles */
		TacklesWon("Gewonnene Zweikämpfe"),
		/** This is the statistic for lost tackles */
		TacklesLost("Verlorene Zweikämpfe");
		
		private String	descriptor;
		
		
		EAvailableStatistic(final String descriptor)
		{
			this.descriptor = descriptor;
		}
		
		
		/**
		 * @return A human readable Descriptor of a specific Statistic
		 */
		public String getDescriptor()
		{
			return descriptor;
		}
	}
	
	private Map<EAvailableStatistic, StatisticData>	statistics	= new HashMap<>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the statistics
	 */
	public Map<EAvailableStatistic, StatisticData> getStatistics()
	{
		return statistics;
	}
	
	
	/**
	 * This adds a specific type of Statistic data to be displayed in the statisticsPanel
	 * 
	 * @param key The Statistic Type to be put
	 * @param value The Statistic to be put
	 */
	public void putStatisticData(final EAvailableStatistic key, final StatisticData value)
	{
		statistics.put(key, value);
	}
	
	
	/**
	 * 
	 */
	public MatchStatistics()
	{
		ballPossessionGeneral = new HashMap<EBallPossession, Percentage>();
		for (EBallPossession bp : EBallPossession.values())
		{
			ballPossessionGeneral.put(bp, new Percentage());
		}
		ballPossessionTigers = new HashMap<Integer, Percentage>();
		ballPossessionOpponents = new HashMap<BotID, Percentage>();
		tackleWon = new HashMap<BotID, Percentage>();
		tackleLost = new HashMap<BotID, Percentage>();
		tackleGeneralWon = new Percentage();
		tackleGeneralLost = new Percentage();
		possibleBotGoals = new HashMap<BotID, Percentage>();
		
		countFramesAsDefender = new BotIDMap<Integer>();
		countFramesAsOffensive = new BotIDMap<Integer>();
		countFramesAsSupporter = new BotIDMap<Integer>();
		
		roleTransitions = new BotIDMap<MarkovChain<ERole>>();
		
		passAccuracy = new HashMap<>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Prints statistic
	 */
	public void printStatistic()
	{
		DecimalFormat df = new DecimalFormat("###.##");
		StringBuffer message = new StringBuffer("\n");
		for (Entry<EBallPossession, Percentage> entry : ballPossessionGeneral.entrySet())
		{
			message.append(entry.getKey().name()).append(": ").append(df.format((entry.getValue().getPercent() * 100)))
					.append("%").append(" - "
					).append(entry.getValue().getCurrent()).append("\n");
		}
		for (Entry<Integer, Percentage> entry : ballPossessionTigers.entrySet())
		{
			message.append("Tiger: ").append(entry.getKey().intValue()).append(": "
					).append(df.format((entry.getValue().getPercent() * 100))).append("%"
					).append(" - "
					).append(entry.getValue().getCurrent()).append("\n");
		}
		for (Entry<BotID, Percentage> entry : ballPossessionOpponents.entrySet())
		{
			message.append("Opponent: ").append(entry.getKey().getNumber()).append(": "
					).append(df.format((entry.getValue().getPercent() * 100))).append("%"
					).append(" - "
					).append(entry.getValue().getCurrent()).append("\n");
		}
		for (Entry<BotID, Percentage> entry : tackleWon.entrySet())
		{
			message.append("Won: ").append(entry.getKey().getNumber()).append(": ")
					.append(df.format((entry.getValue().getPercent() * 100))
					).append("%"
					).append(" - "
					).append(entry.getValue().getCurrent()).append("\n");
		}
		for (Entry<BotID, Percentage> entry : tackleLost.entrySet())
		{
			message.append("Lost: ").append(entry.getKey().getNumber()).append(": ")
					.append(df.format((entry.getValue().getPercent() * 100))
					).append("%"
					).append(" - "
					).append(entry.getValue().getCurrent()).append("\n");
		}
		message.append("Won: ").append(df.format((tackleGeneralWon.getPercent() * 100))
				).append("%"
				).append(" - "
				).append(tackleGeneralWon.getCurrent()).append("\n");
		message.append("Lost: ").append(df.format((tackleGeneralLost.getPercent() * 100))
				).append("%"
				).append(" - "
				).append(tackleGeneralLost.getCurrent()).append("\n");
		log.trace(message.toString());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the ballPossessionGeneral
	 */
	public Map<EBallPossession, Percentage> getBallPossessionGeneral()
	{
		return ballPossessionGeneral;
	}
	
	
	/**
	 * @return the ballPossessionTigers
	 */
	public Map<Integer, Percentage> getBallPossessionTigers()
	{
		return ballPossessionTigers;
	}
	
	
	/**
	 * @param ballPossessionTigers2 the ballPossessionTigers to set
	 */
	public void setBallPossessionTigers(final Map<Integer, Percentage> ballPossessionTigers2)
	{
		ballPossessionTigers = ballPossessionTigers2;
	}
	
	
	/**
	 * @return the ballPossessionOpponents
	 */
	public Map<BotID, Percentage> getBallPossessionOpponents()
	{
		return ballPossessionOpponents;
	}
	
	
	/**
	 * @return the tackleWon
	 */
	public Map<BotID, Percentage> getTackleWon()
	{
		return tackleWon;
	}
	
	
	/**
	 * @param tackleWon the tackleWon to set
	 */
	public void setTackleWon(final Map<BotID, Percentage> tackleWon)
	{
		this.tackleWon = tackleWon;
	}
	
	
	/**
	 * @return the tackleLost
	 */
	public Map<BotID, Percentage> getTackleLost()
	{
		return tackleLost;
	}
	
	
	/**
	 * @param tackleLost the tackleLost to set
	 */
	public void setTackleLost(final Map<BotID, Percentage> tackleLost)
	{
		this.tackleLost = tackleLost;
	}
	
	
	/**
	 * @param tackleGeneralWon
	 * @param tackleGeneralLost
	 */
	public void setTackleGeneral(final Percentage tackleGeneralWon, final Percentage tackleGeneralLost)
	{
		this.tackleGeneralWon = tackleGeneralWon;
		this.tackleGeneralLost = tackleGeneralLost;
	}
	
	
	/**
	 * @return
	 */
	public Percentage getTackleGeneralWon()
	{
		return tackleGeneralWon;
	}
	
	
	/**
	 * @return
	 */
	public Percentage getTackleGeneralLost()
	{
		return tackleGeneralLost;
	}
	
	
	/**
	 * @return the possibleTigersGoals
	 */
	public int getPossibleTigersGoals()
	{
		return possibleTigersGoals;
	}
	
	
	/**
	 * @param possibleTigersGoals the possibleTigersGoals to set
	 */
	public void setPossibleTigersGoals(final int possibleTigersGoals)
	{
		this.possibleTigersGoals = possibleTigersGoals;
	}
	
	
	/**
	 * @return the possibleOpponentsGoals
	 */
	public int getPossibleOpponentsGoals()
	{
		return possibleOpponentsGoals;
	}
	
	
	/**
	 * @param possibleOpponentsGoals the possibleOpponentsGoals to set
	 */
	public void setPossibleOpponentsGoals(final int possibleOpponentsGoals)
	{
		this.possibleOpponentsGoals = possibleOpponentsGoals;
	}
	
	
	/**
	 * @param possibleBotGoals
	 */
	public void setPossibleBotGoals(final Map<BotID, Percentage> possibleBotGoals)
	{
		this.possibleBotGoals = possibleBotGoals;
	}
	
	
	/**
	 * @return the possibleBotGoals
	 */
	public Map<BotID, Percentage> getPossibleBotGoals()
	{
		return possibleBotGoals;
	}
	
	
	/**
	 * @param bestPenaltyShooters
	 */
	public void setBestPenaltyShooterStats(final List<PenaltyStats> bestPenaltyShooters)
	{
		this.bestPenaltyShooters = bestPenaltyShooters;
	}
	
	
	/**
	 * @return
	 */
	public List<PenaltyStats> getBestPenaltyShooterStats()
	{
		return bestPenaltyShooters;
	}
	
	
	/**
	 * @return the countFramesAsDefender
	 */
	public BotIDMap<Integer> getCountFramesAsDefender()
	{
		return countFramesAsDefender;
	}
	
	
	/**
	 * @param countFramesAsDefender the countFramesAsDefender to set
	 */
	public void setCountFramesAsDefender(final BotIDMap<Integer> countFramesAsDefender)
	{
		this.countFramesAsDefender = countFramesAsDefender;
	}
	
	
	/**
	 * @return the countFramesAsOffensive
	 */
	public BotIDMap<Integer> getCountFramesAsOffensive()
	{
		return countFramesAsOffensive;
	}
	
	
	/**
	 * @param countFramesAsOffensive the countFramesAsOffensive to set
	 */
	public void setCountFramesAsOffensive(final BotIDMap<Integer> countFramesAsOffensive)
	{
		this.countFramesAsOffensive = countFramesAsOffensive;
	}
	
	
	/**
	 * @return the countFramesAsSupporter
	 */
	public BotIDMap<Integer> getCountFramesAsSupporter()
	{
		return countFramesAsSupporter;
	}
	
	
	/**
	 * @param countFramesAsSupporter the countFramesAsSupporter to set
	 */
	public void setCountFramesAsSupporter(final BotIDMap<Integer> countFramesAsSupporter)
	{
		this.countFramesAsSupporter = countFramesAsSupporter;
	}
	
	
	/**
	 * @return the roleTransitions
	 */
	public BotIDMap<MarkovChain<ERole>> getRoleTransitions()
	{
		return roleTransitions;
	}
	
	
	/**
	 * @param roleTransitions the roleTransitions to set
	 */
	public void setRoleTransitions(final BotIDMap<MarkovChain<ERole>> roleTransitions)
	{
		this.roleTransitions = roleTransitions;
	}
	
	
	/**
	 * @return the passAccuracy
	 */
	public Map<Integer, Percentage> getPassAccuracy()
	{
		return passAccuracy;
	}
	
	
	/**
	 * @param passAccuracyBots the passAccuracy to set
	 */
	public void setPassAccuracy(final Map<Integer, Percentage> passAccuracyBots)
	{
		passAccuracy = passAccuracyBots;
	}
	
}
