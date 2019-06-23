/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sim.util.network.IReceiver;
import edu.dhbw.mannheim.tigers.sim.util.network.MulticastUDPReceiver;
import edu.dhbw.mannheim.tigers.sim.util.network.NetworkUtility;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.StartModuleException;


/**
 * RefereeBox-implementation.<br/>
 * Receives every message from the RefereeBox, buts passes only new state information to its consumer (the Agent). See
 * {@link #isNewMessage(RefereeMsg)} for details.
 * 
 * TODO: handle color-change after half-time!
 * @author GuntherB, AndreR, MalteM, DionH, FriederB, Gero
 */
public class RefereeReceiver extends AReferee implements Runnable, IReceiver
{
	protected final Logger				log			= Logger.getLogger(getClass());
	
	// Constants
	private final static int			BUFFER_SIZE	= 10000;
	
	// Model
	private final SumatraModel			model			= SumatraModel.getInstance();
	
	// Connection
	private Thread							referee;
	private IReceiver						receiver;
	private final Object					receiveSync	= new Object();
	
	private final int						port;
	private final String					address;
	// private final boolean multicastMode;
	
	private final String					network;
	private final NetworkUtility		networkUtil	= new NetworkUtility();
	private final NetworkInterface	nif;
	
	private boolean						expectIOE	= false;
	
	// Translation
	private final boolean				weAreYellow;
	
	private int								lastId;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public RefereeReceiver(SubnodeConfiguration subnodeConfiguration)
	{
		log.debug("referee instatiated!");
		lastId = -1;
		address = subnodeConfiguration.getString("address", "224.5.23.1");
		port = Integer.valueOf(subnodeConfiguration.getInt("port", 10001));
		network = subnodeConfiguration.getString("interface", "192.168.1.0");
		
		// --- Choose network-interface
		nif = networkUtil.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.error("No proper nif for referee in network '" + network + "' found!");
		} else
		{
			log.info("Chose nif for referee: " + nif.getDisplayName() + ".");
		}
		
		// multicastMode = model.getGlobalConfiguration().getBoolean("multicastMode", true);
		
		weAreYellow = model.getGlobalConfiguration().getString("ourColor").equals("yellow");
	}
	

	// --------------------------------------------------------------------------
	// --- init and start -------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		resetCountDownLatch();
		
		log.info("Initialized.");
	}
	

	@Override
	public void startModule() throws StartModuleException
	{
		// if (multicastMode)
		// {
		receiver = new MulticastUDPReceiver(port, address, nif);
		// } else
		// {
		// receiver = new UnicastUDPReceiver(port);
		// }
		
		referee = new Thread(this, "Referee");
		referee.start();
		
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
			log.debug("Interrupted while waiting for referee-consumer to be set!");
			return;
		}
		

		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				// Fetch packet
				final DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
				
				synchronized (receiveSync)
				{
					if (receiver == null)
					{
						break;
					}
					receive(packet);
				}
				
				// Copy data
				byte[] tempBuffer = new byte[packet.getLength()];
				for (int i = 0; i < packet.getLength(); i++)
				{
					tempBuffer[i] = packet.getData()[i];
				}
				
				// Translate
				RefereeMsg msg = RefereeMsgHandler.translate(tempBuffer, weAreYellow);
				
				// If this message really contains new game-state information: Pass it to the agent!
				if (isNewMessage(msg))
				{
					consumer.onNewRefereeMsg(msg);
				}
				
				// Notify the receipt of a new RefereeMessage to any other observers
				notifyNewRefereeMsg(msg);
				
			} catch (IOException err)
			{
				lastId = -1;
				if (!expectIOE)
				{
					log.error("Error while receiving referee-message!", err);
					break;
				}
			}
		}
		
		// Cleanup
		expectIOE = false;
	}
	

	/**
	 * @param msg The recently received message
	 * @return Whether this message does really new game-state information
	 * @author FriederB
	 */
	private boolean isNewMessage(RefereeMsg msg)
	{
		if (lastId < msg.id)
		{
			lastId = msg.id;
			return true;
		}
		return false;
	}
	

	@Override
	public DatagramPacket receive(DatagramPacket store) throws IOException
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
		
		log.info("Stopped.");
	}
	

	@Override
	public void deinitModule()
	{
		consumer = null;
		
		log.info("Deinitialized.");
	}
	

	@Override
	public void cleanup()
	{
		if (referee != null)
		{
			referee.interrupt();
			referee = null;
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
	public InetAddress getLocalAddress()
	{
		return receiver == null ? null : receiver.getLocalAddress();
	}
	
	
	@Override
	public int getLocalPort()
	{
		return receiver == null ? IReceiver.UNDEFINED_PORT : receiver.getLocalPort();
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
