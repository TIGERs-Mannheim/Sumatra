/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares.Ares;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Assigner;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.DestinationCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERoleBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * Abstract type for roles, all roles inherit from here
 * Already includes some generic implementations for:
 * <ul>
 * <li>list for {@link Condition}s
 * <li>list for {@link ASkill}s
 * <li>checkAllConditions
 * </ul>
 * <p>
 * <strong>Note:</strong> There is no destination field: If the role needs a destination use {@link DestinationCon} to
 * manage a destination
 * </p>
 * 
 * @author DanielW, Oliver Steinbrecher <OST1988@aol.com>
 */
public abstract class ARole implements Serializable
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long						serialVersionUID		= -6884433787238092197L;
	
	protected final Logger							log						= Logger.getLogger(getClass());
	
	protected static final int						UNINITIALIZED_BOTID	= -1;
	
	private final ERole								type;
	
	private int											botID						= UNINITIALIZED_BOTID;
	/** Still allowed to write {@link #botID}? ({@link #assignBotID(int)}, {@link #reassignBotID(int, RoleReassignKey)}) */
	private boolean									botIDlock				= true;
	
	private ERoleBehavior							behavior					= null;
	/** Still allowed to write {@link #behavior}? ({@link #setBehavior(ERoleBehavior)}) */
	private boolean									behaviorLock			= true;
	
	private final Map<ECondition, ACondition>	conditions;
	
	private boolean									firstUpdate 			= true;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @see ARole
	 * @param type
	 */
	public ARole(ERole type)
	{
		this.type = type;
		this.conditions = new HashMap<ECondition, ACondition>();
	}
	

	// --------------------------------------------------------------------------
	// --- interface -----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void doUpdate(AIInfoFrame curFrame)
	{
		firstUpdate(curFrame);
		
		update(curFrame);
	}
	
	
	public void firstUpdate(AIInfoFrame curFrame)
	{
		if (firstUpdate)
		{
			doFirstUpdate(curFrame);
			firstUpdate = false;
		}
	}
	
	protected void doFirstUpdate(AIInfoFrame curFrame)
	{
		
	}
	
	
	/**
	 * This function is used to update the role with the new {@link AIInfoFrame}.
	 * Normally {@link APlay} will call this function to update <code>this</code>.<br/>
	 * <br/>
	 * <code>update()</code> and <code>calculateSkills()</code> are not redundant, because: {@link Lachesis} needs
	 * information about the destination to distribute bots to roles. Therefore,
	 * this information has to be present long before {@link Ares} <code>calculateSkills()</code>.
	 */
	public abstract void update(AIInfoFrame currentFrame);
	

	/**
	 * This function is used to calculate the needed bot-skills.(For example to move
	 * to a specified position on the field.) Normally {@link Ares} will call this function.
	 * 
	 * @param wFrame The current {@link WorldFrame}
	 * @param skills The {@link SkillFacade} which is to be filled by the role-programmer. It contains a slot for every
	 *           {@link ESkillGroup}.
	 */
	public abstract void calculateSkills(WorldFrame wFrame, SkillFacade skills);
	

	/**
	 * Sets the initial value for the result of {@link #getDestination()} before the first call of
	 * {@link #update(AIInfoFrame)}
	 * 
	 * @param destination
	 */
	public abstract void initDestination(IVector2 destination);
	

	/**
	 * @return The destination this roles likes to approach (used for cost-calculations in {@link Lachesis} and maybe
	 *         {@link Athena}) <strong>May not be <code>null</code>!!!</strong>
	 */
	public abstract IVector2 getDestination();
	

	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public ERole getType()
	{
		return type;
	}
	

	void setBehavior(ERoleBehavior behavior)
	{
		if (behaviorLock)
		{
			this.behavior = behavior;
			behaviorLock = false;
		} else
		{
			log.warn("Change of behavior in role " + this + " denied!");
		}
	}
	

	public ERoleBehavior getBehavior()
	{
		return behavior;
	}
	

	/**
	 * This method should only be called from the {@link Assigner} or
	 * {@link APlay#switchRoles(ARole, ARole, AIInfoFrame)}!!!
	 * 
	 * @param botID
	 */
	public void assignBotID(int botID)
	{
		if (botIDlock)
		{
			this.botID = botID;
			botIDlock = false;
		} else
		{
			log.warn("Change of BotID in role " + this + " denied!");
		}
	}
	

	// The concept of re-assigning of roles seems not to be safe enough, thus it has been abandoned! ;-) See "Assigner",
	// too!
	// /**
	// * Used by the {@link Assigner} to reassign formerly used roles
	// *
	// * @param newBotID
	// * @param key Used for authentication ({@link RoleReassignKey#RoleReassignKey} is private!)
	// *
	// * @author Gero
	// */
	// public void reassignBotID(int newBotID, RoleReassignKey key)
	// {
	// // Authentication
	// if (key == null)
	// {
	// throw new NullPointerException("Reassign of role in ARole.reassignBotID failed because key was null!");
	// }
	//
	// // Set
	// this.botID = newBotID;
	// botIDlock = false; // In case the bot-id haven't been set before
	//
	// // Mark state as dirty
	// for (ACondition con : conditions.values())
	// {
	// con.resetCache();
	// }
	// }
	

	public int getBotID()
	{
		return botID;
	}
	

	/**
	 * Adds a condition to a role. This method should be
	 * used in the constructor in derived classes of this.
	 * 
	 * @param condition
	 */
	protected void addCondition(ACondition condition)
	{
		conditions.put(condition.getType(), condition);
		condition.setRole(this);
	}
	

	/**
	 * Iterates over every {@link ACondition} of this {@link ARole} and checks whether <strong>every single one</strong>
	 * is <code>true</code>, else returning <code>false</code>.
	 * 
	 * @param currentFrame
	 * @return state
	 */
	public boolean checkAllConditions(AIInfoFrame currentFrame)
	{
		if (botID == UNINITIALIZED_BOTID)
		{
			log.fatal("Someone forgot to call this roles setBotId!!!");
			return false;
		}
		
		for (ACondition condition : conditions.values())
		{
			if (!condition.checkCondition(currentFrame.worldFrame)) // a condition is false
			{
				return false;
			}
		}
		
		return true; // all conditions are true
	}
	

	/**
	 * Returns the current position of that role.
	 * 
	 * @param f
	 * @return position
	 */
	public IVector2 getPos(AIInfoFrame f)
	{
		return getPos(f.worldFrame);
	}
	

	/**
	 * Returns the current position of that role.
	 * 
	 * @param wf
	 * @return position
	 */
	public IVector2 getPos(WorldFrame wf)
	{
		if (getBotID() == UNINITIALIZED_BOTID)
		{
			log.warn("called getPos from an unititialized Role!");
			
			return AIConfig.INIT_VECTOR;
		}
		return wf.tigerBots.get(getBotID()).pos;
	}
	

	/**
	 * Returns an immutable version of the internal {@link #conditions}-map
	 */
	public Map<ECondition, ACondition> getConditions()
	{
		return Collections.unmodifiableMap(conditions);
	}
	

	/**
	 * Needed for Role assigning. If your role is a keeper
	 * override this method and return true.
	 * 
	 * @return keeper?
	 * @author Malte
	 */
	public boolean isKeeper()
	{
		return false;
	}
	

	/**
	 * @return Whether this role has been assigned to a bot (by {@link Lachesis})
	 */
	public boolean hasBeenAssigned()
	{
		return botID != UNINITIALIZED_BOTID;
	}
	

	@Override
	public String toString()
	{
		return type.toString();
	}
}
