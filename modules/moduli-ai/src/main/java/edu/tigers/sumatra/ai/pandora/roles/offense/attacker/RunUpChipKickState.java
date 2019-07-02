/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.RunUpChipSkill;


public class RunUpChipKickState extends AOffensiveState
{
	private IVector2 initBallPos;
	private IPassTarget passTarget;
	
	
	public RunUpChipKickState(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		
		double kickDistance = offensiveAction.getKickTarget().getTarget().getPos().distanceTo(getBall().getPos());
		final double maxChipSpeed = getRole().getBot().getRobotInfo().getBotParams().getKickerSpecs()
				.getMaxAbsoluteChipVelocity();
		double kickSpeed = OffensiveMath.passSpeedChip(kickDistance, maxChipSpeed);
		RunUpChipSkill skill = new RunUpChipSkill(offensiveAction.getKickTarget().getTarget(), kickSpeed);
		setNewSkill(skill);
		
		initBallPos = getBall().getPos();
		
		passTarget = offensiveAction.getRatedPassTarget().orElse(null);
	}
	
	
	@Override
	public void doUpdate()
	{
		if (getBall().getPos().distanceTo(initBallPos) > 100)
		{
			triggerEvent(EBallHandlingEvent.BALL_KICKED);
		}
		getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(passTarget);
	}
}
