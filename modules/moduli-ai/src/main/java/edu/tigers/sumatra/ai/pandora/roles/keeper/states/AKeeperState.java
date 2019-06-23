/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Base class for all keeper states
 */
public abstract class AKeeperState extends AState
{
	protected final KeeperRole parent;
	private final EKeeperState keeperState;
	
	
	protected AKeeperState(KeeperRole parent, final EKeeperState keeperState)
	{
		this.parent = parent;
		this.keeperState = keeperState;
	}
	
	
	protected double calcDefendingOrientation()
	{
		return parent.getAiFrame().getWorldFrame().getBall().getPos().subtractNew(parent.getPos()).getAngle()
				+ KeeperRole.getTurnAngleOfKeeper();
	}
	
	
	protected void setNewSkill(ISkill skill)
	{
		parent.setNewSkill(skill);
	}
	
	
	protected AthenaAiFrame getAiFrame()
	{
		return parent.getAiFrame();
	}
	
	
	protected WorldFrame getWFrame()
	{
		return parent.getWFrame();
	}
	
	
	protected IVector2 getPos()
	{
		return parent.getPos();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return keeperState.name();
	}
}
