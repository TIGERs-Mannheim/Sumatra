/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;

public class ReceiverTCP
{
	protected InputStream inStream = null;
	protected List<IReceiverTCPObserver> observers = new ArrayList<IReceiverTCPObserver>();
	private Logger log = Logger.getLogger(getClass());
	private Thread decoderThread = null;
	private Statistics stats = new Statistics();
	
	public ReceiverTCP()
	{
	}

	public ReceiverTCP(Socket socket) throws IOException
	{
		setSocket(socket);
	}

	public void addObserver(IReceiverTCPObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}
	}

	public void removeObserver(IReceiverTCPObserver o)
	{
		synchronized(observers)
		{
			observers.remove(o);
		}
	}

	public void start()
	{
		if(decoderThread != null)
		{
			stop();
		}
		
		stats.reset();
		
		decoderThread = new Thread(new Decoder());

		decoderThread.start();
	}
	
	public void stop()
	{
		if(decoderThread == null)
		{
			return;
		}
		
		decoderThread.interrupt();
		try
		{
			decoderThread.join(100);
		}
		catch (InterruptedException err)
		{
		}
		
		decoderThread = null;
	}
	
	public void setSocket(Socket socket) throws IOException
	{
		boolean start = false;
		if(decoderThread != null)
		{
			start = true;
			stop();
		}
		
		inStream = socket.getInputStream();
		
		if(start)
		{
			start();
		}
	}
	
	public boolean isConnected()
	{
		if(decoderThread != null)
		{
			return true;
		}
		
		return false;
	}
	
	public Statistics getStats()
	{
		return stats;
	}
	
	protected void newCommand(ACommand cmd)
	{
		stats.packets++;
		
		synchronized(observers)
		{
			for (IReceiverTCPObserver o : observers)
			{
				o.onNewCommand(cmd);
			}
		}
	}
	
	protected void connectionLost()
	{
		stop();
		
		synchronized(observers)
		{
			for(IReceiverTCPObserver o : observers)
			{
				o.onConnectionLost();
			}
		}
	}

	protected class Decoder implements Runnable
	{
		public void run()
		{
			Thread.currentThread().setName("Receiver");

			byte header[] = new byte[CommandConstants.HEADER_SIZE];
			int headerPos = 0;

			byte body[] = null;
			int bodyPos = 0;

			boolean waitForHeader = true;
			boolean waitForData = false;
			boolean nextIsCoded = false;
			ACommand cmd = null;
			
			boolean lost = false;

			while (!Thread.currentThread().isInterrupted())
			{
				try
				{
					int c = inStream.read();
					if (c == -1)
					{
						throw new IOException();
					}
					
					stats.raw++;
					
					if(c == 0x7E)	//start flag
					{
						if(waitForData)
						{
							log.warn("Packet of cmd type: 0x" + Integer.toHexString(cmd.getCommand()) + " did not complete");
						}
						
						waitForHeader = true;
						waitForData = false;
						header = new byte[CommandConstants.HEADER_SIZE];
						headerPos = 0;
						
						continue;
					}
					
					if(c == 0x7D)	//escape flag
					{
						nextIsCoded = true;
						continue;
					}
					
					if(nextIsCoded)
					{
						nextIsCoded = false;
						
						if(c == 0x5E)
						{
							c = 0x7E;
						}
						
						if(c == 0x5D)
						{
							c = 0x7D;
						}
					}
					
					stats.payload++;

					if (waitForHeader)
					{
						header[headerPos++] = (byte) c;

						if (headerPos < CommandConstants.HEADER_SIZE)
						{
							continue;
						}

						// header is complete
						cmd = CommandFactory.createEmptyPacket(header);
						headerPos = 0;
						header = new byte[CommandConstants.HEADER_SIZE];
						
						if(cmd == null)	//unknown command
						{
							continue;
						}

						if (cmd.getDataLength() == 0) // packet already complete
						{
							newCommand(cmd);
						}
						else
						{
							waitForHeader = false;
							waitForData = true;
							bodyPos = 0;
							body = new byte[cmd.getDataLength()];
						}

						continue;
					}

					if (waitForData)
					{
						body[bodyPos++] = (byte) c;

						if (bodyPos < cmd.getDataLength())
						{
							continue;
						}

						// data complete
						cmd.setData(body);
						newCommand(cmd);
						bodyPos = 0;
						waitForData = false;
						waitForHeader = true;
					}
				}
				catch(InterruptedIOException e)
				{
					Thread.currentThread().interrupt();
					lost = true;
				}
				catch (IOException e)
				{
					Thread.currentThread().interrupt();
				}
			}
			
			if(lost)
			{
				connectionLost();
			}
		}
	}
}
