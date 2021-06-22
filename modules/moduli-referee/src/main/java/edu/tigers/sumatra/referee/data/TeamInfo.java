/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


/**
 * Persistent wrapper for TeamInfo
 */
@Persistent(version = 1)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamInfo
{
	String name;
	int score;
	int redCards;
	int yellowCards;
	/**
	 * The amount of time (in microseconds) left on each yellow card issued to the team.
	 * If no yellow cards are issued, this array has no elements.
	 * Otherwise, times are ordered from smallest to largest.
	 */
	List<Integer> yellowCardsTimes;
	int timeouts;
	/**
	 * in microseconds
	 */
	int timeoutTime;
	int goalie;

	int foulCounter;
	int ballPlacementFailures;
	boolean canPlaceBall;
	int maxAllowedBots;
	boolean botSubstitutionIntent;


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
	public TeamInfo(final SslGcRefereeMessage.Referee.TeamInfo teamInfo)
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
}
