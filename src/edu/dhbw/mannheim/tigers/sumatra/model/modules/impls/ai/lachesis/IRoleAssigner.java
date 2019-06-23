/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * Gero
 * Oliver Steinbrecher
 * Daniel Waigand
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;


/**
 * Encapsulates types which are capable of assigning roles to bots
 * 
 * @author Gero, Oliver Steinbrecher, Daniel Waigand
 * 
 */
public interface IRoleAssigner
{
	/**
	 * @param assignees The {@link TrackedTigerBot}s which are waiting for {@link ARole}
	 * @param rolesToAssign The {@link ARole}s which have to be assigned
	 * @param assignments The map the assignments of this AI-cycle are placed in. May very well be the {@link AIInfoFrame#assignedRoles}!
	 * @param frame The {@link AIInfoFrame} of the current AI-cycle
	 */
	public void assignRoles(Collection<TrackedTigerBot> assignees, List<ARole> rolesToAssign, Map<Integer, ARole> assignments, AIInfoFrame frame);
}
