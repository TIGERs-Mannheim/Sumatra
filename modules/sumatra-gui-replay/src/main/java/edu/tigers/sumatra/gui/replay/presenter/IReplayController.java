/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.replay.presenter;

import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public interface IReplayController
{
	/**
	 * Update for given timestamp
	 * 
	 * @param db current database
	 * @param sumatraTimestampNs current timestamp
	 */
	default void update(final PersistenceDb db, long sumatraTimestampNs)
	{
	}
	
	
	/**
	 * @param db
	 * @param wfw
	 */
	default void update(final PersistenceDb db, WorldFrameWrapper wfw)
	{
	}
}
