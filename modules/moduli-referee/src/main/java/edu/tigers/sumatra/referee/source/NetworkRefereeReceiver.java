/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.source;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.network.MulticastUDPReceiver;
import edu.tigers.sumatra.network.NetworkUtility;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Optional;


/**
 * New implementation of the referee receiver with the new protobuf message format (2013)
 */
@Log4j2
public class NetworkRefereeReceiver extends ARefereeMessageSource implements Runnable
{
	private static final int BUFFER_SIZE = 10000;

	@Configurable(defValue = "224.5.23.1")
	private static String address;

	@Configurable
	private static String network;

	private int port;
	private Thread referee;
	private MulticastUDPReceiver receiver;

	private InetAddress refBoxAddress = null;

	private boolean expectIOE = false;

	static
	{
		ConfigRegistration.registerClass("user", NetworkRefereeReceiver.class);
	}


	/**
	 * Constructor
	 */
	public NetworkRefereeReceiver()
	{
		super(ERefereeMessageSource.NETWORK);
	}


	@Override
	public void start()
	{
		// --- Choose network-interface
		NetworkInterface nif = NetworkUtility.chooseNetworkInterface(network, 3);

		if (nif == null)
		{
			log.debug("No nif for referee specified, will try all.");
			receiver = new MulticastUDPReceiver(address, port);
		} else
		{
			log.info("Chose nif for referee: {}", nif.getDisplayName());
			receiver = new MulticastUDPReceiver(address, port, nif);
		}

		referee = new Thread(this, "External Referee");
		referee.start();
	}


	@Override
	public void run()
	{
		final DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);

		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				receiver.receive(packet);
			} catch (final IOException err)
			{
				if (!expectIOE)
				{
					log.error("Error while receiving referee-message!", err);
				}
				break;
			}

			refBoxAddress = packet.getAddress();

			try
			{
				var packetIn = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
				var sslRefereeMsg = SslGcRefereeMessage.Referee.parseFrom(packetIn);

				// Notify the receipt of a new RefereeMessage to any other observers
				notifyNewRefereeMessage(sslRefereeMsg);
			} catch (IOException err)
			{
				log.error("Could not read referee message", err);
			}
		}

		// Cleanup
		expectIOE = false;
	}


	@Override
	public void stop()
	{
		if (referee != null)
		{
			referee.interrupt();
			referee = null;
		}

		if (receiver != null)
		{
			expectIOE = true;

			receiver.close();

			receiver = null;
		}
	}


	@Override
	public Optional<InetAddress> getRefBoxAddress()
	{
		return Optional.ofNullable(refBoxAddress);
	}


	public void setPort(final int port)
	{
		this.port = port;
	}
}
