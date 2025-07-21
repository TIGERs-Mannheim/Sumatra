/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.bot.BotBallState;
import edu.tigers.sumatra.bot.BotLastKickState;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotHealthState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.ICommandSink;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemVersion;
import edu.tigers.sumatra.botmanager.communication.ECommandVerdict;
import edu.tigers.sumatra.botmanager.data.BotFeedback;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.EnumMap;
import java.util.Map;


@Log4j2
public class CommandBasedBot extends ABot implements ICommandSink
{
	private final ICommandSink commandSink;
	private String version = "Unknown";

	@Getter
	@Setter
	private BaseStationWifiStats.BotStats wifiStats = new BaseStationWifiStats.BotStats();


	protected CommandBasedBot(final EBotType type, final BotID id, final ICommandSink commandSink)
	{
		super(type, id);
		this.commandSink = commandSink;
	}


	@Override
	public String getVersionString()
	{
		return version;
	}


	@Override
	public EBotParamLabel getBotParamLabel()
	{
		return EBotParamLabel.TIGER_V2020;
	}


	@Override
	public void sendMatchCommand(MatchCommand matchCommand)
	{
		lastSentMatchCommand = matchCommand;
		sendCommand(new TigerSystemMatchCtrl(matchCommand));
	}


	@Override
	public ERobotHealthState getHealthState()
	{
		var features = lastReceivedBotFeedback.getBotFeatures();

		boolean ready = EFeature.getReadyFeatureSet().stream()
				.allMatch(f -> features.getOrDefault(f, EFeatureState.UNKNOWN) == EFeatureState.WORKING);

		boolean unusable = EFeature.getUnusableFeatureSet().stream()
				.anyMatch(f -> features.getOrDefault(f, EFeatureState.UNKNOWN) != EFeatureState.WORKING);

		if (unusable)
		{
			return ERobotHealthState.UNUSABLE;
		}

		if (ready)
		{
			return ERobotHealthState.READY;
		}

		return ERobotHealthState.DEGRADED;
	}


	public ECommandVerdict processIncomingCommand(ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK -> processSystemMatchFeedback((TigerSystemMatchFeedback) cmd);
			case CMD_SYSTEM_VERSION -> version = ((TigerSystemVersion) cmd).getFullVersionString();
			case CMD_SYSTEM_CONSOLE_PRINT -> processSystemConsolePrint((TigerSystemConsolePrint) cmd);
			default -> { /* Not interested in this */ }
		}

		return ECommandVerdict.PASS;
	}


	private void processSystemConsolePrint(TigerSystemConsolePrint print)
	{
		log.info(
				"Console({}): {}", getBotId().getNumberWithColorOffset(),
				print.getText().replaceAll("[\n\r]$", "")
		);
	}


	private void processSystemMatchFeedback(TigerSystemMatchFeedback feedback)
	{
		State state = State.of(
				Pose.from(feedback.getPosition().multiply(1000), feedback.getOrientation()),
				Vector3.from2d(feedback.getVelocity(), feedback.getAngularVelocity())
		);

		Map<EFeature, EFeatureState> features = new EnumMap<>(EFeature.class);

		for (EFeature f : EFeature.values())
		{
			features.put(f, feedback.isFeatureWorking(f) ? EFeatureState.WORKING : EFeatureState.KAPUT);
		}

		BotBallState ballState;
		if (feedback.isBallPositionValid())
		{
			ballState = new BotBallState(
					feedback.getBallPosition().multiply(1000), feedback.getBallAge());
		} else
		{
			ballState = null;
		}

		lastReceivedBotFeedback = BotFeedback.newBuilder()
				.withInternalState(BotState.of(getBotId(), state))
				.withKickerLevel(feedback.getKickerLevel())
				.withKickerMax(feedback.getKickerMax())
				.withDribbleSpeed(feedback.getDribblerSpeed())
				.withDribbleTraction(feedback.getDribbleTractionState())
				.withBatteryLevel(feedback.getBatteryLevel())
				.withBatteryLevelRelative(feedback.getBatteryPercentage())
				.withBarrierInterrupted(feedback.isBarrierInterrupted())
				.withDribblerTemperature(feedback.getDribblerTemperature())
				.withKickToggleBit(feedback.getKickCounter() != 0)
				.withBallObservationState(feedback.getBallObservationState())
				.withBotFeatures(features)
				.withRobotMode(feedback.getRobotMode())
				.withHardwareId(feedback.getHardwareId())
				.withBallState(ballState)
				.withLastKick(new BotLastKickState(
						feedback.getLastKickDevice() == EKickerDevice.CHIP, feedback.getLastKickDuration(),
						feedback.getLastKickDribblerSpeed(),
						feedback.getLastKickDribblerForce()
				))
				.build();
	}


	@Override
	public void sendCommand(ACommand cmd)
	{
		commandSink.sendCommand(new BaseStationACommand(getBotId(), cmd));
	}
}
