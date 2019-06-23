/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public abstract class ARoleState extends AState
{
	private final ARole role;
	
	
	protected ARoleState(final ARole role)
	{
		this.role = role;
	}
	
	
	protected final ARole getRole()
	{
		return role;
	}
	
	
	protected final void setNewSkill(final ISkill newSkill)
	{
		role.setNewSkill(newSkill);
	}
	
	
	protected final void triggerEvent(final IEvent event)
	{
		role.triggerEvent(event);
	}
	
	
	protected final AthenaAiFrame getAiFrame()
	{
		return role.getAiFrame();
	}
	
	
	protected final WorldFrame getWFrame()
	{
		return role.getWFrame();
	}
	
	
	protected final ITrackedBall getBall()
	{
		return role.getBall();
	}
	
	
	protected final IVector2 getPos()
	{
		return role.getPos();
	}
	
	
	protected final BotID getBotID()
	{
		return role.getBotID();
	}
	
	
	protected final ITrackedBot getBot()
	{
		return role.getBot();
	}
	
	
	protected final ISkill getCurrentSkill()
	{
		return role.getCurrentSkill();
	}
	
}
