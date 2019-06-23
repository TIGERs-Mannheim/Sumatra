/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.states.OffensiveRoleSupportiveAttackerState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRole extends OffensiveRoleSupportiveAttackerState
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public OffensiveRole()
	{
		super();
		BallGettingState getter = new BallGettingState();
		KickState kicker = new KickState();
		StopState stop = new StopState();
		DelayState delay = new DelayState();
		RedirectCatchSpecialMoveState redirector = new RedirectCatchSpecialMoveState();
		InterceptionState intercept = new InterceptionState();
		SupportiveAttackerState supporter = new SupportiveAttackerState();
		setInitialState(stop);
		
		addTransition(EOffensiveStrategy.GET, getter);
		addTransition(EOffensiveStrategy.KICK, kicker);
		addTransition(EOffensiveStrategy.DELAY, delay);
		addTransition(EOffensiveStrategy.STOP, stop);
		addTransition(EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE, redirector);
		addTransition(EOffensiveStrategy.INTERCEPT, intercept);
		addTransition(EOffensiveStrategy.SUPPORTIVE_ATTACKER, supporter);
	}
	
	
	@Override
	public void beforeFirstUpdate()
	{
		EOffensiveStrategy initialState = getAiFrame().getTacticalField().getOffensiveStrategy()
				.getCurrentOffensivePlayConfiguration().get(getBotID());
		
		if (initialState == null)
		{
			int idx = getAiFrame().getPrevFrame().getAICom().getUnassignedStateCounter();
			
			if (idx >= getAiFrame().getTacticalField().getOffensiveStrategy().getUnassignedStrategies().size())
			{
				initialState = EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE;
			} else
			{
				initialState = getAiFrame().getTacticalField().getOffensiveStrategy().getUnassignedStrategies()
						.get(idx);
				getAiFrame().getPrevFrame().getAICom().setUnassignedStateCounter(idx + 1);
			}
		}
		
		switch (initialState)
		{
			case REDIRECT_CATCH_SPECIAL_MOVE:
				triggerEvent(EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE);
				break;
			case DELAY:
				triggerEvent(EOffensiveStrategy.DELAY);
				break;
			case GET:
				triggerEvent(EOffensiveStrategy.GET);
				break;
			case INTERCEPT:
				triggerEvent(EOffensiveStrategy.INTERCEPT);
				break;
			case KICK:
				triggerEvent(EOffensiveStrategy.KICK);
				break;
			case STOP:
				triggerEvent(EOffensiveStrategy.STOP);
				break;
			case SUPPORTIVE_ATTACKER:
				triggerEvent(EOffensiveStrategy.SUPPORTIVE_ATTACKER);
				break;
			default:
				break;
		}
	}
	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
}
