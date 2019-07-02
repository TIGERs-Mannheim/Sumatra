/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This is the abstract play class. all plays inherit from here.<br/>
 * This type already includes a list for contained roles ({@link #roles}
 * 
 * @author DanielW, Oliver, Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class APlay
{
	private static final Logger log = Logger.getLogger(APlay.class.getName());
	
	private final List<ARole> roles;
	private final EPlay type;
	private AthenaAiFrame aiFrame;
	
	
	/**
	 * @param type of the play
	 */
	public APlay(final EPlay type)
	{
		roles = new CopyOnWriteArrayList<>();
		this.type = type;
	}
	
	
	// --------------------------------------------------------------
	// --- roles ----------------------------------------------------
	// --------------------------------------------------------------
	
	
	/**
	 * Switch oldRole with newRole. OldRole must have a botId assigned.
	 * Example: The OffenseRole has finished its task and shall now shoot a goal, therefore
	 * a new ShooterRole is created by the play and the roles are switched by using this function.
	 * Also, the oldRole's botID is automatically set in the newRole
	 *
	 * @param oldRole the currently assigned role
	 * @param newRole the new (not assigned) role
	 * @return new new role, if switch was successful
	 */
	protected final ARole switchRoles(final ARole oldRole, final ARole newRole)
	{
		if (newRole.isCompleted())
		{
			log.error("Role is already completed. Can not switch to new role: " + newRole.getType());
			return oldRole;
		}
		
		boolean removed = roles.remove(oldRole);
		if (!removed)
		{
			log.error("Could not switch roles. Role to switch is not in list. + " + oldRole);
			return oldRole;
		}
		roles.add(newRole);
		
		newRole.assignBotID(oldRole.getBotID());
		newRole.update(oldRole.getAiFrame());
		return newRole;
	}
	
	
	/**
	 * Add role to roles
	 * 
	 * @param role to be added
	 */
	private void addRole(final ARole role)
	{
		roles.add(role);
	}
	
	
	/**
	 * Remove a role. Do not call this from your Play!! This is intended for the RoleAssigner only
	 * 
	 * @param role to be removed
	 */
	public final void removeRole(final ARole role)
	{
		roles.remove(role);
		role.setCompleted();
		onRoleRemoved(role);
	}
	
	
	/**
	 * @return the last role in the internal list
	 */
	protected final ARole getLastRole()
	{
		return getRoles().get(getRoles().size() - 1);
	}
	
	
	/**
	 * Ask the play to add the specified number of roles
	 * 
	 * @param count number of roles to add
	 * @return the added roles
	 */
	public final List<ARole> addRoles(final int count)
	{
		List<ARole> newRoles = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			ARole role = onAddRole();
			newRoles.add(role);
			addRole(role);
		}
		return newRoles;
	}
	
	
	/**
	 * Ask the play to remove the specified number of roles
	 * 
	 * @param count number of roles to be removed
	 * @param frame current frame
	 */
	public final void removeRoles(final int count, final MetisAiFrame frame)
	{
		for (int i = 0; i < count; i++)
		{
			ARole role = onRemoveRole(frame);
			removeRole(role);
		}
	}
	
	
	/**
	 * Remove one role from this Play.
	 * Assume that there is at least one role left.
	 * 
	 * @param frame current frame
	 * @return the removed role
	 */
	protected abstract ARole onRemoveRole(MetisAiFrame frame);
	
	
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
	 * @param role that was removed
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
	}
	
	
	/**
	 * @param frame current frame
	 */
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
		aiFrame = frame;
	}
	
	
	/**
	 * @param frame current frame
	 */
	public final void update(final AthenaAiFrame frame)
	{
		if (!frame.getPrevFrame().getTacticalField().getGameState().equals(frame.getTacticalField().getGameState()))
		{
			onGameStateChanged(frame.getTacticalField().getGameState());
		}
		doUpdate(frame);
	}
	
	
	/**
	 * Compatibility method for old plays that require continues update with ai frame.
	 * It is preferred to only react in new frames in your roles.
	 * 
	 * @param frame current frame
	 */
	protected void doUpdate(final AthenaAiFrame frame)
	{
		// nothing
	}
	
	
	/**
	 * This is called, when the game state has changed.
	 * It is called before {@link APlay#doUpdate(AthenaAiFrame)}.
	 * Note: You can also get the gameState from the TacticalField
	 * 
	 * @param gameState new gameState
	 */
	protected void onGameStateChanged(final GameState gameState)
	{
		// nothing
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
	 * @return a view on the current roles
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
	protected final void setReorderedRoles(final List<ARole> orderedRoles)
	{
		if (roles.size() != orderedRoles.size())
		{
			throw new IllegalArgumentException("Provided orderedRoles list does not have correct size: "
					+ orderedRoles.size() + " != " + roles.size());
		}
		for (ARole role : orderedRoles)
		{
			if (!roles.contains(role))
			{
				throw new IllegalArgumentException(
						"Provided orderedRoles list contains an unknown role! " + role.getType());
			}
		}
		roles.clear();
		roles.addAll(orderedRoles);
	}
	
	
	/**
	 * @return the type
	 */
	public final EPlay getType()
	{
		return type;
	}
	
	
	/**
	 * Roles will be reordered so that first role in list is nearest to first destination and so on
	 * 
	 * @param destinations list of new desired destinations
	 */
	protected void reorderRolesToDestinations(final List<IVector2> destinations)
	{
		List<ARole> currentRoles = new ArrayList<>(getRoles());
		List<ARole> rolesSorted = new ArrayList<>(getRoles().size());
		for (IVector2 dest : destinations)
		{
			double minDist = Double.MAX_VALUE;
			ARole theRole = null;
			for (ARole role : currentRoles)
			{
				double dist = VectorMath.distancePP(dest, role.getPos());
				if (dist < minDist)
				{
					minDist = dist;
					theRole = role;
				}
			}
			currentRoles.remove(theRole);
			rolesSorted.add(theRole);
		}
		setReorderedRoles(rolesSorted);
	}
	
	
	protected AthenaAiFrame getAiFrame()
	{
		return aiFrame;
	}
	
	
	public WorldFrame getWorldFrame()
	{
		return aiFrame.getWorldFrame();
	}
	
	
	protected ITrackedBall getBall()
	{
		return getWorldFrame().getBall();
	}
}
