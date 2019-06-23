/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 18, 2016
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;


/**
 * @author MarkG
 */
public class AutomatedThrowInInfo
{
	/**
	 * 
	 */
	public enum EPrepareThrowInAction
	{
		/**
		 * 
		 */
		MOVE_BALL_FROM_WALL,
		/**
		 * 
		 */
		PASS_TO_RECEIVER_DIRECTLY,
		/**
		 * 
		 */
		FINE_ADJUSTMENT;
	}
	
	private EPrepareThrowInAction	action			= null;
	private IVector2					pos				= null;
	private boolean					receiverReady	= false;
	private boolean					finished			= false;
	private List<BotID>				desiredBots		= new ArrayList<BotID>();
	
	
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
	 * @return the receiverReady
	 */
	public boolean isReceiverReady()
	{
		return receiverReady;
	}
	
	
	/**
	 * @param receiverReady the receiverReady to set
	 */
	public void setReceiverReady(final boolean receiverReady)
	{
		this.receiverReady = receiverReady;
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
	public List<BotID> getDesiredBots()
	{
		return desiredBots;
	}
	
	
	/**
	 * @param desiredBots the desiredBots to set
	 */
	public void setDesiredBots(final List<BotID> desiredBots)
	{
		this.desiredBots = desiredBots;
	}
	
}
