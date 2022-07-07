/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;


/**
 * Part of a trajectory
 */
@Persistent
class BBTrajectoryPart
{
	float tEnd;
	float acc;
	float v0;
	float s0;
}
