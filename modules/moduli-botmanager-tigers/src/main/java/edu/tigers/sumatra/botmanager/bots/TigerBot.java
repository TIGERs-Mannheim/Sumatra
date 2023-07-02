/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.tigers.sumatra.botmanager.communication.ReliableCmdManager;
import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.ids.BotID;
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
public class TigerBot extends BasicTigerBot
{
	@Configurable(defValue = "250", comment = "[ms] Timeout for incoming config structures")
	private static int incomingConfigsTimeout = 250;
	private final ReliableCmdManager reliableCmdManager = new ReliableCmdManager(this);
	private final Watchdog watchdog = new Watchdog(incomingConfigsTimeout, "TigerBot", this::handleTimeoutEvent);
	private ConfigFileDatabaseManager databaseManager;
	private final Map<Integer, Integer> configIdVersion = new HashMap<>();

	private boolean configsUpdated = false;

	static
	{
		ConfigRegistration.registerClass("botmgr", TigerBot.class);
	}

	public TigerBot(final BotID botId, final IBaseStation baseStation)
	{
		super(botId, baseStation);
		TigersBotManager tigersBotManager = SumatraModel.getInstance().getModule(TigersBotManager.class);
		databaseManager = tigersBotManager.getConfigDatabase();
		updateConfigs();
	}


	@Override
	public TigersBaseStation getBaseStation()
	{
		return (TigersBaseStation) super.getBaseStation();
	}


	@Override
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


	@Override
	public void onIncomingBotCommand(final ACommand cmd)
	{
		reliableCmdManager.incomingCommand(cmd);

		super.onIncomingBotCommand(cmd);
	}


	@Override
	protected void onNewCommandConfigFileStructure(final ACommand cmd)
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
	public boolean isAvailableToAi()
	{
		return super.isAvailableToAi() && configsUpdated;
	}
}
