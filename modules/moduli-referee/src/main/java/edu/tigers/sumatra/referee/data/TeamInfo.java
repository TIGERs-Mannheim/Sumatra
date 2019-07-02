/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.Referee.SSL_Referee;


/**
 * Persistent wrapper for TeamInfo
 */
@Persistent(version = 1)
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

	private final int foulCounter;
	private final int ballPlacementFailures;
	private final boolean canPlaceBall;
	private final int maxAllowedBots;
	private final boolean botSubstitutionIntent;


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
		foulCounter = 0;
		ballPlacementFailures = 0;
		canPlaceBall = true;
		maxAllowedBots = 8;
		botSubstitutionIntent = false;
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
		goalie = teamInfo.getGoalkeeper();
		foulCounter = teamInfo.hasFoulCounter() ? teamInfo.getFoulCounter() : 0;
		ballPlacementFailures = teamInfo.hasBallPlacementFailures() ? teamInfo.getBallPlacementFailures() : 0;
		canPlaceBall = !teamInfo.hasCanPlaceBall() || teamInfo.getCanPlaceBall();
		maxAllowedBots = teamInfo.hasMaxAllowedBots() ? teamInfo.getMaxAllowedBots() : 8;
		botSubstitutionIntent = teamInfo.hasBotSubstitutionIntent() && teamInfo.getBotSubstitutionIntent();
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


	public int getFoulCounter()
	{
		return foulCounter;
	}


	public int getBallPlacementFailures()
	{
		return ballPlacementFailures;
	}


	public boolean isCanPlaceBall()
	{
		return canPlaceBall;
	}


	public int getMaxAllowedBots()
	{
		return maxAllowedBots;
	}


	public boolean isBotSubstitutionIntent()
	{
		return botSubstitutionIntent;
	}


	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", name)
				.append("score", score)
				.append("redCards", redCards)
				.append("yellowCards", yellowCards)
				.append("yellowCardsTimes", yellowCardsTimes)
				.append("timeouts", timeouts)
				.append("timeoutTime", timeoutTime)
				.append("goalie", goalie)
				.append("foulCounter", foulCounter)
				.append("ballPlacementFailures", ballPlacementFailures)
				.append("canPlaceBall", canPlaceBall)
				.append("maxAllowedBots", maxAllowedBots)
				.append("maxAllowedBots", botSubstitutionIntent)
				.toString();
	}
}
