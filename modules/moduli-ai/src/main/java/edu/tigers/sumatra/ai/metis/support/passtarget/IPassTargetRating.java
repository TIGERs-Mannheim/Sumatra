/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

/**
 * A PassTargetRating is the difference between a PassTarget and a RatedPassTarget
 */
public interface IPassTargetRating
{
	double getGoalKickScore();
	
	
	double getPassScore();
	
	
	double getPressureScore();
	
	
	double getPassScoreStraight();
	
	
	double getPassScoreChip();
	
	
	double getPassDurationStraight();
	
	
	double getPassDurationChip();
	
	
	double getDurationScoreStraight();
	
	
	double getDurationScoreChip();
}
