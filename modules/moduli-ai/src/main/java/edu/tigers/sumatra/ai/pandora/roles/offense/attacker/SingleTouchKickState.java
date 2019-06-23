/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class SingleTouchKickState extends AKickState
{
	@Configurable(defValue = "1.0")
	private static double switchToRunUpKickSpeedOffset = 1.0;
	
	static
	{
		ConfigRegistration.registerClass("roles", SingleTouchKickState.class);
	}
	
	private IVector2 initBallPos;
	private IPassTarget passTarget;
	private KickTarget kickTarget;
	
	private KickParams kickParams;
	
	private OffensiveAction currentOffensiveAction;
	
	
	public SingleTouchKickState(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		currentOffensiveAction = offensiveAction;
		kickTarget = offensiveAction.getKickTarget();
		passTarget = offensiveAction.getPassTarget().orElse(null);
		initBallPos = getBall().getPos();
		EKickerDevice device = detectDevice(kickTarget);
		final DynamicPosition target = new DynamicPosition(kickTarget.getTarget());
		kickParams = KickParams.of(device, getKickSpeed(device, kickTarget));
		
		setNewSkill(new SingleTouchKickSkill(target, kickParams));
		
		if (device == EKickerDevice.CHIP && chipKickNeedsRunUp())
		{
			triggerEvent(EBallHandlingEvent.SWITCH_TO_RUN_UP);
		}
	}
	
	
	@Override
	public void doUpdate()
	{
		final SingleTouchKickSkill skill = (SingleTouchKickSkill) getCurrentSkill();
		skill.setReadyForKick(roleIsReadyToKick());
		kickParams.setDevice(detectDevice(kickTarget));
		
		// check for distance AND velocity:
		// distance ensures, that velocity peaks are not considered
		// velocity ensures, that vision filter has detected the kick
		if (getBall().getPos().distanceTo(initBallPos) > 200
				&& getBall().getVel().getLength2() > 1)
		{
			triggerEvent(EBallHandlingEvent.BALL_KICKED);
		}
		
		getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(passTarget);
	}
	
	
	private boolean roleIsReadyToKick()
	{
		return getAiFrame().getTacticalField().getOffensiveStrategy().isAttackerIsAllowedToKick();
	}
	
	
	private boolean chipKickNeedsRunUp()
	{
		double distance = kickTarget.getTarget().distanceTo(getBot().getBotKickerPos());
		double kickSpeed = OffensiveMath.passSpeedChip(distance);
		return kickSpeed > RuleConstraints.getMaxBallSpeed() - switchToRunUpKickSpeedOffset;
	}
	
	
	@Override
	public Optional<OffensiveAction> getCurrentOffensiveAction()
	{
		return Optional.of(currentOffensiveAction);
	}
}
