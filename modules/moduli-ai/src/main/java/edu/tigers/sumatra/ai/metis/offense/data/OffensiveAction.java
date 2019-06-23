/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * 
 */
@Persistent(version = 2)
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
		KICK_INS_BLAUE,
		/** PUSHING_KICK: Go in front of foeBot to prevent him kicking the ball. */
		PUSHING_KICK,
		/** CLEARING_KICK: Kick ball away of current situation (if no better way is found) */
		CLEARING_KICK,
		/** PULL_BACK: Pull back ball with dribbler usage, then kick */
		PULL_BACK,
		/** GOAL_SHOT: Simply shoot on goal, without any steps in between */
		GOAL_SHOT,
		/**
		 *
		 */
		@Deprecated
		MOVING_KICK,
	}

	private OffensiveMoveAndTargetInformation	moveAndTargetInformation		= null;

	private IPassTarget								passTarget							= null;

	private DynamicPosition							directShotAndClearingTarget	= null;

	private EOffensiveAction						type									= null;

	private boolean									kickInsBlauePossible				= false;

	private IVector2									kickInsBlaueTarget				= null;

	private boolean									isRoleReadyToKick					= true;

	private double										viability							= 0;
	
	
	/**
	 * 
	 */
	public OffensiveAction()
	{
		// nothing to do
	}
	
	
	/**
	 * @return the passTarget
	 */
	public IPassTarget getPassTarget()
	{
		return passTarget;
	}
	
	
	/**
	 * @param passTarget the passTarget to set
	 */
	public void setPassTarget(final IPassTarget passTarget)
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
	
	
	/**
	 * @return the kickInsBlauePossible
	 */
	public boolean isKickInsBlauePossible()
	{
		return kickInsBlauePossible;
	}
	
	
	/**
	 * @param kickInsBlauePossible the kickInsBlauePossible to set
	 */
	public void setKickInsBlauePossible(final boolean kickInsBlauePossible)
	{
		this.kickInsBlauePossible = kickInsBlauePossible;
	}
	
	
	/**
	 * @return the kickInsBlaueTarget
	 */
	public IVector2 getKickInsBlaueTarget()
	{
		return kickInsBlaueTarget;
	}
	
	
	/**
	 * @param kickInsBlaueTarget the kickInsBlaueTarget to set
	 */
	public void setKickInsBlaueTarget(final IVector2 kickInsBlaueTarget)
	{
		this.kickInsBlaueTarget = kickInsBlaueTarget;
	}
	
	
	/**
	 * @return the isRoleReadyToKick
	 */
	public boolean isRoleReadyToKick()
	{
		return isRoleReadyToKick;
	}
	
	
	/**
	 * @param isRoleReadyToKick the isRoleReadyToKick to set
	 */
	public void setRoleReadyToKick(final boolean isRoleReadyToKick)
	{
		this.isRoleReadyToKick = isRoleReadyToKick;
	}
	
	
	/**
	 * @return viability score [0,1]. 1 is good
	 */
	public double getViability()
	{
		return viability;
	}
	
	
	/**
	 * @param viability set viability
	 */
	public void setViability(final double viability)
	{
		this.viability = viability;
	}
	
	
	public OffensiveMoveAndTargetInformation getMoveAndTargetInformation()
	{
		return moveAndTargetInformation;
	}
	
	
	public void setMoveAndTargetInformation(final OffensiveMoveAndTargetInformation moveAndTargetInformation)
	{
		this.moveAndTargetInformation = moveAndTargetInformation;
	}
}
