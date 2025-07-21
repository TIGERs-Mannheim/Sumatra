/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.skirmish;

public enum ESkirmishStrategyType
{
	NONE,
	RIP_FREE, // Make a fast spinning turn during a skirmish to rip the ball free towards a waiting supportive attacker
	BODYGUARD, // Position a supportive attacker within the dribbling circle that shall annoy the opponent during a skirmish
	BLOCKER_FOR_FINISHER, // Block opponent PenAreaDefender movement to make holes for the finisher
	BULLY,   // Block opponents close to our goal from moving while handling the ball
	PREPARE_DEFENSIVE, // Prepare a defensive strategy by already pulling a supportive attacker closer to the future skirmish
	PREPARE_OFFENSIVE, // Prepare an offensive strategy by already pulling a supportive attacker closer to the future skirmish
}
