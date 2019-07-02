/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * A PassTarget with a PassTargetRating but without a ScoreMode, so not a complete RatedPassTarget
 */
@Persistent
public class RatedPassTargetNoScore extends PassTarget
{
	private final IPassTargetRating passTargetRating;
	
	
	protected RatedPassTargetNoScore()
	{
		super();
		passTargetRating = null;
	}
	
	
	/**
	 * New RatedPassTargetNoScore with required values
	 *
	 * @param passTarget
	 * @param passTargetRating
	 */
	public RatedPassTargetNoScore(final IPassTarget passTarget, final IPassTargetRating passTargetRating)
	{
		super(passTarget.getDynamicPos(), passTarget.getBotId());
		Validate.notNull(passTargetRating);
		this.passTargetRating = passTargetRating;
		
	}
	
	
	public RatedPassTargetNoScore(final DynamicPosition dynamicPosition, final BotID id,
			final IPassTargetRating passTargetRating)
	{
		super(dynamicPosition, id);
		Validate.notNull(passTargetRating);
		this.passTargetRating = passTargetRating;
	}
	
	
	public IPassTargetRating getPassTargetRating()
	{
		return passTargetRating;
	}
}
