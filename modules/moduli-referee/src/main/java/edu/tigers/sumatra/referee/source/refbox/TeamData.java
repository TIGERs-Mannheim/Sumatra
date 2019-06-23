/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source.refbox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TeamData
{
	/** The team's name (empty string if operator has not typed anything). */
	private String name;
	
	/** The number of goals scored by the team during normal play and overtime. */
	private int score;
	
	/** The number of red cards issued to the team since the beginning of the game. */
	private int redCards;
	
	/** The total number of yellow cards ever issued to the team. */
	private int yellowCards;
	
	/**
	 * The amount of time (in microseconds) left on each yellow card issued to the team.
	 * If no yellow cards are issued, this array has no elements.
	 * Otherwise, times are ordered from smallest to largest.
	 */
	private List<Integer> yellowCardTimes = new ArrayList<>();
	
	/**
	 * The number of timeouts this team can still call.
	 * If in a timeout right now, that timeout is excluded.
	 */
	private int timeouts = 4;
	
	/** The number of microseconds of timeout this team can use. */
	private int timeoutTime = (int) TimeUnit.MINUTES.toMicros(5);
	
	/** The pattern number of this team's goalie. */
	private int goalie;
	
	
	/** Constructor. */
	public TeamData()
	{
		name = "Unknown";
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param teamName
	 */
	public TeamData(final String teamName)
	{
		name = teamName;
	}
	
	
	/**
	 * Reset all values to "begin of match" values.
	 */
	public void reset()
	{
		score = 0;
		redCards = 0;
		yellowCards = 0;
		yellowCardTimes.clear();
		timeouts = 4;
		timeoutTime = (int) TimeUnit.MINUTES.toMicros(5);
	}
	
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	
	/**
	 * @param name the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}
	
	
	/**
	 * @return the score
	 */
	public int getScore()
	{
		return score;
	}
	
	
	/**
	 * @param score the score to set
	 */
	public void setScore(final int score)
	{
		this.score = score;
	}
	
	
	/**
	 * Add a goal.
	 */
	public void addScore()
	{
		++score;
	}
	
	
	/**
	 * @return the redCards
	 */
	public int getRedCards()
	{
		return redCards;
	}
	
	
	/**
	 * @param redCards the redCards to set
	 */
	public void setRedCards(final int redCards)
	{
		this.redCards = redCards;
	}
	
	
	/**
	 * Add a red card.
	 */
	public void addRedCard()
	{
		++redCards;
	}
	
	
	/**
	 * @return the yellowCards
	 */
	public int getYellowCards()
	{
		return yellowCards;
	}
	
	
	/**
	 * @param yellowCards the yellowCards to set
	 */
	public void setYellowCards(final int yellowCards)
	{
		this.yellowCards = yellowCards;
	}
	
	
	/**
	 * @return the yellowCardTimes
	 */
	public List<Integer> getYellowCardTimes()
	{
		return yellowCardTimes;
	}
	
	
	/**
	 * @param yellowCardTimes the yellowCardTimes to set
	 */
	public void setYellowCardTimes(final List<Integer> yellowCardTimes)
	{
		this.yellowCardTimes = yellowCardTimes;
	}
	
	
	/**
	 * Add a yellow card.
	 */
	public void addYellowCard()
	{
		yellowCardTimes.add((int) TimeUnit.MINUTES.toMicros(2));
		++yellowCards;
	}
	
	
	/**
	 * Tick yellow cards with some time that passed.
	 * Timed out yellow cards will be removed.
	 * 
	 * @param timePassed
	 */
	public void tickYellowCards(final int timePassed)
	{
		yellowCardTimes = yellowCardTimes.stream()
				.map(time -> time - timePassed)
				.filter(time -> time > 0)
				.collect(Collectors.toList());
	}
	
	
	/**
	 * @return the timeouts
	 */
	public int getTimeouts()
	{
		return timeouts;
	}
	
	
	/**
	 * @param timeouts the timeouts to set
	 */
	public void setTimeouts(final int timeouts)
	{
		this.timeouts = timeouts;
	}
	
	
	/**
	 * Take a timeout.
	 */
	public void takeTimeout()
	{
		if (timeouts > 0)
		{
			--timeouts;
		}
	}
	
	
	/**
	 * @return the timeoutTime
	 */
	public int getTimeoutTime()
	{
		return timeoutTime;
	}
	
	
	/**
	 * @param timeoutTime the timeoutTime to set
	 */
	public void setTimeoutTime(final int timeoutTime)
	{
		this.timeoutTime = timeoutTime;
	}
	
	
	/**
	 * Tick the timeout clock.
	 * 
	 * @param timePassed
	 */
	public void tickTimeoutTime(final int timePassed)
	{
		timeoutTime -= timePassed;
		if (timeoutTime < 0)
		{
			timeoutTime = 0;
		}
	}
	
	
	/**
	 * @return the goalie
	 */
	public int getGoalie()
	{
		return goalie;
	}
	
	
	/**
	 * @param goalie the goalie to set
	 */
	public void setGoalie(final int goalie)
	{
		this.goalie = goalie;
	}
}
