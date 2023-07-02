/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.clock.NanoTime;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogRecorder;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.IReceiverObserver;
import edu.tigers.sumatra.network.MulticastUDPReceiver;
import edu.tigers.sumatra.network.NetworkUtility;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Optional;


/**
 * The most important implementation of the {@link ACam}-type, which is capable of receiving SSL-Vision data on a
 * certain port (and multicast-group)
 */
@Log4j2
public class SSLVisionCam extends ACam implements Runnable, IReceiverObserver, IConfigObserver
{
	private static final int BUFFER_SIZE = 10000;
	private final byte[] bufferArr = new byte[BUFFER_SIZE];

	@Configurable(comment = "Custom vision port that overwrites the value from moduli")
	@Setter
	private static int customPort;

	@Configurable(comment = "Custom vision address that overwrites the value from moduli")
	@Setter
	private static String customAddress;

	@Configurable(comment = "Enter a network address to limit network to a certain network interface")
	private static String network = "";

	static
	{
		ConfigRegistration.registerClass("user", SSLVisionCam.class);
	}

	private Thread cam;
	private MulticastUDPReceiver receiver;
	private boolean expectIOE = false;
	private int port;
	private String address;
	private InetAddress visionAddress;

	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();

	private GameLogRecorder gameLogRecorder;


	@Override
	public void initModule() throws InitModuleException
	{
		super.initModule();
		port = customPort > 0 ? customPort : getSubnodeConfiguration().getInt("port", 10006);
		address = StringUtils.isNotBlank(customAddress) ?
				customAddress :
				getSubnodeConfiguration().getString("address", "224.5.23.2");
	}


	@Override
	public void startModule()
	{
		final NetworkInterface nif = NetworkUtility.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.debug("No nif for vision-cam specified, will try all.");
			receiver = new MulticastUDPReceiver(address, port);
		} else
		{
			log.debug("Chose nif for vision-cam: " + nif.getDisplayName());
			receiver = new MulticastUDPReceiver(address, port, nif);
		}
		receiver.addObserver(this);

		gameLogRecorder = SumatraModel.getInstance().getModuleOpt(GameLogRecorder.class).orElse(null);

		cam = new Thread(this, "SSLVisionCam");
		cam.start();

		ConfigRegistration.registerConfigurableCallback("user", this);
	}


	@Override
	public void stopModule()
	{
		cleanup();
		ConfigRegistration.unregisterConfigurableCallback("user", this);
	}


	@Override
	public void run()
	{
		// Create new buffer
		final ByteBuffer buffer = ByteBuffer.wrap(bufferArr);

		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				buffer.clear();

				// Fetch packet
				final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity());

				if (receiver == null)
				{
					break;
				}
				receiver.receive(packet);

				visionAddress = packet.getAddress();
				final ByteArrayInputStream packetIn = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());

				// Translate
				final SSL_WrapperPacket sslPacket;
				try
				{
					sslPacket = SSL_WrapperPacket.parseFrom(packetIn);
				} catch (Exception err)
				{
					log.error("invalid ssl package", err);
					continue;
				}

				publishData(sslPacket);
			} catch (final IOException err)
			{
				if (!expectIOE)
				{
					log.error("Error while receiving SSLVision-Packet!", err);
					break;
				}
			} catch (Throwable err)
			{
				log.error("Error in SSL vision cam", err);
			}
		}

		// Cleanup
		expectIOE = true;
	}


	private void publishData(final SSL_WrapperPacket sslPacket)
	{
		// start with sending out the detection. It is most time critical
		if (sslPacket.hasDetection())
		{
			notifyNewCameraFrame(sslPacket.getDetection());
		}

		if (sslPacket.hasGeometry())
		{
			final CamGeometry geometry = geometryTranslator.fromProtobuf(sslPacket.getGeometry());

			notifyNewCameraCalibration(geometry);
		}

		notifyNewVisionPacket(sslPacket);

		if(gameLogRecorder != null)
		{
			gameLogRecorder.writeMessage(new GameLogMessage(NanoTime.getTimestampNow(), EMessageType.SSL_VISION_2014, sslPacket.toByteArray()));
		}
	}


	@Override
	public void onSocketTimedOut()
	{
		notifyVisionLost();
	}


	private void cleanup()
	{
		if (cam != null)
		{
			cam.interrupt();
			cam = null;
		}

		if (receiver != null)
		{
			expectIOE = true;
			receiver.close();
			receiver = null;
		}
	}


	@Override
	public void afterApply(final IConfigClient configClient)
	{
		if (cam != null)
		{
			stopModule();
			startModule();
		}
	}


	/**
	 * @return the port
	 */
	public final int getPort()
	{
		return port;
	}


	/**
	 * @return the address
	 */
	public final String getAddress()
	{
		return address;
	}


	public Optional<InetAddress> getVisionAddress()
	{
		return Optional.ofNullable(visionAddress);
	}
}
