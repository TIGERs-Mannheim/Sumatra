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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.IPlayFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;


/**
 * This interface encapsulates the different states of the {@link AthenaGuiAdapter}. Each method is a callback executed
 * in {@link Athena}.
 * 
 * @author Gero
 */
public interface IGuiAdapterState
{
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Called before the {@link IPlayFinder} is called. Lets subclasses set
	 * {@link PlayStrategy#setForceNewDecision(boolean)}, e.g.
	 * 
	 * @param current
	 * @param previous
	 */
	public abstract void beforePlayFinding(AIInfoFrame current, AIInfoFrame previous);
	

	/**
	 * Called if {@link #overridePlayFinding()} returns <code>true</code>
	 * 
	 * @param current
	 * @param previous
	 */
	public abstract void choosePlays(AIInfoFrame current, AIInfoFrame previous);
	

	/**
	 * May be used for influencing the chosen {@link APlay}s (or something like that
	 * 
	 * @param current
	 * @param previous
	 */
	public abstract void betweenPlayRole(AIInfoFrame current, AIInfoFrame previous);
	

	/**
	 * Gets called if {@link #overrideRoleAssignment()} returns <code>true</code>
	 * 
	 * @param current
	 * @param previous
	 */
	public abstract void assignRoles(AIInfoFrame current, AIInfoFrame previous);
	

	/**
	 * May be used to check or change role-assignment
	 * 
	 * @param current
	 * @param previous
	 */
	public abstract void afterRoleAssignment(AIInfoFrame current, AIInfoFrame previous);
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return Whether {@link #choosePlays(AIInfoFrame, AIInfoFrame)} should be used instead of {@link Athena}s
	 *         {@link IPlayFinder}-implementation
	 */
	public abstract boolean overridePlayFinding();
	

	/**
	 * @return Whether {@link #assignRoles(AIInfoFrame, AIInfoFrame)} should be used instead of {@link Lachesis}
	 */
	public abstract boolean overrideRoleAssignment();
	
}