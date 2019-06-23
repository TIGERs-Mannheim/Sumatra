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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.BotConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;


/**
 * <p>
 * Abstract Class of any Condition implementation.
 * </p>
 * <p>
 * By instantiating a Condition in a Role, you give it a state which can then be tried to achieved and worked with.
 * </p>
 * <p>
 * Conditions must implement a {@link #doCheckCondition(WorldFrame, BotID)} method, which will determine the state of
 * the condition and is only accessible by the {@link #checkCondition(WorldFrame, BotID)}-Method, which will cache the
 * state of the Condition for the next calls with the same frame-id.
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
	
	/** @see #checkCondition(WorldFrame, BotID) */
	private static final EConditionState	UNDEFINED_RESULT	= EConditionState.NOT_CHECKED;
	
	private FrameID								lastId;
	private EConditionState						lastResult			= EConditionState.NOT_CHECKED;
	
	/** Used to ensure re-calculation of the conditions state ({@link #resetCache()}) */
	private boolean								stateChanged		= false;
	
	private String									condition			= "not implemented";
	
	/** check this condition? */
	private boolean								active				= true;
	
	private EBotType								botType				= EBotType.UNKNOWN;
	
	private BotConfig								botConfig			= null;
	
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
		
		
		private EConditionState(boolean ok)
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
	public ACondition(ECondition type)
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
	public EConditionState checkCondition(WorldFrame worldFrame, BotID botId)
	{
		if (!active)
		{
			setCondition("inactive");
			// return true if not active
			return EConditionState.DISABLED;
		}
		if (stateChanged || (lastId == null) || !lastId.equals(worldFrame.id))
		{
			lastId = worldFrame.id;
			
			// Check for valid botIds...
			final TrackedTigerBot bot = worldFrame.tigerBotsVisible.getWithNull(botId);
			if (bot != null)
			{
				if (botConfig == null)
				{
					botConfig = AIConfig.getBotConfig(bot.getBotType());
				}
				if (botType == EBotType.UNKNOWN)
				{
					botType = bot.getBotType();
				}
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
	 * Do not call this on field initialization or in the constructor. At least one
	 * call to doCheckCondition has to be processed!
	 * 
	 * @return the botConfig
	 */
	public final BotConfig getBotConfig()
	{
		if (botConfig == null)
		{
			throw new IllegalStateException(
					"botConfig is still null. Only call after at least one doCalculation was processed!");
		}
		return botConfig;
	}
	
	
	/**
	 * @return the botType
	 */
	public final EBotType getBotType()
	{
		return botType;
	}
	
	
	/**
	 * @return The last result calculated by {@link #checkCondition(WorldFrame,BotID)}
	 */
	public EConditionState getLastConditionResult()
	{
		return lastResult;
	}
	
	
	/**
	 * Resets the internal cache and ensures that a new calculation is performed on
	 * {@link #checkCondition(WorldFrame, BotID)}!
	 */
	public void resetCache()
	{
		stateChanged = true;
		
		lastId = null;
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
	protected abstract EConditionState doCheckCondition(WorldFrame worldFrame, BotID botId);
	
	
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
	public boolean compare(ACondition condition)
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
	 * 
	 * @param condition
	 */
	public void setCondition(String condition)
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
	public final void setActive(boolean active)
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
