/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.06.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sim.util.network.NetworkUtility;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.MulticastTransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMulticastUpdateAllV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemAnnouncement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetIdentity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;

/**
 * Perform multicast handling and capsulation.
 * 
 * @author AndreR
 * 
 */
public class MulticastDelegate implements IMulticastDelegate
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger log = Logger.getLogger(getClass());
	
	private Map<Integer, TigerMotorMoveV2>		allMoves				= new HashMap<Integer, TigerMotorMoveV2>();
	private Map<Integer, TigerKickerKickV2>	allKicks				= new HashMap<Integer, TigerKickerKickV2>();
	private Map<Integer, TigerDribble>			allDribbles			= new HashMap<Integer, TigerDribble>();

	private long										updateAllSleep	= 20;
	private UpdateAllThread							updateAllThread	= null;

	private MulticastTransceiverUDP				mcastTransceiver	= new MulticastTransceiverUDP();
	private final NetworkUtility					networkUtil			= new NetworkUtility();

	private List<Integer>							activeBots			= new ArrayList<Integer>();
	private final ReentrantReadWriteLock		activeBotsLock		= new ReentrantReadWriteLock(true);	// fair lock

	private List<Integer>							onFieldBots			= new ArrayList<Integer>();
	private final ReentrantReadWriteLock		onFieldBotsLock	= new ReentrantReadWriteLock(true);	// fair lock

	private ABotManager								botmanager			= null;
	
	private final MulticastHandler				mcastHandler		= new MulticastHandler();


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public MulticastDelegate(SubnodeConfiguration subnodeConfiguration, ABotManager botmanager)
	{
		this.botmanager = botmanager;
		
		String group = subnodeConfiguration.getString("multicast.group", "225.42.42.42");
		int local = subnodeConfiguration.getInt("multicast.localPort", 10040);
		int remote = subnodeConfiguration.getInt("multicast.remotePort", 10041);

		// Detect the correct interface for multicast-discovery of the Tiger-bots
		String botNetworkStr = subnodeConfiguration.getString("multicast.interface", "192.168.7.0");
		NetworkInterface nif = networkUtil.chooseNetworkInterface(botNetworkStr, 3);
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
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setGroupedMove(int botId, TigerMotorMoveV2 move)
	{
		allMoves.put(botId, move);
	}

	@Override
	public void setGroupedKick(int botId, TigerKickerKickV2 kick)
	{
		allKicks.put(botId, kick);
	}

	@Override
	public void setGroupedDribble(int botId, TigerDribble dribble)
	{
		allDribbles.put(botId, dribble);
	}
	
	public void enable(boolean enable)
	{
		if(enable)
		{
			enable(false);
			
			mcastTransceiver.open();
			
			mcastTransceiver.addObserver(mcastHandler);
			
			updateAllThread = new UpdateAllThread();
			updateAllThread.start();
		}
		else
		{
			if(updateAllThread != null)
			{
				updateAllThread.terminate();
				updateAllThread = null;
			}
			
			mcastTransceiver.removeObserver(mcastHandler);
			
			mcastTransceiver.close();
		}
	}
	
	public boolean isEnabled()
	{
		return updateAllThread != null;
	}
	
	public void setUpdateAllSleepTime(long time)
	{
		updateAllSleep = time;
	}
	
	public long getUpdateAllSleepTime()
	{
		return updateAllSleep;
	}
	
	public void setOnFieldBots(List<Integer> field)
	{
		Lock writeLock = onFieldBotsLock.writeLock();
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
	public boolean setMulticast(int botId, boolean enable)
	{
		Lock writeLock = activeBotsLock.writeLock();
		try
		{
			writeLock.lock();
			
			if(enable)
			{
				if(activeBots.contains(new Integer(botId)))
				{
					return true;
				}
				else
				{
					if(activeBots.size() >= 5)
					{
						log.error("5 Multicast slots already set!");
						return false;
					}

					activeBots.add(new Integer(botId));
				}
			}
			else
			{
				activeBots.remove(new Integer(botId));
			}
		} 
		finally
		{
			writeLock.unlock();
		}
		
		return true;
	}
	
	public ITransceiverUDP getTransceiver()
	{
		return mcastTransceiver;
	}
	
	@Override
	public void setIdentity(TigerBot bot)
	{
		TigerSystemSetIdentity ident = new TigerSystemSetIdentity();
		
		ident.setBotId(bot.getBotId());
		ident.setCpuId(bot.getCpuId());
		ident.setIp(bot.getIp());
		ident.setMac(bot.getMac());
		ident.setPort(bot.getPort());
		ident.setServerPort(bot.getServerPort());
		
		mcastTransceiver.enqueueCommand(ident);
	}

	@Override
	public void removeIdentity(TigerBot bot)
	{
		TigerSystemSetIdentity ident = new TigerSystemSetIdentity();
		
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
		private boolean			active			= true;
		private List<Integer>	lastOnFieldBots	= new ArrayList<Integer>();
		
		public UpdateAllThread()
		{
			setName("UpdateAll Multicast");
			setPriority(Thread.MAX_PRIORITY);
		}
		
		@Override
		public void run()
		{
			active = true;
			
			long lastCall = System.nanoTime();
			
			final List<Integer> currentActiveBots = new ArrayList<Integer>();
			final List<Integer> currentOnFieldBots = new ArrayList<Integer>();
			
			while (active)
			{
				long now = System.nanoTime();
				final long diff = now - lastCall;
				if (diff > TimeUnit.MILLISECONDS.toNanos(200))
				{
					log.warn("UpdateThread slept for more then 200ms! (" + TimeUnit.NANOSECONDS.toMillis(diff) + ")");
				}
				lastCall = now;
				
				long startTime = System.nanoTime();
				
				TigerMulticastUpdateAllV2 update = new TigerMulticastUpdateAllV2();
				
				currentActiveBots.clear();
				currentOnFieldBots.clear();

				Lock readLock = activeBotsLock.readLock();
				try
				{
					readLock.lock();
					currentActiveBots.addAll(activeBots);
				}
				finally
				{
					readLock.unlock();
				}
				
				readLock = onFieldBotsLock.readLock();
				try
				{
					readLock.lock();
					currentOnFieldBots.addAll(onFieldBots);
				}
				finally
				{
					readLock.unlock();
				}
				
				lastOnFieldBots.removeAll(currentOnFieldBots);
				for (Integer i : lastOnFieldBots)
				{
					TigerBot tiger = (TigerBot)botmanager.getAllBots().get(i);	// This bot left the field!
					
					if(tiger == null)
					{
						continue;
					}
					
					if(tiger.getOofCheck())
					{
						// Out-Of-Field check enabled
						boolean updateAllSave = tiger.getUseUpdateAll();
						tiger.setUseUpdateAll(false);
						
						tiger.execute(new TigerMotorMoveV2(new Vector2f(0, 0), 0));
						tiger.execute(new TigerDribble(0));
						
						tiger.setUseUpdateAll(updateAllSave);
						log.info("Bot " + i + " vanished from WP, sent Stop commands");
					}
				}
				lastOnFieldBots.clear();
				lastOnFieldBots.addAll(currentOnFieldBots);
				
				for (int i = 0; i < currentActiveBots.size(); i++)
				{
					Integer botId = currentActiveBots.get(i);
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
					
					update.setId(i, botId);
				}
				
				mcastTransceiver.enqueueCommand(update);
				
				final long stopTime = System.nanoTime();
				final long duration = stopTime - startTime;
				final long sleepTotal = updateAllSleep * 1000000 - duration;
				
				if (sleepTotal < 0)
				{
					log.fatal("sleep negative!");
				} else
				{
					ThreadUtil.parkNanosSafe(sleepTotal);
				}
			}
		}
		
		public synchronized void terminate()
		{
			active = false;
			
			this.interrupt();
			
			try
			{
				this.join();
			} catch (InterruptedException err)
			{
			}
		}
	}
	
	private class MulticastHandler implements ITransceiverUDPObserver
	{
		@Override
		public void onIncommingCommand(ACommand cmd)
		{
			if (cmd.getCommand() == CommandConstants.CMD_SYSTEM_ANNOUNCEMENT)
			{
				TigerSystemAnnouncement announce = (TigerSystemAnnouncement) cmd;
				
				for(ABot bot : botmanager.getAllBots().values())
				{
					if(bot.getType() != EBotType.TIGER)
					{
						continue;
					}
					
					TigerBot tiger = (TigerBot)bot;
					
					if(tiger.getNetworkState() == ENetworkState.OFFLINE)
					{
						continue;
					}
					
//					log.info("Received system announcement with CPU ID: " + announce.getCpuId());
					
					if(tiger.getCpuId().equals(announce.getCpuId()))
					{
						log.info("Found id: " + tiger.getCpuId());
						
						setIdentity(tiger);
						
						return;
					}
				}
			}
		}

		@Override
		public void onOutgoingCommand(ACommand cmd)
		{
		}
	}
}
