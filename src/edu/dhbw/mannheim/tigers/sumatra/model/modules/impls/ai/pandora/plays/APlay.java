/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * This is the abstract play class. all plays inherit from here.<br/>
 * This type already includes a list for contained roles ({@link #roles}
 * 
 * @author DanielW, Oliver, Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class APlay
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(APlay.class.getName());
	
	private EPlayState				state;
	private final List<ARole>		roles;
	private final EPlay				type;
	
	private enum EPlayState
	{
		/** */
		RUNNING,
		/** */
		FINISHED;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public APlay(final EPlay type)
	{
		roles = new ArrayList<ARole>();
		state = EPlayState.RUNNING;
		this.type = type;
	}
	
	
	// --------------------------------------------------------------
	// --- roles ----------------------------------------------------
	// --------------------------------------------------------------
	
	
	/**
	 * deletes oldRole from the assignedRoles, gives newRole the botID of the oldRole
	 * and puts botId/newRole into the assignedRoles-Map
	 * i.e. A {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.OffensiveRole} has
	 * finished its task and shall now shoot a goal, therefore
	 * a new ShooterRole is created by the play and the roles are switched by using this function.
	 * Also, the oldRole's botID is automatically set in the newRole
	 * 
	 * @param oldRole
	 * @param newRole
	 * @param newBotId
	 */
	public final void switchRoles(final ARole oldRole, final ARole newRole, final BotID newBotId)
	{
		if (!newBotId.isBot())
		{
			log.error("Could not switch roles. New botId is not valid: " + newBotId);
			return;
		}
		
		boolean removed = roles.remove(oldRole);
		if (!removed)
		{
			log.error("Could not switch roles. Role to switch is not in list. + " + oldRole);
			return;
		}
		roles.add(newRole);
		
		newRole.assignBotID(newBotId);
		newRole.update(oldRole.getAiFrame());
		
		oldRole.removeSkillObserver();
		newRole.putSkillObserver();
	}
	
	
	/**
	 * Switch oldRole with newRole. OldRole must have a botId assigned.
	 * 
	 * @param oldRole
	 * @param newRole
	 */
	protected final void switchRoles(final ARole oldRole, final ARole newRole)
	{
		switchRoles(oldRole, newRole, oldRole.getBotID());
	}
	
	
	/**
	 * Add role to roles, but check if maxNumRoles is already reached
	 * 
	 * @param role
	 */
	private void addRole(final ARole role)
	{
		roles.add(role);
		role.putSkillObserver();
	}
	
	
	/**
	 * Remove a role
	 * TODO should not be public
	 * 
	 * @param role
	 */
	public final void removeRole(final ARole role)
	{
		roles.remove(role);
		role.setCompleted();
		onRoleRemoved(role);
	}
	
	
	/**
	 * 
	 */
	protected final ARole getLastRole()
	{
		ARole role = getRoles().get(getRoles().size() - 1);
		return role;
	}
	
	
	/**
	 * Ask the play to add the specified number of roles
	 * 
	 * @param count
	 * @return the added roles
	 */
	public final List<ARole> addRoles(final int count)
	{
		List<ARole> roles = new ArrayList<ARole>();
		for (int i = 0; i < count; i++)
		{
			ARole role = onAddRole();
			roles.add(role);
			addRole(role);
		}
		return roles;
	}
	
	
	/**
	 * Ask the play to remove the specified number of roles
	 * 
	 * @param count
	 * @return
	 */
	public final List<ARole> removeRoles(final int count)
	{
		List<ARole> roles = new ArrayList<ARole>();
		for (int i = 0; i < count; i++)
		{
			ARole role = onRemoveRole();
			roles.add(role);
			removeRole(role);
		}
		return roles;
	}
	
	
	/**
	 * Remove one role from this Play.
	 * Assume that there is at least one role left.
	 * 
	 * @return the removed role
	 */
	protected abstract ARole onRemoveRole();
	
	
	/**
	 * Some upper layer has decided that this play will now play with an additional role.
	 * Please assume that the Play will start with zero roles until the play can only run with
	 * a static number of roles anyway
	 * 
	 * @return the added role
	 */
	protected abstract ARole onAddRole();
	
	
	/**
	 * This is called, when the roleAssigner removed a role from this play.
	 * This play will possibly get a new role with onAddRole.
	 * 
	 * @param role
	 */
	protected void onRoleRemoved(final ARole role)
	{
	}
	
	
	/**
	 * Finish the play. This is not useful for the default plays
	 */
	public final void changeToFinished()
	{
		for (ARole role : getRoles())
		{
			role.setCompleted();
		}
		state = EPlayState.FINISHED;
	}
	
	
	/**
	 * Is the play finished? This should only be the case for standard situation plays.
	 * 
	 * @return
	 */
	public final boolean isFinished()
	{
		return state == EPlayState.FINISHED;
	}
	
	
	/**
	 * @param frame
	 */
	public final void update(final AthenaAiFrame frame)
	{
		if (frame.getPrevFrame().getTacticalField().getGameState() != frame.getTacticalField().getGameState())
		{
			onGameStateChanged(frame.getTacticalField().getGameState());
			for (ARole role : getRoles())
			{
				role.onGameStateChanged(frame.getTacticalField().getGameState());
			}
		}
		doUpdate(frame);
	}
	
	
	/**
	 * Compatibility method for old plays that require continues update with ai frame.
	 * It is preferred to only react in new frames in your roles.
	 * 
	 * @param frame
	 */
	protected void doUpdate(final AthenaAiFrame frame)
	{
	}
	
	
	/**
	 * This is called, when the game state has changed.
	 * It is called before {@link APlay#doUpdate(AthenaAiFrame)}.
	 * 
	 * @param gameState
	 */
	protected abstract void onGameStateChanged(EGameState gameState);
	
	
	/**
	 * Override this, if the role assigner should not "steal" your roles
	 * 
	 * @return
	 */
	public boolean overrideRoleAssignment()
	{
		return false;
	}
	
	
	// --------------------------------------------------------------
	// --- setter/getter --------------------------------------------
	// --------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		return type.toString();
	}
	
	
	/**
	 * Return all roles of this play
	 * 
	 * @return
	 */
	public final List<ARole> getRoles()
	{
		return Collections.unmodifiableList(roles);
	}
	
	
	/**
	 * Returns the actual play state.
	 * 
	 * @return
	 */
	public final EPlayState getPlayState()
	{
		return state;
	}
	
	
	/**
	 * @return the type
	 */
	public final EPlay getType()
	{
		return type;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		APlay other = (APlay) obj;
		if (type != other.type)
		{
			return false;
		}
		return true;
	}
}
