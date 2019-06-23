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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayFactory;


/**
 * This state of the {@link AthenaGuiAdapter} lets the GUI set the plays and uses default role-assignment from
 * {@link Lachesis}
 * 
 * @see AthenaGuiAdapter
 * @see AthenaControl
 * @see IGuiAdapterState
 * 
 * @author Gero
 */
public class PlayTestAdapterState extends AGuiAdapterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param adapter
	 */
	public PlayTestAdapterState(AthenaGuiAdapter adapter)
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
			for (EPlay ePlay : getControl().getActivePlays())
			{
				APlay play = PlayFactory.getInstance().createPlay(ePlay, current);
				current.playStrategy.getActivePlays().add(play);
			}
			current.playStrategy.setChangedPlay();
		} else
		{
			// Note: The following lines may cause Athena to update already finished (succeeded/failed) plays.
		
			current.playStrategy.getActivePlays().addAll(previous.playStrategy.getActivePlays());
			//current.assignedRoles.putAll(previous.assignedRoles);
			
			// Remove finished plays
			if(previous.playStrategy.getFinishedPlays().size()!=0){
				
				for (APlay aPlay : previous.playStrategy.getFinishedPlays())
				{
					List<EPlay> asList = new ArrayList<EPlay>();
					asList.add(aPlay.getType());
					getControl().removePlay(asList);
				}

				current.playStrategy.getActivePlays().removeAll(previous.playStrategy.getFinishedPlays());
				current.playStrategy.setChangedPlay();
			}
			
		}
	}
	

	@Override
	public void betweenPlayRole(AIInfoFrame current, AIInfoFrame previous)
	{

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
		return false;
	}
}
