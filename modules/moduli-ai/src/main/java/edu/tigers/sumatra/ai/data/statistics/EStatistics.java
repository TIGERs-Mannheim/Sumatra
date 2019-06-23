/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 16, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics;

/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public enum EStatistics
{
	/** Contains the calculations for ball possession */
	BallPossession,
	/** Contains the calculations for scored goals */
	Goal,
	/** Contains calculations for pass accuracy of bots */
	PassAccuracy,
	/** Describes the statistics for time in roles */
	RoleTime,
	/** Describes the changes between roles */
	RoleChange,
	/** Describe the statistics for tackles */
	Tackle;
}
