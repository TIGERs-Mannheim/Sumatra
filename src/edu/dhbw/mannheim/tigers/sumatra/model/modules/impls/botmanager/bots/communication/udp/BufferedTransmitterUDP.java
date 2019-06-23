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
	private final Logger log = Logger.getLogger(getClass());
	
	private InetAddress destination = null;
	private int destPort = 0;
	private DatagramSocket socket = null;
	private Queue<ACommand> sendQueue = new LinkedList<ACommand>();
	private Sender sender = null;
	private Statistics stats = new Statistics();
	private Map<Integer, Long> buffer = new HashMap<Integer, Long>();
	private Map<Integer, ACommand> lateCmds = new HashMap<Integer, ACommand>();
	private long minDelay = 1000000;	// in [ns]
	
	private Lock lock = new ReentrantLock();
	private Condition wakeup = lock.newCondition();
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void enqueueCommand(ACommand cmd)
	{
		synchronized(sendQueue)
		{
			sendQueue.add(cmd);
		}

		try
		{
			lock.lock();
			
			wakeup.signalAll();
		}
		finally
		{
			lock.unlock();
		}
	}
	
	@Override
	public void start()
	{
		if(sender != null)
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
		if(sender == null)
		{
			return;
		}
		
		while(!sendQueue.isEmpty())
		{
			try
			{
				Thread.sleep(10);
			}
			
			catch(InterruptedException e)
			{
			}
		}
		
		sender.disable();
		sender.interrupt();
		try
		{
			sender.join(100);
		}
		catch (InterruptedException err)
		{
		}
		
		sender = null;
	}

	@Override
	public void setSocket(DatagramSocket newSocket) throws IOException
	{
		boolean start = false;
		
		if(sender != null)
		{
			stop();
			start = true;
		}
		
		socket = newSocket;

		if(start)
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

	// --------------------------------------------------------------------------
	// --- Threads --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class Sender extends Thread
	{
		private boolean active;
		
		public void run()
		{
			active = true;
			
			Thread.currentThread().setName("Transmitter UDP");
			
			if(socket == null)
			{
				log.error("Cannot start a transmitter on a null socket");
				return;
			}
			
			ACommand cmd;
			
			while(active)
			{
				cmd = null;
				
				do
				{
					synchronized(sendQueue)
					{
						cmd = sendQueue.poll();
					}
					
					if(cmd != null)
					{
						int cmdId = cmd.getCommand();
						
						// check time constraints
						Long lastSendTime = buffer.get(cmdId);
						if(lastSendTime != null)
						{
							if(lastSendTime > System.nanoTime() - minDelay)
							{
								// latest command of this type was less than <minDelay> ns ago
								lateCmds.put(cmdId, cmd);
							}
							else
							{
								// waited at least minDelay ms
								buffer.put(cmdId, System.nanoTime());
								processCommand(cmd);
							}
						}
						else
						{
							buffer.put(cmdId, System.nanoTime());
							processCommand(cmd);
						}					
					}
				}
				while(cmd != null);
				
				// check late commands
				long earliestTime = System.nanoTime();
				
				for(Iterator<ACommand> iter = lateCmds.values().iterator(); iter.hasNext(); )
				{
					ACommand lateCmd = iter.next();
					
					long lastSendTime = buffer.get(lateCmd.getCommand());
					
					if(lastSendTime <= System.nanoTime() - minDelay)	// command now waited <minDelay>
					{
						synchronized(sendQueue)
						{
							sendQueue.add(lateCmd);
						}
						
						iter.remove();
					}
					else
					{
						if(lastSendTime < earliestTime)
							earliestTime = lastSendTime;
					}
				}
				
				synchronized(sendQueue)
				{
					if(!sendQueue.isEmpty())
					{
						continue;
					}
				}
				
				try
				{
					lock.lock();
					
					if(lateCmds.isEmpty())
					{
						wakeup.await();
					}
					else
					{
						wakeup.awaitNanos(System.nanoTime() - earliestTime);
					}
				} 
				catch (InterruptedException err)
				{
				}
				finally
				{
					lock.unlock();
				}
			}
			
			sendQueue.clear();
			lateCmds.clear();
		}
		
		public void disable()
		{
			active = false;
		}

		private void processCommand(ACommand cmd)
		{
			// process cmd
			try
			{
				byte data[] = cmd.getTransferData();
				
				DatagramPacket packet = new DatagramPacket(data, data.length, destination, destPort);
				socket.send(packet);
				
//				log.debug("Sent: " + cmd.getCommand());
				
				stats.packets++;
				stats.raw += data.length + 54;	// Ethernet + IP + UDP header length
				stats.payload += data.length;
			}
			catch (IOException e)
			{
				active = false;
				return;
			}
		}
	}
}
