/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

/**
 * A RatedPassTarget is a PassTarget with a PassTargetRating
 */
public interface IRatedPassTarget extends IPassTarget, Comparable<IRatedPassTarget>
{
	/**
	 * @return the score of this pass target in [0..1] where 1 is best, how the score is calculated can be decided by the
	 *         ScoreMode
	 */
	double getScore();


	/**
	 * @return the ScoreMode fo the @Link{getScore()} Method
	 */
	EScoreMode getScoreMode();
	
	
	/**
	 * @return PassTargetRating (all durations and scores, except the main score, in one object)
	 */
	IPassTargetRating getPassTargetRating();
}
