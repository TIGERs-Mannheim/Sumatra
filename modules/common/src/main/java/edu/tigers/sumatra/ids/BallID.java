/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ids;


import com.sleepycat.persist.model.Persistent;


/**
 * Identifier for balls
 * 
 * @author Oliver Steinbrecher
 */
@Persistent
public class BallID extends AObjectID
{
	private static final BallID DEFAULT_INSTANCE = new BallID();
	
	
	private BallID()
	{
		super(AObjectID.BALL_ID);
	}
	
	
	/**
	 * @return default instance
	 */
	public static BallID instance()
	{
		return DEFAULT_INSTANCE;
	}
}
