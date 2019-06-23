/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;


/**
 * Persistent wrapper for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.TeamInfo}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class TeamInfo
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String				name;
	private final int					score;
	private final int					redCards;
	private final int					yellowCards;
	private final List<Integer>	yellowCardsTimes;
	private final int					timeouts;
	private final int					timeoutTime;
	private final int					goalie;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private TeamInfo()
	{
		name = "";
		score = 0;
		redCards = 0;
		yellowCards = 0;
		yellowCardsTimes = new ArrayList<Integer>(0);
		timeouts = 0;
		timeoutTime = 0;
		goalie = 0;
	}
	
	
	/**
	 * @param teamInfo
	 */
	public TeamInfo(final SSL_Referee.TeamInfo teamInfo)
	{
		name = teamInfo.getName();
		score = teamInfo.getScore();
		redCards = teamInfo.getRedCards();
		yellowCards = teamInfo.getYellowCards();
		yellowCardsTimes = new ArrayList<Integer>(teamInfo.getYellowCardTimesList());
		timeouts = teamInfo.getTimeouts();
		timeoutTime = teamInfo.getTimeoutTime();
		goalie = teamInfo.getGoalie();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}
	
	
	/**
	 * @return the score
	 */
	public final int getScore()
	{
		return score;
	}
	
	
	/**
	 * @return the redCards
	 */
	public final int getRedCards()
	{
		return redCards;
	}
	
	
	/**
	 * @return the yellowCards
	 */
	public final int getYellowCards()
	{
		return yellowCards;
	}
	
	
	/**
	 * @return the yellowCardsTimes
	 */
	public final List<Integer> getYellowCardsTimes()
	{
		return yellowCardsTimes;
	}
	
	
	/**
	 * @return the timeouts
	 */
	public final int getTimeouts()
	{
		return timeouts;
	}
	
	
	/**
	 * @return the timeoutTime
	 */
	public final int getTimeoutTime()
	{
		return timeoutTime;
	}
	
	
	/**
	 * @return the goalie
	 */
	public final int getGoalie()
	{
		return goalie;
	}
}
