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
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.IReceiver;
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
	private IReceiver receiver;
	private boolean expectIOE = false;
	
	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();
	
	
	@Configurable(defValue = "10006")
	private int port = 10006;
	
	@Configurable(defValue = "224.5.23.2")
	private String address = "224.5.23.2";
	
	@Configurable(comment = "Enter a network address to limit network to a certain network interface")
	private String network = "";
	
	private NetworkInterface nif;
	private final TimeSync timeSync = new TimeSync();
	
	
	static
	{
		ConfigRegistration.registerClass("user", SSLVisionCam.class);
	}
	
	
	@Override
	public void initModule()
	{
		// nothing to init
	}
	
	
	@Override
	public void startModule()
	{
		ConfigRegistration.applySpezis(this, "user",
				SumatraModel.getInstance().getGlobalConfiguration().getString("environment"));
		// --- Choose network-interface
		nif = NetworkUtility.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.debug("No nif for vision-cam specified, will try all.");
		} else
		{
			log.debug("Chose nif for vision-cam: " + nif.getDisplayName() + ".");
		}
		if (getNif() == null)
		{
			MulticastUDPReceiver recv = new MulticastUDPReceiver(getPort(), getAddress());
			recv.addObserver(this);
			receiver = recv;
		} else
		{
			MulticastUDPReceiver recv = new MulticastUDPReceiver(getPort(), getAddress(), getNif());
			recv.addObserver(this);
			receiver = recv;
		}
		
		cam = new Thread(this, "SSLVisionCam");
		cam.start();
		
		ConfigRegistration.registerConfigurableCallback("user", this);
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
				
				// At this point we got a problem: The DatagramPacket has a total length of BUFFER_SIZE (= 10000), but the
				// actual data is smaller.
				// We can't simply pass packet.getData() to the protobufs parseFrom(...)-method, because this array is 10000
				// byte long, and it is
				// not possible to detect the end of the actual message and thus not possible to read directly from it
				// (InvalidProtocolBufferException)
				// As the information about the actual message-length is provided in the packet (.getLength()), we got two
				// options:
				// - copy all data into a new array with length defined by packet.getLength() or
				// - open a ByteArrayInputStream on packet.getData() from 0-packet.getLength()
				// =)
				// Copy data
				// byte[] tempBuffer = new byte[packet.getLength()];
				// for (int i = 0; i < packet.getLength(); i++)
				// {
				// tempBuffer[i] = packet.getData()[i];
				// }
				
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
	
	
	// --------------------------------------------------------------------------
	// --- deinit and stop ------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void stopModule()
	{
		cleanup();
		ConfigRegistration.unregisterConfigurableCallback("user", this);
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
			
			try
			{
				receiver.cleanup();
			} catch (final IOException err)
			{
				log.debug("Socket closed...", err);
			}
			
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
	
	
	/**
	 * @return the nif
	 */
	public final NetworkInterface getNif()
	{
		return nif;
	}
}
