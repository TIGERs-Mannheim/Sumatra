/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Data holder for StatisticsCalc elements for persisting them.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@Persistent
public class Statistics
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger					log							= Logger.getLogger(Statistics.class.getName());
	
	private Map<EBallPossession, Percentage>	ballPossessionGeneral;
	private Map<BotID, Percentage>				ballPossessionTigers;
	private Map<BotID, Percentage>				ballPossessionOpponents;
	
	private Map<BotID, Percentage>				tackleWon;
	private Map<BotID, Percentage>				tackleLost;
	private Percentage								tackleGeneralWon;
	private Percentage								tackleGeneralLost;
	
	private int											possibleTigersGoals		= 0;
	private int											possibleOpponentsGoals	= 0;
	private Map<BotID, Percentage>				possibleBotGoals;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
		for (Entry<BotID, Percentage> entry : ballPossessionTigers.entrySet())
		{
			message.append("Tiger: ").append(entry.getKey().getNumber()).append(": "
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
	 * @param ballPossessionGeneral the ballPossessionGeneral to set
	 */
	public void setBallPossessionGeneral(final Map<EBallPossession, Percentage> ballPossessionGeneral)
	{
		this.ballPossessionGeneral = ballPossessionGeneral;
	}
	
	
	/**
	 * @return the ballPossessionTigers
	 */
	public Map<BotID, Percentage> getBallPossessionTigers()
	{
		return ballPossessionTigers;
	}
	
	
	/**
	 * @param ballPossessionTigers the ballPossessionTigers to set
	 */
	public void setBallPossessionTigers(final Map<BotID, Percentage> ballPossessionTigers)
	{
		this.ballPossessionTigers = ballPossessionTigers;
	}
	
	
	/**
	 * @return the ballPossessionOpponents
	 */
	public Map<BotID, Percentage> getBallPossessionOpponents()
	{
		return ballPossessionOpponents;
	}
	
	
	/**
	 * @param ballPossessionOpponents the ballPossessionOpponents to set
	 */
	public void setBallPossessionOpponents(final Map<BotID, Percentage> ballPossessionOpponents)
	{
		this.ballPossessionOpponents = ballPossessionOpponents;
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
}
