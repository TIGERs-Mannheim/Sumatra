/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s):
 * Bernhard
 * Gunther
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * These enums describe a special referee command each.
 */
public enum ERefereeCommand
{
	Start,
	Stop,
	Halt,
	Ready,
	BeginFirstHalf,
	BeginHalfTime,
	BeginSecondHalf,
	BeginOvertimeHalf1,
	BeginOvertimeHalf2,
	BeginPenaltyShootout,
	KickOffTigers,
	KickOffEnemies,
	PenaltyTigers,
	PenaltyEnemies,
	DirectFreeKickTigers,
	DirectFreeKickEnemies,
	IndirectFreeKickTigers,
	IndirectFreeKickEnemies,
	TimeoutTigers,
	TimeoutEnemies,
	TimeoutEnd,
	GoalScoredTigers,
	GoalScoredEnemies,
	DecreaseGoalScoreTigers,
	DecreaseGoalScoreEnemies,
	YellowCardTigers,
	YellowCardEnemies,
	RedCardTigers,
	RedCardEnemies,
	Cancel,
	TimeUpdate,
	UnknownCommand;
}
