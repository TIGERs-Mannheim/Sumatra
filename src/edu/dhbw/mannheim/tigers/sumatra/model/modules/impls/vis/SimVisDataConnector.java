/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.vis;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.vis.data.Translator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AVisDataConnector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.util.network.ITransmitter;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPTransmitter;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.exceptions.StartModuleException;


/**
 * This class should gather calculation-data produced by the CS-internals which is worth visualizing, and provide it to
 * the Simulator.
 */
public class SimVisDataConnector extends AVisDataConnector implements Runnable, IAIObserver
{
	// Logger
	private static final Logger						log					= Logger.getLogger(SimVisDataConnector.class
																								.getName());
	
	// Model
	private final SumatraModel							model					= SumatraModel.getInstance();
	
	// Thread
	private Thread											thread;
	private boolean										expectInterrupt	= false;
	private final int										sleepFor;
	
	// Connection
	private ITransmitter<byte[]>						transmitter;
	private final int										localPort;
	private final int										targetPort;
	private final String									address;
	
	private final String									network;
	private final NetworkInterface					nif;
	
	// Input
	private AAgent											agent					= null;
	private final AtomicReference<AIInfoFrame>	frameBuffer			= new AtomicReference<AIInfoFrame>();
	private HashMap<BotID, Path>						pathsBuffer			= new HashMap<BotID, Path>();
	private final Object									pathsSync			= new Object();
	
	// Translation
	private final Translator							translator;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 */
	public SimVisDataConnector(SubnodeConfiguration subnodeConfiguration)
	{
		localPort = subnodeConfiguration.getInt("localPort", 10130);
		targetPort = subnodeConfiguration.getInt("targetPort", 40201);
		address = subnodeConfiguration.getString("address", "224.5.23.2");
		
		sleepFor = Integer.parseInt(subnodeConfiguration.getString("sleepFor", "50"));
		
		network = subnodeConfiguration.getString("interface", "192.168.1.0");
		
		// --- Choose network-interface
		nif = NetworkUtility.chooseNetworkInterface(network, 3);
		if (nif == null)
		{
			log.error("No proper nif for sim-vis in network '" + network + "' found!");
		} else
		{
			log.info("Chose nif for sim-vis: " + nif.getDisplayName() + ".");
		}
		
		translator = new Translator();
	}
	
	
	// --------------------------------------------------------------------------
	// --- collect and send -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void run()
	{
		// Temporary WorldFrames
		AIInfoFrame frame = null;
		// Temporary list of Paths
		HashMap<BotID, Path> paths = null;
		
		try
		{
			while (!Thread.currentThread().isInterrupted())
			{
				// Sleep
				Thread.sleep(sleepFor);
				
				frame = frameBuffer.get();
				
				synchronized (pathsSync)
				{
					paths = pathsBuffer;
					pathsBuffer = new HashMap<BotID, Path>();
				}
				
				
				// Convert wfs to VisData (if there's any)
				if (frame != null)
				{
					translator.translateSituations(frame);
					translator.translateBots(frame);
					
					// Convert path to VisData (if there's any; needs frame!)
					if (paths != null)
					{
						translator.translatePaths(paths, frame.worldFrame);
						paths = null;
					}
					frame = null;
				}
				
				// ...more data
				
				
				// Get SimVisData, convert and send as byte[]
				final SimVisData data = translator.build();
				transmitter.send(data.toByteArray());
				
				// Reset
				translator.reset();
			}
			
		} catch (final InterruptedException err)
		{
			if (!expectInterrupt)
			{
				log.error("Error while sleeping!", err);
			}
		} finally
		{
			// Clear references
			translator.reset();
			
			frame = null;
			
			paths = null;
			
			try
			{
				transmitter.cleanup();
			} catch (final IOException err)
			{
				if (!expectInterrupt)
				{
					log.debug("Error while closing SimVisDataConnector!", err);
				}
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- input ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewPath(Path path)
	{
		synchronized (pathsSync)
		{
			pathsBuffer.put(path.getBotID(), path);
		}
	}
	
	
	@Override
	public void onNewAIInfoFrame(AIInfoFrame lastAIInfoframe)
	{
		frameBuffer.lazySet(lastAIInfoframe);
	}
	
	
	@Override
	public void onAIException(Exception ex, AIInfoFrame frame, AIInfoFrame prevFrame)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- life-cycle -----------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		transmitter = new MulticastUDPTransmitter(localPort, address, targetPort, nif);
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		expectInterrupt = false;
		thread = new Thread(this, "SimVisDataConnector");
		thread.start();
		
		try
		{
			agent = (AAgent) model.getModule(AAgent.MODULE_ID);
			agent.addObserver(this);
			
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + AWorldPredictor.MODULE_ID + "'!", err);
		}
		
		//
		// // register at Sisyphus
		// Sisyphus.getInstance().addObserver(this);
	}
	
	
	@Override
	public void stopModule()
	{
		expectInterrupt = true;
		thread.interrupt();
		
		if (agent != null)
		{
			agent.removeObserver(this);
			agent = null;
		}
	}
	
	
	@Override
	public void deinitModule()
	{
		log.debug("Deinitialized.");
	}
	
}
