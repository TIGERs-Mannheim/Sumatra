/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;

import java.util.Optional;


public class RedirectBallState extends AKickState
{
	private RedirectBallSkill skill;
	private IPassTarget passTarget;
	private KickTarget kickTarget;
	private KickParams kickParams;
	private OffensiveAction currentOffensiveAction;
	
	
	public RedirectBallState(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		final OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		currentOffensiveAction = offensiveAction;
		kickTarget = offensiveAction.getKickTarget();
		DynamicPosition target = new DynamicPosition(kickTarget.getTarget());
		EKickerDevice device = detectDevice(kickTarget);
		kickParams = KickParams.of(device, getKickSpeed(device, kickTarget));
		skill = new RedirectBallSkill(getReceivingPosition(), target, kickParams);
		setNewSkill(skill);
		
		passTarget = offensiveAction.getPassTarget().orElse(null);
	}
	
	
	@Override
	public void doUpdate()
	{
		final EKickerDevice device = detectDevice(kickTarget);
		kickParams.setDevice(device);
		kickParams.setKickSpeed(getKickSpeed(device, kickTarget));
		
		OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		currentOffensiveAction = offensiveAction;
		if (!offensiveAction.isAllowRedirect())
		{
			triggerEvent(EBallHandlingEvent.BALL_NOT_REDIRECTABLE);
		} else if (!skill.ballCanBeRedirected())
		{
			triggerEvent(EBallHandlingEvent.BALL_NOT_REDIRECTED);
		}
		getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(passTarget);
	}
	
	
	private IVector2 getReceivingPosition()
	{
		return getAiFrame().getTacticalField().getOffensiveStrategy().getActivePassTarget()
				.filter(pt -> pt.getBotId().equals(getBotID()))
				.map(IPassTarget::getKickerPos)
				.orElse(getPos());
	}
	
	
	@Override
	public Optional<OffensiveAction> getCurrentOffensiveAction()
	{
		return Optional.of(currentOffensiveAction);
	}
}
