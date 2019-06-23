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
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
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
		
		if (newRole.isCompleted())
		{
			log.error("Role is already completed. Can not switch to new role: " + newRole.getType());
			return;
		}
		
		boolean removed = roles.remove(oldRole);
		if (!removed)
		{
			log.error("Could not switch roles. Role to switch is not in list. + " + oldRole);
			return;
		}
		roles.add(newRole);
		
		newRole.assignBotID(newBotId, oldRole.getAiFrame());
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
	 * Remove a role. Do not call this from your Play!! This is intended for the RoleAssigner only
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
	 * @param frame
	 * @return the added roles
	 */
	public final List<ARole> addRoles(final int count, final MetisAiFrame frame)
	{
		List<ARole> roles = new ArrayList<ARole>();
		for (int i = 0; i < count; i++)
		{
			ARole role = onAddRole(frame);
			roles.add(role);
			addRole(role);
		}
		return roles;
	}
	
	
	/**
	 * Ask the play to remove the specified number of roles
	 * 
	 * @param count
	 * @param frame
	 * @return
	 */
	public final List<ARole> removeRoles(final int count, final MetisAiFrame frame)
	{
		List<ARole> roles = new ArrayList<ARole>();
		for (int i = 0; i < count; i++)
		{
			ARole role = onRemoveRole(frame);
			roles.add(role);
			removeRole(role);
		}
		return roles;
	}
	
	
	/**
	 * Remove one role from this Play.
	 * Assume that there is at least one role left.
	 * 
	 * @param frame
	 * @return the removed role
	 */
	protected abstract ARole onRemoveRole(MetisAiFrame frame);
	
	
	/**
	 * Some upper layer has decided that this play will now play with an additional role.
	 * Please assume that the Play will start with zero roles until the play can only run with
	 * a static number of roles anyway
	 * 
	 * @param frame
	 * @return the added role
	 */
	protected abstract ARole onAddRole(MetisAiFrame frame);
	
	
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
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
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
	 * You can reorder the roles you get with getRoles() and update them here.
	 * getRoles() will return an unmodifiable list, so you can not reorder this list directly.
	 * This method will check if you have not put any extra roles or wrong number of roles!
	 * 
	 * @param orderedRoles
	 */
	public final void setReorderedRoles(final List<ARole> orderedRoles)
	{
		if (roles.size() != orderedRoles.size())
		{
			throw new IllegalArgumentException("Provided orderedRoles list does not have correct size: "
					+ orderedRoles.size());
		}
		for (ARole role : orderedRoles)
		{
			if (!roles.contains(role))
			{
				throw new IllegalArgumentException("Provided orderedRoles list contains an unknown role! " + role.getType());
			}
		}
		roles.clear();
		roles.addAll(orderedRoles);
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
	
	
	/**
	 * Roles will be resorted to that first role in list is nearest to first destination and so on
	 * 
	 * @param destinations
	 */
	protected void reorderRolesToDestinations(final List<IVector2> destinations)
	{
		List<ARole> roles = new ArrayList<ARole>(getRoles());
		List<ARole> rolesSorted = new ArrayList<ARole>(getRoles().size());
		for (IVector2 dest : destinations)
		{
			float minDist = Float.MAX_VALUE;
			ARole theRole = null;
			for (ARole role : roles)
			{
				float dist = GeoMath.distancePP(dest, role.getPos());
				if (dist < minDist)
				{
					minDist = dist;
					theRole = role;
				}
			}
			roles.remove(theRole);
			rolesSorted.add(theRole);
		}
		setReorderedRoles(rolesSorted);
	}
}
