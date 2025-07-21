/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.botcenter.presenter;

import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.BotCommand;
import edu.tigers.sumatra.botmanager.bots.CommandBasedBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tiger.TigerSystemPong;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.botmanager.ping.PingStats;
import edu.tigers.sumatra.botmanager.ping.PingThread;
import edu.tigers.sumatra.botmanager.ping.PingThread.IPingThreadObserver;
import edu.tigers.sumatra.gui.botcenter.presenter.config.ConfigPresenter;
import edu.tigers.sumatra.gui.botcenter.view.BcBotPingPanel.IBcBotPingPanelObserver;
import edu.tigers.sumatra.gui.botcenter.view.bots.ConsolePanel.IConsolePanelObserver;
import edu.tigers.sumatra.gui.botcenter.view.bots.SystemMatchFeedbackPanel.ISystemMatchFeedbackPanelObserver;
import edu.tigers.sumatra.gui.botcenter.view.config.BotConfigOverviewPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Presenter for a single tigers bot (the selected one)
 */
@Log4j2
public class TigerBotPresenter
{
	private CommandBasedBot bot;
	private final BotConfigOverviewPanel botConfigOverviewPanel;
	private final ConfigPresenter configPresenter;
	private PingThread pingThread = null;

	private final BcBotPingPanelObserver botPingPanelObserver = new BcBotPingPanelObserver();
	private final ConsolePanelObserver consolePanelObserver = new ConsolePanelObserver();
	private final SystemMatchFeedbackObserver systemMatchFeedbackObserver = new SystemMatchFeedbackObserver();


	public TigerBotPresenter(final BotConfigOverviewPanel botConfigOverviewPanel, ConfigFileDatabaseManager database)
	{
		this.botConfigOverviewPanel = botConfigOverviewPanel;

		configPresenter = new ConfigPresenter(botConfigOverviewPanel.getConfigPanel(), database);

		this.botConfigOverviewPanel.getManualControlPanel().getPingPanel().addObserver(botPingPanelObserver);
		this.botConfigOverviewPanel.getConsolePanel().addObserver(consolePanelObserver);
		this.botConfigOverviewPanel.getSystemStatusPanel().addObserver(systemMatchFeedbackObserver);
	}


	public void dispose()
	{
		configPresenter.dispose();

		this.botConfigOverviewPanel.getManualControlPanel().getPingPanel().removeObserver(botPingPanelObserver);
		this.botConfigOverviewPanel.getConsolePanel().removeObserver(consolePanelObserver);
		this.botConfigOverviewPanel.getSystemStatusPanel().removeObserver(systemMatchFeedbackObserver);
	}


	public void setBot(final CommandBasedBot bot)
	{
		this.bot = bot;
		configPresenter.setBot(bot);
	}


	private void processPong(final TigerSystemPong pong)
	{
		if (pingThread != null)
		{
			pingThread.pongArrived(pong.getId());
			if (!pong.payloadValid())
			{
				log.warn("Invalid payload received: {}", () -> Arrays.toString(pong.getPayload()));
			}
		}
	}


	/**
	 * @param cmd
	 */
	private void processCommand(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_SYSTEM_CONSOLE_PRINT ->
			{
				TigerSystemConsolePrint print = (TigerSystemConsolePrint) cmd;
				SwingUtilities.invokeLater(() -> botConfigOverviewPanel.getConsolePanel().addConsolePrint(print));
			}
			case CMD_SYSTEM_PONG -> processPong((TigerSystemPong) cmd);
			case CMD_SYSTEM_MATCH_FEEDBACK -> SwingUtilities.invokeLater(() -> procCmdSystemMatchFeedback(cmd));
			case CMD_CONFIG_FILE_STRUCTURE, CMD_CONFIG_ITEM_DESC, CMD_CONFIG_READ -> configPresenter.onNewCommand(cmd);
		}
	}


	private void procCmdSystemMatchFeedback(final ACommand cmd)
	{
		final TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) cmd;
		botConfigOverviewPanel.getSystemStatusPanel().addTigerSystemMatchFeedback(feedback);
	}

	private class BcBotPingPanelObserver implements IBcBotPingPanelObserver
	{
		private ScheduledExecutorService pingService = null;


		@Override
		public void onStartPing(final int numPings, final int payloadSize)
		{
			onStopPing();

			if (bot != null)
			{
				pingThread = new PingThread(payloadSize, bot);
				pingThread.addObserver(new PingThreadObserver());
				pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
				pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
			}
		}


		@Override
		public void onStopPing()
		{
			if (pingService == null)
			{
				return;
			}

			if (pingThread != null)
			{
				pingThread.clearObservers();
			}

			pingService.shutdown();
			pingService = null;
			pingThread = null;
		}
	}

	private class PingThreadObserver implements IPingThreadObserver
	{
		@Override
		public void onNewPingStats(final PingStats stats)
		{
			SwingUtilities.invokeLater(
					() -> botConfigOverviewPanel.getManualControlPanel().getPingPanel().setPingStats(stats));
		}
	}


	private class ConsolePanelObserver implements IConsolePanelObserver
	{
		@Override
		public void onConsoleCommand(final String cmd, final ConsoleCommandTarget target)
		{
			if (bot == null)
			{
				return;
			}
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);
			bot.sendCommand(consoleCmd);
		}


		@Override
		public void onConsoleCommand2All(final String cmd, final ConsoleCommandTarget target)
		{
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);

			SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
					.ifPresent(mgr ->
							mgr.getBots().values().forEach(b ->
									b.sendCommand(consoleCmd)));
		}
	}

	private class SystemMatchFeedbackObserver implements ISystemMatchFeedbackPanelObserver
	{
		BotWatcher botWatcher = null;


		@Override
		public void onCapture(final boolean enable)
		{
			if (enable && bot != null)
			{
				if (botWatcher != null)
				{
					botWatcher.stop();
				}
				botWatcher = new BotWatcher(bot.getBotId(), "manual");
				botWatcher.start();
			} else
			{
				if (botWatcher != null)
				{
					botWatcher.stop();
					botWatcher = null;
				}
			}

		}
	}


	public void onIncomingBotCommand(BotCommand botCommand)
	{
		if (bot != null && bot.getBotId().equals(botCommand.botId()))
		{
			processCommand(botCommand.command());
		}
	}
}
