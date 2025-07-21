/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotHealthState;
import edu.tigers.sumatra.botmanager.ICommandSink;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.communication.ECommandVerdict;
import edu.tigers.sumatra.botmanager.communication.ReliableCmdManager;
import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import edu.tigers.sumatra.botmanager.configs.ConfigUpdater;
import edu.tigers.sumatra.botmanager.configs.IConfigFileDatabase;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.ids.BotID;


/**
 * A TIGERs Bot implementation
 */
public class TigerBot extends CommandBasedBot implements ReliableCmdManager.IReliableCmdSender
{
	private final ReliableCmdManager reliableCmdManager;
	private final ConfigUpdater configUpdater;


	public TigerBot(final BotID id, final ICommandSink commandSink, final IConfigFileDatabase configFileDatabase)
	{
		super(EBotType.TIGERS, id, commandSink);
		reliableCmdManager = new ReliableCmdManager(id, this);
		configUpdater = new ConfigUpdater(id, configFileDatabase, this);
		configUpdater.start();
	}


	public void stop()
	{
		configUpdater.stop();
		reliableCmdManager.clear();
	}


	@Override
	public void sendMatchCommand(MatchCommand matchCommand)
	{
		lastSentMatchCommand = matchCommand;
		sendCommand(new TigerSystemMatchCtrl(matchCommand));
	}


	@Override
	public void sendCommand(ACommand cmd)
	{
		if(reliableCmdManager.processOutgoingCommand(cmd) == ECommandVerdict.PASS)
		{
			super.sendCommand(cmd);
		}
	}


	@Override
	public void sendReliableCmdOutput(ReliableCmdManager.Output out)
	{
		super.sendCommand(out.getCmd());
	}


	@Override
	public ECommandVerdict processIncomingCommand(ACommand cmd)
	{
		if(reliableCmdManager.processIncomingCommand(cmd) == ECommandVerdict.DROP)
		{
			return ECommandVerdict.DROP;
		}

		configUpdater.processIncomingCommand(cmd);
		return super.processIncomingCommand(cmd);
	}


	@Override
	public ERobotHealthState getHealthState()
	{
		if (!configUpdater.isConfigUpdated())
		{
			return ERobotHealthState.UNUSABLE;
		}

		return super.getHealthState();
	}


	public void sendConfigFile(final ConfigFile configFile)
	{
		if (configUpdater.isSameConfigVersion(configFile.getConfigId(), configFile.getVersion()))
		{
			sendCommand(configFile.getWriteCmd());
		}
	}
}
