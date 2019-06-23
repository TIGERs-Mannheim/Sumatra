/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;


/**
 * This class should help you to track an objects position over time
 * 
 * @author Gero
 */
public class TrackedPosition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** DEPENDS ON BALL ID == -1 !!! */
	public static final int	BALL_ID							= -1;
	
	private final int			objId;
	private final Vector2	pos;
	/** Whether the position of the tracked object has been updated during the last {@link #update(WorldFrame)} */
	private boolean			positionUpdatedLastCycle	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param objId
	 */
	public TrackedPosition(int objId)
	{
		this(objId, AIConfig.INIT_VECTOR);
	}
	

	/**
	 * @param objId
	 * @param vec
	 */
	public TrackedPosition(int objId, IVector2 vec)
	{
		this.objId = objId;
		this.pos = new Vector2(vec);
	}
	

	/**
	 * @param obj
	 */
	public TrackedPosition(ATrackedObject obj)
	{
		this(obj, AIConfig.INIT_VECTOR);
	}
	

	/**
	 * @param obj
	 * @param vec
	 */
	public TrackedPosition(ATrackedObject obj, IVector2 vec)
	{
		this.objId = obj.id;
		this.pos = new Vector2(vec);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Updates the objects position from the given {@link AIInfoFrame}
	 * 
	 * @param aiFrame
	 */
	public void update(AIInfoFrame aiFrame)
	{
		update(aiFrame.worldFrame);
	}
	

	/**
	 * Updates the objects position from the given {@link WorldFrame}
	 * 
	 * @param wf
	 */
	public void update(WorldFrame wf)
	{
		if (objId == BALL_ID)
		{
			setPos(wf.ball);
			positionUpdatedLastCycle = true;
		} else
		{
			if (!setPos(wf.tigerBots.get(objId)))
			{
				if (!setPos(wf.foeBots.get(objId)))
				{
					positionUpdatedLastCycle = false;
				} else
				{
					positionUpdatedLastCycle = true;
				}
			} else
			{
				positionUpdatedLastCycle = true;
			}
		}
	}
	

	private boolean setPos(ATrackedObject newPos)
	{
		if (newPos == null)
		{
			return false; // and hold last position
		}
		
		pos.set(newPos.pos);
		return true;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return A reference to the position which
	 */
	public IVector2 getPos()
	{
		return pos;
	}
	

	/**
	 * @return {@link #positionUpdatedLastCycle}
	 */
	public boolean posUpdated()
	{
		return positionUpdatedLastCycle;
	}
	

	/**
	 * @return The objId of the tracke object
	 */
	public int getObjId()
	{
		return objId;
	}
}
