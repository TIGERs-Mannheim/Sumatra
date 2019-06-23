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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IAthenaControlHandler;


/**
 * This class is the adapter that allows the GUI to influence the behavior of
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena}. It receives {@link AthenaControl}
 * -objects from the GUI and carries them out by changing the given {@link AIInfoFrame} in on of
 * its callbacks.
 * 
 * @author Gero
 */
public class AthenaGuiAdapter implements IAthenaControlHandler, IGuiAdapterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private AthenaControl		control;
	private boolean				changed	= false;
	
	private AGuiAdapterState	currentState;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public AthenaGuiAdapter()
	{
		control = new AthenaControl();
		onNewAthenaControl(control);
	}
	
	
	/**
	 * This method is <code>synchronized</code> because it is accessed by the GUI while
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena} is working on <code>this</code>
	 */
	@Override
	public final synchronized void onNewAthenaControl(AthenaControl newControl)
	{
		control = newControl;
		
		switch (newControl.getControlState())
		{
			case MATCH_MODE:
				currentState = new MatchModeAdapterState(this);
				break;
			
			case MIXED_TEAM_MODE:
				currentState = new MixedTeamModeAdapterState(this);
				break;
			
			case PLAY_TEST_MODE:
				currentState = new PlayTestAdapterState(this);
				break;
			
			case ROLE_TEST_MODE:
				currentState = new RoleTestAdapterState(this);
				break;
			
			case EMERGENCY_MODE:
				currentState = new EmergencyModeAdapterState(this);
				break;
		}
		
		// Changed-flag! (see afterRoleAssignment(...) )
		changed = true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void beforePlayFinding(AIInfoFrame current, AIInfoFrame previous)
	{
		currentState.beforePlayFinding(current, previous);
	}
	
	
	@Override
	public void choosePlays(AIInfoFrame current, AIInfoFrame previous)
	{
		currentState.choosePlays(current, previous);
	}
	
	
	@Override
	public void betweenPlayRole(AIInfoFrame current, AIInfoFrame previous)
	{
		currentState.betweenPlayRole(current, previous);
	}
	
	
	@Override
	public void assignRoles(AIInfoFrame current, AIInfoFrame previous)
	{
		currentState.assignRoles(current, previous);
	}
	
	
	@Override
	public void afterRoleAssignment(AIInfoFrame current, AIInfoFrame previous)
	{
		currentState.afterRoleAssignment(current, previous);
		
		// Clear flag (see onNewAthenaControl(...) )
		changed = false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean overridePlayFinding()
	{
		return currentState.overridePlayFinding();
	}
	
	
	@Override
	public boolean overrideRoleAssignment()
	{
		return currentState.overrideRoleAssignment();
	}
	
	
	/**
	 * @return
	 */
	public boolean hasChanged()
	{
		return changed;
	}
	
	
	/**
	 * @return
	 */
	public AthenaControl getControl()
	{
		return control;
	}
}
