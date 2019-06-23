/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.data;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.Referee.SSL_Referee;


/**
 * Persistent wrapper for TeamInfo
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class TeamInfo
{
	private final String name;
	private final int score;
	private final int redCards;
	private final int yellowCards;
	/**
	 * The amount of time (in microseconds) left on each yellow card issued to the team.
	 * If no yellow cards are issued, this array has no elements.
	 * Otherwise, times are ordered from smallest to largest.
	 */
	private final List<Integer> yellowCardsTimes;
	private final int timeouts;
	/** in microseconds */
	private final int timeoutTime;
	private final int goalie;
	
	
	@SuppressWarnings("unused")
	TeamInfo()
	{
		name = "";
		score = 0;
		redCards = 0;
		yellowCards = 0;
		yellowCardsTimes = new ArrayList<>(0);
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
		yellowCardsTimes = new ArrayList<>(teamInfo.getYellowCardTimesList());
		timeouts = teamInfo.getTimeouts();
		timeoutTime = teamInfo.getTimeoutTime();
		goalie = teamInfo.getGoalie();
	}
	
	
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
	 * The amount of time (in microseconds) left on each yellow card issued to the team.
	 * If no yellow cards are issued, this array has no elements.
	 * Otherwise, times are ordered from smallest to largest.
	 * 
	 * @return the yellowCardsTimes in microseconds
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
	 * @return the timeoutTime left for the team in microseconds
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
	
	
	@Override
	public String toString()
	{
		return "TeamInfo{" +
				"name='" + name + '\'' +
				", score=" + score +
				", redCards=" + redCards +
				", yellowCards=" + yellowCards +
				", yellowCardsTimes=" + yellowCardsTimes +
				", timeouts=" + timeouts +
				", timeoutTime=" + timeoutTime +
				", goalie=" + goalie +
				'}';
	}
}
