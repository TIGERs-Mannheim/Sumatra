package edu.tigers.sumatra.network;


import lombok.extern.log4j.Log4j2;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Connect to a broadcast IP and receive {@link DatagramPacket}s on it.
 */
@Log4j2
public class BroadcastUDPReceiver implements UDPReceiver
{
	private static final int SO_TIMEOUT = 500;
	private final List<IReceiverObserver> observers = new CopyOnWriteArrayList<>();
	private DatagramSocket currentSocket;


	public BroadcastUDPReceiver(String host, int port)
	{
		currentSocket = connect(host, port);
	}


	@Override
	public void addObserver(final IReceiverObserver observer)
	{
		observers.add(observer);
	}


	@Override
	public void removeObserver(final IReceiverObserver observer)
	{
		observers.remove(observer);
	}


	private DatagramSocket connect(String host, int port)
	{
		try
		{
			return new DatagramSocket(new InetSocketAddress(host, port));
		} catch (IOException err)
		{
			log.error("Could not create new multicast socket", err);
		}
		return null;
	}


	@Override
	public void receive(final DatagramPacket store) throws IOException
	{
		if (currentSocket == null)
		{
			throw new IOException("Connection is closed");
		}

		while (currentSocket != null)
		{
			try
			{
				currentSocket.receive(store);
				currentSocket.setSoTimeout(SO_TIMEOUT);
				return;
			} catch (EOFException eof)
			{
				log.error("EOF error, buffer may be too small", eof);
			} catch (SocketTimeoutException e)
			{
				log.debug("No data received for {} ms", SO_TIMEOUT, e);
				observers.forEach(IReceiverObserver::onSocketTimedOut);
				currentSocket.setSoTimeout(0);
			}
		}
		throw new IOException("No data received");
	}


	@Override
	public void close()
	{
		if (currentSocket != null)
		{
			currentSocket.close();
		}
		observers.clear();
	}
}
