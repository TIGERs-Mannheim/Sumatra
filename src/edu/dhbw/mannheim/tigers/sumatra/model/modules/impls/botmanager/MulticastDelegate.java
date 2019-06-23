/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.06.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.MulticastTransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMulticastUpdateAllV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemAnnouncement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetIdentity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * Perform multicast handling and capsulation.
 * 
 * @author AndreR
 */
public class MulticastDelegate implements IMulticastDelegate
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log					= Logger.getLogger(MulticastDelegate.class.getName());
	
	private final Map<BotID, TigerMotorMoveV2>	allMoves				= new ConcurrentHashMap<BotID, TigerMotorMoveV2>();
	private final Map<BotID, TigerKickerKickV2>	allKicks				= new ConcurrentHashMap<BotID, TigerKickerKickV2>();
	private final Map<BotID, TigerDribble>			allDribbles			= new ConcurrentHashMap<BotID, TigerDribble>();
	
	/** [ms] */
	private long											updateAllSleep		= 20;
	private UpdateAllThread								updateAllThread	= null;
	
	private final MulticastTransceiverUDP			mcastTransceiver	= new MulticastTransceiverUDP(true);
	
	private final List<BotID>							activeBots			= new ArrayList<BotID>();
	// fair lock
	private final ReentrantReadWriteLock			activeBotsLock		= new ReentrantReadWriteLock(true);
	
	private final List<BotID>							onFieldBots			= new ArrayList<BotID>();
	// fair lock
	private final ReentrantReadWriteLock			onFieldBotsLock	= new ReentrantReadWriteLock(true);
	
	private ABotManager									botmanager			= null;
	
	private final MulticastHandler					mcastHandler		= new MulticastHandler();
	
	private final HierarchicalConfiguration		config;
	
	private static final int							MAX_BOTS				= 6;
	
	private final int										id;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 * @param botmanager
	 */
	public MulticastDelegate(final HierarchicalConfiguration subnodeConfiguration, final ABotManager botmanager)
	{
		this.botmanager = botmanager;
		config = subnodeConfiguration;
		id = subnodeConfiguration.getInt("[@id]");
		
		final String group = subnodeConfiguration.getString("group", "225.42.42.42");
		final int local = subnodeConfiguration.getInt("localPort", 10040);
		final int remote = subnodeConfiguration.getInt("remotePort", 10041);
		
		// Detect the correct interface for multicast-discovery of the Tiger-bots
		final String botNetworkStr = subnodeConfiguration.getString("interface", "192.168.7.0");
		final NetworkInterface nif = NetworkUtility.chooseNetworkInterface(botNetworkStr, 3);
		if (nif == null)
		{
			log.error("No proper nif for bot-detection in network '" + botNetworkStr + "' found!");
		} else
		{
			log.info("Chose nif for bot-detection: " + nif.getDisplayName() + ".");
		}
		
		// Configure MulticastTxRx
		mcastTransceiver.setNetworkInterface(nif);
		mcastTransceiver.setDestination(group, remote);
		mcastTransceiver.setLocalPort(local);
		
		updateAllSleep = (subnodeConfiguration.getLong("updateAll.sleep", 20));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setGroupedMove(final BotID botId, final TigerMotorMoveV2 move)
	{
		allMoves.put(botId, move);
	}
	
	
	@Override
	public void setGroupedKick(final BotID botId, final TigerKickerKickV2 kick)
	{
		allKicks.put(botId, kick);
	}
	
	
	@Override
	public void setGroupedDribble(final BotID botId, final TigerDribble dribble)
	{
		allDribbles.put(botId, dribble);
	}
	
	
	/**
	 * @param enable
	 */
	public void enable(final boolean enable)
	{
		if (enable)
		{
			enable(false);
			
			mcastTransceiver.open();
			
			mcastTransceiver.addObserver(mcastHandler);
			
			updateAllThread = new UpdateAllThread();
			updateAllThread.start();
		} else
		{
			if (updateAllThread != null)
			{
				updateAllThread.terminate();
				updateAllThread = null;
			}
			
			mcastTransceiver.removeObserver(mcastHandler);
			
			mcastTransceiver.close();
		}
	}
	
	
	/**
	 * @return
	 */
	public boolean isEnabled()
	{
		return updateAllThread != null;
	}
	
	
	/**
	 * @return the config
	 */
	public HierarchicalConfiguration getConfig()
	{
		return config;
	}
	
	
	/**
	 * @param field
	 */
	public void setOnFieldBots(final Set<BotID> field)
	{
		final Lock writeLock = onFieldBotsLock.writeLock();
		try
		{
			writeLock.lock();
			
			onFieldBots.clear();
			onFieldBots.addAll(field);
		}
		
		finally
		{
			writeLock.unlock();
		}
	}
	
	
	@Override
	public boolean setMulticast(final BotID botId, final boolean enable)
	{
		final Lock writeLock = activeBotsLock.writeLock();
		try
		{
			writeLock.lock();
			
			if (enable)
			{
				if (activeBots.contains(botId))
				{
					return true;
				}
				if (activeBots.size() >= MAX_BOTS)
				{
					log.error(MAX_BOTS + " Multicast slots already set!");
					return false;
				}
				
				activeBots.add(botId);
			} else
			{
				activeBots.remove(botId);
			}
		} finally
		{
			writeLock.unlock();
		}
		
		return true;
	}
	
	
	/**
	 * @return
	 */
	public ITransceiverUDP getTransceiver()
	{
		return mcastTransceiver;
	}
	
	
	@Override
	public void setIdentity(final TigerBot bot)
	{
		final TigerSystemSetIdentity ident = new TigerSystemSetIdentity();
		
		ident.setBotId(bot.getBotID().getNumber());
		ident.setCpuId(bot.getCpuId());
		ident.setIp(bot.getIp());
		ident.setMac(bot.getMac());
		ident.setPort(bot.getPort());
		ident.setServerPort(bot.getServerPort());
		
		mcastTransceiver.enqueueCommand(ident);
	}
	
	
	@Override
	public void removeIdentity(final TigerBot bot)
	{
		final TigerSystemSetIdentity ident = new TigerSystemSetIdentity();
		
		ident.setBotId(255);
		ident.setCpuId(bot.getCpuId());
		ident.setIp(bot.getIp());
		ident.setMac(bot.getMac());
		ident.setPort(bot.getPort());
		ident.setServerPort(bot.getServerPort());
		
		mcastTransceiver.enqueueCommand(ident);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- classes --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class UpdateAllThread extends Thread
	{
		private boolean				active				= true;
		private final List<BotID>	lastOnFieldBots	= new ArrayList<BotID>();
		
		
		/**
		 */
		public UpdateAllThread()
		{
			setName("UpdateAll Multicast " + id);
		}
		
		
		@Override
		public void run()
		{
			active = true;
			
			long lastCall = SumatraClock.nanoTime();
			
			final List<BotID> currentActiveBots = new ArrayList<BotID>();
			final List<BotID> currentOnFieldBots = new ArrayList<BotID>();
			
			while (active)
			{
				final long startTime = SumatraClock.nanoTime();
				final long diff = startTime - lastCall;
				if (diff > TimeUnit.MILLISECONDS.toNanos(200))
				{
					log.warn("UpdateThread slept for more then 200ms! (" + TimeUnit.NANOSECONDS.toMillis(diff) + ")");
				}
				lastCall = startTime;
				
				final TigerMulticastUpdateAllV2 update = new TigerMulticastUpdateAllV2();
				
				currentActiveBots.clear();
				currentOnFieldBots.clear();
				
				Lock readLock = activeBotsLock.readLock();
				try
				{
					readLock.lock();
					currentActiveBots.addAll(activeBots);
				} finally
				{
					readLock.unlock();
				}
				
				readLock = onFieldBotsLock.readLock();
				try
				{
					readLock.lock();
					currentOnFieldBots.addAll(onFieldBots);
				} finally
				{
					readLock.unlock();
				}
				
				lastOnFieldBots.removeAll(currentOnFieldBots);
				for (final BotID botId : lastOnFieldBots)
				{
					ABot bot = botmanager.getAllBots().get(botId);
					if ((bot == null) || (bot.getType() != EBotType.TIGER))
					{
						continue;
					}
					
					// This bot left the field!
					final TigerBot tiger = (TigerBot) botmanager.getAllBots().get(botId);
					
					if (tiger == null)
					{
						continue;
					}
					
					if (tiger.getOofCheck())
					{
						// Out-Of-Field check enabled
						final boolean updateAllSave = tiger.getUseUpdateAll();
						tiger.setUseUpdateAll(false);
						
						tiger.execute(new TigerMotorMoveV2(new Vector2f(0, 0), 0));
						tiger.execute(new TigerDribble(0));
						
						tiger.setUseUpdateAll(updateAllSave);
						log.info("Bot " + botId + " vanished from WP, sent Stop commands");
					}
				}
				lastOnFieldBots.clear();
				lastOnFieldBots.addAll(currentOnFieldBots);
				
				for (int i = 0; i < currentActiveBots.size(); i++)
				{
					final BotID botId = currentActiveBots.get(i);
					if (allMoves.get(botId) != null)
					{
						update.setMove(i, allMoves.get(botId));
						allMoves.remove(botId);
					}
					
					if (allKicks.get(botId) != null)
					{
						update.setKick(i, allKicks.get(botId));
						allKicks.remove(botId);
					}
					
					if (allDribbles.get(botId) != null)
					{
						update.setDribble(i, allDribbles.get(botId));
					}
					
					update.setId(i, botId.getNumber());
				}
				
				mcastTransceiver.enqueueCommand(update);
				
				final long stopTime = SumatraClock.nanoTime();
				final long duration = stopTime - startTime;
				final long sleepTotal = TimeUnit.MILLISECONDS.toNanos(updateAllSleep) - duration;
				
				if (sleepTotal < 0)
				{
					log.warn("MCast-Delegate needed more then 200ms! (+" + TimeUnit.NANOSECONDS.toMillis(-sleepTotal) + ")");
				} else
				{
					ThreadUtil.parkNanosSafe(sleepTotal);
				}
			}
		}
		
		
		/**
		 */
		public synchronized void terminate()
		{
			active = false;
			
			interrupt();
			
			try
			{
				this.join();
			} catch (final InterruptedException err)
			{
			}
		}
	}
	
	private class MulticastHandler implements ITransceiverUDPObserver
	{
		@Override
		public void onIncommingCommand(final ACommand cmd)
		{
			if (cmd.getType() == ECommand.CMD_SYSTEM_ANNOUNCEMENT)
			{
				final TigerSystemAnnouncement announce = (TigerSystemAnnouncement) cmd;
				
				for (final ABot bot : botmanager.getAllBots().values())
				{
					if ((bot.getType() != EBotType.TIGER) && (bot.getType() != EBotType.GRSIM))
					{
						continue;
					}
					
					final TigerBot tiger = (TigerBot) bot;
					
					if (tiger.getNetworkState() == ENetworkState.OFFLINE)
					{
						continue;
					}
					
					// log.info("Received system announcement with CPU ID: " + announce.getCpuId());
					
					if (tiger.getCpuId().equals(announce.getCpuId()))
					{
						log.info("Found id: " + tiger.getCpuId());
						
						setIdentity(tiger);
						
						return;
					}
				}
			}
		}
		
		
		@Override
		public void onOutgoingCommand(final ACommand cmd)
		{
		}
	}
	
	
	/**
	 * @return
	 */
	public long getUpdateAllSleepTime()
	{
		return updateAllSleep;
	}
	
	
	/**
	 * @param time
	 */
	public void setUpdateAllSleepTime(final long time)
	{
		updateAllSleep = time;
		config.setProperty("updateAll.sleep", time);
	}
}
