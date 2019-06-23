/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible;

import java.util.HashSet;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


/**
 * The TargetVisibleCondition checks if there are obstacles between starting
 * position and end. This is abstract because there are different conditions
 * which use the same algorithm. See {@link TargetVisibleCon}, {@link VisibleCon}
 * 
 * @author DanielW, Oliver Steinbrecher <OST1988@aol.com>, GuntherB
 */
public abstract class AVisibleCon extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** the ending point of the line (see checkVisibilty()) */
	private IVector2				end			= Vector2.ZERO_VECTOR;
	/** the start point of the line (see checkVisibilty()) */
	private IVector2				start			= Vector2.ZERO_VECTOR;
	/** ray size for {@link GeoMath#p2pVisibility(WorldFrame, IVector2, IVector2, Float, BotID...)} */
	private float					raySize		= 0;
	
	/** list of bots that should be ignored */
	private final Set<BotID>	ignoreIds	= new HashSet<BotID>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public AVisibleCon(final ECondition type)
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
	 * @return the start point of the visibility line
	 */
	public IVector2 getStart()
	{
		return start;
	}
	
	
	/**
	 * adds a bot to the ignore list
	 * bots on that list will not be considered when calculating the visibility
	 * 
	 * @param botID
	 */
	public void addToIgnore(final BotID botID)
	{
		if (!ignoreIds.contains(botID))
		{
			ignoreIds.add(botID);
		}
	}
	
	
	/**
	 * removes a bot from the ignore list
	 * bots on that list will not be considered when calculating the visibility
	 * 
	 * @param botID
	 */
	public void removeFromIgnore(final BotID botID)
	{
		
		ignoreIds.remove(botID);
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
	protected void updateEnd(final IVector2 end)
	{
		this.end = end;
		resetCache();
	}
	
	
	/**
	 * updates the starting point of the visibility line
	 * 
	 * @param start
	 */
	protected void updateStart(final IVector2 start)
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
	protected boolean checkVisibility(final SimpleWorldFrame worldFrame)
	{
		return GeoMath.p2pVisibility(worldFrame, start, end, raySize, ignoreIds);
	}
	
	
	@Override
	protected boolean compareContent(final ACondition condition)
	{
		final AVisibleCon con = (AVisibleCon) condition;
		if (start.equals(con.getStart()) && end.equals(con.getEnd()))
		{
			if (ignoreIds.containsAll(con.ignoreIds))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * @return the raySize
	 */
	public final float getRaySize()
	{
		return raySize;
	}
	
	
	/**
	 * @param raySize the raySize to set
	 */
	public final void setRaySize(final float raySize)
	{
		this.raySize = raySize;
	}
}
