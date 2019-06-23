/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.01.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

/**
 * Low level bot transmitter.
 * This transmitter waits for some packets to arrive and sends them together.
 * 
 * @author AndreR
 * 
 */
public class BurstTransmitterTCP
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private OutputStream outStream = null;
	private BlockingQueue<ACommand> sendQueue = new LinkedBlockingQueue<ACommand>();
	private List<ITransmitterTCPObserver> observers = new ArrayList<ITransmitterTCPObserver>();
	private Thread encoderThread = null;
	private Statistics stats = new Statistics();
	
	private static final int SLEEP_TIME = 10;
	private static final int MIN_PACKETS = 10;
	private static final int MAX_DELAY = 50;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BurstTransmitterTCP()
	{
	}
	
	public BurstTransmitterTCP(Socket socket) throws IOException
	{
		setSocket(socket);
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ITransmitterTCPObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}
	}
	
	public void removeObserver(ITransmitterTCPObserver o)
	{
		synchronized(observers)
		{
			observers.remove(o);
		}
	}
	
	public void enqueueCommand(ACommand cmd)
	{
		try
		{
			sendQueue.put(cmd);
		}
		catch (InterruptedException err)
		{
		}
	}
	
	public void start()
	{
		if(encoderThread != null)
		{
			stop();
		}
		
		stats.reset();
		
		encoderThread = new Thread(new Encoder());
		
		encoderThread.start();
	}
	
	public void stop()
	{
		if(encoderThread == null)
		{
			return;
		}
		
		encoderThread.interrupt();
		try
		{
			encoderThread.join(100);
		}
		catch (InterruptedException err)
		{
		}
		
		encoderThread = null;
		sendQueue.clear();
	}

	public void setSocket(Socket socket) throws IOException
	{
		boolean start = false;
		if(encoderThread != null)
		{
			start = true;
			stop();
		}
		
		outStream = socket.getOutputStream();
		
		if(start)
		{
			start();
		}
	}

	public boolean isConnected()
	{
		if(encoderThread != null)
		{
			return true;
		}
		
		return false;
	}
	
	public Statistics getStats()
	{
		return stats;
	}
	
	protected void connectionLost()
	{
		stop();
		
		synchronized(observers)
		{
			for(ITransmitterTCPObserver o : observers)
			{
				o.onConnectionLost();
			}
		}
	}

	protected class Encoder implements Runnable
	{
		private boolean lost = false;
		
		public void run()
		{
			Thread.currentThread().setName("Transmitter");
			
			long lastSendTime = System.nanoTime()/1000000;
			
			while(!Thread.currentThread().isInterrupted())
			{
				long time = System.nanoTime()/1000000;
				
				if(sendQueue.size() > MIN_PACKETS || (time-lastSendTime) > MAX_DELAY)
				{
					sendData();
					
					lastSendTime = time;
				}
				else
				{
					try
					{
						Thread.sleep(SLEEP_TIME);
					}
					catch (InterruptedException err)
					{
						Thread.currentThread().interrupt();
					}
				}
			}
			
			try
			{
				outStream.close();
			}
			catch (IOException e)
			{
			}
			
			if(lost)
			{
				connectionLost();
			}
		}
		
		private void sendData()
		{
			ArrayList<ACommand> cmds = new ArrayList<ACommand>();
			
			sendQueue.drainTo(cmds);
			
			int dataLength = 0;
			for(ACommand cmd : cmds)
				dataLength += cmd.getTransferData().length;
			
			byte data[] = new byte[dataLength];
			
			int offset = 0;
			for(ACommand cmd : cmds)
			{
				byte cmdData[] = cmd.getTransferData();
				System.arraycopy(cmdData, 0, data, offset, cmdData.length);
				offset += cmdData.length;
				
				stats.packets++;
				stats.raw += cmdData.length;
				stats.payload += cmd.getDataLength() + CommandConstants.HEADER_SIZE;
			}
			
			try
			{
				outStream.write(data);
			}
			catch (IOException e)
			{
				Thread.currentThread().interrupt();
				lost = true;
			}
		}
	}
}
