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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;

public class TransceiverTCP implements IReceiverTCPObserver, ITransmitterTCPObserver, ITransceiverTCP
{
	private final Logger log = Logger.getLogger(getClass());
	
	private ReceiverTCP receiver = new ReceiverTCP();
	private BurstTransmitterTCP transmitter = new BurstTransmitterTCP();
	private int connectTimeout = 500;
	private int connectInterval = 2000;
	private Thread autoConnectorThread = null;
	private String host = "";
	private int port = 0;
	private List<ITransceiverTCPObserver> observers = new ArrayList<ITransceiverTCPObserver>();
	private Socket socket = null;
	
	public enum EConnectionState
	{
		DISCONNECTED,
		CONNECTED,
		CONNECTING,
	}

	public TransceiverTCP()
	{
		receiver.addObserver(this);
		transmitter.addObserver(this);
	}

	public TransceiverTCP(String host, int port)
	{
		receiver.addObserver(this);
		transmitter.addObserver(this);

		connect(host, port);
	}
	
	@Override
	public void addObserver(ITransceiverTCPObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}
	}
	
	@Override
	public void removeObserver(ITransceiverTCPObserver o)
	{
		synchronized(observers)
		{
			observers.remove(o);
		}
	}
	
	@Override
	public void connect(String host, int port)
	{
		disconnect();
		
		this.host = host;
		this.port = port;
		
		autoConnectorThread = new Thread(new AutoConnector());
		
		notifyConnectionChanged(EConnectionState.CONNECTING);
		
		autoConnectorThread.start();
	}
	
	@Override
	public void disconnect()
	{
		if(autoConnectorThread != null)
		{
			autoConnectorThread.interrupt();
			autoConnectorThread = null;
		}
		
		if(isConnected())
		{
			receiver.stop();
			transmitter.stop();
			try
			{
				if(!socket.isClosed())
				{
					socket.close();
				}
			}
			catch (IOException err)
			{
				log.error("Could not close socket: " + host + ":" + port);
			}
			socket = null;
			
		}
		
		notifyConnectionChanged(EConnectionState.DISCONNECTED);
	}
	
	@Override
	public void reconnect()
	{
		connect(host, port);
	}
	
	@Override
	public void setDestination(String host, int port)
	{
		boolean connect = false;
		
		if(isConnected() || isConnecting())
		{
			connect = true;
		}
		
		disconnect();
		
		this.host = host;
		this.port = port;
		
		if(connect)
		{
			connect(this.host, this.port);
		}
	}

	@Override
	public void enqueueCommand(ACommand cmd)
	{
		if(!isConnected())
		{
			log.warn("Bot " + host + ":" + port + " is not connected, discarding command");
			return;
		}
		
		transmitter.enqueueCommand(cmd);
		notifyNewCommandToBot(cmd);
	}
	
	@Override
	public int getConnectTimeout()
	{
		return connectTimeout;
	}

	@Override
	public void setConnectTimeout(int connectTimeout)
	{
		this.connectTimeout = connectTimeout;
	}

	@Override
	public int getConnectInterval()
	{
		return connectInterval;
	}

	@Override
	public void setConnectInterval(int connectInterval)
	{
		this.connectInterval = connectInterval;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public String getHost()
	{
		return host;
	}
	
	@Override
	public boolean isConnected()
	{
		if(socket != null && socket.isConnected())
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isConnecting()
	{
		if(autoConnectorThread != null)
		{
			return true;
		}
		
		return false;
	}
	
	public EConnectionState getConnectionState()
	{
		if(isConnected())
		{
			return EConnectionState.CONNECTED;
		}
		
		if(isConnecting())
		{
			return EConnectionState.CONNECTING;
		}
		
		return EConnectionState.DISCONNECTED;
	}
	
	@Override
	public Statistics getReceiverStats()
	{
		return receiver.getStats();
	}

	@Override
	public Statistics getTransmitterStats()
	{
		return transmitter.getStats();
	}

	@Override
	public void onNewCommand(ACommand cmd)
	{
		notifyNewCommandFromBot(cmd);
	}

	@Override
	public void onConnectionLost()
	{
		disconnect();
		
		notifyConnectionChanged(EConnectionState.DISCONNECTED);
	}
	
	protected void connectionEstablished()
	{
		autoConnectorThread = null;
		
		try
		{
			receiver.setSocket(socket);
			transmitter.setSocket(socket);
		}
		catch (IOException err)
		{
			disconnect();
			
			log.error("Setting receiver or transmitter socket failed. (" + host + ":" + port + ")");
			
			return;
		}
		
		receiver.start();
		transmitter.start();
		
		//inform observer
		notifyConnectionChanged(EConnectionState.CONNECTED);
	}
	
	private void notifyConnectionChanged(EConnectionState state)
	{
		synchronized(observers)
		{
			for(ITransceiverTCPObserver o : observers)
			{
				o.onConnectionChanged(state);
			}
		}
	}
	
	private void notifyNewCommandToBot(ACommand cmd)
	{
		synchronized(observers)
		{
			for (ITransceiverTCPObserver observer : observers)
			{
				observer.onIncommingCommand(cmd);
			}
		}
	}
	
	private void notifyNewCommandFromBot(ACommand cmd)
	{
		synchronized(observers)
		{
			for (ITransceiverTCPObserver observer : observers)
			{
				observer.onOutgoingCommand(cmd);
			}
		}
	}
	
	protected class AutoConnector implements Runnable
	{
		@Override
		public void run()
		{
			Thread.currentThread().setName("AutoConnector");
			
			while(!Thread.currentThread().isInterrupted())
			{
				socket = new Socket();
				
				try
				{
					socket.bind(null);
					socket.connect(new InetSocketAddress(host, port), connectTimeout);
					socket.setTcpNoDelay(true);
				}
				catch (IOException err)
				{
					try
					{
						Thread.sleep(connectInterval);
					}
					catch (InterruptedException err1)
					{
						Thread.currentThread().interrupt();
						socket = null;
					}
					
					continue;
				}
				
				connectionEstablished();
				
				Thread.currentThread().interrupt();
			}
		}		
	}
}
