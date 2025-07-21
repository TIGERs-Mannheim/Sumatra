/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.bot.ERobotHealthState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botparams.BotParamsProvider;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.util.IRobotInfoProvider;

import java.util.Set;
import java.util.stream.Collectors;


public class RobotInfoProvider implements IRobotInfoProvider
{
	private final IBotProvider botProvider;
	private final BotParamsProvider botParamsProvider;

	private long lastWFTimestamp = -1;


	public RobotInfoProvider(final IBotProvider botProvider, final BotParamsProvider botParamsProvider)
	{
		this.botProvider = botProvider;
		this.botParamsProvider = botParamsProvider;
	}


	@Override
	public RobotInfo getRobotInfo(final BotID botID)
	{
		return botProvider.getBot(botID)
				.map(this::botToRobotInfo)
				.orElseGet(() -> unknownRobotInfo(botID));
	}


	private ITrajectory<IVector3> trajectoryOfBot(final ABot bot)
	{
		return bot.getCurrentTrajectory()
				.map(t -> t.synchronizeTo(lastWFTimestamp))
				.map(t -> bot.getColor() != Geometry.getNegativeHalfTeam() ? t.mirrored() : t)
				.orElse(null);
	}


	private RobotInfo unknownRobotInfo(final BotID botID)
	{
		assert lastWFTimestamp >= 0;
		return RobotInfo.stubBuilder(botID, lastWFTimestamp)
				.withBotParams(botParamsProvider.get(EBotParamLabel.OPPONENT))
				.build();
	}


	@Override
	public Set<BotID> getConnectedBotIds()
	{
		return botProvider.getBots().values().stream()
				.filter(bot -> bot.getHealthState() != ERobotHealthState.UNUSABLE)
				.map(ABot::getBotId)
				.collect(Collectors.toSet());
	}


	private RobotInfo botToRobotInfo(final ABot bot)
	{
		var fb = bot.getLastReceivedBotFeedback();
		var mc = bot.getLastSentMatchCommand();
		
		return RobotInfo.newBuilder()
				.withTimestamp(lastWFTimestamp)
				.withBotId(bot.getBotId())
				.withDribbleTraction(fb.getDribbleTraction())
				.withBarrierInterrupted(fb.isBarrierInterrupted())
				.withBatteryRelative((float) fb.getBatteryLevelRelative())
				.withBotFeatures(fb.getBotFeatures())
				.withBotParams(bot.getBotParams())
				.withChip(mc.getSkill().getDevice().equals(EKickerDevice.CHIP))
				.withArmed(mc.getSkill().getMode().equals(EKickerMode.ARM))
				.withDribbleSpeed((float) mc.getSkill().getDribbleSpeed())
				.withHardwareId(fb.getHardwareId())
				.withInternalState(fb.getInternalState().orElse(null))
				.withKickerLevelRelative((float) fb.getKickerLevelRelative())
				.withKickSpeed((float) mc.getSkill().getKickSpeed())
				.withType(bot.getType())
				.withRobotMode(fb.getRobotMode())
				.withHealthState(bot.getHealthState())
				.withAvailableToAi(bot.isAvailableToAi())
				.withTrajectory(trajectoryOfBot(bot))
				.withBallState(fb.getBallState().orElse(null))
				.build();
	}


	@Override
	public void setLastWFTimestamp(final long lastWFTimestamp)
	{
		this.lastWFTimestamp = lastWFTimestamp;
	}
}