/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;


/**
 * The TargetVisibleCondition checks if there are obstacles between starting
 * position and end. This is abstract because there are different conditions
 * which use the same algorithm. See {@link BallVisibleCon},{@link TargetVisibleCon}, {@link VisibleCon}
 * 
 * @author DanielW, Oliver Steinbrecher <OST1988@aol.com>, GuntherB
 */
public abstract class AVisibleCon extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** the ending point of the line (see checkVisibilty()) */
	private IVector2			end			= AIConfig.INIT_VECTOR;
	/** the start point of the line (see checkVisibilty()) */
	private IVector2			start			= AIConfig.INIT_VECTOR;
	
	/** list of bots that should be ignored */
	private List<Integer>	ignoreIds	= new ArrayList<Integer>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public AVisibleCon(ECondition type)
	{
		super(type);
	}
	

	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the end point of the visibility line
	 */
	public IVector2 getEnd()
	{
		return end;
	}
	

	/**
	 * 
	 * 
	 * @return the start point of the visibility line
	 */
	public IVector2 getStart()
	{
		return start;
	}
	

	/**
	 * adds a bot to the ignore list
	 * bots on that list will not be considered when calculating the visibility
	 * @param botId
	 */
	public void addToIgnore(int botId)
	{
		if (!ignoreIds.contains(botId))
		{
			ignoreIds.add(botId);
		}
	}
	

	/**
	 * removes a bot from the ignore list
	 * bots on that list will not be considered when calculating the visibility
	 * @param botId
	 */
	public void removeFromIgnore(int botId)
	{
		
		ignoreIds.remove(botId);
	}
	

	/**
	 * reset Ignore-List
	 */
	public void resetIgnore()
	{
		
		ignoreIds.clear();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Updates the end of the visibility line
	 * 
	 * @param end
	 */
	protected void updateEnd(IVector2 end)
	{
		this.end = end;
		resetCache();
	}
	

	/**
	 * updates the starting point of the visibility line
	 * 
	 * @param start
	 */
	protected void updateStart(IVector2 start)
	{
		this.start = start;
		resetCache();
	}
	

	/**
	 * improved algorithm for determining if a certain endpoint is visible from a certain start point
	 * visible means, a ball could be shot from start to end
	 * 
	 * @param worldFrame
	 * @return
	 */
	protected boolean checkVisibility(WorldFrame worldFrame)
	{
		return AIMath.p2pVisibility(worldFrame, start, end, ignoreIds);
	}
	
}
