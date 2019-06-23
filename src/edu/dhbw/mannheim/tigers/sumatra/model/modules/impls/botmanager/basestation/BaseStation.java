/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.04.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation;

import java.net.NetworkInterface;
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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.UnicastTransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationAuth;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationConfigV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationConfigV2.BSModuleConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationConfigV2.EWifiSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationEthStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats.WifiStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationWifiStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationWifiStats.BotStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SSLVisionCam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.IWatchdogObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.Watchdog;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.config.EConfigurableCat;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * Base station main class.
 * Acts as a packaging Transceiver which prepends commands by BotIDs.
 * 
 * @author AndreR
 */
public class BaseStation implements IBaseStation, ITransceiverUDPObserver, IWatchdogObserver
{
	private static final Logger					log				= Logger.getLogger(BaseStation.class.getName());
	private final List<IBaseStationObserver>	observers		= new CopyOnWriteArrayList<IBaseStationObserver>();
	private final UnicastTransceiverUDP			transceiver		= new UnicastTransceiverUDP(false);
	private final IConfigObserver					configObserver	= new ConfigObserver();
	
	@Configurable(defValue = "10200", spezis = { "PRIMARY", "SECONDARY" })
	private int											localPort		= 10200;
	@Configurable(defValue = "192.168.20.210", spezis = { "PRIMARY", "SECONDARY" })
	private String										host				= "192.168.20.210";
	@Configurable(defValue = "10201", spezis = { "PRIMARY", "SECONDARY" })
	private int											dstPort			= 10201;
	
	@Configurable(defValue = "92", spezis = { "PRIMARY", "SECONDARY" })
	private int											channel			= 92;
	@Configurable(comment = "Fix the runtime regardless of the number of bot that are connected.", defValue = "true", spezis = {
			"PRIMARY", "SECONDARY" })
	private boolean									fixedRuntime	= true;
	@Configurable(comment = "Max communication slots to open for communication to bots", defValue = "8", spezis = {
			"PRIMARY", "SECONDARY" })
	private int											maxBots			= 8;
	@Configurable(comment = "timeout when bot is considered to be offline.", defValue = "1000", spezis = {
			"PRIMARY", "SECONDARY" })
	private int											timeout			= 1000;
	@Configurable(comment = "wifi speed (must be consistent with bots wifi speed!)", defValue = "WIFI_SPEED_2M", spezis = {
			"PRIMARY", "SECONDARY" })
	private EWifiSpeed								speed				= EWifiSpeed.WIFI_SPEED_2M;
	
	@Configurable(comment = "If true, connect automatically on startup", defValue = "false", spezis = {
			"PRIMARY", "SECONDARY" })
	private boolean									active			= false;
	private int											visionPort		= -1;
	private String										visionAddress	= "";
	
	private ScheduledExecutorService				pingService		= null;
	private PingThread								pingThread		= null;
	private Connector									connectTimer	= null;
	
	private static final int						TIMEOUT			= 2000;
	private final Watchdog							watchdog			= new Watchdog(TIMEOUT);
	private ENetworkState							netState			= ENetworkState.OFFLINE;
	
	private Set<BotID>								lastBots			= new HashSet<BotID>();
	private BaseStationStats						latestStats		= null;
	
	private final int									key;
	
	private static final int						STAT_ENTRIES	= 10;
	private Queue<BaseStationWifiStats>			wifiStats		= new LinkedList<BaseStationWifiStats>();
	private Queue<BaseStationEthStats>			ethStats			= new LinkedList<BaseStationEthStats>();
	
	private int											updateRate		= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStation()
	{
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
		
		transceiver.setNetworkInterface(nif);
		transceiver.setLocalPort(localPort);
		transceiver.setDestination(host, dstPort);
	}
	
	
	@Override
	public void addObserver(final IBaseStationObserver observer)
	{
		observers.add(observer);
	}
	
	
	@Override
	public void removeObserver(final IBaseStationObserver observer)
	{
		observers.remove(observer);
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
	
	
	/**
	 * @param cmd
	 */
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
					{
						final TigerSystemConsolePrint print = (TigerSystemConsolePrint) baseCmd.getChild();
						log.info("Console(" + baseCmd.getId().getNumberWithColorOffset() + "): " + print.getText());
					}
						break;
					default:
						break;
				}
				
				notifyIncommingBotCommand(baseCmd.getId(), baseCmd.getChild());
			}
				break;
			case CMD_BASE_PING:
			{
				BaseStationPing ping = (BaseStationPing) cmd;
				
				if (pingThread != null)
				{
					pingThread.pongArrived((int) ping.getId());
				}
			}
				break;
			case CMD_BASE_STATS:
			{
				BaseStationStats stats = (BaseStationStats) cmd;
				
				Set<BotID> curBots = new HashSet<BotID>();
				for (WifiStats wifiStats : stats.getWifiStats())
				{
					if (wifiStats.getBotId().isBot())
					{
						curBots.add(wifiStats.getBotId());
					}
				}
				for (BotID botId : lastBots)
				{
					if (!curBots.contains(botId))
					{
						notifyBotOffline(botId);
					}
				}
				
				lastBots = curBots;
				latestStats = stats;
				
				notifyNewBaseStationStats(stats);
			}
				break;
			case CMD_BASE_WIFI_STATS:
			{
				BaseStationWifiStats stats = (BaseStationWifiStats) cmd;
				
				wifiStats.add(stats);
				
				if (wifiStats.size() > STAT_ENTRIES)
				{
					// this gives a nice report over the last second every 100ms :)
					stats = new BaseStationWifiStats(stats, wifiStats.remove());
				}
				updateRate = stats.getUpdateRate();
				
				Set<BotID> curBots = new HashSet<BotID>();
				for (BotStats wifiStats : stats.getBotStats())
				{
					BotID botId = wifiStats.getBotId();
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
						notifyBotOnline(botId);
					}
				}
				
				lastBots = curBots;
				
				notifyNewBaseStationWifiStats(stats);
			}
				break;
			case CMD_BASE_ETH_STATS:
			{
				BaseStationEthStats stats = (BaseStationEthStats) cmd;
				
				ethStats.add(stats);
				
				if (ethStats.size() > STAT_ENTRIES)
				{
					stats = new BaseStationEthStats(stats, ethStats.remove());
				}
				
				notifyNewBaseStationEthStats(stats);
			}
				break;
			case CMD_BASE_AUTH:
			{
				notifyIncommingBaseStationCommand(cmd);
			}
				break;
			default:
			{
				log.warn("Unknown incomming command from Base Station.");
			}
				break;
		}
	}
	
	
	@Override
	public void onOutgoingCommand(final ACommand cmd)
	{
	}
	
	
	/**
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		this.active = active;
	}
	
	
	/** */
	@Override
	public void connect()
	{
		if (!active)
		{
			return;
		}
		
		ConfigRegistration.registerConfigurableCallback(EConfigurableCat.BOTMGR, configObserver);
		
		if (netState == ENetworkState.OFFLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
	}
	
	
	/** */
	@Override
	public void disconnect()
	{
		changeNetworkState(ENetworkState.OFFLINE);
		ConfigRegistration.unregisterConfigurableCallback(EConfigurableCat.BOTMGR, configObserver);
	}
	
	
	private void reconnect()
	{
		boolean conn = transceiver.isOpen();
		
		disconnect();
		
		init();
		
		if (conn)
		{
			connect();
		}
	}
	
	
	/**
	 * @return
	 */
	public ENetworkState getNetState()
	{
		return netState;
	}
	
	
	/**
	 * @param numPings
	 * @param payloadLength
	 */
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
	public void stopPing()
	{
		if (pingService == null)
		{
			return;
		}
		
		pingService.shutdownNow();
		pingService = null;
		pingThread = null;
	}
	
	
	private void sendConfig()
	{
		BaseStationConfigV2 config = new BaseStationConfigV2();
		config.setVisionIp(visionAddress);
		config.setVisionPort(visionPort);
		BSModuleConfig modConf = config.getModuleConfig(0);
		modConf.setChannel(channel);
		modConf.setFixedRuntime(fixedRuntime);
		modConf.setMaxBots(maxBots);
		modConf.setSpeed(speed);
		modConf.setTimeout(timeout);
		enqueueCommand(config);
	}
	
	
	private void changeNetworkState(final ENetworkState newState)
	{
		if (netState == newState)
		{
			return;
		}
		
		if ((netState == ENetworkState.OFFLINE) && (newState == ENetworkState.CONNECTING))
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
			
			return;
		}
		
		if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.OFFLINE))
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
			
			return;
		}
		
		// if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.ONLINE))
		if ((netState != ENetworkState.ONLINE) && (newState == ENetworkState.ONLINE))
		{
			// if (connectTimer != null)
			// {
			// connectTimer.cancel();
			// }
			//
			try
			{
				SSLVisionCam cam = (SSLVisionCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
				visionAddress = cam.getAddress();
				visionPort = cam.getPort();
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not find cam module", err);
			}
			
			// start watchdog
			watchdog.start(this);
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			sendConfig();
			
			log.info("Connected base station");
			
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.CONNECTING))
		{
			if (latestStats != null)
			{
				for (WifiStats wifiStats : latestStats.getWifiStats())
				{
					notifyBotOffline(wifiStats.getBotId());
				}
			}
			
			// stop watchdog
			watchdog.stop();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			connectTimer = new Connector();
			GeneralPurposeTimer.getInstance().schedule(connectTimer, 0, 1000);
			
			log.debug("Base station timed out");
			
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.OFFLINE))
		{
			if (latestStats != null)
			{
				for (WifiStats wifiStats : latestStats.getWifiStats())
				{
					notifyBotOffline(wifiStats.getBotId());
				}
			}
			
			// stop watchdog
			watchdog.stop();
			
			// terminate transceiver
			transceiver.removeObserver(this);
			transceiver.close();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Disconnected base station");
			
			return;
		}
		
		log.error("Invalid state transition from " + netState + " to " + newState);
	}
	
	
	private void notifyNetworkStateChanged(final ENetworkState netState)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNetworkStateChanged(netState);
		}
	}
	
	
	/**
	 * @param id
	 * @param cmd
	 */
	private void notifyIncommingBotCommand(final BotID id, final ACommand cmd)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onIncommingBotCommand(id, cmd);
		}
	}
	
	
	/**
	 * @param cmd
	 */
	private void notifyIncommingBaseStationCommand(final ACommand cmd)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onIncommingBaseStationCommand(cmd);
		}
	}
	
	
	private void notifyNewBaseStationStats(final BaseStationStats stats)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewBaseStationStats(stats);
		}
	}
	
	
	private void notifyNewBaseStationWifiStats(final BaseStationWifiStats stats)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewBaseStationWifiStats(stats);
		}
	}
	
	
	private void notifyNewBaseStationEthStats(final BaseStationEthStats stats)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewBaseStationEthStats(stats);
		}
	}
	
	
	private void notifyNewPingDelay(final float delay)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewPingDelay(delay);
		}
	}
	
	
	private void notifyBotOffline(final BotID id)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onBotOffline(id);
		}
	}
	
	
	private void notifyBotOnline(final BotID id)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onBotOnline(id);
		}
	}
	
	
	/**
	 * Set ip config.
	 * 
	 * @param host
	 * @param dstPort
	 * @param localPort
	 */
	public void setIpConfig(final String host, final int dstPort, final int localPort)
	{
		boolean conn = transceiver.isOpen();
		
		disconnect();
		
		this.host = host;
		this.dstPort = dstPort;
		this.localPort = localPort;
		
		transceiver.setLocalPort(localPort);
		transceiver.setDestination(host, dstPort);
		
		if (conn)
		{
			connect();
		}
	}
	
	
	/**
	 * @return the localPort
	 */
	public int getLocalPort()
	{
		return localPort;
	}
	
	
	/**
	 * @return the host
	 */
	public String getHost()
	{
		return host;
	}
	
	
	/**
	 * @return the dstPort
	 */
	public int getDstPort()
	{
		return dstPort;
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
	 * @return the channel
	 */
	public int getChannel()
	{
		return channel;
	}
	
	
	/**
	 * @return the maxBots
	 */
	public int getMaxBots()
	{
		return maxBots;
	}
	
	
	/**
	 * @return the timeout
	 */
	public int getTimeout()
	{
		return timeout;
	}
	
	
	/**
	 * @return the updateRate
	 */
	public final int getUpdateRate()
	{
		return updateRate;
	}
	
	private class PingThread extends Thread
	{
		private int								id					= 0;
		private int								payloadLength	= 0;
		
		private final Map<Integer, Long>	activePings		= new HashMap<Integer, Long>();
		
		
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
				activePings.put(id, SumatraClock.nanoTime());
			}
			
			enqueueCommand(new BaseStationPing(id, payloadLength));
			id++;
		}
		
		
		/**
		 * @param id
		 */
		public void pongArrived(final int id)
		{
			Long startTime = null;
			
			synchronized (activePings)
			{
				startTime = activePings.remove(id);
			}
			
			if (startTime == null)
			{
				return;
			}
			
			final float delayPongArrive = (SumatraClock.nanoTime() - startTime) / 1000000.0f;
			
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
	
	private class ConfigObserver implements IConfigObserver
	{
		@Override
		public void onLoad(final HierarchicalConfiguration newConfig)
		{
		}
		
		
		@Override
		public void onReload(final HierarchicalConfiguration freshConfig)
		{
			reconnect();
		}
	}
}
