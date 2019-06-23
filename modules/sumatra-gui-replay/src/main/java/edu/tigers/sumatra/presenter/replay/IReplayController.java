/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public interface IReplayController
{
	/**
	 * Update for given timestamp
	 * 
	 * @param db current database
	 * @param sumatraTimestampNs current timestamp
	 */
	default void update(final BerkeleyDb db, long sumatraTimestampNs)
	{
	}
	
	
	/**
	 * @param db
	 * @param wfw
	 */
	default void update(final BerkeleyDb db, WorldFrameWrapper wfw)
	{
	}
}
