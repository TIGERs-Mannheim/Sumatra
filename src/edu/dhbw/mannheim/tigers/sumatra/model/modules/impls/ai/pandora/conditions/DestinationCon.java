/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Gunther Berthold
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;

import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.distancePP;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;


/**
 * This is a condition which can be used to check if a role/bot has reached its destination.
 * 
 * @author GuntherB
 * 
 */
public class DestinationCon extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Vector2	destination;
	private float				tolerance;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public DestinationCon(float tolerance)
	{
		super(ECondition.DESTINATION);
		this.tolerance = tolerance;
		this.destination = new Vector2(AIConfig.INIT_VECTOR);
	}
	
	public DestinationCon(IVector2 startDest)
	{
		this(AIConfig.getTolerances().getPositioning());
		this.destination.set(startDest);
	}

	public DestinationCon()
	{
		this(AIConfig.getTolerances().getPositioning());
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean doCheckCondition(WorldFrame worldFrame, int botID)
	{
		final TrackedTigerBot bot = worldFrame.tigerBots.get(botID);
		
		float dist = distancePP(bot, destination);
		
		if (dist < tolerance)
		{
			return true;
		}
		return false;
	}
	

	/**
	 * Changes the tolerance for the destination. Calls {@link #resetCache()}, too!
	 * 
	 * @param newTolerance
	 */
	public void setTolerance(float newTolerance)
	{
		tolerance = newTolerance;
		resetCache();
	}
	

	/**
	 * 
	 * Updates the destination of this condition.
	 * 
	 * @param newDestination
	 */
	public void updateDestination(IVector2 newDestination)
	{
		this.destination.set(newDestination);
		resetCache();
	}
	

	public IVector2 getDestination()
	{
		return destination;
	}
	

	public float getTolerance()
	{
		return tolerance;
	}
	
}
