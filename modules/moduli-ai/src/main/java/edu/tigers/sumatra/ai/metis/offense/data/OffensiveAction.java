/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.12.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense.data;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.support.data.AdvancedPassTarget;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.DribblePath;
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
	
	private AdvancedPassTarget		passTarget							= null;
	
	private DynamicPosition			directShotAndClearingTarget	= null;
	
	private EOffensiveAction		type									= null;
	
	private OffensiveMovePosition	movePosition						= null;
	
	private List<DribblePath>		dribblePaths						= null;
	
	
	private boolean					kickInsBlauePossible				= false;
	
	private IVector2					kickInsBlaueTarget				= null;
	
	private boolean					isRoleReadyToKick					= true;
	
	
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
	
	
	/**
	 * @return the movePosition
	 */
	public OffensiveMovePosition getMovePosition()
	{
		return movePosition;
	}
	
	
	/**
	 * @param movePosition the movePosition to set
	 */
	public void setMovePosition(final OffensiveMovePosition movePosition)
	{
		this.movePosition = movePosition;
	}
	
	
	/**
	 * @return the dribblePaths
	 */
	public List<DribblePath> getDribblePaths()
	{
		return dribblePaths;
	}
	
	
	/**
	 * @param dribblePaths the dribblePaths to set
	 */
	public void setDribblePaths(final List<DribblePath> dribblePaths)
	{
		this.dribblePaths = dribblePaths;
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
	
	
}
