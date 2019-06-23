/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.basestation;

import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.bots.TigerBotV3;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.tigers.sumatra.botmanager.bots.communication.udp.UnicastTransceiverUDP;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationAuth;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationConfigV2;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationConfigV2.BSModuleConfig;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationConfigV2.EWifiSpeed;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationPing;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats.BotStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.NetworkUtility;
import edu.tigers.sumatra.thread.GeneralPurposeTimer;
import edu.tigers.sumatra.thread.IWatchdogObserver;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.thread.Watchdog;


/**
 * Base station main class.
 * Acts as a packaging Transceiver which prepends commands by BotIDs.
 * 
 * @author AndreR
 */
public class TigersBaseStation extends ABaseStation implements ITransceiverUDPObserver, IWatchdogObserver
{
	
	private static final Logger					log				= Logger.getLogger(TigersBaseStation.class.getName());
	private final UnicastTransceiverUDP			transceiver		= new UnicastTransceiverUDP(false);
	
	@Configurable(defValue = "10200")
	private int											localPort		= 10200;
	@Configurable(defValue = "192.168.20.210", spezis = { "ROBOCUP", "LAB", "ANDRE" })
	private String										host				= "192.168.20.210";
	@Configurable(defValue = "10201")
	private int											dstPort			= 10201;
	
	@Configurable(spezis = { "ROBOCUP", "LAB", "ANDRE" }, defValue = "121")
	private int											channel			= 0;
	@Configurable(comment = "Fix the runtime regardless of the number of bot that are connected.", defValue = "true")
	private boolean									fixedRuntime	= true;
	@Configurable(comment = "Max communication slots to open for communication to bots", defValue = "8")
	private int											maxBots			= 8;
	@Configurable(comment = "timeout when bot is considered to be offline.", defValue = "1000")
	private int											timeout			= 1000;
	@Configurable(comment = "wifi speed (must be consistent with bots wifi speed!)", defValue = "WIFI_SPEED_2M")
	private EWifiSpeed								speed				= EWifiSpeed.WIFI_SPEED_2M;
	
	@Configurable(defValue = "10010")
	private int											rstPort			= 10010;
	@Configurable(defValue = "50")
	private int											rstRate			= 50;
	@Configurable(defValue = "false")
	private boolean									rstEnabled		= false;
	
	private int											visionPort		= -1;
	private String										visionAddress	= "";
	
	private ScheduledExecutorService				pingService		= null;
	private PingThread								pingThread		= null;
	private Connector									connectTimer	= null;
	
	private final Watchdog							watchdog			= new Watchdog(timeout);
	private ENetworkState							netState			= ENetworkState.OFFLINE;
	
	private Set<BotID>								lastBots			= new HashSet<>();
	
	private final int									key;
	
	private static final int						STAT_ENTRIES	= 10;
	private final Queue<BaseStationWifiStats>	wifiStats		= new LinkedList<>();
	private final Queue<BaseStationEthStats>	ethStats			= new LinkedList<>();
	
	private int											updateRate		= 0;
	
	private BotParamsManager						botParamsManager;
	
	
	private static final String					CONFIG_CAT		= "botmgr";
	static
	{
		ConfigRegistration.registerClass(CONFIG_CAT, TigersBaseStation.class);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default constructor.
	 */
	public TigersBaseStation()
	{
		super(EBotType.TIGER_V3);
		key = 0;
	}
	
	
	private void init()
	{
		// Detect the correct interface for base station
		final NetworkInterface nif = NetworkUtility.chooseNetworkInterface(host, 3);
		if (nif == null)
		{
			log.error("No proper nif for base station in network '" + host + "' found!");
		} else
		{
			log.info("Chose nif for base station: " + nif.getDisplayName() + ".");
		}
		
		try
		{
			SSLVisionCam cam = (SSLVisionCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			visionAddress = cam.getAddress();
			visionPort = cam.getPort();
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
		
		try
		{
			botParamsManager = (BotParamsManager) SumatraModel.getInstance().getModule(BotParamsManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find BotParamsManager module", err);
		}
		
		
		transceiver.setNetworkInterface(nif);
		transceiver.setLocalPort(localPort);
		transceiver.setDestination(host, dstPort);
	}
	
	
	@Override
	public void enqueueCommand(final BotID id, final ACommand cmd)
	{
		if (!transceiver.isOpen())
		{
			return;
		}
		
		final BaseStationACommand baseCmd = new BaseStationACommand(id, cmd);
		
		transceiver.enqueueCommand(baseCmd);
	}
	
	
	@Override
	public void enqueueCommand(final ACommand cmd)
	{
		if (!transceiver.isOpen())
		{
			return;
		}
		
		transceiver.enqueueCommand(cmd);
	}
	
	
	@Override
	public void onIncommingCommand(final ACommand cmd)
	{
		if (watchdog.isActive())
		{
			watchdog.reset();
		} else
		{
			changeNetworkState(ENetworkState.ONLINE);
		}
		
		switch (cmd.getType())
		{
			case CMD_BASE_ACOMMAND:
				incCmdBaseACommand(cmd);
				break;
			case CMD_BASE_PING:
				incCmdBasePing(cmd);
				break;
			case CMD_BASE_WIFI_STATS:
				incCmdBaseWifiStats(cmd);
				break;
			case CMD_BASE_ETH_STATS:
				incCmdBaseEthStats(cmd);
				break;
			case CMD_BASE_AUTH:
				notifyIncommingBaseStationCommand(cmd);
				break;
			default:
				log.warn("Unknown incomming command from Base Station.");
				break;
		}
	}
	
	
	private void incCmdBaseEthStats(final ACommand cmd)
	{
		BaseStationEthStats stats = (BaseStationEthStats) cmd;
		
		ethStats.add(stats);
		
		if (ethStats.size() > STAT_ENTRIES)
		{
			stats = new BaseStationEthStats(stats, ethStats.remove());
		}
		
		notifyNewBaseStationEthStats(stats);
	}
	
	
	private void incCmdBaseWifiStats(final ACommand cmd)
	{
		BaseStationWifiStats stats = (BaseStationWifiStats) cmd;
		
		wifiStats.add(stats);
		
		if (wifiStats.size() > STAT_ENTRIES)
		{
			// this gives a nice report over the last second every 100ms :)
			stats = new BaseStationWifiStats(stats, wifiStats.remove());
		}
		updateRate = stats.getUpdateRate();
		
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
				notifyBotOffline(botId);
			}
		}
		for (BotID botId : curBots)
		{
			if (!lastBots.contains(botId))
			{
				TigerBotV3 botV3 = new TigerBotV3(botId, this, botParamsManager);
				botV3.setUpdateRate(updateRate - 10.0);
				notifyBotOnline(botV3);
			}
		}
		
		lastBots = curBots;
		
		notifyNewBaseStationWifiStats(stats);
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
			log.warn("Invalid BaseStationACommand lost");
			return;
		}
		
		switch (baseCmd.getChild().getType())
		{
			case CMD_SYSTEM_CONSOLE_PRINT:
				final TigerSystemConsolePrint print = (TigerSystemConsolePrint) baseCmd.getChild();
				log.info("Console(" + baseCmd.getId().getNumberWithColorOffset() + "): " + print.getText());
				break;
			case CMD_SYSTEM_MATCH_FEEDBACK:
				notifyNewMatchFeedback(baseCmd.getId(), (TigerSystemMatchFeedback) baseCmd.getChild());
				break;
			default:
				break;
		}
		
		notifyIncommingBotCommand(baseCmd.getId(), baseCmd.getChild());
	}
	
	
	@Override
	public void onOutgoingCommand(final ACommand cmd)
	{
		// Not interested in any outgoind commands
	}
	
	
	@Override
	public void onConnect()
	{
		ConfigRegistration.applySpezis(this, CONFIG_CAT, "");
		ConfigRegistration.applySpezis(this, CONFIG_CAT, SumatraModel.getInstance().getEnvironment());
		if (netState == ENetworkState.OFFLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
		// user config is needed for vision port.
		ConfigRegistration.registerConfigurableCallback("user", this);
	}
	
	
	@Override
	public void onDisconnect()
	{
		changeNetworkState(ENetworkState.OFFLINE);
		ConfigRegistration.unregisterConfigurableCallback("user", this);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ENetworkState getNetState()
	{
		return netState;
	}
	
	
	/**
	 * @param numPings
	 * @param payloadLength
	 */
	@Override
	public void startPing(final int numPings, final int payloadLength)
	{
		stopPing();
		
		pingThread = new PingThread(payloadLength);
		pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
		pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
	}
	
	
	/**
	 *
	 */
	@Override
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
	
	
	private void sendConfig()
	{
		BaseStationConfigV2 config = new BaseStationConfigV2();
		config.setVisionIp(visionAddress);
		config.setVisionPort(visionPort);
		config.setRstPort(rstPort);
		config.setRstRate(rstRate);
		config.setRstEnabled(rstEnabled);
		BSModuleConfig modConf = config.getModuleConfig(0);
		modConf.setChannel(channel);
		modConf.setFixedRuntime(fixedRuntime);
		modConf.setMaxBots(maxBots);
		modConf.setSpeed(speed);
		modConf.setTimeout(timeout);
		enqueueCommand(config);
	}
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private void changeNetworkState(final ENetworkState newState)
	{
		if (netState == newState)
		{
			return;
		}
		
		if ((netState == ENetworkState.OFFLINE) && (newState == ENetworkState.CONNECTING))
		{
			offline2connecting(newState);
			return;
		}
		
		if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.OFFLINE))
		{
			connecting2offline(newState);
			return;
		}
		
		if ((netState != ENetworkState.ONLINE) && (newState == ENetworkState.ONLINE))
		{
			toOnline(newState);
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.CONNECTING))
		{
			online2Connecting(newState);
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.OFFLINE))
		{
			online2offline(newState);
			return;
		}
		
		log.error("Invalid state transition from " + netState + " to " + newState);
	}
	
	
	private void online2offline(final ENetworkState newState)
	{
		for (BotID botId : lastBots)
		{
			notifyBotOffline(botId);
		}
		lastBots.clear();
		
		// stop watchdog
		watchdog.stop();
		
		// terminate transceiver
		transceiver.removeObserver(this);
		transceiver.close();
		
		netState = newState;
		notifyNetworkStateChanged(netState);
		
		log.info("Disconnected base station");
	}
	
	
	private void online2Connecting(final ENetworkState newState)
	{
		for (BotID botId : lastBots)
		{
			notifyBotOffline(botId);
		}
		lastBots.clear();
		
		// stop watchdog
		watchdog.stop();
		
		netState = newState;
		notifyNetworkStateChanged(netState);
		
		connectTimer = new Connector();
		GeneralPurposeTimer.getInstance().schedule(connectTimer, 0, 1000);
		
		log.debug("Base station timed out");
	}
	
	
	private void toOnline(final ENetworkState newState)
	{
		// start watchdog
		watchdog.start(this);
		
		netState = newState;
		notifyNetworkStateChanged(netState);
		sendConfig();
		
		log.info("Connected base station");
	}
	
	
	private void connecting2offline(final ENetworkState newState)
	{
		if (connectTimer != null)
		{
			connectTimer.cancel();
		}
		
		// stop transceiver
		transceiver.removeObserver(this);
		transceiver.close();
		
		netState = newState;
		notifyNetworkStateChanged(netState);
		
		log.info("Disconnected base station");
	}
	
	
	private void offline2connecting(final ENetworkState newState)
	{
		init();
		// start transceiver
		transceiver.addObserver(this);
		transceiver.open();
		
		connectTimer = new Connector();
		GeneralPurposeTimer.getInstance().schedule(connectTimer, 0, 1000);
		
		netState = newState;
		notifyNetworkStateChanged(netState);
		
		log.debug("Base station connecting");
	}
	
	
	@Override
	public void onWatchdogTimeout()
	{
		if (netState == ENetworkState.ONLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
	}
	
	
	@Override
	public String getName()
	{
		return "BaseStation " + key;
	}
	
	
	/**
	 * @return the updateRate
	 */
	public final int getUpdateRate()
	{
		return updateRate;
	}
	
	
	private class PingThread implements Runnable
	{
		private int								id					= 0;
		private int								payloadLength	= 0;
		
		private final Map<Integer, Long>	activePings		= new HashMap<>();
		
		
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
			
			notifyNewPingDelay(delayPongArrive);
		}
	}
	
	private class Connector extends TimerTask
	{
		@Override
		public void run()
		{
			enqueueCommand(new BaseStationAuth());
			if (netState == ENetworkState.ONLINE)
			{
				connectTimer.cancel();
			}
		}
	}
}
