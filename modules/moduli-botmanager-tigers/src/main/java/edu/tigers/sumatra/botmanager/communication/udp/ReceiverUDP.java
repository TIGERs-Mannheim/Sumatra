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
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Receiver for UDP packets.
 */
@Log4j2
public class ReceiverUDP
{
	private static final int PORT_UNREACHABLE_RETRY_WAIT = 1500;
	private final Statistics stats = new Statistics();
	private final List<IReceiverUDPObserver> observers = new CopyOnWriteArrayList<>();
	private DatagramSocket socket = null;
	private Thread receiverThread = null;


	public void addObserver(final IReceiverUDPObserver observer)
	{
		observers.add(observer);
	}


	public void removeObserver(final IReceiverUDPObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * Start receiver thread.
	 */
	public void start()
	{
		if (receiverThread != null)
		{
			stop();
		}

		stats.reset();

		receiverThread = new Thread(new Receiver(), "ReceiverUDP");

		receiverThread.start();
	}


	/**
	 * Stop receiver thread.
	 */
	public void stop()
	{
		if (receiverThread == null)
		{
			return;
		}

		receiverThread.interrupt();
		try
		{
			receiverThread.join(100);
		} catch (final InterruptedException err)
		{
			Thread.currentThread().interrupt();
		}

		receiverThread = null;
	}


	/**
	 * @param newSocket
	 */
	public final void setSocket(final DatagramSocket newSocket)
	{
		boolean start = false;

		if (receiverThread != null)
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


	/**
	 * @return
	 */
	public Statistics getStats()
	{
		return stats;
	}


	protected class Receiver implements Runnable
	{
		@Override
		public void run()
		{
			if (socket == null)
			{
				log.error("Cannot start a receiver on a null socket");
				return;
			}

			Thread.currentThread().setName("Receiver UDP " + socket.getInetAddress() + ":" + socket.getPort());

			byte[] buf;

			try
			{
				buf = new byte[socket.getReceiveBufferSize()];
			} catch (final SocketException err)
			{
				log.error("Could not get receive buffer size", err);
				return;
			}

			log.debug("Receive buffer size set to: " + buf.length);

			while (!Thread.currentThread().isInterrupted())
			{
				final DatagramPacket packet = new DatagramPacket(buf, buf.length);

				try
				{
					socket.receive(packet);

					final byte[] packetData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

					final ACommand cmd = CommandFactory.getInstance().decode(packetData);
					if (cmd == null)
					{
						log.warn("Error decoding command.");
						continue;
					}

					stats.packets++;
					stats.payload += packetData.length;
					stats.raw += packetData.length + 54;

					notifyNewCommand(cmd);
				} catch (final PortUnreachableException e)
				{
					final long waits = TimeUnit.MILLISECONDS.toSeconds(PORT_UNREACHABLE_RETRY_WAIT);
					log.info(socket.getLocalPort() + "->" + socket.getPort() + ": ICMP port unreachable, retry in " + waits
							+ "s.", e);

					try
					{
						Thread.sleep(PORT_UNREACHABLE_RETRY_WAIT);
					} catch (final InterruptedException err)
					{
						log.debug("Interrupted while waiting after ICMP port unreachable.");
						Thread.currentThread().interrupt();
					}

				} catch (final SocketException e)
				{
					log.info("UDP transceiver terminating", e);
					Thread.currentThread().interrupt();
				} catch (final IOException err)
				{
					log.error("Some IOException", err);
				} catch (final Exception err)
				{
					log.warn("Unexpected exception! See stacktrace", err);
				}
			}
		}


		/**
		 * @param cmd
		 */
		private void notifyNewCommand(final ACommand cmd)
		{
			synchronized (observers)
			{
				for (final IReceiverUDPObserver observer : observers)
				{
					observer.onNewCommand(cmd);
				}
			}
		}
	}
}
