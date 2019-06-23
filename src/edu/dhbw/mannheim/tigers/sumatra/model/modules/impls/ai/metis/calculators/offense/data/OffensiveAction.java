/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.12.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;


/**
 * 
 */
@Persistent
public class OffensiveAction
{
	
	/**
	 * defines offensive Actions
	 */
	public enum EOffensiveAction
	{
		/** PASS: Pass ball to another tiger */
		PASS,
		/** */
		KICKOFF,
		/** MOVING_KICK: first Kick ball beside foeBot, then go there and kick to goal. */
		MOVING_KICK,
		/** PUSHING_KICK: Go in front of foeBot to prevent him kicking the ball. */
		PUSHING_KICK,
		/** CLEARING_KICK: Kick ball away of current situation (if no better way is found) */
		CLEARING_KICK,
		/** PULL_BACK: Pull back ball with dribbler usage, then kick */
		PULL_BACK,
		/** GOAL_SHOT: Simply shoot on goal, without any steps in between */
		GOAL_SHOT;
	}
	
	private AdvancedPassTarget	passTarget							= null;
	
	private DynamicPosition		directShotAndClearingTarget	= null;
	
	private EOffensiveAction	type									= null;
	
	
	/**
	 * 
	 */
	public OffensiveAction()
	{
		
	}
	
	
	/**
	 * @return the passTarget
	 */
	public AdvancedPassTarget getPassTarget()
	{
		return passTarget;
	}
	
	
	/**
	 * @param passTarget the passTarget to set
	 */
	public void setPassTarget(final AdvancedPassTarget passTarget)
	{
		this.passTarget = passTarget;
	}
	
	
	/**
	 * @return the directShotTarget
	 */
	public DynamicPosition getDirectShotAndClearingTarget()
	{
		return directShotAndClearingTarget;
	}
	
	
	/**
	 * @param directShotTarget the directShotTarget to set
	 */
	public void setDirectShotAndClearingTarget(final DynamicPosition directShotTarget)
	{
		directShotAndClearingTarget = directShotTarget;
	}
	
	
	/**
	 * @return the type
	 */
	public EOffensiveAction getType()
	{
		return type;
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setType(final EOffensiveAction type)
	{
		this.type = type;
	}
	
	
}
