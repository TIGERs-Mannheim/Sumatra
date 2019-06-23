/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * The Offensive role is always ball oriented.
 *
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleSupportiveAttackerState extends AOffensiveRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	private AMoveToSkill skill = null;
	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * @param role
	 */
	public OffensiveRoleSupportiveAttackerState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getMoveDest()
	{
		return skill.getMoveCon().getDestination();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return OffensiveStrategy.EOffensiveStrategy.SUPPORTIVE_ATTACKER.name();
	}
	
	
	@Override
	public void doExitActions()
	{
		// nothing to do here
	}
	
	
	@Override
	public void doEntryActions()
	{
		skill = AMoveToSkill.createMoveToSkill();
		setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 movePos;
		switch (getAiFrame().getTacticalField().getSkirmishInformation().getStrategy())
		{
			case FREE_BALL:
				movePos = calcMovePositionFreeBall();
				break;
			default:
				movePos = getAiFrame().getTacticalField().getSupportiveAttackerMovePos();
		}
		movePos = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
		skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
		skill.getMoveCon().updateDestination(movePos);
	}
	
	
	private IVector2 calcMovePositionFreeBall()
	{
		return getAiFrame().getTacticalField().getSkirmishInformation().getSupportiveCircleCatchPos();
	}
}
