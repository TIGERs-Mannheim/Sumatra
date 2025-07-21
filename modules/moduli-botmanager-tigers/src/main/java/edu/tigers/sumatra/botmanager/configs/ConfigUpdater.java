/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.configs;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.ICommandSink;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.thread.Watchdog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Log4j2
@RequiredArgsConstructor
public class ConfigUpdater
{
	@Configurable(defValue = "250", comment = "[ms] Timeout for incoming config structures")
	private static int incomingConfigsTimeout = 250;

	static
	{
		ConfigRegistration.registerClass("botmgr", TigerBot.class);
	}

	private final BotID botId;
	private final IConfigFileDatabase configFileDatabase;
	private final ICommandSink commandSink;
	private final Watchdog watchdog = new Watchdog(incomingConfigsTimeout, "TigerBot", this::stop);
	@Getter
	private boolean configUpdated = false;
	private final Map<Integer, Integer> configIdVersion = new HashMap<>();


	public void start()
	{
		commandSink.sendCommand(new TigerConfigQueryFileList());
		watchdog.start();
	}


	public void stop()
	{
		watchdog.stop();
		log.debug(
				"TigerBot {} has finished querying its config files ({} configs).", botId,
				configIdVersion.size()
		);
		configUpdated = true;
	}


	public void processIncomingCommand(ACommand cmd)
	{
		if (cmd.getType() != ECommand.CMD_CONFIG_FILE_STRUCTURE)
		{
			return;
		}

		TigerConfigFileStructure structure = (TigerConfigFileStructure) cmd;
		configIdVersion.put(structure.getConfigId(), structure.getVersion());
		watchdog.reset();

		if (configFileDatabase.isAutoUpdate(structure.getConfigId(), structure.getVersion()))
		{
			Optional<ConfigFile> file = configFileDatabase.getSelectedEntry(
					structure.getConfigId(), structure.getVersion());
			file.ifPresent(f -> commandSink.sendCommand(f.getWriteCmd()));
		}
	}


	public boolean isSameConfigVersion(int configId, int version)
	{
		return configIdVersion.containsKey(configId) && configIdVersion.get(configId) == version;
	}
}
