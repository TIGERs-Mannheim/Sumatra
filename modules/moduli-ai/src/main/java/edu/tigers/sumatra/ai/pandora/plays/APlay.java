/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;


/**
 * This is the abstract play class. all plays inherit from here.<br/>
 * This type already includes a list for contained roles ({@link #roles}
 */
@Log4j2
public abstract class APlay
{
	private final List<ARole> roles = new CopyOnWriteArrayList<>();
	private final EPlay type;
	private AthenaAiFrame aiFrame;

	private boolean numberOfBotsChanged = false;


	protected APlay(final EPlay type)
	{
		this.type = type;
	}


	/**
	 * Switch oldRole with newRole. OldRole must have a botId assigned.
	 * Example: The OffenseRole has finished its task and shall now shoot a goal, therefore
	 * a new ShooterRole is created by the play and the roles are switched by using this function.
	 * Also, the oldRole's botID is automatically set in the newRole
	 *
	 * @param oldRole the currently assigned role
	 * @param newRole the new (not assigned) role
	 * @return new role, if switch was successful
	 */
	protected final <T extends ARole> T switchRoles(final ARole oldRole, final T newRole)
	{
		if (newRole.isCompleted())
		{
			throw new IllegalStateException("Role is already completed. Can not switch to new role: " + newRole);
		}

		boolean removed = roles.remove(oldRole);
		if (!removed)
		{
			throw new IllegalStateException("Could not switch roles. Role to switch is not in list. + " + oldRole);
		}
		roles.add(newRole);

		newRole.assignBotID(oldRole.getBotID());
		newRole.updateBefore(oldRole.getAiFrame());
		return newRole;
	}


	@SuppressWarnings("unchecked")
	protected final <T extends ARole> T reassignRole(
			final ARole role,
			Class<T> roleClass,
			Supplier<T> roleConstructor
	)
	{
		if (!role.getClass().equals(roleClass))
		{
			return switchRoles(role, roleConstructor.get());
		}
		return (T) role;
	}


	/**
	 * Add role to roles
	 *
	 * @param role to be added
	 */
	protected final void addRole(final ARole role)
	{
		log.trace("Adding role: {} {}", role.getType(), role.getBotID());
		role.updateBefore(aiFrame);
		roles.add(role);
		numberOfBotsChanged = true;
		log.trace("Added role: {} {}", role.getType(), role.getBotID());
	}


	/**
	 * Remove a role. Do not call this from your Play!! This is intended for the RoleAssigner only
	 *
	 * @param role to be removed
	 */
	public final void removeRole(final ARole role)
	{
		log.trace("Removing role: {} {}", role.getType(), role.getBotID());
		roles.remove(role);
		role.setCompleted();
		numberOfBotsChanged = true;
	}


	protected void onNumberOfBotsChanged()
	{
	}


	/**
	 * @return the last role in the internal list
	 */
	private ARole getLastRole()
	{
		return getRoles().getLast();
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
	 */
	public final void removeRoles(final int count)
	{
		for (int i = 0; i < count; i++)
		{
			ARole role = onRemoveRole();
			removeRole(role);
		}
	}


	/**
	 * Remove one role from this Play.
	 * Assume that there is at least one role left.
	 *
	 * @return the removed role
	 */
	private ARole onRemoveRole()
	{
		return getLastRole();
	}


	/**
	 * Some upper layer has decided that this play will now play with an additional role.
	 * Please assume that the Play will start with zero roles until the play can only run with
	 * a static number of roles anyway
	 *
	 * @return the added role
	 */
	protected ARole onAddRole()
	{
		return new MoveRole();
	}


	/**
	 * Update before roles have been updated
	 *
	 * @param frame current frame
	 */
	public final void updateBeforeRoles(final AthenaAiFrame frame)
	{
		aiFrame = frame;

		if (numberOfBotsChanged)
		{
			onNumberOfBotsChanged();
			numberOfBotsChanged = false;
		}

		doUpdateBeforeRoles();
	}


	/**
	 * Update before roles have been updated
	 */
	protected void doUpdateBeforeRoles()
	{
		// can be overwritten
	}


	/**
	 * Update after roles have been updated
	 */
	public final void updateAfterRoles()
	{
		doUpdateAfterRoles();
	}


	/**
	 * Update after roles have been updated
	 */
	protected void doUpdateAfterRoles()
	{
		// can be overwritten
	}


	@Override
	public final String toString()
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
	 * Get the role for the given botId.
	 * If no role is found, an exception is thrown.
	 *
	 * @param botId the bot id to search for
	 * @return the role for the given bot id
	 */
	public final ARole getRole(final BotID botId)
	{
		return roles.stream()
				.filter(r -> r.getBotID().equals(botId))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No role found for bot: " + botId));
	}


	@SuppressWarnings("unchecked")
	protected final <T extends ARole> List<T> findRoles(Class<T> clazz)
	{
		return roles.stream()
				.filter(r -> r.getClass().equals(clazz))
				.map(r -> (T) r)
				.toList();
	}


	protected final <T extends ARole> T findRole(Class<T> clazz)
	{
		var classRoles = findRoles(clazz);
		if (classRoles.size() != 1)
		{
			throw new IllegalStateException("Expected exactly one role: " + classRoles);
		}
		return classRoles.getFirst();
	}


	protected final List<ARole> findOtherRoles(Class<?> clazz)
	{
		return roles.stream()
				.filter(r -> !r.getClass().equals(clazz))
				.toList();
	}


	protected final List<ARole> allRolesExcept(ARole... except)
	{
		List<ARole> exceptRoles = Arrays.asList(except);
		return roles.stream()
				.filter(r -> !exceptRoles.contains(r))
				.toList();
	}


	/**
	 * @return the type
	 */
	public final EPlay getType()
	{
		return type;
	}


	protected final AthenaAiFrame getAiFrame()
	{
		return aiFrame;
	}


	protected final TacticalField getTacticalField()
	{
		return aiFrame.getTacticalField();
	}


	public final WorldFrame getWorldFrame()
	{
		return aiFrame.getWorldFrame();
	}


	protected final ITrackedBall getBall()
	{
		return getWorldFrame().getBall();
	}


	/**
	 * @param identifier shape layer identifier
	 * @return the respective list from the tactical field
	 */
	protected final List<IDrawableShape> getShapes(final IShapeLayerIdentifier identifier)
	{
		return getAiFrame().getShapeMap().get(identifier);
	}
}
