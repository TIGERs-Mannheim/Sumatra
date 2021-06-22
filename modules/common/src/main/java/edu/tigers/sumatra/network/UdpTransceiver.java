/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.network;

import edu.tigers.sumatra.util.Safe;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.function.Consumer;


/**
 * A threaded UDP sender and receiver.
 */
@Log4j2
@RequiredArgsConstructor
public class UdpTransceiver
{
	private static final int SOCKET_TIMEOUT_MS = 1000;
	private final String host;
	private final int port;
	private DatagramSocket socket;
	private InetAddress address;
	private int bufferSize;
	private Thread thread;
	@Setter
	private Consumer<byte[]> responseConsumer = b -> {
	};


	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(() -> Safe.run(this::receive));
			thread.setName(toString());
			thread.start();
		}
	}


	public void stop()
	{
		if (thread != null)
		{
			thread.interrupt();
			thread = null;
		}
	}


	@Override
	public String toString()
	{
		return "UDP Transceiver " + host + ":" + port;
	}


	private void connect()
	{
		if (socket == null)
		{
			try
			{
				socket = new DatagramSocket();
				address = InetAddress.getByName(host);
				socket.setSoTimeout(SOCKET_TIMEOUT_MS);
				bufferSize = socket.getReceiveBufferSize();
			} catch (IOException e)
			{
				log.warn("Failed to setup socket for {}", this, e);
				socket = null;
			}
		}
	}


	private void disconnect()
	{
		if (socket != null)
		{
			socket.close();
			socket = null;
		}
	}


	private void receive()
	{
		try
		{
			connect();
			log.debug("Start listening for messages from {}", this);
			byte[] buf = new byte[bufferSize];
			while (!Thread.interrupted())
			{
				try
				{
					var packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					responseConsumer.accept(Arrays.copyOf(packet.getData(), packet.getLength()));
				} catch (@SuppressWarnings("squid:S1166") SocketTimeoutException e)
				{
					// go on in the loop (checking if we got interrupted and need to stop)
				} catch (IOException e)
				{
					log.warn("Failed to receive data from {}", this, e);
				}
			}
		} finally
		{
			disconnect();
			log.debug("Stopped listening for messages from {}", this);
		}
	}


	public void send(byte[] bytes)
	{
		if (socket == null || address == null)
		{
			return;
		}
		try
		{
			socket.send(new DatagramPacket(bytes, bytes.length, address, port));
		} catch (IOException e)
		{
			log.warn("Failed to send data to {}", this, e);
		}
	}
}
