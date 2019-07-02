/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Base class for all keeper states
 */
public abstract class AKeeperState extends AState
{
	protected final KeeperRole role;
	private final EKeeperState keeperState;
	
	
	protected AKeeperState(KeeperRole role, final EKeeperState keeperState)
	{
		this.role = role;
		this.keeperState = keeperState;
	}
	
	
	protected double calcDefendingOrientation()
	{
		return role.getAiFrame().getWorldFrame().getBall().getPos().subtractNew(role.getPos()).getAngle()
				+ CriticalKeeperSkill.getTurnAngleOfKeeper();
	}
	
	
	protected void setNewSkill(ISkill skill)
	{
		role.setNewSkill(skill);
	}
	
	
	protected AthenaAiFrame getAiFrame()
	{
		return role.getAiFrame();
	}
	
	
	protected WorldFrame getWFrame()
	{
		return role.getWFrame();
	}
	
	
	protected IVector2 getPos()
	{
		return role.getPos();
	}
	
	
	public KeeperRole getRole()
	{
		return role;
	}
	
	
	@Override
	public String getIdentifier()
	{
		return keeperState.name();
	}
}
