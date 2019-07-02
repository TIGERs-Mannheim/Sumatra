/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.network.IReceiverObserver;
import edu.tigers.sumatra.network.MulticastUDPReceiver;
import edu.tigers.sumatra.network.NetworkUtility;


/**
 * The most important implementation of the {@link ACam}-type, which is capable of receiving SSL-Vision data on a
 * certain port (and multicast-group)
 * 
 * @author Gero
 */
public class SSLVisionCam extends ACam implements Runnable, IReceiverObserver, IConfigObserver
{
	private static final Logger log = Logger.getLogger(SSLVisionCam.class.getName());
	
	private static final int BUFFER_SIZE = 10000;
	private final byte[] bufferArr = new byte[BUFFER_SIZE];
	
	private Thread cam;
	private MulticastUDPReceiver receiver;
	private boolean expectIOE = false;
	
	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();
	
	
	@Configurable(defValue = "10006")
	private static int port = 10006;
	
	@Configurable(defValue = "224.5.23.2")
	private static String address = "224.5.23.2";
	
	@Configurable(comment = "Enter a network address to limit network to a certain network interface")
	private static String network = "";
	
	private final TimeSync timeSync = new TimeSync();
	
	
	static
	{
		ConfigRegistration.registerClass("user", SSLVisionCam.class);
	}
	
	
	@Override
	public void startModule()
	{
		final NetworkInterface nif = NetworkUtility.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.debug("No nif for vision-cam specified, will try all.");
			receiver = new MulticastUDPReceiver(port, address);
		} else
		{
			log.debug("Chose nif for vision-cam: " + nif.getDisplayName());
			receiver = new MulticastUDPReceiver(port, address, nif);
		}
		receiver.addObserver(this);
		
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
				
				// start with sending out the detection. It is most time critical
				if (sslPacket.hasDetection())
				{
					timeSync.update(sslPacket.getDetection().getTSent());
					notifyNewCameraFrame(sslPacket.getDetection(), timeSync);
				}
				
				if (sslPacket.hasGeometry())
				{
					final CamGeometry geometry = geometryTranslator.translate(sslPacket.getGeometry());
					
					notifyNewCameraCalibration(geometry);
				}
				
				notifyNewVisionPacket(sslPacket);
				
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
	
	
	@Override
	public void onInterfaceTimedOut()
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
			receiver.cleanup();
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
}
