/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.04.2016
 * Author(s): Dominik Engelhardt <Dominik.Engelhardt@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleRedirectCatchSpecialMovementState;


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
		setInitialState(new OffensiveRoleRedirectCatchSpecialMovementState(this));
	}
	
	
	@Override
	public void changeStateIfNecessary()
	{
		if (aiEnabled)
		{
			super.changeStateIfNecessary();
		} else if (getCurrentState() != EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE)
		{
			triggerEvent(EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE);
		}
	}
	
	
	@Override
	public void beforeFirstUpdate()
	{
	}
	
	
	/**
	 * @param enabled
	 */
	public void setAiEnabled(final boolean enabled)
	{
		aiEnabled = enabled;
	}
	
	
}
