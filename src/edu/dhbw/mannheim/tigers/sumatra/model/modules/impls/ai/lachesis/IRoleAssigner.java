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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


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
	 * @param frame The {@link AIInfoFrame} of the current AI-cycle
	 */
	void assignRoles(Collection<TrackedTigerBot> assignees, List<ARole> rolesToAssign, MetisAiFrame frame);
}
