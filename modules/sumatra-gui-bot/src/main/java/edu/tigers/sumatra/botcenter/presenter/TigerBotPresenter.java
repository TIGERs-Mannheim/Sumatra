/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.presenter;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botcenter.view.BcBotPingPanel.IBcBotPingPanelObserver;
import edu.tigers.sumatra.botcenter.view.BotConfigOverviewPanel;
import edu.tigers.sumatra.botcenter.view.bots.ConsolePanel.IConsolePanelObserver;
import edu.tigers.sumatra.botcenter.view.bots.SystemMatchFeedbackPanel.ISystemMatchFeedbackPanelObserver;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.basestation.ITigersBaseStationObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tiger.TigerSystemPong;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemPerformance;
import edu.tigers.sumatra.botmanager.ping.PingStats;
import edu.tigers.sumatra.botmanager.ping.PingThread;
import edu.tigers.sumatra.botmanager.ping.PingThread.IPingThreadObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.thread.NamedThreadFactory;


/**
 * Presenter for a single tigers bot (the selected one)
 */
public class TigerBotPresenter implements ITigersBaseStationObserver
{
	private static final Logger log = Logger.getLogger(TigerBotPresenter.class.getName());
	private PingThread pingThread = null;
	private TigerBot bot = null;
	private final BotConfigOverviewPanel botConfigOverviewPanel;
	private ABotManager botManager;
	private final ConfigPresenter configPresenter;


	public TigerBotPresenter(final BotConfigOverviewPanel botConfigOverviewPanel)
	{
		this.botConfigOverviewPanel = botConfigOverviewPanel;

		configPresenter = new ConfigPresenter(botConfigOverviewPanel.getConfigPanel(), null);

		botConfigOverviewPanel.getManualControlPanel().getPingPanel().addObserver(new BcBotPingPanelObserver());
		botConfigOverviewPanel.getConsolePanel().addObserver(new ConsolePanelObserver());
		botConfigOverviewPanel.getSystemStatusPanel().addObserver(new SystemMatchFeedbackObserver());
	}


	/**
	 * @return the bot
	 */
	public IBot getBot()
	{
		return bot;
	}


	/**
	 * @param bot the selectedBotId to set
	 */
	public void updateSelectedBotId(final TigerBot bot)
	{
		this.bot = bot;
		configPresenter.setBot(bot);
	}


	/**
	 * @param botManager the botManager to set
	 */
	public void setBotManager(final ABotManager botManager)
	{
		this.botManager = botManager;
		configPresenter.setBotManager(botManager);
	}


	private void processPong(final TigerSystemPong pong)
	{
		if (pingThread != null)
		{
			pingThread.pongArrived(pong.getId());
			if (!pong.payloadValid())
			{
				log.warn("Invalid payload received: " + Arrays.toString(pong.getPayload()));
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
			case CMD_SYSTEM_CONSOLE_PRINT:
				TigerSystemConsolePrint print = (TigerSystemConsolePrint) cmd;
				botConfigOverviewPanel.getConsolePanel().addConsolePrint(print);
				break;
			case CMD_SYSTEM_PONG:
				processPong((TigerSystemPong) cmd);
				break;
			case CMD_SYSTEM_MATCH_FEEDBACK:
				SwingUtilities.invokeLater(() -> procCmdSystemMatchFeedback(cmd));
				break;
			case CMD_SYSTEM_PERFORMANCE:
				SwingUtilities.invokeLater(() -> procCmdSystemPerformance(cmd));
				break;
			case CMD_CONFIG_FILE_STRUCTURE:
			case CMD_CONFIG_ITEM_DESC:
			case CMD_CONFIG_READ:
				configPresenter.onNewCommand(cmd);
				break;
			default:
				break;
		}
	}


	private void procCmdSystemPerformance(final ACommand cmd)
	{
		final TigerSystemPerformance perf = (TigerSystemPerformance) cmd;
		botConfigOverviewPanel.getSystemStatusPanel().addPerformance(perf);
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

			pingThread = new PingThread(payloadSize, bot);
			pingThread.addObserver(new PingThreadObserver());
			pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
			pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
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
			botConfigOverviewPanel.getManualControlPanel().getPingPanel().setPingStats(stats);
		}
	}


	private class ConsolePanelObserver implements IConsolePanelObserver
	{
		@Override
		public void onConsoleCommand(final String cmd, final ConsoleCommandTarget target)
		{
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);
			bot.execute(consoleCmd);
		}


		@Override
		public void onConsoleCommand2All(final String cmd, final ConsoleCommandTarget target)
		{
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);
			for (ABot aBot : botManager.getBots().values())
			{
				TigerBot tigerBot = (TigerBot) aBot;
				tigerBot.execute(consoleCmd);
			}
		}
	}

	private class SystemMatchFeedbackObserver implements ISystemMatchFeedbackPanelObserver
	{
		BotWatcher botWatcher = null;


		@Override
		public void onCapture(final boolean enable)
		{
			if (enable)
			{
				if (botWatcher != null)
				{
					botWatcher.stop();
				}
				botWatcher = new BotWatcher(bot);
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


	@Override
	public void onIncomingBotCommand(final BotID id, final ACommand command)
	{
		if (bot != null && bot.getBotId().equals(id))
		{
			processCommand(command);
		}
	}
}
