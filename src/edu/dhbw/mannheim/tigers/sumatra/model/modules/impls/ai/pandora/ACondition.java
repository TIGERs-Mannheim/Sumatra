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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


/**
 * <p>
 * Abstract Class of any Condition implementation.
 * </p>
 * <p>
 * By instantiating a Condition in a Role, you give it a state which can then be tried to achieved and worked with.
 * </p>
 * <p>
 * Conditions must implement a {@link #doCheckCondition(SimpleWorldFrame, BotID)} method, which will determine the state
 * of the condition and is only accessible by the {@link #checkCondition(SimpleWorldFrame, BotID)}-Method, which will
 * cache the state of the Condition for the next calls with the same frame-id.
 * </p>
 * 
 * @author GuntherB, Gero
 */
public abstract class ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log					= Logger.getLogger(ACondition.class.getName());
	
	
	private ECondition							type;
	
	/** @see #checkCondition(SimpleWorldFrame, BotID) */
	private static final EConditionState	UNDEFINED_RESULT	= EConditionState.NOT_CHECKED;
	
	private long									lastId;
	private EConditionState						lastResult			= EConditionState.NOT_CHECKED;
	
	/** Used to ensure re-calculation of the conditions state ({@link #resetCache()}) */
	private boolean								stateChanged		= false;
	
	private String									condition			= "";
	
	/** check this condition? */
	private boolean								active				= true;
	
	
	/**
	 * What is the state of this condition? This enum is used to have more than binary states
	 */
	public enum EConditionState
	{
		/** everything is fine, go on */
		FULFILLED(true),
		/** something blocks this condition so possibly the condition will never be fulfilled */
		BLOCKED(false),
		/** we are working on it */
		PENDING(false),
		/** this condition is disabled, no checks will be processed */
		DISABLED(true),
		/** condition check crashed somehow... */
		CRASHED(false),
		/** condition was not checked yet */
		NOT_CHECKED(false);
		
		private boolean	ok;
		
		
		private EConditionState(final boolean ok)
		{
			this.ok = ok;
		}
		
		
		/**
		 * @return true if fulfilled or disabled
		 */
		public boolean isOk()
		{
			return ok;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public ACondition(final ECondition type)
	{
		this.type = type;
	}
	
	
	/**
	 * Called by {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole} to check if this
	 * condition is valid.
	 * <p>
	 * <strong>NOTE:</strong>
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon#updateDestination(edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2)}
	 * has to be <strong>called at least once</strong> before this returns a valid result!!!
	 * </p>
	 * 
	 * @param worldFrame
	 * @param botId
	 * @return condition state (cached, if worldFrame.id equals the one from last call!)
	 */
	public EConditionState checkCondition(final SimpleWorldFrame worldFrame, final BotID botId)
	{
		if (!active)
		{
			setCondition("inactive");
			// return true if not active
			return EConditionState.DISABLED;
		}
		if (stateChanged || (lastId != worldFrame.getId()))
		{
			lastId = worldFrame.getId();
			
			// Check for valid botIds...
			final TrackedTigerBot bot = worldFrame.getBot(botId);
			if (bot != null)
			{
				lastResult = doCheckCondition(worldFrame, botId);
				stateChanged = false;
			} else
			{
				log.warn("Condition [" + type + "]: Bot with botId: '" + botId + "' vanished from WF!");
				return EConditionState.CRASHED;
			}
		}
		
		return lastResult;
	}
	
	
	/**
	 * @return The last result calculated by {@link #checkCondition(SimpleWorldFrame,BotID)}
	 */
	public EConditionState getLastConditionResult()
	{
		return lastResult;
	}
	
	
	/**
	 * Resets the internal cache and ensures that a new calculation is performed on
	 * {@link #checkCondition(SimpleWorldFrame, BotID)}!
	 */
	public void resetCache()
	{
		stateChanged = true;
		
		lastId = 0;
		lastResult = UNDEFINED_RESULT;
	}
	
	
	/**
	 * <strong>To be called from {@link ACondition} only!</strong>
	 * <p>
	 * <strong>NOTE:</strong>
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon#updateDestination(edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2)}
	 * has to be <strong>called at least once</strong> before this returns a valid result!!!
	 * </p>
	 * 
	 * @param worldFrame
	 * @param botId
	 * @return condition state
	 */
	protected abstract EConditionState doCheckCondition(SimpleWorldFrame worldFrame, BotID botId);
	
	
	/**
	 * Update this condition with the worldframe
	 * 
	 * @param swf
	 * @param botId
	 */
	public void update(final SimpleWorldFrame swf, final BotID botId)
	{
	}
	
	
	/**
	 * @return The {@link ECondition}-type
	 */
	public ECondition getType()
	{
		return type;
	}
	
	
	/**
	 * Compares with conditions.
	 * 
	 * @param condition
	 * @return true when the conditions are equal
	 */
	public boolean compare(final ACondition condition)
	{
		if ((condition == null) || (condition.getType() != getType()))
		{
			return false;
		}
		
		if (!isActive() || !condition.isActive())
		{
			return false;
		}
		
		return compareContent(condition);
	}
	
	
	/**
	 * Compares the condition content.
	 * 
	 * @param condition
	 * @return true when the conditions are equal
	 */
	protected abstract boolean compareContent(ACondition condition);
	
	
	/**
	 * Get the condition as a string
	 * 
	 * @return
	 */
	public String getCondition()
	{
		return condition;
	}
	
	
	/**
	 * @param condition
	 */
	public void setCondition(final String condition)
	{
		this.condition = condition;
	}
	
	
	/**
	 * @return the active
	 */
	public final boolean isActive()
	{
		return active;
	}
	
	
	/**
	 * @param active the active to set
	 */
	public final void setActive(final boolean active)
	{
		this.active = active;
	}
	
	
	/**
	 * @return the stateChanged
	 */
	public final boolean isStateChanged()
	{
		return stateChanged;
	}
}
