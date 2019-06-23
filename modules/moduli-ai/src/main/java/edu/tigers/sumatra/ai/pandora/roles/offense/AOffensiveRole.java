/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.offense;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class AOffensiveRole extends ARole
{
	protected static final Logger	log					= Logger.getLogger(AOffensiveRole.class.getName());
	
	protected boolean					activeSwitching	= true;
	
	
	/**
	 * Default
	 */
	public AOffensiveRole()
	{
		super(ERole.OFFENSIVE);
	}
	
	
	/**
	 * This method is called before the state machine update.
	 * Use this for role-global actions. <br>
	 */
	@Override
	protected void beforeUpdate()
	{
		if (activeSwitching)
		{
			changeStateIfNecessary();
		}
	}
	
	
	/**
	 * Check if state change necessary and perform switch
	 */
	public void changeStateIfNecessary()
	{
		if (getAiFrame().getTacticalField().getOffensiveStrategy() != null
				&& getAiFrame().getTacticalField().getOffensiveStrategy().getCurrentOffensivePlayConfiguration() != null
				&& getAiFrame().getTacticalField().getOffensiveStrategy().getCurrentOffensivePlayConfiguration()
						.containsKey(getBotID())
				&& !getCurrentState().getIdentifier().equals(getAiFrame().getTacticalField().getOffensiveStrategy()
						.getCurrentOffensivePlayConfiguration().get(getBotID()).name()))
		{
			triggerEvent(getAiFrame().getTacticalField().getOffensiveStrategy()
					.getCurrentOffensivePlayConfiguration().get(getBotID()));
		}
		
	}
	
	
	protected void printDebugInformation(final String text)
	{
		if (OffensiveConstants.isShowDebugInformations())
		{
			log.debug(text);
		}
	}
}
