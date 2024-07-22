/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

/**
 * This enum is giving an overview of the available statistics
 */
public enum EMatchStatistics
{
	/**
	 * This is the statistic for ball possession
	 */
	BALL_POSSESSION("Ball possession"),
	/**
	 * This is the statistic for a count how many times it was an active pass target
	 */
	ACTIVE_PASS_TARGET("pass target"),
	/** This is the statstic for total passes in the game */
	COUNT_PASSES("pass count"),
	/** This is the percentage of successful passes */
	RATIO_SUCCESSFUL_PASSES("ratio of successful passes"),
	/** This is the statistic for scored goals */
	GOALS_SCORED("Goals"),
	/** Shows the relative frames as Offensive */
	FRAMES_AS_OFFENSIVE("Offensive"),
	/** Shows the relative frames as Supporter */
	FRAMES_AS_SUPPORT("Supporter"),
	/**
	 * Shows the relative frames as Defender
	 */
	FRAMES_AS_DEFENDER("Defender"),
	/**
	 * This is the statistic for won tackles
	 */
	DUELS_WON("Duels won"),
	/**
	 * This is the statistic for lost tackles
	 */
	DUELS_LOST("Duels lost"),
	/**
	 * This is the statistic for transitions from defensive to offensive
	 */
	DEFENSIVE_TO_OFFENSIVE("Def->Off"),
	/**
	 * This is the statistic for transitions from defensive to support
	 */
	DEFENSIVE_TO_SUPPORT("Def->Sup"),
	/**
	 * This is the statistic for transitions from offensive to support
	 */
	OFFENSIVE_TO_SUPPORT("Off->Sup"),
	/**
	 * This is the statistic for transitions from offensive to defensive
	 */
	OFFENSIVE_TO_DEFENSIVE("Off->Def"),
	/**
	 * This is the statistic for transitions from support to defensive
	 */
	SUPPORT_TO_DEFENSIVE("Sup->Def"),
	/**
	 * This is the statistic for transitions from support to offensive
	 */
	SUPPORT_TO_OFFENSIVE("Sup->Off"),
	/**
	 * This is the statistic for direct Shots
	 */
	DIRECT_SHOTS("Direct shots (DS)"),
	DIRECT_SHOTS_SUCCESS("   DS Successful"),
	DIRECT_SHOTS_BLOCKED_DEFENSE("   DS Fail - Defense"),
	DIRECT_SHOTS_BLOCKED_KEEPER("   DS Fail - Keeper"),
	DIRECT_SHOTS_BLOCKED_BOTH("   DS Fail - Keeper+Defense"),
	DIRECT_SHOTS_SKIRMISH("   DS Fail - Skirmish Stuck"),
	DIRECT_SHOTS_OTHER("   DS Fail - Other"),

	DEFENSE_COVERAGE("Uncovered Goal angle (Cov)"),
	DEFENSE_COVERAGE_OVERALL("   Cov Avg. Overall"),
	DEFENSE_COVERAGE_ATTACKER_NEAR_BALL("   Cov Avg. Attacker Near Ball"),
	DEFENSE_COVERAGE_AT_BALL_RECEIVED("   Cov. at receive"),

	DEFENSE_DIST_AT_BALL_RECEIVED("Def. Distance at receive"),
	DEFENSE_VEL_AT_BALL_RECEIVED("Def. Vel Diff at receive"),
	DEFENSE_BALL_POS_X_AT_BALL_RECEIVED("Def. X component of Ball pos at receive"),

	DEFENSE_BOT_THREAT_RATING_RAW("BotThreatRating 0 Defender"),
	DEFENSE_BOT_THREAT_RATING_RAW_AVG("Avg. BotThreatRating 0 Defender"),
	DEFENSE_BOT_THREAT_RATING_WANTED("BotThreatRating perfect Defender"),
	DEFENSE_BOT_THREAT_RATING_WANTED_AVG("Avg. BotThreatRating perfect Defender"),
	DEFENSE_BOT_THREAT_RATING_ACTUAL("BotThreatRating actual Defender"),
	DEFENSE_BOT_THREAT_RATING_ACTUAL_AVG("Avg. BotThreatRating actual Defender"),

	;

	private final String descriptor;


	EMatchStatistics(final String descriptor)
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
