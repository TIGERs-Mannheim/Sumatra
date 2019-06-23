/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Gunther
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


/**
 * <p>
 * Abstract Class of any Condition implementation.
 * </p>
 * <p>
 * By instantiating a Condition in a Role, you give it a state which can then be tried to achieved and worked with.
 * </p>
 * <p>
 * Conditions must implement a {@link #doCheckCondition(WorldFrame, int)} method, which will determine the state of the
 * condition and is only accessible by the {@link #checkCondition(WorldFrame, int)}-Method, which will cache the state
 * of the Condition for the next calls with the same frame-id.
 * </p>
 * 
 * @author GuntherB, Gero
 */
public abstract class ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger				log					= Logger.getLogger(getClass());
	

	private final ECondition		type;
	
	/** @see #checkCondition(WorldFrame, int) */
	private static final Boolean	UNDEFINED_RESULT	= null;
	
	private FrameID					lastId;
	private Boolean					lastResult			= UNDEFINED_RESULT;
	
	/** Used to ensure re-calculation of the conditions state ({@link #resetCache()}) */
	private boolean					stateChanged		= false;
	

	private ARole						role					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public ACondition(ECondition type)
	{
		this.type = type;
	}
	

	/**
	 * Called by {@link ARole} to check if this condition is valid.
	 * <p>
	 * <strong>NOTE:</strong> {@link #updateDestination(Object)} has to be <strong>called at least once</strong> before
	 * this returns a valid result!!!
	 * </p>
	 * 
	 * @param worldFrame
	 * @param botId
	 * @return condition state (cached, if worldFrame.id equals the one from last call!)
	 */
	public boolean checkCondition(WorldFrame worldFrame)
	{
		if (role == null)
		{
			log.warn("inappropriate use of condition: role was not set");
			return false;
		}
		
		if (stateChanged || lastId == null || !lastId.equals(worldFrame.id))
		{
			lastId = worldFrame.id;
			
			// Check for valid botIds...
			final TrackedTigerBot bot = worldFrame.tigerBots.get(role.getBotID());
			if (bot != null)
			{
				lastResult = doCheckCondition(worldFrame, role.getBotID());
				stateChanged = false;
			} else
			{
				log.fatal("Condition [" + type + "]: Something's wrong with botId: '" + role.getBotID() + "'!");
				return false; // if crashed, better think it's false
			}
		}
		
		return lastResult == UNDEFINED_RESULT ? false : lastResult;
	}
	

	/**
	 * @return The last result calculated by {@link #checkCondition(WorldFrame)}. <b>Note:</b> Returns <u>null</u> if
	 *         {@link #checkCondition(WorldFrame)} got never called!
	 */
	public Boolean getLastConditionResult()
	{
		return lastResult;
	}
	

	/**
	 * Resets the internal cache and ensures that a new calculation is performed on
	 * {@link #checkCondition(WorldFrame, int)}!
	 */
	protected void resetCache()
	{
		stateChanged = true;
		
		lastId = null;
		lastResult = UNDEFINED_RESULT;
	}
	

	/**
	 * @param role the role associated with that condition
	 */
	void setRole(ARole role)
	{
		this.role = role;
	}
	

	/**
	 * <strong>To be called from {@link ACondition} only!</strong>
	 * <p>
	 * <strong>NOTE:</strong> {@link #updateDestination(Object)} has to be <strong>called at least once</strong> before
	 * this returns a valid result!!!
	 * </p>
	 * 
	 * @param worldFrame
	 * @param botId
	 * @return condition state
	 */
	protected abstract boolean doCheckCondition(WorldFrame worldFrame, int botId);
	

	/**
	 * @return The {@link ECondition}-type
	 */
	public ECondition getType()
	{
		return type;
	}
}
