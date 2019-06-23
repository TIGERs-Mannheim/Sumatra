/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


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
	
	private final AObjectID	objId;
	private final Vector2	pos;
	/** Whether the position of the tracked object has been updated during the last {@link #update(WorldFrame)} */
	private boolean			positionUpdatedLastCycle	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param objId
	 */
	public TrackedPosition(AObjectID objId)
	{
		this(objId, GeoMath.INIT_VECTOR);
	}
	
	
	/**
	 * @param objId
	 * @param vec
	 */
	public TrackedPosition(AObjectID objId, IVector2 vec)
	{
		this.objId = objId;
		pos = new Vector2(vec);
	}
	
	
	/**
	 * @param obj
	 */
	public TrackedPosition(ATrackedObject obj)
	{
		this(obj, GeoMath.INIT_VECTOR);
	}
	
	
	/**
	 * @param obj
	 * @param vec
	 */
	public TrackedPosition(ATrackedObject obj, IVector2 vec)
	{
		objId = obj.id;
		pos = new Vector2(vec);
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
		update(aiFrame.getWorldFrame());
	}
	
	
	/**
	 * Updates the objects position from the given {@link WorldFrame}
	 * 
	 * @param wf
	 */
	public void update(WorldFrame wf)
	{
		if (objId.isBall())
		{
			setPos(wf.ball);
			positionUpdatedLastCycle = true;
		} else
		{
			if (!setPos(wf.getTiger((BotID) objId)))
			{
				if (!setPos(wf.foeBots.get((BotID) objId)))
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
			// and hold last position
			return false;
		}
		
		pos.set(newPos.getPos());
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
	public AObjectID getObjId()
	{
		return objId;
	}
}
