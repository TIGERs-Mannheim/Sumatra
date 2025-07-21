/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.network;

import edu.tigers.sumatra.util.Safe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * Byte-array based unicast UDP transceiver with reconnect logic and optional receive timeout.
 * Offers callbacks for received data and state changes.
 * This class always tries to reconnect the UDP socket in case of errors or timeout, until it is stopped.
 */
@Log4j2
@RequiredArgsConstructor
public class RobustUnicastUdpTransceiver
{
	private final String host;
	private final int port;
	private final int receiveTimeoutMs;

	private static final int RECONNECT_DELAY_MS = 2000;
	private DatagramSocket socket;
	private InetAddress address;
	private byte[] receiveBuffer;
	private Thread supervisorThread;
	private Thread receiveThread;
	private Thread sendThread;
	private final BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();
	@Getter
	private State state = State.DISCONNECTED;
	private CountDownLatch supervisorWakeup = new CountDownLatch(1);

	@Setter
	private Consumer<byte[]> responseConsumer = b -> {};
	@Setter
	private Consumer<State> stateConsumer = s -> {};

	public enum State
	{
		DISCONNECTED,
		CONNECTING,
		CONNECTED,
	}


	/**
	 * Create a UDP transceiver with no receive timeout.
	 *
	 * @param host Hostname or IP.
	 * @param port Remote port.
	 */
	public RobustUnicastUdpTransceiver(final String host, final int port)
	{
		this(host, port, 0);
	}


	public void start()
	{
		if (supervisorThread == null)
		{
			supervisorThread = new Thread(() -> Safe.run(this::supervisorLoop));
			supervisorThread.start();
		}
	}


	public void stop()
	{
		if (supervisorThread != null)
		{
			supervisorThread.interrupt();
			try
			{
				supervisorThread.join(100);
			} catch (InterruptedException err)
			{
				Thread.currentThread().interrupt();
			}
			supervisorThread = null;
			sendQueue.clear();
		}
	}


	@Override
	public String toString()
	{
		return "UDP Transceiver " + host + ":" + port;
	}


	public void send(byte[] bytes)
	{
		if (state == State.CONNECTED)
		{
			try
			{
				sendQueue.put(bytes);
			} catch (InterruptedException e)
			{
				log.debug("Could not queue cmd.", e);
				Thread.currentThread().interrupt();
			}
		}
	}


	private void connect()
	{
		try
		{
			address = InetAddress.getByName(host);
			socket = new DatagramSocket();
			socket.setSoTimeout(receiveTimeoutMs);
			socket.connect(address, port);
			receiveBuffer = new byte[socket.getReceiveBufferSize()];
			return;
		} catch (final UnknownHostException e)
		{
			log.debug("Could not resolve {}", host, e);
		} catch (UncheckedIOException | SocketException e)
		{
			log.debug("Failed to create UDP socket.", e);
		} catch (IllegalArgumentException e)
		{
			log.error("Invalid host ({}) or port ({})", host, port, e);
		}

		socket = null;
		address = null;
	}


	private void disconnect()
	{
		if (receiveThread != null)
		{
			receiveThread.interrupt();
			receiveThread = null;
		}

		if (sendThread != null)
		{
			sendThread.interrupt();
			sendThread = null;
		}

		if (socket != null)
		{
			socket.close();
			socket = null;
		}
	}


	private void setState(State newState)
	{
		if (newState != state)
		{
			state = newState;
			stateConsumer.accept(newState);
		}
	}


	private boolean checkSocket()
	{
		if (socket == null)
		{
			setState(State.CONNECTING);
			connect();

			if (socket == null)
			{
				try
				{
					Thread.sleep(RECONNECT_DELAY_MS);
					return true;
				} catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					return true;
				}
			} else
			{
				receiveThread = new Thread(() -> Safe.run(this::receiveLoop));
				sendThread = new Thread(() -> Safe.run(this::sendLoop));
				receiveThread.start();
				sendThread.start();
				setState(State.CONNECTED);
			}
		} else
		{
			if (!receiveThread.isAlive() || !sendThread.isAlive() || receiveThread.isInterrupted()
					|| sendThread.isInterrupted())
			{
				disconnect();
				setState(State.DISCONNECTED);
				return true;
			}
		}

		return false;
	}


	private void supervisorLoop()
	{
		Thread.currentThread().setName(toString());

		while (!Thread.currentThread().isInterrupted())
		{
			if (checkSocket())
			{
				// When checkSocket returns true it wants to be re-run immediately
				continue;
			}

			try
			{
				if (supervisorWakeup.await(100, TimeUnit.MILLISECONDS))
				{
					supervisorWakeup = new CountDownLatch(1);
				}
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}

		disconnect();
		setState(State.DISCONNECTED);
	}


	private void receiveLoop()
	{
		Thread.currentThread().setName(this + " RX");

		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				var packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				socket.receive(packet);
				responseConsumer.accept(Arrays.copyOf(packet.getData(), packet.getLength()));
			} catch (SocketTimeoutException e)
			{
				log.debug("Socket timeout while receiving packet.", e);
				Thread.currentThread().interrupt();
				supervisorWakeup.countDown();
			} catch (PortUnreachableException e)
			{
				log.debug("{}->{}: ICMP port unreachable on receive.", socket.getLocalPort(), socket.getPort(), e);
				Thread.currentThread().interrupt();
				supervisorWakeup.countDown();
			} catch (IOException e)
			{
				// Socket IO error is expected as a result of closing the socket and thereby terminating this thread.
				Thread.currentThread().interrupt();
				supervisorWakeup.countDown();
			}
		}
	}


	private void sendLoop()
	{
		Thread.currentThread().setName(this + " TX");

		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				var data = sendQueue.take();
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				socket.send(packet);
			} catch (PortUnreachableException e)
			{
				log.debug("{}->{}: ICMP port unreachable on send.", socket.getLocalPort(), socket.getPort(), e);
				Thread.currentThread().interrupt();
				supervisorWakeup.countDown();
			} catch (InterruptedException | IOException e)
			{
				// Socket IO error is expected as a result of closing the socket and thereby terminating this thread.
				// Interrupted exception originates from sendQueue.take and is expected when the thread is interrupted.
				Thread.currentThread().interrupt();
				supervisorWakeup.countDown();
			}
		}
	}
}
