/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

import static java.lang.Math.round;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * An implementation of a pass target.
 */
@Persistent
public class RatedPassTarget extends RatedPassTargetNoScore implements IRatedPassTarget
{
	private final EScoreMode scoreMode;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private RatedPassTarget()
	{
		super();
		scoreMode = null;
	}
	
	
	/**
	 * New Pass targets with required values
	 *
	 * @param passTarget
	 * @param passTargetRating
	 * @param scoreMode
	 */
	public RatedPassTarget(final IPassTarget passTarget, final IPassTargetRating passTargetRating,
			final EScoreMode scoreMode)
	{
		super(passTarget, passTargetRating);
		this.scoreMode = scoreMode;
	}
	
	
	public RatedPassTarget(final DynamicPosition dynamicPosition, final BotID id,
			final IPassTargetRating passTargetRating,
			final EScoreMode scoreMode)
	{
		super(dynamicPosition, id, passTargetRating);
		this.scoreMode = scoreMode;
	}
	
	
	/**
	 * @param ratedPassTargetNoScore
	 * @param scoreMode
	 */
	public RatedPassTarget(final RatedPassTargetNoScore ratedPassTargetNoScore, final EScoreMode scoreMode)
	{
		this(ratedPassTargetNoScore, ratedPassTargetNoScore.getPassTargetRating(), scoreMode);
	}
	
	
	@Override
	public int compareTo(final IRatedPassTarget o)
	{
		int scoreCmp = -Double.compare(round(100 * getScore()), round(100 * o.getScore()));
		if (scoreCmp == 0)
		{
			scoreCmp = Double.compare(getPos().distanceToSqr(Geometry.getGoalTheir().getCenter()),
					o.getPos().distanceToSqr(Geometry.getGoalTheir().getCenter()));
		}
		return scoreCmp;
	}
	
	
	@Override
	public double getScore()
	{
		switch (scoreMode)
		{
			case SCORE_BY_PASS:
				return getPassTargetRating().getPassScore();
			case SCORE_BY_GOAL_KICK:
				return getPassTargetRating().getGoalKickScore();
			default:
				return 0;
		}
	}
	
	
	@Override
	public EScoreMode getScoreMode()
	{
		return scoreMode;
	}
	
	
	@Override
	public String toString()
	{
		return "RatedPassTarget{" +
				"targetPos=" + getPos() +
				", botID=" + getBotId() +
				'}';
	}
}
