/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.communication.udp;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.communication.Statistics;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Transmitter for UDP packets.
 */
@Log4j2
public class TransmitterUDP
{
	private InetAddress destination = null;
	private int destPort = 0;
	private DatagramSocket socket = null;
	private final BlockingQueue<ACommand> sendQueue = new LinkedBlockingQueue<>();
	private Thread sendingThread = null;
	private final Statistics stats = new Statistics();
	private boolean running = true;


	public void enqueueCommand(final ACommand cmd)
	{
		try
		{
			sendQueue.put(cmd);
		} catch (InterruptedException err)
		{
			log.debug("Could not queue cmd.", err);
			Thread.currentThread().interrupt();
		}
	}


	public void start()
	{
		if (sendingThread != null)
		{
			stop();
		}

		stats.reset();

		running = true;
		sendingThread = new Thread(new Sender(), "TransmitterUDP");
		sendingThread.start();
	}


	public void stop()
	{
		if (sendingThread == null)
		{
			return;
		}

		int retries = 100;
		while (!sendQueue.isEmpty() && (retries > 0))
		{
			retries--;
			try
			{
				//noinspection BusyWait
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}

		running = false;
		sendingThread.interrupt();
		try
		{
			sendingThread.join(100);
		} catch (InterruptedException err)
		{
			Thread.currentThread().interrupt();
		}

		sendingThread = null;
	}


	public void setSocket(final DatagramSocket newSocket)
	{
		boolean start = false;

		if (sendingThread != null)
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


	public void setDestination(final InetAddress dstIp, final int dstPort)
	{
		destination = dstIp;
		destPort = dstPort;
	}


	public Statistics getStats()
	{
		return stats;
	}


	private class Sender implements Runnable
	{
		@Override
		public void run()
		{
			if (socket == null)
			{
				log.error("Cannot start a transmitter on a null socket");
				return;
			}

			Thread.currentThread().setName("Transmitter UDP " + socket.getInetAddress() + ":" + socket.getPort());

			while (running)
			{
				ACommand cmd;

				try
				{
					cmd = sendQueue.take();
				} catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					continue;
				}

				try
				{
					byte[] data = CommandFactory.getInstance().encode(cmd);

					DatagramPacket packet = new DatagramPacket(data, data.length, destination, destPort);
					socket.send(packet);

					stats.packets++;
					// Ethernet + IP + UDP header length
					stats.raw += data.length + 54;
					stats.payload += data.length;
				} catch (IOException e)
				{
					log.error("Could not send packet.", e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
