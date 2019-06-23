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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;


/**
 * This interface encapsulates the different states of the {@link AthenaGuiAdapter}. Each method is a callback executed
 * in {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena}.
 * 
 * @author Gero
 */
public interface IGuiAdapterState
{
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Called before the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.APlayFinder} is
	 * called. Lets subclasses set
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy#setForceNewDecision()}
	 * 
	 * @param current
	 * @param previous
	 */
	void beforePlayFinding(AIInfoFrame current, AIInfoFrame previous);
	
	
	/**
	 * Called if {@link #overridePlayFinding()} returns <code>true</code>
	 * 
	 * @param current
	 * @param previous
	 */
	void choosePlays(AIInfoFrame current, AIInfoFrame previous);
	
	
	/**
	 * May be used for influencing the chosen
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay}s (or something like that
	 * 
	 * @param current
	 * @param previous
	 */
	void betweenPlayRole(AIInfoFrame current, AIInfoFrame previous);
	
	
	/**
	 * Gets called if {@link #overrideRoleAssignment()} returns <code>true</code>
	 * 
	 * @param current
	 * @param previous
	 */
	void assignRoles(AIInfoFrame current, AIInfoFrame previous);
	
	
	/**
	 * May be used to check or change role-assignment
	 * 
	 * @param current
	 * @param previous
	 */
	void afterRoleAssignment(AIInfoFrame current, AIInfoFrame previous);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return Whether {@link #choosePlays(AIInfoFrame, AIInfoFrame)} should be used instead of
	 *         {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena}s
	 *         {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.APlayFinder}
	 *         -implementation
	 */
	boolean overridePlayFinding();
	
	
	/**
	 * @return Whether {@link #assignRoles(AIInfoFrame, AIInfoFrame)} should be used instead of
	 *         {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis}
	 */
	boolean overrideRoleAssignment();
	
}