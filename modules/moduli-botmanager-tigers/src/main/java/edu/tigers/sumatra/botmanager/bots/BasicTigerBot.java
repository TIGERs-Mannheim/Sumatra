/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.bot.BotBallState;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBallObservationState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.bot.EDribblerTemperature;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemVersion;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;


/**
 * TIGER Bot base implementation. Mostly handling match feedback.
 */
@Log4j2
public class BasicTigerBot extends ABot
{
	private TigerSystemMatchFeedback latestFeedbackCmd = new TigerSystemMatchFeedback();
	private BaseStationWifiStats.BotStats botStats;
	protected String version = "not available";


	public BasicTigerBot(final BotID botId, final IBaseStation baseStation)
	{
		super(EBotType.TIGERS, botId, baseStation);
	}


	private void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		for (EFeature f : EFeature.values())
		{
			getBotFeatures().put(f, cmd.isFeatureWorking(f) ? EFeatureState.WORKING : EFeatureState.KAPUT);
		}
	}


	public void onIncomingBotCommand(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK ->
			{
				onNewFeedbackCmd((TigerSystemMatchFeedback) cmd);
				latestFeedbackCmd = (TigerSystemMatchFeedback) cmd;
				lastFeedback = System.nanoTime();
			}
			case CMD_SYSTEM_VERSION -> version = ((TigerSystemVersion) cmd).getFullVersionString();
			case CMD_CONFIG_FILE_STRUCTURE -> onNewCommandConfigFileStructure(cmd);
			default -> { /* Not interested in this */ }
		}
	}


	protected void onNewCommandConfigFileStructure(final ACommand cmd)
	{
		// config file capable implementations may use this
	}


	public void execute(final ACommand cmd)
	{
		// Basic implementation cannot send anything
	}


	@Override
	public int getHardwareId()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getHardwareId();
		}
		return 255;
	}


	@Override
	public boolean isAvailableToAi()
	{
		return super.isAvailableToAi() && ((latestFeedbackCmd == null) || latestFeedbackCmd.isFeatureWorking(
				EFeature.MOVE));

	}


	@Override
	public double getBatteryRelative()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getBatteryPercentage();
		}

		return 0;
	}


	@Override
	public double getBatteryAbsolute()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getBatteryLevel();
		}

		return 0;
	}


	@Override
	public double getKickerLevel()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getKickerLevel();
		}
		return 0;
	}


	@Override
	public double getKickerLevelMax()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getKickerMax();
		}

		return super.getKickerLevelMax();
	}


	@Override
	public ERobotMode getRobotMode()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getRobotMode();
		}
		return ERobotMode.IDLE;
	}


	@Override
	public boolean isHealthy()
	{
		boolean energetic = getBotFeatures().get(EFeature.ENERGETIC) == EFeatureState.WORKING;
		boolean straight = getBotFeatures().get(EFeature.STRAIGHT_KICKER) == EFeatureState.WORKING;
		boolean chip = getBotFeatures().get(EFeature.CHIP_KICKER) == EFeatureState.WORKING;
		boolean charge = getBotFeatures().get(EFeature.CHARGE_CAPS) == EFeatureState.WORKING;
		boolean dribbler = getBotFeatures().get(EFeature.DRIBBLER) == EFeatureState.WORKING;
		boolean barrier = getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING;

		return energetic && straight && chip && charge && dribbler && barrier;
	}


	@Override
	public Optional<BotState> getSensoryState()
	{
		if (latestFeedbackCmd == null)
		{
			return Optional.empty();
		}
		State state = State.of(
				Pose.from(latestFeedbackCmd.getPosition().multiply(1000), latestFeedbackCmd.getOrientation()),
				Vector3.from2d(latestFeedbackCmd.getVelocity(), latestFeedbackCmd.getAngularVelocity()));
		return Optional.of(BotState.of(getBotId(), state));
	}


	@Override
	public boolean isBarrierInterrupted()
	{
		return getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING
				&& latestFeedbackCmd.isBarrierInterrupted();
	}


	@Override
	public double getCenter2DribblerDist()
	{
		return getBotParams().getDimensions().getCenter2DribblerDist();
	}


	@Override
	public String getVersionString()
	{
		return version;
	}


	@Override
	public Optional<BotBallState> getBallState()
	{
		if (latestFeedbackCmd == null || !latestFeedbackCmd.isBallPositionValid())
			return Optional.empty();

		BotBallState ball = new BotBallState(latestFeedbackCmd.getBallPosition().multiply(1000), latestFeedbackCmd.getBallAge());
		return Optional.of(ball);
	}


	@Override
	public EDribblerTemperature getDribblerTemperature()
	{
		return latestFeedbackCmd.getDribblerTemperature();
	}


	@Override
	public EDribbleTractionState getDribbleTractionState()
	{
		return latestFeedbackCmd.getDribbleTractionState();
	}


	@Override
	public EBallObservationState getBallObservationState()
	{
		return latestFeedbackCmd.getBallObservationState();
	}


	@Override
	public EBotParamLabel getBotParamLabel()
	{
		if (getBotFeatures().getOrDefault(EFeature.V2016, EFeatureState.UNKNOWN) == EFeatureState.WORKING)
		{
			return EBotParamLabel.TIGER_V2016;
		}
		return EBotParamLabel.TIGER_V2020;
	}


	public BaseStationWifiStats.BotStats getStats()
	{
		if (botStats != null)
		{
			return botStats;
		}

		return new BaseStationWifiStats.BotStats();
	}


	public void setStats(final BaseStationWifiStats.BotStats botStats)
	{
		this.botStats = botStats;
	}
}
