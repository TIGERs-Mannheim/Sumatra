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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sim.util.network.IReceiver;
import edu.dhbw.mannheim.tigers.sim.util.network.MulticastUDPReceiver;
import edu.dhbw.mannheim.tigers.sim.util.network.NetworkUtility;
import edu.dhbw.mannheim.tigers.sim.util.network.UnicastUDPReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.FileReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionCamDetectionTranslator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionCamGeometryTranslator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision.SSLVisionData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.Timer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.exceptions.StartModuleException;


/**
 * The most important implementation of the {@link ACam}-type, which is capable of receiving SSL-Vision data on a
 * certain port (and multicast-group)
 * 
 * @author Gero
 * 
 */
public class SSLVisionCam extends ACam implements Runnable, IReceiver
{
	// Constants
	private static final int			BUFFER_SIZE	= 10000;
	private final byte[]					bufferArr	= new byte[BUFFER_SIZE];
	
	// Model
	private final SumatraModel			model			= SumatraModel.getInstance();
	
	protected ITimer						timer			= null;
	
	// Connection
	private Thread							cam;
	private IReceiver						receiver;
	private final Object					receiveSync	= new Object();
	
	private final int						port;
	private final String					address;
	private final boolean				multicastMode;
	
	private final String					network;
	private final NetworkUtility		networkUtil	= new NetworkUtility();
	private final NetworkInterface	nif;
	
	private boolean						expectIOE	= false;
	
	// Translation
	private final double					timeOffsetMillis;
	private final long					timeOffsetNanos;
	
	private long							packetCount	= 0;
	
	//debug, write sslvision data
	private long normTime;
	private FileOutputStream fos;
	private ObjectOutputStream out;
	private String filename = "D:\\sslvision.log";
	private boolean write = false;
	private boolean read = false;
	
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public SSLVisionCam(SubnodeConfiguration subnodeConfiguration)
	{
		port = subnodeConfiguration.getInt("port", 40101);
		address = subnodeConfiguration.getString("address", "224.5.23.2");
		
		multicastMode = model.getGlobalConfiguration().getBoolean("multicastMode", true);
		network = subnodeConfiguration.getString("interface", "192.168.1.0");
		

		// --- Compute time offset
		// Because we can't just convert currentMillis to nanos and subtract or the other way around,
		// we'd run into a overflow either way. So we do a little trick and separate nanos...
		long nowNanos = System.nanoTime();
		long currentMillis = System.currentTimeMillis();
		// log.debug("nowNanos: " + nowNanos);
		// log.debug("currentMillis: " + currentMillis);
		
		long nowMillis = nowNanos / 1000000;
		double restNanos = nowNanos % 1000000;
		// double restMillis = restNanos / 1000000;
		// log.debug("nowMillis: " + nowMillis);
		// log.debug("restNanos: " + restNanos);
		// log.debug("restMillis: " + restMillis);
		
		this.timeOffsetMillis = currentMillis - nowMillis;
		this.timeOffsetNanos = (long) restNanos;
		// log.debug("timeOffsetMillis: " + timeOffsetMillis);
		// log.debug("timeOffsetNanos: " + timeOffsetNanos);
		

		// --- Choose network-interface
		nif = networkUtil.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.error("No proper nif for vision-cam in network '" + network + "' found!");
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
		
		try
		{
			timer = (ATimer) model.getModule(Timer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.debug("No timer found.");
		}
		
		log.info("Initialized.");
	}
	

	@Override
	public void startModule() throws StartModuleException
	{
		if(read) {
			try
			{
				receiver = new FileReceiver(filename);
			} catch (IOException err)
			{
				log.error("Error opening file to playback");
			}
			write = false;
		} else {
			if (multicastMode)
			{
				receiver = new MulticastUDPReceiver(port, address, nif);
			} else
			{
				receiver = new UnicastUDPReceiver(port);
			}
		}
		
		normTime = System.nanoTime();
		if(write) {
			try
			{
				fos = new FileOutputStream(filename);
				out = new ObjectOutputStream(fos);
			} catch (Exception err)
			{
				log.error("Error opening streams for playback");
			}
		}
		
		cam = new Thread(this, "SSLVisionCam");
		cam.start();
		log.info("Started.");
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
		} catch (InterruptedException err)
		{
			log.debug("Interrupte while waiting for consumer to be set!");
			return;
		}
		

		// Create new buffer
		ByteBuffer buffer = ByteBuffer.wrap(bufferArr);
		
		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				buffer.clear();
				
				// Fetch packet
				final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity());
				
				synchronized (receiveSync)
				{
					if (receiver == null)
					{
						break;
					}
					receive(packet);
					if(write) {
						byte[] copy = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
						SSLVisionData object = new SSLVisionData((System.nanoTime() - normTime), copy);
						out.writeObject(object);
						out.flush();
					}
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
				final SSL_WrapperPacket sslPacket = SSL_WrapperPacket.parseFrom(packetIn); // tempBuffer);
				
				if (sslPacket.hasDetection())
				{
					CamDetectionFrame frame = SSLVisionCamDetectionTranslator.translate(sslPacket.getDetection(),
							timeOffsetMillis, timeOffsetNanos, System.nanoTime(), packetCount);
					
//					if(frame.balls.size() != 0)
//						System.out.println(frame.balls.get(0).pos.x + " " + frame.balls.get(0).pos.y);
					time(frame);
					
					consumer.onNewCamDetectionFrame(frame);
					
					detectionObservable.notifyObservers(frame);
				}
				
				if (sslPacket.hasGeometry())
				{
					CamGeometryFrame frame = SSLVisionCamGeometryTranslator.translate(sslPacket.getGeometry());
					
					// TODO Gero:GeometryFrame??? (Gero) consumer.onNewCamGeometryFrame(frame);
					
					geometryObservable.notifyObservers(frame);
				}
				
			} catch (IOException err)
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
	

	private void time(CamDetectionFrame frame)
	{
		if (timer != null)
		{
			timer.time(frame);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- deinit and stop ------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void stopModule()
	{
		cleanup();
		if(write) {
			try
			{
				fos.close();
				out.close();
			} catch (IOException err)
			{
				// TODO Auto-generated catch block
				err.printStackTrace();
			}
		}
		log.info("Stopped.");
	}
	

	@Override
	public void deinitModule()
	{
		detectionObservable.removeAllObservers();
		geometryObservable.removeAllObservers();
		
		consumer = null;
		timer = null;
		
		log.info("Deinitialized.");
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
			} catch (IOException err)
			{
				log.debug("Socket closed...");
			}
			
			synchronized (receiveSync)
			{
				receiver = null;
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int getLocalPort()
	{
		return receiver == null ? null : receiver.getLocalPort();
	}
	
	
	@Override
	public InetAddress getLocalAddress()
	{
		return receiver == null ? null : receiver.getLocalAddress();
	}
	

	@Override
	public boolean isReady()
	{
		synchronized (receiveSync)
		{
			return receiver != null;
		}
	}
}
