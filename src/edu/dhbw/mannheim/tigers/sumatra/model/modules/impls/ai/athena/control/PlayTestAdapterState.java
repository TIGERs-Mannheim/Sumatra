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


import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayFactory;


/**
 * This state of the {@link AthenaGuiAdapter} lets the GUI set the plays and uses default role-assignment from
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis}
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
	private static final Logger	log	= Logger.getLogger(PlayTestAdapterState.class.getName());
	
	
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
		if (current.worldFrame.ball.getPos().equals(GeoMath.INIT_VECTOR))
		{
			// there is no ball, Sumatra WILL crash in this state!
			if (!current.playStrategy.getActivePlays().isEmpty() || !getControl().getActivePlays().isEmpty())
			{
				log.warn("There is no ball");
			}
			getControl().clear();
			current.playStrategy.getActivePlays().clear();
			return;
		}
		if (adapterHasChanged())
		{
			if (!getControl().getActivePlays().isEmpty())
			{
				current.playStrategy.getActivePlays().addAll(previous.playStrategy.getActivePlays());
			}
			for (PlayAndRoleCount parc : getControl().getNewPlays())
			{
				EPlay ePlay = parc.getePlay();
				if (ePlay == EPlay.GUI_TEST_PLAY)
				{
					continue;
				}
				int numRolesToAssign = parc.getNumRolesToAssign();
				APlay play = PlayFactory.getInstance().createPlay(ePlay, current, numRolesToAssign);
				if (play == null)
				{
					continue;
				}
				int roleCount = play.getRoleCount();
				if (roleCount <= current.worldFrame.tigerBotsAvailable.size())
				{
					log.trace("New Play: " + play);
					current.playStrategy.getActivePlays().add(play);
					getControl().addActivePlay(play);
				}
			}
			
			int roleSum = 0;
			for (APlay play : current.playStrategy.getActivePlays())
			{
				roleSum += play.getNumAssignedRoles();
			}
			
			List<APlay> tobeRemoved = new LinkedList<APlay>();
			for (APlay play : current.playStrategy.getActivePlays())
			{
				if (roleSum <= current.worldFrame.tigerBotsAvailable.size())
				{
					break;
				}
				tobeRemoved.add(play);
				roleSum -= play.getNumAssignedRoles();
			}
			
			for (APlay play : tobeRemoved)
			{
				play.changeToCanceled();
				current.playStrategy.getActivePlays().remove(play);
				log.trace("Play removed: " + play);
			}
			
			getControl().getNewPlays().clear();
		} else
		{
			if (current.playStrategy.isForceNewDecision())
			{
				// most properly there was an exception, because forceNewDecision from GUI is disabled
				// we remove all plays to be safe
				getControl().clear();
				for (APlay play : current.playStrategy.getActivePlays())
				{
					play.changeToCanceled();
				}
				current.playStrategy.getActivePlays().clear();
				return;
			}
			
			current.playStrategy.getActivePlays().addAll(previous.playStrategy.getActivePlays());
			
			// Remove finished plays
			if (!previous.playStrategy.getFinishedPlays().isEmpty())
			{
				for (APlay aPlay : previous.playStrategy.getFinishedPlays())
				{
					getControl().removePlay(aPlay);
				}
				
				current.playStrategy.getActivePlays().removeAll(previous.playStrategy.getFinishedPlays());
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
