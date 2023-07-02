/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.basestation;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.botskills.data.MatchCommand;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationAuth;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationConfigV3;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationPing;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats.BotStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.communication.ITransceiverObserver;
import edu.tigers.sumatra.botmanager.communication.udp.UnicastTransceiverUDP;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.clock.NanoTime;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogRecorder;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.GeneralPurposeTimer;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.thread.Watchdog;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Base station implementation for TIGERs bots
 */
@Log4j2
public class TigersBaseStation extends ABaseStation
		implements ITransceiverObserver, IConfigObserver
{
	private static final String CONFIG_CAT = "botmgr";
	private static final int BASE_STATION_TIMEOUT = 1000;
	private static final int STAT_ENTRIES = 10;

	@Configurable(defValue = "192.168.60.210", spezis = { "ROBOCUP", "LAB", "ANDRE", "TISCH", "NICOLAI" })
	private static String host = "192.168.60.210";

	@Configurable(defValue = "10201")
	private static int dstPort = 10201;

	@Configurable(spezis = { "ROBOCUP", "LAB", "ANDRE", "TISCH", "NICOLAI" }, defValue = "80")
	private static int channel = 0;

	@Configurable(comment = "Fix the runtime regardless of the number of bot that are connected.", defValue = "true")
	private static boolean fixedRuntime = true;

	@Configurable(comment = "Max communication slots to open for communication to bots", defValue = "12")
	private static int maxBots = 12;

	private final UnicastTransceiverUDP transceiver = new UnicastTransceiverUDP();
	private final Watchdog watchdog = new Watchdog(BASE_STATION_TIMEOUT, "TIGERs BS", this::handleTimeoutEvent);

	private int visionPort = -1;
	private String visionAddress = "";

	private GameLogRecorder gameLogRecorder;

	private ScheduledExecutorService pingService = null;
	private PingThread pingThread = null;
	private Connector connectTimer = null;

	private ENetworkState netState = ENetworkState.OFFLINE;

	private Set<BotID> lastBots = new HashSet<>();

	private final Queue<BaseStationWifiStats> wifiStats = new LinkedList<>();
	private final Queue<BaseStationEthStats> ethStats = new LinkedList<>();

	private final List<ITigersBaseStationObserver> observers = new CopyOnWriteArrayList<>();


	static
	{
		ConfigRegistration.registerClass(CONFIG_CAT, TigersBaseStation.class);
	}


	private void init()
	{
		transceiver.setDestination(host, dstPort);

		SumatraModel.getInstance().getModuleOpt(SSLVisionCam.class)
				.ifPresent(cam -> {
					visionAddress = cam.getAddress();
					visionPort = cam.getPort();
				});

		gameLogRecorder = SumatraModel.getInstance().getModuleOpt(GameLogRecorder.class).orElse(null);
	}


	public void addTigersBsObserver(final ITigersBaseStationObserver observer)
	{
		observers.add(observer);
	}


	public void removeTigersBsObserver(final ITigersBaseStationObserver observer)
	{
		observers.remove(observer);
	}


	public void enqueueCommand(final BotID id, final ACommand cmd)
	{
		if (!transceiver.isOpen())
		{
			return;
		}

		var baseCmd = new BaseStationACommand(id, cmd);
		transceiver.enqueueCommand(baseCmd);
		recordCommand(baseCmd, EMessageType.TIGERS_BASE_STATION_CMD_SENT);
	}


	public void enqueueCommand(final ACommand cmd)
	{
		if (!transceiver.isOpen())
		{
			return;
		}

		transceiver.enqueueCommand(cmd);
		recordCommand(cmd, EMessageType.TIGERS_BASE_STATION_CMD_SENT);
	}


	@Override
	public void acceptMatchCommand(final BotID botId, final MatchCommand matchCommand)
	{
		TigerSystemMatchCtrl matchCtrl = new TigerSystemMatchCtrl(matchCommand);
		enqueueCommand(botId, matchCtrl);
	}


	@Override
	public void onIncomingCommand(final ACommand cmd)
	{
		handleDataReceivedEvent();

		switch (cmd.getType())
		{
			case CMD_BASE_ACOMMAND -> incCmdBaseACommand(cmd);
			case CMD_BASE_PING -> incCmdBasePing(cmd);
			case CMD_BASE_WIFI_STATS -> incCmdBaseWifiStats(cmd);
			case CMD_BASE_ETH_STATS -> incCmdBaseEthStats(cmd);
			default -> log.warn("Unknown incoming command from Base Station.");
		}

		recordCommand(cmd, EMessageType.TIGERS_BASE_STATION_CMD_RECEIVED);
	}


	private void recordCommand(final ACommand cmd, EMessageType type)
	{
		if(gameLogRecorder != null)
		{
			byte[] data = CommandFactory.getInstance().encode(cmd);
			gameLogRecorder.writeMessage(new GameLogMessage(NanoTime.getTimestampNow(), type, data));
		}
	}


	private void incCmdBaseEthStats(final ACommand cmd)
	{
		BaseStationEthStats incomingStats = (BaseStationEthStats) cmd;

		ethStats.add(incomingStats);

		BaseStationEthStats stats;
		if (ethStats.size() > STAT_ENTRIES)
		{
			stats = new BaseStationEthStats(incomingStats, ethStats.remove());
		} else
		{
			stats = incomingStats;
		}

		observers.forEach(c -> c.onNewBaseStationEthStats(stats));
	}


	private void incCmdBaseWifiStats(final ACommand cmd)
	{
		BaseStationWifiStats incomingStats = (BaseStationWifiStats) cmd;

		wifiStats.add(incomingStats);

		BaseStationWifiStats stats;
		if (wifiStats.size() > STAT_ENTRIES)
		{
			// this gives a nice report over the last second every 100ms :)
			stats = new BaseStationWifiStats(incomingStats, wifiStats.remove());
		} else
		{
			stats = incomingStats;
		}

		Set<BotID> curBots = new HashSet<>();
		for (BotStats botStats : stats.getBotStats())
		{
			BotID botId = botStats.getBotId();
			if (botId.isBot())
			{
				curBots.add(botId);
			}
		}
		for (BotID botId : lastBots)
		{
			if (!curBots.contains(botId))
			{
				botOffline(botId);
			}
		}
		for (BotID botId : curBots)
		{
			if (!lastBots.contains(botId))
			{
				TigerBot bot = new TigerBot(botId, this);
				botOnline(bot);
			}
		}

		lastBots = curBots;

		observers.forEach(c -> c.onNewBaseStationWifiStats(stats));
	}


	private void incCmdBasePing(final ACommand cmd)
	{
		BaseStationPing ping = (BaseStationPing) cmd;
		if (pingThread != null)
		{
			pingThread.pongArrived((int) ping.getId());
		}
	}


	private void incCmdBaseACommand(final ACommand cmd)
	{
		BaseStationACommand baseCmd = (BaseStationACommand) cmd;

		if (baseCmd.getChild() == null)
		{
			log.info("Invalid BaseStationACommand lost");
			return;
		}

		if (baseCmd.getChild().getType() == ECommand.CMD_SYSTEM_CONSOLE_PRINT)
		{
			final TigerSystemConsolePrint print = (TigerSystemConsolePrint) baseCmd.getChild();
			log.info("Console({}): {}", baseCmd.getId().getNumberWithColorOffset(),
					print.getText().replaceAll("[\n\r]$", ""));
		}

		observers.forEach(c -> c.onIncomingBotCommand(baseCmd.getId(), baseCmd.getChild()));
	}


	@Override
	public void afterApply(final IConfigClient configClient)
	{
		// reconnect to make sure all config changes are applied
		reconnect();
	}


	@Override
	public void connect()
	{
		ConfigRegistration.applySpezi(CONFIG_CAT, SumatraModel.getInstance().getEnvironment());

		handleConnectEvent();

		// user config is needed for vision port.
		ConfigRegistration.registerConfigurableCallback("user", this);
		// get notified when bot manager config has been updated
		ConfigRegistration.registerConfigurableCallback(CONFIG_CAT, this);
	}


	@Override
	public void disconnect()
	{
		handleDisconnectEvent();

		ConfigRegistration.unregisterConfigurableCallback(CONFIG_CAT, this);
		ConfigRegistration.unregisterConfigurableCallback("user", this);
	}


	public ENetworkState getNetState()
	{
		return netState;
	}


	public void startPing(final int numPings, final int payloadLength)
	{
		stopPing();

		pingThread = new PingThread(payloadLength);
		pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
		pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
	}


	public void stopPing()
	{
		if (pingService == null)
		{
			return;
		}

		pingService.shutdown();
		pingService = null;
		pingThread = null;
	}


	private void doOnlineActions()
	{
		watchdog.start();

		activateDefaultConfig();
	}


	public void activateDefaultConfig()
	{
		BaseStationConfigV3 config = new BaseStationConfigV3();
		config.setVisionIp(visionAddress);
		config.setVisionPort(visionPort);
		config.setChannel(channel);
		config.setFixedRuntime(fixedRuntime);
		config.setMaxBots(maxBots);
		enqueueCommand(config);
	}


	public void activateDataBurstMode()
	{
		BaseStationConfigV3 config = new BaseStationConfigV3();
		config.setVisionIp(visionAddress);
		config.setVisionPort(visionPort);
		config.setChannel(channel);
		config.setFixedRuntime(true);
		config.setMaxBots(1);
		enqueueCommand(config);
	}


	private void revokeOnlineActions()
	{
		for (BotID botId : lastBots)
		{
			botOffline(botId);
		}
		lastBots.clear();

		watchdog.stop();
	}


	private void doConnectingActions()
	{
		init();

		// start transceiver
		transceiver.addObserver(this);
		transceiver.open();
	}


	private void revokeConnectingActions()
	{
		// terminate transceiver
		transceiver.removeObserver(this);
		transceiver.close();
	}


	private void createConnectTimer()
	{
		if (connectTimer == null)
		{
			connectTimer = new Connector();
			GeneralPurposeTimer.getInstance().schedule(connectTimer, 0, 1000);
		}
	}


	private void cancelConnectTimer()
	{
		if (connectTimer != null)
		{
			connectTimer.cancel();
			connectTimer = null;
		}
	}


	private void handleConnectEvent()
	{
		if (netState != ENetworkState.OFFLINE)
		{
			return;
		}

		doConnectingActions();
		createConnectTimer();

		netState = ENetworkState.CONNECTING;
		observers.forEach(c -> c.onNetworkStateChanged(netState));

		log.debug("Base station connecting");
	}


	private void handleDisconnectEvent()
	{
		if (netState == ENetworkState.OFFLINE)
		{
			return;
		}

		revokeOnlineActions();
		revokeConnectingActions();
		cancelConnectTimer();

		netState = ENetworkState.OFFLINE;
		observers.forEach(c -> c.onNetworkStateChanged(netState));

		log.info("Disconnected base station");
	}


	private void handleTimeoutEvent()
	{
		if (netState != ENetworkState.ONLINE)
		{
			return;
		}

		revokeOnlineActions();
		createConnectTimer();

		netState = ENetworkState.CONNECTING;
		observers.forEach(c -> c.onNetworkStateChanged(netState));

		log.debug("Base station timed out");
	}


	private void handleDataReceivedEvent()
	{
		watchdog.reset();

		if (netState != ENetworkState.CONNECTING)
		{
			return;
		}

		cancelConnectTimer();
		doOnlineActions();

		netState = ENetworkState.ONLINE;
		observers.forEach(c -> c.onNetworkStateChanged(netState));

		log.info("Connected base station");
	}


	private class PingThread implements Runnable
	{
		private int id = 0;
		private final int payloadLength;

		private final Map<Integer, Long> activePings = new HashMap<>();


		/**
		 * @param payloadLength
		 */
		public PingThread(final int payloadLength)
		{
			this.payloadLength = payloadLength;
		}


		@Override
		public void run()
		{
			synchronized (activePings)
			{
				activePings.put(id, System.nanoTime());
			}

			enqueueCommand(new BaseStationPing(id, payloadLength));
			id++;
		}


		/**
		 * @param id
		 */
		public void pongArrived(final int id)
		{
			Long startTime;

			synchronized (activePings)
			{
				startTime = activePings.remove(id);
			}

			if (startTime == null)
			{
				return;
			}

			final double delayPongArrive = (System.nanoTime() - startTime) / 1000000.0;

			observers.forEach(c -> c.onNewPingDelay(delayPongArrive));
		}
	}

	private class Connector extends TimerTask
	{
		@Override
		public void run()
		{
			enqueueCommand(new BaseStationAuth());
		}
	}
}
