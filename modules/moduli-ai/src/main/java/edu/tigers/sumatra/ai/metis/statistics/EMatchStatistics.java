package edu.tigers.sumatra.ai.metis.statistics;

/**
 * This enum is giving an overview of the available statistics
 */
public enum EMatchStatistics
{
	/** This is the statistic for ball possession */
	BALL_POSSESSION("Ball possession"),
	/** This is the statistic for a count how many times it was an active pass target */
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
	/** Shows the relative frames as Defender */
	FRAMES_AS_DEFENDER("Defender"),
	/** This is the statistic for won tackles */
	DUELS_WON("Duels won"),
	/** This is the statistic for lost tackles */
	DUELS_LOST("Duels lost"),
	/** This is the statistic for transitions from defensive to offensive */
	DEFENSIVE_TO_OFFENSIVE("Def->Off"),
	/** This is the statistic for transitions from defensive to support */
	DEFENSIVE_TO_SUPPORT("Def->Sup"),
	/** This is the statistic for transitions from offensive to support */
	OFFENSIVE_TO_SUPPORT("Off->Sup"),
	/** This is the statistic for transitions from offensive to defensive */
	OFFENSIVE_TO_DEFENSIVE("Off->Def"),
	/** This is the statistic for transitions from support to defensive */
	SUPPORT_TO_DEFENSIVE("Sup->Def"),
	/** This is the statistic for transitions from support to offensive */
	SUPPORT_TO_OFFENSIVE("Sup->Off"),
	/** This is the statistic for direct Shots */
	DIRECT_SHOTS("Direct shots"),
	DIRECT_SHOTS_SUCCESS_RATE("Direct shots success Rate"),
	
	DEFENSE_COVERAGE("Def. Cov."),
	DEFENSE_COVERAGE_OVERALL("Def. Cov. Avg. Overall"),
	DEFENSE_COVERAGE_ATTACKER_NEAR_BALL("Def. Cov. Avg. Attacker Near Ball"),
	
	;
	
	private String descriptor;
	
	
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
