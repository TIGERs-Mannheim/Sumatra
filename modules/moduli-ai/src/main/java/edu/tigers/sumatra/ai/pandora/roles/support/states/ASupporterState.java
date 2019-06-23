/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.states;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author chris
 */
public class ASupporterState implements IState
{
	
	protected SupportRole parent;
	
	/**
	 * @param role the parent of the states
	 */
	public ASupporterState(SupportRole role)
	{
		parent = role;
	}
	
	
	protected void setNewSkill(ISkill newSkill)
	{
		parent.setNewSkill(newSkill);
	}
	
	
	protected IVector2 getPos()
	{
		return parent.getPos();
	}
	
	
	protected WorldFrame getWFrame()
	{
		return parent.getWFrame();
	}
	
	
	protected AthenaAiFrame getAiFrame()
	{
		return parent.getAiFrame();
	}
	
	
	protected BotID getBotID()
	{
		return parent.getBotID();
	}
	
	
	protected void triggerEvent(IEvent event)
	{
		parent.triggerEvent(event);
	}
	
	
	protected IVector2 getGlobalPosition()
	{
		return parent.getGlobalPosition();
	}
}
