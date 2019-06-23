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

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.GuiTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
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
	private final Lachesis	lachesis	= new Lachesis();
	
	
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
		if (adapterHasChanged())
		{
			Map<ERole, IVector2> rolesAndInitPos = new HashMap<ERole, IVector2>();
			// Check given active roles
			for (Pair<BotID, ERole> rolePair : getControl().getActiveRoles())
			{
				if (rolePair.left.isUninitializedID())
				{
					rolesAndInitPos.put(rolePair.right, new Vector2());
				} else
				{
					rolesAndInitPos.put(rolePair.right, current.worldFrame.tigerBotsVisible.get(rolePair.left).getPos());
				}
			}
			
			// Create play and add roles which have to bee assigned
			GuiTestPlay guiPlay = new GuiTestPlay(current, rolesAndInitPos);
			
			current.playStrategy.getActivePlays().add(guiPlay);
		} else
		{
			current.playStrategy.getActivePlays().addAll(previous.playStrategy.getActivePlays());
			current.putAllAssignedRoles(previous.getAssigendRoles());
		}
	}
	
	
	@Override
	public void assignRoles(AIInfoFrame current, AIInfoFrame previous)
	{
		if (adapterHasChanged() && current.playStrategy.hasPlayChanged())
		{
			// Assign roles in play (and NOT the ones which shall already are assigned)
			lachesis.assignRoles(current);
			
			// Now, add roles which already are in the assignments to guarantee consistency
			// At this point only ONE GUI_TEST_PLAY is active
			APlay guiPlay = current.playStrategy.getActivePlays().get(0);
			for (ARole role : current.getAssigendRoles().values())
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
