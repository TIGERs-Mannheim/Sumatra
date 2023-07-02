/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;


public class FreeKickState extends AAttackerRoleState<SingleTouchKickSkill>
{
	public FreeKickState(AttackerRole role)
	{
		super(SingleTouchKickSkill::new, role, EAttackerState.FREE_KICK);
	}


	@Override
	protected void doStandardUpdate()
	{
		// kick == null: No pass possible, wait for another possibility or just perform the current kick
		if (getRole().getAction().getKick() != null)
		{
			skill.setTarget(getRole().getAction().getKick().getTarget());
			skill.setPassRange(getRole().getAction().getKick().getAimingTolerance());
			skill.setDesiredKickParams(getRole().getAction().getKick().getKickParams());
			skill.setReadyForKick(!getRole().isWaitForKick());
		}
	}
}

