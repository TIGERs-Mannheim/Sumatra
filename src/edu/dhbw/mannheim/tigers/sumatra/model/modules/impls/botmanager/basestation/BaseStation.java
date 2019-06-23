/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation;

import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.UnicastTransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationAuth;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats.WifiStats;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.IWatchdogObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.Watchdog;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * Base station main class.
 * Acts as a packaging Transceiver which prepends commands by BotIDs.
 * 
 * @author AndreR
 * 
 */
public class BaseStation implements IBaseStation, ITransceiverUDPObserver, IWatchdogObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Set<IBaseStationObserver>	observers		= new HashSet<IBaseStationObserver>();
	private final UnicastTransceiverUDP			transceiver		= new UnicastTransceiverUDP(false);
	private final Logger								log				= Logger.getLogger(getClass());
	
	private int											localPort		= 10200;
	private String										host				= "127.0.0.1";
	private int											dstPort			= 10201;
	
	private boolean									invertPosition	= false;
	private int											channel			= 100;
	private int											visionRate		= 30;
	private int											maxBots			= 6;
	private int											timeout			= 1000;
	
	private ScheduledExecutorService				pingService		= null;
	private PingThread								pingThread		= null;
	private Connector									connectTimer	= null;
	
	private static final int						TIMEOUT			= 2000;
	private final Watchdog							watchdog			= new Watchdog(TIMEOUT);
	private ENetworkState							netState			= ENetworkState.OFFLINE;
	private boolean									active			= false;
	private BaseStationStats						latestStats		= null;
	
	private final int									key;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStation()
	{
		init();
		key = 0;
	}
	
	
	/**
	 * 
	 * @param config
	 */
	public BaseStation(SubnodeConfiguration config)
	{
		host = config.getString("ip", "127.0.0.1");
		localPort = config.getInt("localPort", 10200);
		dstPort = config.getInt("remotePort", 10201);
		active = config.getBoolean("active", false);
		invertPosition = config.getBoolean("invertPos", false);
		visionRate = config.getInt("visionRate", 30);
		maxBots = config.getInt("maxBots", 6);
		channel = config.getInt("channel", 100);
		timeout = config.getInt("timeout", 1000);
		key = config.getInt("[@id]");
		init();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
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
	public void addObserver(IBaseStationObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	@Override
	public void removeObserver(IBaseStationObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	@Override
	public void enqueueCommand(BotID id, ACommand cmd)
	{
		if (!transceiver.isOpen())
		{
			return;
		}
		
		final BaseStationACommand baseCmd = new BaseStationACommand(id, cmd);
		
		transceiver.enqueueCommand(baseCmd);
	}
	
	
	/**
	 * 
	 * @param cmd
	 */
	public void enqueueCommand(ACommand cmd)
	{
		if (!transceiver.isOpen())
		{
			return;
		}
		
		transceiver.enqueueCommand(cmd);
	}
	
	
	@Override
	public void onIncommingCommand(ACommand cmd)
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
				
				latestStats = stats;
				
				for (WifiStats wifiStats : stats.getWifiStats())
				{
					if (wifiStats.getLinkQuality() == 0.0f)
					{
						notifyBotOffline(wifiStats.getBotId());
					}
				}
				
				notifyNewBaseStationStats(stats);
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
	public void onOutgoingCommand(ACommand cmd)
	{
	}
	
	
	/**
	 * 
	 * @param active
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	
	/** */
	public void connect()
	{
		if (!active)
		{
			return;
		}
		
		if (netState == ENetworkState.OFFLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
	}
	
	
	/** */
	public void disconnect()
	{
		changeNetworkState(ENetworkState.OFFLINE);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ENetworkState getNetState()
	{
		return netState;
	}
	
	
	/**
	 * 
	 * @param numPings
	 * @param payloadLength
	 */
	public void startPing(int numPings, int payloadLength)
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
	
	
	/**
	 * 
	 * @param channel
	 * @param invert
	 * @param maxBots
	 * @param rate
	 * @param timeout
	 */
	public void setConfig(int channel, boolean invert, int maxBots, int rate, int timeout)
	{
		enqueueCommand(new BaseStationConfig(channel, invert, maxBots, rate, timeout));
		
		this.channel = channel;
		invertPosition = invert;
		this.maxBots = maxBots;
		visionRate = rate;
		this.timeout = timeout;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isPositionInverted()
	{
		return invertPosition;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getVisionRate()
	{
		return visionRate;
	}
	
	
	private void changeNetworkState(ENetworkState newState)
	{
		if (netState == newState)
		{
			return;
		}
		
		if ((netState == ENetworkState.OFFLINE) && (newState == ENetworkState.CONNECTING))
		{
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
		
		if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.ONLINE))
		{
			if (connectTimer != null)
			{
				connectTimer.cancel();
			}
			
			setConfig(channel, invertPosition, maxBots, visionRate, timeout);
			
			// start watchdog
			watchdog.start(this);
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Connected base station");
			
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.CONNECTING))
		{
			for (WifiStats wifiStats : latestStats.getWifiStats())
			{
				notifyBotOffline(wifiStats.getBotId());
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
			for (WifiStats wifiStats : latestStats.getWifiStats())
			{
				notifyBotOffline(wifiStats.getBotId());
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
	
	
	private void notifyNetworkStateChanged(ENetworkState netState)
	{
		synchronized (observers)
		{
			for (IBaseStationObserver observer : observers)
			{
				observer.onNetworkStateChanged(netState);
			}
		}
	}
	
	
	/**
	 * 
	 * @param id
	 * @param cmd
	 */
	private void notifyIncommingBotCommand(BotID id, ACommand cmd)
	{
		synchronized (observers)
		{
			for (IBaseStationObserver observer : observers)
			{
				observer.onIncommingBotCommand(id, cmd);
			}
		}
	}
	
	
	/**
	 * 
	 * @param cmd
	 */
	private void notifyIncommingBaseStationCommand(ACommand cmd)
	{
		synchronized (observers)
		{
			for (IBaseStationObserver observer : observers)
			{
				observer.onIncommingBaseStationCommand(cmd);
			}
		}
	}
	
	
	private void notifyNewBaseStationStats(BaseStationStats stats)
	{
		synchronized (observers)
		{
			for (IBaseStationObserver observer : observers)
			{
				observer.onNewBaseStationStats(stats);
			}
		}
	}
	
	
	private void notifyNewPingDelay(float delay)
	{
		synchronized (observers)
		{
			for (IBaseStationObserver observer : observers)
			{
				observer.onNewPingDelay(delay);
			}
		}
	}
	
	
	private void notifyBotOffline(BotID id)
	{
		synchronized (observers)
		{
			for (IBaseStationObserver observer : observers)
			{
				observer.onBotOffline(id);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the config
	 */
	public HierarchicalConfiguration getConfig()
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		Node node = new Node("baseStation");
		node.addAttribute(new Node("id", key));
		config.setRoot(node);
		config.addProperty("ip", host);
		config.addProperty("localPort", localPort);
		config.addProperty("remotePort", dstPort);
		config.addProperty("active", active);
		config.addProperty("invertPos", invertPosition);
		config.addProperty("visionRate", visionRate);
		config.addProperty("channel", channel);
		config.addProperty("maxBots", maxBots);
		config.addProperty("timeout", 1000);
		
		return config;
	}
	
	
	/**
	 * Set ip config.
	 * 
	 * @param host
	 * @param dstPort
	 * @param localPort
	 */
	public void setIpConfig(String host, int dstPort, int localPort)
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
	
	private class PingThread extends Thread
	{
		private int								id					= 0;
		private int								payloadLength	= 0;
		
		private final Map<Integer, Long>	activePings		= new HashMap<Integer, Long>();
		
		
		/**
		 * @param payloadLength
		 */
		public PingThread(int payloadLength)
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
		public void pongArrived(int id)
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
			
			final float delayPongArrive = (System.nanoTime() - startTime) / 1000000.0f;
			
			notifyNewPingDelay(delayPongArrive);
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
}
