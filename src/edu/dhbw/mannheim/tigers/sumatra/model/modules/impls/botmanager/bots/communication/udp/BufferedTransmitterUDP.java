/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;


/**
 * Transmitter for UDP packets.
 * 
 * @author AndreR
 * 
 */
public class BufferedTransmitterUDP implements ITransmitterUDP
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log			= Logger.getLogger(BufferedTransmitterUDP.class.getName());
	
	private InetAddress							destination	= null;
	private int										destPort		= 0;
	private DatagramSocket						socket		= null;
	private final Queue<ACommand>				sendQueue	= new LinkedList<ACommand>();
	private Sender									sender		= null;
	private final Statistics					stats			= new Statistics();
	private final Map<Integer, Long>			buffer		= new HashMap<Integer, Long>();
	private final Map<Integer, ACommand>	lateCmds		= new HashMap<Integer, ACommand>();
	// in [ns]
	private static final long					minDelay		= 1000000;
	private boolean								legacy		= false;
	
	private final Lock							lock			= new ReentrantLock();
	private final Condition						wakeup		= lock.newCondition();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void enqueueCommand(ACommand cmd)
	{
		synchronized (sendQueue)
		{
			sendQueue.add(cmd);
		}
		
		try
		{
			lock.lock();
			wakeup.signalAll();
		} finally
		{
			lock.unlock();
		}
	}
	
	
	@Override
	public void start()
	{
		if (sender != null)
		{
			stop();
		}
		
		stats.reset();
		
		sender = new Sender();
		
		sender.start();
	}
	
	
	@Override
	public void stop()
	{
		if (sender == null)
		{
			return;
		}
		
		while (!sendQueue.isEmpty())
		{
			try
			{
				log.debug(sender.isInterrupted() + " BLUBB");
				Thread.sleep(10);
			}
			
			catch (final InterruptedException e)
			{
			}
		}
		
		sender.disable();
		sender.interrupt();
		try
		{
			sender.join(100);
		} catch (final InterruptedException err)
		{
		}
		
		sender = null;
	}
	
	
	@Override
	public void setSocket(DatagramSocket newSocket) throws IOException
	{
		boolean start = false;
		
		if (sender != null)
		{
			stop();
			start = true;
		}
		
		socket = newSocket;
		
		if (start)
		{
			start();
		}
	}
	
	
	@Override
	public void setDestination(InetAddress dstIp, int dstPort)
	{
		destination = dstIp;
		destPort = dstPort;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Statistics getStats()
	{
		return stats;
	}
	
	
	/**
	 * @return the legacy
	 */
	public boolean isLegacy()
	{
		return legacy;
	}
	
	
	/**
	 * @param legacy the legacy to set
	 */
	@Override
	public void setLegacy(boolean legacy)
	{
		this.legacy = legacy;
	}
	
	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class Sender extends Thread
	{
		private boolean	active;
		
		
		@Override
		public void run()
		{
			active = true;
			
			if (socket == null)
			{
				log.error("Cannot start a transmitter on a null socket");
				return;
			}
			
			Thread.currentThread().setName("Buffered Transmitter UDP " + socket.getInetAddress() + ":" + socket.getPort());
			
			ACommand cmd;
			
			while (active)
			{
				cmd = null;
				
				do
				{
					synchronized (sendQueue)
					{
						cmd = sendQueue.poll();
					}
					
					if (cmd != null)
					{
						final int cmdId = cmd.getType().getId();
						
						// check time constraints
						final Long lastSendTime = buffer.get(cmdId);
						if (lastSendTime != null)
						{
							if (lastSendTime > (System.nanoTime() - minDelay))
							{
								// latest command of this type was less than <minDelay> ns ago
								lateCmds.put(cmdId, cmd);
							} else
							{
								// waited at least minDelay ms
								buffer.put(cmdId, System.nanoTime());
								processCommand(cmd);
							}
						} else
						{
							buffer.put(cmdId, System.nanoTime());
							processCommand(cmd);
						}
					}
				} while (cmd != null);
				
				// check late commands
				long earliestTime = System.nanoTime();
				
				for (final Iterator<ACommand> iter = lateCmds.values().iterator(); iter.hasNext();)
				{
					final ACommand lateCmd = iter.next();
					
					final long lastSendTime = buffer.get(lateCmd.getType().getId());
					
					// command now waited <minDelay>
					if (lastSendTime <= (System.nanoTime() - minDelay))
					{
						synchronized (sendQueue)
						{
							sendQueue.add(lateCmd);
						}
						
						iter.remove();
					} else
					{
						if (lastSendTime < earliestTime)
						{
							earliestTime = lastSendTime;
						}
					}
				}
				
				synchronized (sendQueue)
				{
					if (!sendQueue.isEmpty())
					{
						continue;
					}
				}
				
				try
				{
					lock.lock();
					
					if (lateCmds.isEmpty())
					{
						wakeup.await();
					} else
					{
						wakeup.awaitNanos(System.nanoTime() - earliestTime);
					}
				} catch (final InterruptedException err)
				{
				} finally
				{
					lock.unlock();
				}
			}
			
			sendQueue.clear();
			lateCmds.clear();
		}
		
		
		/**
		 */
		public void disable()
		{
			active = false;
		}
		
		
		private void processCommand(ACommand cmd)
		{
			// process cmd
			try
			{
				final byte data[] = CommandFactory.getInstance().encode(cmd, legacy);
				
				final DatagramPacket packet = new DatagramPacket(data, data.length, destination, destPort);
				socket.send(packet);
				
				stats.packets++;
				// Ethernet + IP + UDP header length
				stats.raw += data.length + 54;
				stats.payload += data.length;
			} catch (final IOException e)
			{
				log.warn("Error while processing command", e);
				return;
			}
		}
	}
}
