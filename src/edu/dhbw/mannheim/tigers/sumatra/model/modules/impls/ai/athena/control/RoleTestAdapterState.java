/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.GuiTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.Pair;


/**
 * This state of the {@link AthenaGuiAdapter} lets the user set single roles
 * 
 * @see AthenaGuiAdapter
 * @see AthenaControl
 * @see IGuiAdapterState
 *  
 * @author Gero, OliverS
 */
public class RoleTestAdapterState extends AGuiAdapterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<ERole>	rolesNoId	= new ArrayList<ERole>();
	private final Lachesis		lachesis		= new Lachesis();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param adapter
	 */
	public RoleTestAdapterState(AthenaGuiAdapter adapter)
	{
		super(adapter);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void choosePlays(AIInfoFrame current, AIInfoFrame previous)
	{
		if (hasChanged())
		{
			// Check given active roles
			for (Pair<Integer, ERole> rolePair : getControl().getActiveRoles())
			{
				if (rolePair.left == AthenaControl.USE_ROLE_ASSIGNMENT)
				{
					rolesNoId.add(rolePair.right);
				} else
				{
					ARole role = RoleFactory.getInstance().createRole(rolePair.right);
					role.assignBotID(rolePair.left);
					current.assignedRoles.put(role.getBotID(), role);
				}
			}
			
			// Create play and add roles which have to bee assigned
			GuiTestPlay guiPlay = new GuiTestPlay(current, rolesNoId);
			
			current.playStrategy.getActivePlays().add(guiPlay);
			current.playStrategy.setChangedPlay();
		} else
		{
			current.playStrategy.getActivePlays().addAll(previous.playStrategy.getActivePlays());
			current.assignedRoles.putAll(previous.assignedRoles);
		}
	}
	

	@Override
	public void assignRoles(AIInfoFrame current, AIInfoFrame previous)
	{
		if (hasChanged() && current.playStrategy.hasPlayChanged())
		{
			// Assign roles in play (and NOT the ones which shall already are assigned)
			lachesis.assignRoles(current);
			
			// Now, add roles which already are in the assignments to guarantee consistency
			APlay guiPlay = current.playStrategy.getActivePlays().get(0); // At this point only ONE GUI_TEST_PLAY is active
			for (ARole role : current.assignedRoles.values())
			{
				if (!guiPlay.getRoles().contains(role))
				{
					guiPlay.getRoles().add(role);
				}
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean overridePlayFinding()
	{
		return true;
	}
	

	@Override
	public boolean overrideRoleAssignment()
	{
		return true;
	}
}
