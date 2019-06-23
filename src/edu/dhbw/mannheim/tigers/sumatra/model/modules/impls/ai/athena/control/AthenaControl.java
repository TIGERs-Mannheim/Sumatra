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

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
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
	
	// Logger
	private static final Logger				log	= Logger.getLogger(AthenaControl.class.getName());
	
	/** Stores the actual active plays of this configuration object (May be empty if {@link #activeRoles} is not!) */
	private final List<APlay>					activePlays;
	private final List<PlayAndRoleCount>	newPlays;
	
	/** Stores the actual active roles of this configuration object */
	private final List<Pair<BotID, ERole>>	activeRoles;
	
	private EAIControlState						controlState;
	
	/** Forces a play finder to choose new plays. */
	private boolean								forceNewDecision;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public AthenaControl()
	{
		controlState = EAIControlState.PLAY_TEST_MODE;
		activePlays = new ArrayList<APlay>();
		newPlays = new ArrayList<PlayAndRoleCount>();
		activeRoles = new ArrayList<Pair<BotID, ERole>>();
		forceNewDecision = false;
	}
	
	
	/**
	 * @param copy
	 */
	public AthenaControl(AthenaControl copy)
	{
		controlState = copy.controlState;
		activePlays = new ArrayList<APlay>(copy.activePlays);
		newPlays = new ArrayList<PlayAndRoleCount>(copy.newPlays);
		activeRoles = new ArrayList<Pair<BotID, ERole>>(copy.activeRoles);
		forceNewDecision = copy.forceNewDecision;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// -------------------------------------------------------------------------
	/**
	 * @return
	 */
	public boolean isForceNewDecision()
	{
		return forceNewDecision;
	}
	
	
	/**
	 * @param b
	 */
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
	/**
	 * @return
	 */
	public List<APlay> getActivePlays()
	{
		return activePlays;
	}
	
	
	/**
	 * @param play
	 */
	public void addActivePlay(APlay play)
	{
		activePlays.add(play);
	}
	
	
	/**
	 * @param parc
	 */
	public void addNewPlay(PlayAndRoleCount parc)
	{
		newPlays.add(parc);
	}
	
	
	/**
	 * @param play
	 */
	public void removePlay(APlay play)
	{
		activePlays.remove(play);
	}
	
	
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public List<Pair<BotID, ERole>> getActiveRoles()
	{
		return activeRoles;
	}
	
	
	/**
	 * @param role
	 */
	public void addRole(ERole role)
	{
		activeRoles.add(new Pair<BotID, ERole>(new BotID(), role));
	}
	
	
	/**
	 * @param role
	 * @param botId
	 */
	public void addRole(ERole role, BotID botId)
	{
		activeRoles.add(new Pair<BotID, ERole>(botId, role));
	}
	
	
	/**
	 * @param role
	 * @param botId
	 */
	public void removeRole(ERole role, BotID botId)
	{
		if (!activeRoles.remove(new Pair<BotID, ERole>(botId, role)))
		{
			log.error("Role (" + role + "/" + botId + ") not found and cannot be removed.");
		}
	}
	
	
	/**
	 * @param role
	 */
	public void removeRole(ERole role)
	{
		if (!activeRoles.remove(new Pair<Integer, ERole>(AObjectID.UNINITIALIZED_ID, role)))
		{
			log.error("Role (" + role + ") not found and cannot be removed.");
		}
	}
	
	
	/**
	 */
	public void clearRoles()
	{
		activeRoles.clear();
	}
	
	
	/**
	 */
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
		newPlays.clear();
		
		forceNewDecision = false;
	}
	
	
	/**
	 * @return the newPlays
	 */
	public final List<PlayAndRoleCount> getNewPlays()
	{
		return newPlays;
	}
	
}
