/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trees;/**
 */
public enum EOffensiveSituation
{
	/**
	 * Default Situation
	 */
	DEFAULT_SITUATION,
	/**
	 * Standard Situation for us in opponent half
	 */
	STANDARD_AGGRESSIVE,
	/**
	 * Standard Situation for us in our half
	 */
	STANDARD_DEFENSIVE,
	/**
	 * Special Case for close ranged combats near opponent goal
	 */
	CLOSE_COMBAT_AGGRESSIVE,
	/**
	 * Special Case for close ranged combats near our goal
	 */
	CLOSE_COMBAT_DEFENSIVE

}
