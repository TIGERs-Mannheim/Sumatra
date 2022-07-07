/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EDribblerState;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemVersion;
import edu.tigers.sumatra.botmanager.communication.ReliableCmdManager;
import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.Watchdog;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * A TIGERs Bot implementation
 */
@Log4j2
public class TigerBot extends ABot
{
	@Configurable(defValue = "250", comment = "[ms] Timeout for incoming config structures")
	private static int incomingConfigsTimeout = 250;
	private final ReliableCmdManager reliableCmdManager = new ReliableCmdManager(this);
	private final Watchdog watchdog = new Watchdog(incomingConfigsTimeout, "TigerBot", this::handleTimeoutEvent);
	private TigerSystemMatchFeedback latestFeedbackCmd = new TigerSystemMatchFeedback();
	private String version = "not available";
	private BaseStationWifiStats.BotStats botStats;
	private ConfigFileDatabaseManager databaseManager;
	private final Map<Integer, Integer> configIdVersion = new HashMap<>();

	private boolean configsUpdated = false;

	static
	{
		ConfigRegistration.registerClass("botmgr", TigerBot.class);
	}

	public TigerBot(final BotID botId, final IBaseStation baseStation)
	{
		super(EBotType.TIGERS, botId, baseStation);
		TigersBotManager tigersBotManager = SumatraModel.getInstance().getModule(TigersBotManager.class);
		databaseManager = tigersBotManager.getConfigDatabase();
		updateConfigs();
	}


	@Override
	public TigersBaseStation getBaseStation()
	{
		return (TigersBaseStation) super.getBaseStation();
	}


	public void execute(final ACommand cmd)
	{
		reliableCmdManager.outgoingCommand(cmd);
		getBaseStation().enqueueCommand(getBotId(), cmd);
	}


	private void updateConfigs()
	{
		execute(new TigerConfigQueryFileList());
		watchdog.start();
	}


	private void handleTimeoutEvent()
	{
		watchdog.stop();
		configsUpdated = true;
		log.debug("TigerBot {} has finished querying its config files ({}).", getBotId(), configIdVersion.size());
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
		reliableCmdManager.incomingCommand(cmd);

		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK:
				onNewFeedbackCmd((TigerSystemMatchFeedback) cmd);
				latestFeedbackCmd = (TigerSystemMatchFeedback) cmd;
				lastFeedback = System.nanoTime();
				break;
			case CMD_SYSTEM_VERSION:
				version = ((TigerSystemVersion) cmd).getFullVersionString();
				break;
			case CMD_CONFIG_FILE_STRUCTURE:
				onNewCommandConfigFileStructure(cmd);
				break;
			default:
				break;
		}
	}


	private void onNewCommandConfigFileStructure(final ACommand cmd)
	{
		TigerConfigFileStructure structure = (TigerConfigFileStructure) cmd;
		configIdVersion.put(structure.getConfigId(), structure.getVersion());
		if (configsUpdated)
			return;
		watchdog.reset();

		if (databaseManager.isAutoUpdate(structure.getConfigId(), structure.getVersion()))
		{
			Optional<ConfigFile> file = databaseManager.getSelectedEntry(structure.getConfigId(), structure.getVersion());
			file.ifPresent(f -> execute(f.getWriteCmd()));
		}
	}


	public boolean isSameConfigVersion(int configId, int version)
	{
		return configIdVersion.containsKey(configId) && configIdVersion.get(configId) == version;
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
		return super.isAvailableToAi() && configsUpdated &&
				((latestFeedbackCmd == null) || latestFeedbackCmd.isFeatureWorking(EFeature.MOVE));

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
	public boolean isOK()
	{
		boolean energetic = getBotFeatures().get(EFeature.ENERGETIC) == EFeatureState.WORKING;
		boolean straight = getBotFeatures().get(EFeature.STRAIGHT_KICKER) == EFeatureState.WORKING;
		boolean chip = getBotFeatures().get(EFeature.CHIP_KICKER) == EFeatureState.WORKING;
		boolean charge = getBotFeatures().get(EFeature.CHARGE_CAPS) == EFeatureState.WORKING;

		return energetic && straight && chip && charge;
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
	public EDribblerState getDribblerState()
	{
		return latestFeedbackCmd.getDribblerState();
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
