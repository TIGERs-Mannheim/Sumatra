/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.sim;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.ERobotHealthState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.data.BotFeedback;
import edu.tigers.sumatra.botmanager.data.MatchBroadcast;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillInput;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillOutput;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


/**
 * Bot for internal Sumatra simulation
 */
public class SumatraSimBot extends ABot
{
	private static final double TIME_TILL_FULL_DRIBBLE_STRENGTH = 0.2; // [s]
	private long firstInterruptedTime = -1;
	private BotSkillSimulator botSkillSim = new BotSkillSimulator();


	public SumatraSimBot(final BotID id)
	{
		super(EBotType.SUMATRA, id);
	}


	@Override
	public EBotParamLabel getBotParamLabel()
	{
		return getBotId().getTeamColor() == ETeamColor.YELLOW
				? EBotParamLabel.SIMULATION_YELLOW
				: EBotParamLabel.SIMULATION_BLUE;
	}


	@Override
	public void sendMatchCommand(final MatchCommand matchCommand)
	{
		lastSentMatchCommand = matchCommand;
	}


	@Override
	public ERobotHealthState getHealthState()
	{
		return ERobotHealthState.READY;
	}


	public SimBotAction simulate(final SimBotState botState, final MatchBroadcast matchBroadcast, final long timestamp)
	{
		BotSkillInput input = new BotSkillInput(
				lastSentMatchCommand.getSkill(), botState, getBotParams(), timestamp, matchBroadcast
		);

		final BotSkillOutput botSkillOutput = botSkillSim.execute(input);
		updateSimulatedRobotFeedback(botState, timestamp);
		return botSkillOutput.getAction();
	}


	private void updateSimulatedRobotFeedback(
			final SimBotState botState, final long timestamp)
	{
		var sensoryBotState = BotState.of(
				getBotId(), State.of(
						botState.getPose(), Vector3.from2d(
								botState.getVel().getXYVector().multiplyNew(1e-3),
								botState.getVel().z()
						)
				)
		);

		lastReceivedBotFeedback = BotFeedback.newBuilder()
				.withTimestamp(botState.getLastFeedback())
				.withInternalState(sensoryBotState)
				.withKickerLevel(1.0f)
				.withKickerMax(1.0f)
				.withDribbleSpeed(lastSentMatchCommand.getSkill().getDribbleSpeed())
				.withDribbleTraction(simulateDribbleTraction(
						lastSentMatchCommand.getSkill().getDribbleSpeed(),
						botState.isBarrierInterrupted(), timestamp
				))
				.withBatteryLevel(1.0f)
				.withBatteryLevelRelative(1.0f)
				.withBarrierInterrupted(botState.isBarrierInterrupted())
				.withBotFeatures(EFeature.getDefaultFeatureStates())
				.withRobotMode(ERobotMode.READY)
				.withHardwareId(getBotId().getNumberWithColorOffset())
				.build();
	}


	private EDribbleTractionState simulateDribbleTraction(
			final double cmdDribbleSpeed, final boolean barrierInterrupted, final long timestamp)
	{
		EDribbleTractionState dribbleTraction;

		if (cmdDribbleSpeed <= 0.1)
		{
			dribbleTraction = EDribbleTractionState.OFF;
			firstInterruptedTime = -1;
		} else if (!barrierInterrupted)
		{
			dribbleTraction = EDribbleTractionState.IDLE;
			firstInterruptedTime = -1;
		} else
		{
			if (firstInterruptedTime == -1)
			{
				firstInterruptedTime = timestamp;
			}
			var percentage = SumatraMath.relative(
					(timestamp - firstInterruptedTime) / 1e9, 0,
					TIME_TILL_FULL_DRIBBLE_STRENGTH
			);

			if (percentage > 0.75)
			{
				dribbleTraction = EDribbleTractionState.STRONG;
			} else
			{
				dribbleTraction = EDribbleTractionState.LIGHT;
			}
		}

		return dribbleTraction;
	}
}
