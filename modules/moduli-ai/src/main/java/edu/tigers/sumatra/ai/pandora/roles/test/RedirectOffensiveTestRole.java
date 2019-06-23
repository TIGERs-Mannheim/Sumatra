/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleSpecialMovementState;


/**
 * @author Dominik Engelhardt <Dominik.Engelhardt@dlr.de>
 */
public class RedirectOffensiveTestRole extends OffensiveRole
{
	
	private boolean aiEnabled = true;
	
	
	@Override
	protected void beforeUpdate()
	{
		changeStateIfNecessary();
	}
	
	
	/**
	 * 
	 */
	public RedirectOffensiveTestRole()
	{
		setInitialState(new OffensiveRoleSpecialMovementState(this));
	}
	
	
	@Override
	public void changeStateIfNecessary()
	{
		if (aiEnabled)
		{
			super.changeStateIfNecessary();
		}
	}
	
	
	/**
	 * @param enabled
	 */
	public void setAiEnabled(final boolean enabled)
	{
		aiEnabled = enabled;
	}
}
