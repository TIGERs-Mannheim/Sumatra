/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public abstract class AKickState extends AOffensiveState
{
	
	
	public AKickState(final ARole role)
	{
		super(role);
	}
	
	
	protected EKickerDevice detectDevice(KickTarget kickTarget)
	{
		boolean chipBall = kickTarget.getChipPolicy() == KickTarget.ChipPolicy.FORCE_CHIP ||
				kickTarget.getChipPolicy() == KickTarget.ChipPolicy.ALLOW_CHIP && ballMustBeChipped(kickTarget.getTarget());
		return chipBall ? EKickerDevice.CHIP : EKickerDevice.STRAIGHT;
	}
	
	
	private boolean ballMustBeChipped(IVector2 kickTarget)
	{
		final double distance = getWFrame().getBall().getPos().distanceTo(kickTarget);
		double passSpeedForChipDetection = OffensiveMath.passSpeedChip(distance);
		
		IBotIDMap<ITrackedBot> obstacles = new BotIDMap<>(getWFrame().getBots());
		obstacles.remove(getBot().getBotId());
		
		ChipKickReasonableDecider chipDecider = new ChipKickReasonableDecider(getWFrame().getBall().getPos(),
				kickTarget,
				obstacles.values(),
				passSpeedForChipDetection);
		return chipDecider.isChipKickReasonable(
				getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_IS_CHIP_NEEDED));
	}
	
	
	protected double getKickSpeed(EKickerDevice device, final KickTarget kickTarget)
	{
		double distance = getBall().getPos().distanceTo(kickTarget.getTarget());
		if (device == EKickerDevice.STRAIGHT)
		{
			return getBall().getStraightConsultant().getInitVelForDist(distance, kickTarget.getBallSpeedAtTarget());
		}
		return OffensiveMath.passSpeedChip(distance);
	}
}
