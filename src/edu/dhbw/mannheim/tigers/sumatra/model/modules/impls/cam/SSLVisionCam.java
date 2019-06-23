/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
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
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.ITeamConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.FileReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionCamDetectionTranslator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionCamGeometryTranslator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.util.network.IReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * The most important implementation of the {@link ACam}-type, which is capable of receiving SSL-Vision data on a
 * certain port (and multicast-group)
 * 
 * @author Gero
 * 
 */
public class SSLVisionCam extends ACam implements Runnable, IReceiver, ITeamConfigObserver
{
	// Logger
	private static final Logger							log						= Logger
																										.getLogger(SSLVisionCam.class.getName());
	
	// Constants
	private static final int								BUFFER_SIZE				= 10000;
	private final byte[]										bufferArr				= new byte[BUFFER_SIZE];
	
	// Connection
	private Thread												cam;
	private IReceiver											receiver;
	
	private final int											port;
	private final String										address;
	
	private final NetworkInterface						nif;
	
	private boolean											expectIOE				= false;
	
	// Translation
	private final SSLVisionCamDetectionTranslator	detectionTranslator	= new SSLVisionCamDetectionTranslator();
	private final SSLVisionCamGeometryTranslator		geometryTranslator	= new SSLVisionCamGeometryTranslator();
	private final double										timeOffsetMillis;
	private final long										timeOffsetNanos;
	
	private long												packetCount				= 0;
	private TeamProps											teamProps				= null;
	private final Object										teampPropsSync			= new Object();
	
	// DEBUG, write sslvision data
	private long												normTime;
	private FileOutputStream								fos;
	private ObjectOutputStream								out;
	private final String										filename					= "D:\\sslvision.log";
	private boolean											write						= false;
	private final boolean									read						= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 */
	public SSLVisionCam(SubnodeConfiguration subnodeConfiguration)
	{
		port = subnodeConfiguration.getInt("port");
		address = subnodeConfiguration.getString("address");
		final String network = subnodeConfiguration.getString("interface");
		
		TeamConfig.getInstance().addObserver(this);
		
		// --- Compute time offset
		// Because we can't just convert currentMillis to nanos and subtract or the other way around,
		// we'd run into a overflow either way. So we do a little trick and separate nanos...
		final long nowNanos = System.nanoTime();
		final long currentMillis = System.currentTimeMillis();
		
		final long nowMillis = nowNanos / 1000000;
		final double restNanos = nowNanos % 1000000;
		
		timeOffsetMillis = currentMillis - nowMillis;
		timeOffsetNanos = (long) restNanos;
		
		// --- Choose network-interface
		nif = NetworkUtility.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.info("No nif for vision-cam specified, will try all.");
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
		resetCountDownLatch();
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
		} else if (nif == null)
		{
			receiver = new MulticastUDPReceiver(port, address);
		} else
		{
			receiver = new MulticastUDPReceiver(port, address, nif);
		}
		
		normTime = System.nanoTime();
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
		try
		{
			startSignal.await();
		} catch (final InterruptedException err)
		{
			log.warn("Interrupted while waiting for consumer to be set!");
			return;
		}
		
		
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
					final SSLVisionData object = new SSLVisionData((System.nanoTime() - normTime), copy);
					out.writeObject(object);
					out.flush();
				}
				
				packetCount++;
				
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
				final TeamProps teamProps = getTeamProperties();
				if (teamProps == null)
				{
					// Was interrupted...
					break;
				}
				
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
					final CamDetectionFrame frame = detectionTranslator.translate(sslPacket.getDetection(),
							timeOffsetMillis, timeOffsetNanos, System.nanoTime(), packetCount, teamProps);
					
					consumer.onNewCamDetectionFrame(frame);
					
					detectionObservable.notifyObservers(frame);
				}
				
				if (sslPacket.hasGeometry())
				{
					final CamGeometryFrame frame = geometryTranslator.translate(sslPacket.getGeometry(), teamProps);
					
					// TODO Gero:GeometryFrame??? (Gero) consumer.onNewCamGeometryFrame(frame);
					
					geometryObservable.notifyObservers(frame);
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
	public DatagramPacket receive(DatagramPacket store) throws IOException
	{
		return receiver.receive(store);
	}
	
	
	@Override
	public void onNewTeamConfig(TeamProps teamProps)
	{
		synchronized (teampPropsSync)
		{
			final boolean wasNull = (this.teamProps == null);
			
			this.teamProps = teamProps;
			
			if (wasNull)
			{
				teampPropsSync.notifyAll();
			}
		}
	}
	
	
	/**
	 * @return <code>null</code> if interrupted!
	 */
	private TeamProps getTeamProperties()
	{
		synchronized (teampPropsSync)
		{
			while (teamProps == null)
			{
				try
				{
					teampPropsSync.wait();
				} catch (final InterruptedException err)
				{
					return null;
				}
			}
			
			return teamProps;
		}
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
	public void deinitModule()
	{
		detectionObservable.removeAllObservers();
		geometryObservable.removeAllObservers();
		
		consumer = null;
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public boolean isReady()
	{
		return true;
	}
}
