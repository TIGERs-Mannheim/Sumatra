/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
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
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected static final Logger log = Logger.getLogger(AOffensiveRole.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
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
		changeStateIfNecessary();
	}
	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * 
	 */
	public void changeStateIfNecessary()
	{
		if (getAiFrame().getTacticalField() != null)
		{
			if (getAiFrame().getTacticalField().getOffensiveStrategy() != null)
			{
				if (getAiFrame().getTacticalField().getOffensiveStrategy().getCurrentOffensivePlayConfiguration() != null)
				{
					if (getAiFrame().getTacticalField().getOffensiveStrategy()
							.getCurrentOffensivePlayConfiguration().containsKey(getBotID()))
					{
						if (getCurrentState() != getAiFrame().getTacticalField().getOffensiveStrategy()
								.getCurrentOffensivePlayConfiguration().get(getBotID()))
						{
							triggerEvent(getAiFrame().getTacticalField().getOffensiveStrategy()
									.getCurrentOffensivePlayConfiguration().get(getBotID()));
						}
					}
				}
			}
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
