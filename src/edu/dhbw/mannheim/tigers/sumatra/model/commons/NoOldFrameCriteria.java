/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.commons;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.ICriteria;

/**
 * This {@link ICriteria} allows only frames that are not identical with {@link #setOldFrame(WorldFrame)}!
 * 
 * @author Gero
 * 
 */
public class NoOldFrameCriteria implements ICriteria<WorldFrame>
{
	private WorldFrame	oldFrame;
	
	
	@Override
	public boolean matches(WorldFrame frame)
	{
		if (oldFrame != null && oldFrame.time > frame.time)
		{
			return false;
		}
		return true;
	}
	

	/**
	 * @param oldFrame The old frame the criteria won't match with
	 */
	public void setOldFrame(WorldFrame oldFrame)
	{
		this.oldFrame = oldFrame;
	}
}