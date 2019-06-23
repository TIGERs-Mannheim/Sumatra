/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.08.2010
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
 * 
 * @author AndreR
 * 
 */
public class TransmitterTCP
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private OutputStream outStream = null;
	private BlockingQueue<ACommand> sendQueue = new LinkedBlockingQueue<ACommand>();
	private List<ITransmitterTCPObserver> observers = new ArrayList<ITransmitterTCPObserver>();
	private Thread encoderThread = null;
	private Statistics stats = new Statistics();

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TransmitterTCP()
	{
	}
	
	public TransmitterTCP(Socket socket) throws IOException
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
		public void run()
		{
			boolean lost = false;
			
			Thread.currentThread().setName("Transmitter");
			
			while(!Thread.currentThread().isInterrupted())
			{
				ACommand cmd;
				
				try
				{
					cmd = sendQueue.take();
				}
				catch(InterruptedException e)
				{
					Thread.currentThread().interrupt();
					continue;
				}
				
				try
				{
					byte data[] = cmd.getTransferData();
					outStream.write(data);
					
					stats.packets++;
					stats.raw += data.length;
					stats.payload += cmd.getDataLength() + CommandConstants.HEADER_SIZE;
				}
				catch (IOException e)
				{
					Thread.currentThread().interrupt();
					lost = true;
					continue;
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
	}
}
