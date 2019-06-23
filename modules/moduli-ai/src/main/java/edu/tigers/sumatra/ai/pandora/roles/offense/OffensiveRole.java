/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleDelayState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleInterceptionState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleKickState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleRedirectCatchSpecialMovementState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleStopState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleSupportiveAttackerState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRole extends AOffensiveRole
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
		OffensiveRoleKickState kicker = new OffensiveRoleKickState(this);
		OffensiveRoleStopState stop = new OffensiveRoleStopState(this);
		OffensiveRoleDelayState delay = new OffensiveRoleDelayState(this);
		OffensiveRoleRedirectCatchSpecialMovementState redirector = new OffensiveRoleRedirectCatchSpecialMovementState(
				this);
		OffensiveRoleInterceptionState intercept = new OffensiveRoleInterceptionState(this);
		OffensiveRoleSupportiveAttackerState supporter = new OffensiveRoleSupportiveAttackerState(this);
		setInitialState(stop);
		
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
				log.warn("Added Get Offenisve, this should not happen anymore!");
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
	
	// // @Override
	// // public void setNewSkill(final ISkill newSkill)
	// // {
	// // super.setNewSkill(newSkill);
	// //
	// // }
	//
	//
	// /**
	// * Start a timeout after which the role will be set so be completed
	// *
	// * @param timeMs [ms]
	// */
	// @Override
	// public void setCompleted(final int timeMs)
	// {
	// super.setCompleted(timeMs);
	// }
	//
	//
	// /**
	// * @return
	// */
	// @Override
	// public ERole getType()
	// {
	// return super.getType();
	// }
	//
	//
	// /**
	// * @return
	// */
	// @Override
	// public BotID getBotID()
	// {
	// return super.getBotID();
	// }
	//
	//
	// /**
	// * Returns the current position of the bot associated with this role.<br>
	// * <strong>WARNING: Use only after role has been assigned!!!</strong> (Makes no sense otherwise...)
	// *
	// * @return position
	// * @throws IllegalStateException if role has not been initialized yet
	// */
	// @Override
	// public IVector2 getPos()
	// {
	// return super.getPos();
	// }
	//
	//
	// /**
	// * Get the TrackedTigerBot of this role
	// *
	// * @return
	// */
	// @Override
	// public ITrackedBot getBot()
	// {
	// return super.getBot();
	// }
	//
	//
	// /**
	// * @return Whether this role has been assigned to a bot (by
	// * {@link edu.tigers.sumatra.ai.lachesis.Lachesis})
	// */
	// @Override
	// public boolean hasBeenAssigned()
	// {
	// return super.hasBeenAssigned();
	// }
	//
	//
	// /**
	// * @return true when this role is completed
	// */
	// @Override
	// public boolean isCompleted()
	// {
	// return super.isCompleted();
	// }
	//
	//
	// /**
	// * Sets this role to completed.
	// */
	// @Override
	// public void setCompleted()
	// {
	// super.setCompleted();
	// }
	//
	//
	// @Override
	// public String toString()
	// {
	// return super.toString();
	// }
	//
	//
	// /**
	// * @return the current ai frame
	// * @throws NullPointerException if called before {@link #update(AthenaAiFrame)}
	// */
	// @Override
	// public AthenaAiFrame getAiFrame()
	// {
	// return super.getAiFrame();
	// }
	//
	//
	// /**
	// * @return the current worldframe
	// */
	// @Override
	// public WorldFrame getWFrame()
	// {
	// return super.getWFrame();
	// }
	//
	//
	// /**
	// * @return the currentSkill
	// */
	// @Override
	// public ISkill getCurrentSkill()
	// {
	// return super.getCurrentSkill();
	// }
	
	
}
