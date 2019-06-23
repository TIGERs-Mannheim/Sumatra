/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 25, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.referee;

/**
 * Team specific referee commands
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ETeamSpecRefCmd
{
	
	/** */
	KickOffTigers,
	/** */
	KickOffEnemies,
	/** */
	PenaltyTigers,
	/** */
	PenaltyEnemies,
	/** */
	DirectFreeKickTigers,
	/** */
	DirectFreeKickEnemies,
	/** */
	IndirectFreeKickTigers,
	/** */
	IndirectFreeKickEnemies,
	/**  */
	BallPlacementTigers,
	/**  */
	BallPlacementEnemies,
	/** */
	TimeoutTigers,
	/** */
	TimeoutEnemies,
	/**  */
	GoalTigers,
	/**  */
	GoalEnemies,
	/**  */
	ForceStart,
	/**  */
	NormalStart,
	/**  */
	Stop,
	/**  */
	Halt,
	/**  */
	NoCommand;
}
