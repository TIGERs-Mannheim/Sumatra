/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.FileReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionCamDetectionTranslator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionCamGeometryTranslator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.UserConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.network.IReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * The most important implementation of the {@link ACam}-type, which is capable of receiving SSL-Vision data on a
 * certain port (and multicast-group)
 * 
 * @author Gero
 */
public class SSLVisionCam extends ACam implements Runnable, IReceiver
{
	private static final Logger							log						= Logger
																										.getLogger(SSLVisionCam.class.getName());
	
	// Constants
	private static final int								BUFFER_SIZE				= 10000;
	private final byte[]										bufferArr				= new byte[BUFFER_SIZE];
	
	// Connection
	private Thread												cam;
	private IReceiver											receiver;
	
	
	private boolean											expectIOE				= false;
	
	// Translation
	private final SSLVisionCamDetectionTranslator	detectionTranslator	= new SSLVisionCamDetectionTranslator();
	private final SSLVisionCamGeometryTranslator		geometryTranslator	= new SSLVisionCamGeometryTranslator();
	
	// DEBUG, write sslvision data
	private long												normTime;
	private FileOutputStream								fos;
	private ObjectOutputStream								out;
	private final String										filename					= "data/sslvision.log";
	private boolean											write						= false;
	private final boolean									read						= false;
	
	private final int											port;
	private final String										address;
	private final NetworkInterface						nif;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 */
	public SSLVisionCam(final SubnodeConfiguration subnodeConfiguration)
	{
		super(subnodeConfiguration);
		
		int visionPort = subnodeConfiguration.getInt("port");
		if ((visionPort == 40102) && (UserConfig.getGrSimPort() > 0))
		{
			// little bit hacky, but if sim port is detected, use the port configured in UserConfig
			visionPort = UserConfig.getGrSimPort();
		} else if ((visionPort == 10002) && (UserConfig.getVisionPort() > 0))
		{
			visionPort = UserConfig.getVisionPort();
		}
		port = visionPort;
		address = subnodeConfiguration.getString("address");
		final String network = subnodeConfiguration.getString("interface");
		
		// --- Choose network-interface
		nif = NetworkUtility.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.debug("No nif for vision-cam specified, will try all.");
		} else
		{
			log.info("Chose nif for vision-cam: " + nif.getDisplayName() + ".");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- init and start -------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		if (read)
		{
			try
			{
				receiver = new FileReceiver(filename);
			} catch (final IOException err)
			{
				log.error("Error opening file to playback");
			}
			write = false;
		} else if (getNif() == null)
		{
			receiver = new MulticastUDPReceiver(getPort(), getAddress());
		} else
		{
			receiver = new MulticastUDPReceiver(getPort(), getAddress(), getNif());
		}
		
		normTime = SumatraClock.nanoTime();
		if (write)
		{
			try
			{
				fos = new FileOutputStream(filename);
				out = new ObjectOutputStream(fos);
			} catch (final IOException err)
			{
				log.error("Error opening streams for playback");
			}
		}
		
		cam = new Thread(this, "SSLVisionCam");
		cam.start();
	}
	
	
	// --------------------------------------------------------------------------
	// --- receive and translate ------------------------------------------------
	// --------------------------------------------------------------------------
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
				receive(packet);
				if (write)
				{
					final byte[] copy = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
					final SSLVisionData object = new SSLVisionData((SumatraClock.nanoTime() - normTime), copy);
					out.writeObject(object);
					out.flush();
				}
				
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
				
				if (sslPacket.hasDetection())
				{
					final CamDetectionFrame frame = detectionTranslator.translate(sslPacket.getDetection());
					
					notifyNewCameraFrame(frame);
				}
				
				if (sslPacket.hasGeometry())
				{
					final CamGeometry geometry = geometryTranslator.translate(sslPacket.getGeometry());
					
					notifyNewCameraCalibration(geometry);
				}
			} catch (final IOException err)
			{
				if (!expectIOE)
				{
					log.error("Error while receiving SSLVision-Packet!", err);
					break;
				}
			}
		}
		
		// Cleanup
		expectIOE = true;
	}
	
	
	@Override
	public DatagramPacket receive(final DatagramPacket store) throws IOException
	{
		return receiver.receive(store);
	}
	
	
	// --------------------------------------------------------------------------
	// --- deinit and stop ------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void stopModule()
	{
		cleanup();
		if (write)
		{
			try
			{
				fos.close();
				out.close();
			} catch (final IOException err)
			{
				log.fatal("IOException", err);
			}
		}
	}
	
	
	@Override
	public void cleanup()
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
				log.debug("Socket closed...");
			}
			
			receiver = null;
		}
	}
	
	
	@Override
	public boolean isReady()
	{
		return true;
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
