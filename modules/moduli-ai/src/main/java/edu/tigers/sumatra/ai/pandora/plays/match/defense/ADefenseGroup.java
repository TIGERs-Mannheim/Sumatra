/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * An abstract defense group. Each group is updated in the following way:
 * <ol>
 * <li>create group</li>
 * <li>ADefenseGroup#assignRoles -> assign new roles</li>
 * <li>APlay#switchRoles -> new roles get assigned and updated</li>
 * <li>ADefenseGroup#updateRoles -> update new roles</li>
 * </ol>
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ADefenseGroup
{
	static final Comparator<IVector2> ANGLE_POS_COMPARATOR = Comparator
			.comparingDouble(pos -> getRoleToThreatAngle(pos, Geometry.getCenter()));
	static final Comparator<IVector2> ANGLE_POS_COMPARATOR_REVERSED = ANGLE_POS_COMPARATOR.reversed();
	
	static final Comparator<ARole> ANGLE_ROLE_COMPARATOR = Comparator
			.comparingDouble(role -> getRoleToThreatAngle(role.getPos(), Geometry.getCenter()));
	static final Comparator<ARole> ANGLE_ROLE_COMPARATOR_REVERSED = ANGLE_ROLE_COMPARATOR.reversed();
	
	protected final List<SwitchableDefenderRole> roles = new ArrayList<>();
	private AObjectID defendingId;
	
	
	/**
	 * @param defendingId an identifying id
	 */
	public ADefenseGroup(final AObjectID defendingId)
	{
		this.defendingId = defendingId;
	}
	
	
	protected static double getRoleToThreatAngle(final IVector2 pos, final IVector2 threat)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		IVector2 goal2Role = pos.subtractNew(goalCenter);
		IVector2 goal2Threat = threat.subtractNew(goalCenter);
		return goal2Threat.angleTo(goal2Role).orElse(0.0);
	}
	
	
	/**
	 * Create new roles, if necessary
	 */
	public abstract void assignRoles();
	
	
	/**
	 * Update roles.
	 * This is called after all new roles have been assigned and initialized
	 *
	 * @param aiFrame
	 */
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
	}
	
	
	/**
	 * Add a role to this group
	 *
	 * @param role the role to add
	 */
	public void addRole(ARole role)
	{
		roles.add(new SwitchableDefenderRole(role));
	}
	
	
	public List<SwitchableDefenderRole> getRoles()
	{
		return Collections.unmodifiableList(roles);
	}
	
	
	public AObjectID getDefendingId()
	{
		return defendingId;
	}
}
