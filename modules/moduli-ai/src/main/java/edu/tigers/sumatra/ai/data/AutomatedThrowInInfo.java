/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.util.HashSet;
import java.util.Set;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author MarkG
 */
public class AutomatedThrowInInfo
{
	private EPrepareThrowInAction action = null;
	private IVector2 pos = null;
	private boolean finished = false;
	private Set<BotID> desiredBots = new HashSet<>();
	
	
	/**
	 * @return the action
	 */
	public EPrepareThrowInAction getAction()
	{
		return action;
	}
	
	
	/**
	 * @param action the action to set
	 */
	public void setAction(final EPrepareThrowInAction action)
	{
		this.action = action;
	}
	
	
	/**
	 * @return the pos
	 */
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public void setPos(final IVector2 pos)
	{
		this.pos = pos;
	}
	
	
	/**
	 * @return the finished
	 */
	public boolean isFinished()
	{
		return finished;
	}
	
	
	/**
	 * @param finished the finished to set
	 */
	public void setFinished(final boolean finished)
	{
		this.finished = finished;
	}
	
	
	/**
	 * @return the desiredBots
	 */
	public Set<BotID> getDesiredBots()
	{
		return desiredBots;
	}
	
	
	/**
	 * @param desiredBots the desiredBots to set
	 */
	public void setDesiredBots(final Set<BotID> desiredBots)
	{
		this.desiredBots = desiredBots;
	}
	
	
	/**
	 * actions
	 */
	public enum EPrepareThrowInAction
	{
		/**	 */
		MOVE_BALL_FROM_WALL,
		/**	 */
		PASS_TO_RECEIVER_DIRECTLY,
		/**	 */
		FINE_ADJUSTMENT
	}
	
}
