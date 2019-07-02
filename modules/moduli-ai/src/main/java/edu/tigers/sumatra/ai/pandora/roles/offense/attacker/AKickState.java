/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.general.EChipDeciderMode;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.EKickTargetMode;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.RuleConstraints;
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
		return detectDevice(kickTarget, EChipDeciderMode.DEFAULT);
	}

	protected EKickerDevice detectDevice(KickTarget kickTarget, EChipDeciderMode chipDeciderMode)
	{
		boolean chipBall = kickTarget.getChipPolicy() == KickTarget.ChipPolicy.FORCE_CHIP ||
				kickTarget.getChipPolicy() == KickTarget.ChipPolicy.ALLOW_CHIP
						&& ballMustBeChipped(kickTarget.getTarget().getPos(), chipDeciderMode);
		return chipBall ? EKickerDevice.CHIP : EKickerDevice.STRAIGHT;
	}


	private boolean ballMustBeChipped(IVector2 kickTarget, EChipDeciderMode chipDeciderMode)
	{
		final double distance = getWFrame().getBall().getPos().distanceTo(kickTarget);
		final double maxChipSpeed = getRole().getBot().getRobotInfo().getBotParams().getKickerSpecs()
				.getMaxAbsoluteChipVelocity();
		double passSpeedForChipDetection = OffensiveMath.passSpeedChip(distance, maxChipSpeed);

		IBotIDMap<ITrackedBot> obstacles = new BotIDMap<>(getWFrame().getBots());
		obstacles.remove(getBot().getBotId());

		ChipKickReasonableDecider chipDecider = new ChipKickReasonableDecider(getWFrame().getBall().getPos(),
				kickTarget,
				obstacles.values(),
				passSpeedForChipDetection,
				chipDeciderMode);
		return chipDecider.isChipKickReasonable(
				getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_IS_CHIP_NEEDED));
	}


	protected double getKickSpeed(EKickerDevice device, final KickTarget kickTarget)
	{
		double distance = getBall().getPos().distanceTo(kickTarget.getTarget().getPos());
		if (device == EKickerDevice.STRAIGHT)
		{
			switch (kickTarget.getMode())
			{
				case GOAL_KICK:
					return RuleConstraints.getMaxBallSpeed();
				case PASS:
					return OffensiveMath.passSpeedStraight(getBot().getBotKickerPos(), kickTarget.getTarget().getPos(),
							kickTarget.getBallSpeedAtTarget());
				case KICK_INS_BLAUE:
					return OffensiveMath.passSpeedStraightKickInsBlaue(getBall().getPos(), kickTarget.getTarget().getPos(),
							OffensiveConstants.getBallSpeedAtTargetKickInsBlaue());
			}
		}
		if (kickTarget.getMode() == EKickTargetMode.KICK_INS_BLAUE)
		{
			final double maxChipSpeed = getBot().getRobotInfo().getBotParams().getKickerSpecs()
					.getMaxAbsoluteChipVelocity();
			return OffensiveMath.passSpeedChipKickInsBlaue(getBall().getPos(), kickTarget.getTarget().getPos(),
					maxChipSpeed);
		}
		final double maxChipSpeed = getRole().getBot().getRobotInfo().getBotParams().getKickerSpecs()
				.getMaxAbsoluteChipVelocity();
		return OffensiveMath.passSpeedChip(distance, maxChipSpeed);
	}
}
