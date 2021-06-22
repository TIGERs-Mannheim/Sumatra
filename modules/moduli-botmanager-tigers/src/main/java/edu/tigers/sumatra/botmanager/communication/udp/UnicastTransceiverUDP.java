/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.communication.udp;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.communication.ITransceiverObserver;
import lombok.extern.log4j.Log4j2;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Transceiver communicating with UDP packets via unicast.
 */
@Log4j2
public class UnicastTransceiverUDP implements IReceiverUDPObserver
{
	private int dstPort = 0;
	private InetAddress destination = null;
	private DatagramSocket socket = null;

	private final TransmitterUDP transmitter = new TransmitterUDP();
	private final ReceiverUDP receiver = new ReceiverUDP();

	private final List<ITransceiverObserver> observers = new CopyOnWriteArrayList<>();


	public void addObserver(final ITransceiverObserver o)
	{
		observers.add(o);
	}


	public void removeObserver(final ITransceiverObserver o)
	{
		observers.remove(o);
	}


	public void enqueueCommand(final ACommand cmd)
	{
		if (socket == null)
		{
			return;
		}

		notifyOutgoingCommand(cmd);

		transmitter.enqueueCommand(cmd);
	}


	public void open()
	{
		close();

		try
		{
			socket = new DatagramSocket();
		} catch (final SocketException err)
		{
			log.error("Could not create UDP socket.", err);
			return;
		}

		socket.connect(destination, dstPort);

		receiver.setSocket(socket);

		transmitter.setSocket(socket);
		transmitter.setDestination(destination, dstPort);

		receiver.addObserver(this);

		transmitter.start();
		receiver.start();
	}


	public void open(final String host, final int newPort)
	{
		close();

		try
		{
			destination = InetAddress.getByName(host);

			dstPort = newPort;

			open();
		} catch (final UnknownHostException err)
		{
			log.error("Could not resolve " + host, err);
		}
	}


	public void close()
	{
		if (socket != null)
		{
			receiver.removeObserver(this);

			transmitter.stop();
			receiver.stop();

			socket.close();
			socket.disconnect();

			if (!socket.isClosed())
			{
				socket.close();
			}

			socket = null;
		}
	}


	public boolean isOpen()
	{
		return socket != null;
	}


	@Override
	public void onNewCommand(final ACommand cmd)
	{
		notifyIncommingCommand(cmd);
	}


	private void notifyIncommingCommand(final ACommand cmd)
	{
		observers.forEach(o -> o.onIncomingCommand(cmd));
	}


	private void notifyOutgoingCommand(final ACommand cmd)
	{
		observers.forEach(o -> o.onOutgoingCommand(cmd));
	}


	public void setDestination(final String dstAddr, final int newPort)
	{
		boolean start = false;

		if (socket != null)
		{
			start = true;
			close();
		}

		try
		{
			destination = InetAddress.getByName(dstAddr);

			dstPort = newPort;
		} catch (final UnknownHostException e)
		{
			log.error("Unknown host: " + dstAddr, e);
		}

		if (start)
		{
			open(dstAddr, newPort);
		}
	}
}
