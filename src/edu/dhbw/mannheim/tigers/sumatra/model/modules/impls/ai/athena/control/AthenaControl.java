/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.Pair;


/**
 * This class should be used to control the sub-module athena on runtime (e.g. from GUI). Its members represent the
 * state of the GUI, and should be enforced by the {@link AthenaGuiAdapter}
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class AthenaControl
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Logger								log						= Logger.getLogger(getClass());
	
	public static final Integer					USE_ROLE_ASSIGNMENT	= -1;
	
	/** Stores the actual active plays of this configuration object (May be empty if {@link #activeRoles} is not!) */
	private final List<EPlay>						activePlays;
	
	/** Stores the actual active roles of this configuration object */
	private final List<Pair<Integer, ERole>>	activeRoles;
	
	private EAIControlState							controlState;
	
	/** Forces a play finder to choose new plays. */
	private boolean									forceNewDecision;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public AthenaControl()
	{
		this.controlState = EAIControlState.PLAY_TEST_MODE;
		this.activePlays = new ArrayList<EPlay>();
		this.activeRoles = new ArrayList<Pair<Integer, ERole>>();
		this.forceNewDecision = false;
	}
	

	public AthenaControl(AthenaControl copy)
	{
		this.controlState = copy.controlState;
		this.activePlays = new ArrayList<EPlay>(copy.activePlays);
		this.activeRoles = new ArrayList<Pair<Integer, ERole>>(copy.activeRoles);
		this.forceNewDecision = copy.forceNewDecision;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// -------------------------------------------------------------------------
	
	public boolean isForceNewDecision()
	{
		return forceNewDecision;
	}
	

	public void forceNewDecision(boolean b)
	{
		forceNewDecision = b;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the guiState
	 */
	public EAIControlState getControlState()
	{
		return controlState;
	}
	

	/**
	 * @param controlState the guiState to set
	 */
	public void setControlState(EAIControlState controlState)
	{
		this.controlState = controlState;
	}
	

	// --------------------------------------------------------------------------
	


	

	// --------------------------------------------------------------------------
	
	public List<EPlay> getActivePlays()
	{
		return activePlays;
	}
	

	public void addPlay(EPlay play)
	{
		activePlays.add(play);
	}
	

	public void removePlay(List<EPlay> plays)
	{
		for (EPlay ePlay : plays)
		{
			if (!activePlays.remove(ePlay))
			{
				log.error("Play " + ePlay + " not found and cannot be removed.");
			}
		}
	}
	

	// --------------------------------------------------------------------------
	
	public List<Pair<Integer, ERole>> getActiveRoles()
	{
		return activeRoles;
	}
	

	public void addRole(ERole role)
	{
		activeRoles.add(new Pair<Integer, ERole>(USE_ROLE_ASSIGNMENT, role));
	}
	

	public void addRole(ERole role, Integer botId)
	{
		activeRoles.add(new Pair<Integer, ERole>(botId, role));
	}
	

	public void removeRole(ERole role, Integer botId)
	{
		if (!activeRoles.remove(new Pair<Integer, ERole>(botId, role)))
		{
			log.error("Role (" + role + "/" + botId + ") not found and cannot be removed.");
		}
	}
	

	public void removeRole(ERole role)
	{
		if (!activeRoles.remove(new Pair<Integer, ERole>(USE_ROLE_ASSIGNMENT, role)))
		{
			log.error("Role (" + role + ") not found and cannot be removed.");
		}
	}
	

	public void clearRoles()
	{
		activeRoles.clear();
	}
	

	public void clearPlays()
	{
		activePlays.clear();
	}
	

	/**
	 * Resets the state of this {@link AthenaControl} object to its default values (except {@link #controlState}!!!)
	 */
	public void clear()
	{
		activePlays.clear();
		activeRoles.clear();
		
		forceNewDecision = false;
	}
}
