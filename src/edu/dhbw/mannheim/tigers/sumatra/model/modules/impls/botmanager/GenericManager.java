/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.BotFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.BotInitException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * The one and only generic botmanager.
 * 
 * @author AndreR
 * 
 */
public class GenericManager extends ABotManager implements IWorldPredictorObserver
{
	// --------------------------------------------------------------
	// --- instance-variables ---------------------------------------
	// --------------------------------------------------------------
	private final Logger								log					= Logger.getLogger(getClass());
	
	private static final String					XML_ENCODING		= "ISO-8859-1";										// "UTF-8";
	private final Map<Integer, ABot>				botTable				= new ConcurrentHashMap<Integer, ABot>();
	private boolean									moduleRunning		= false;
	private AWorldPredictor							wp						= null;
	
	private MulticastDelegate						mcastDelegate		= null;
	private boolean useMulticast = false;
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	/**
	 * Setup properties.
	 * @param properties Properties for module-configuration
	 */
	public GenericManager(SubnodeConfiguration subnodeConfiguration)
	{
		mcastDelegate = new MulticastDelegate(subnodeConfiguration, this);

		loadConfig(selectedPersistentConfig);
		
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addFunctionalObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + AWorldPredictor.MODULE_ID + "'!");
			return;
		}
	}
	

	// --------------------------------------------------------------
	// --- module-method(s) -----------------------------------------
	// --------------------------------------------------------------
	@Override
	public void initModule()
	{
		log.info("Initialized.");
	}
	

	@Override
	public void startModule()
	{
		mcastDelegate.enable(useMulticast);
		
		moduleRunning = true;
		
		loadConfig(selectedPersistentConfig);
		
		log.info("Started.");
	}
	

	@Override
	public void stopModule()
	{
		saveConfig(selectedPersistentConfig);
		
		removeAllBots();

		mcastDelegate.enable(false);
		
		moduleRunning = false;
		
		log.info("Stopped.");
	}
	

	@Override
	public void deinitModule()
	{
		log.info("Deinitialized.");
	}
	

	/**
	 * Be aware of the fact that this method might be called from several threads
	 */
	@Override
	public void execute(int id, ACommand cmd)
	{
		ABot bot = botTable.get(id);
		if (bot != null)
		{
			bot.execute(cmd);
		} else
		{
			log.warn("Execute: Invalid botID: " + id);
		}
	}
	

	public ABot addBot(EBotType type, int id, String name)
	{
		if (botTable.containsKey(id))
		{
			return null;
		}
		
		ABot bot = BotFactory.createBot(type, id, name);
		botTable.put(id, bot);
		
		if (bot.getType() == EBotType.TIGER)
		{
			((TigerBot) bot).setMulticastDelegate(mcastDelegate);
		}
		
		notifyBotAdded(bot);
		
		return bot;
	}
	

	public void removeBot(int id)
	{
		ABot bot = botTable.remove(id);
		
		if (bot.getType() == EBotType.TIGER)
		{
			((TigerBot) bot).setMulticastDelegate(null);
		}
		
		if (bot != null)
		{
			bot.stop();
		}
		
		notifyBotRemoved(bot);
	}
	

	public void changeBotId(int oldId, int newId)
	{
		if (!botTable.containsKey(oldId))
		{
			return;
		}
		
		ABot bot = botTable.get(oldId);
		
		if (botTable.containsKey(newId))
		{
			bot.internalSetBotId(oldId);
			return;
		}
		
		botTable.remove(oldId);
		
		bot.internalSetBotId(newId);
		
		botTable.put(newId, bot);
		
		notifyBotIdChanged(oldId, newId);
	}
	

	@Override
	public Map<Integer, ABot> getAllBots()
	{
		return botTable;
	}
	

	public void removeAllBots()
	{
		stopBots();
		
		for (ABot bot : botTable.values())
		{
			notifyBotRemoved(bot);
		}
		
		botTable.clear();
	}
	
	public void loadConfig(String config)
	{
		if(!getAvailableConfigs().contains(config))
		{
			return;
		}
		
		if(!config.equals(selectedPersistentConfig))
		{
			saveConfig(selectedPersistentConfig);
		}
		
		// load config
		selectedPersistentConfig = config;

		readConfiguration(config, moduleRunning);

		if(moduleRunning)
		{
			startBots();
		}
	}
	
	public List<String> getAvailableConfigs()
	{
		List<String> configs = new ArrayList<String>();
		
		File dir = new File(BOTMANAGER_CONFIG_PATH);
		File[] fileList = dir.listFiles();
		for (File f : fileList)
		{
			if (!f.isHidden())
			{
				String name = f.getName();
				
				configs.add(name);
			}
		}
		
		return configs;
	}
	
	public ITransceiverUDP getMulticastTransceiver()
	{
		return mcastDelegate.getTransceiver();
	}
	
	/**
	 * Can be null if current config has been deleted.
	 * 
	 * @return config file name or NULL
	 */
	public String getLoadedConfig()
	{
		return selectedPersistentConfig;
	}
	
	public void saveConfig(String filename)
	{
		if(filename == null)
		{
			return;
		}
		
		if (!filename.endsWith(".xml"))
		{
			filename += ".xml";
		}
		
		saveConfiguration(BOTMANAGER_CONFIG_PATH + filename);
		
		selectedPersistentConfig = filename;
	}
	
	public void deleteConfig(String config)
	{
		if(selectedPersistentConfig == null)
		{
			return;
		}
		
		File file = new File(BOTMANAGER_CONFIG_PATH + selectedPersistentConfig);
		
		if(file.exists())
		{
			if(!file.delete())
			{
				log.warn("Could not delete config");
			}
			else
			{
				selectedPersistentConfig = null;

				log.debug("Deleted file: " + file.getAbsoluteFile());
			}
		}
	}
	
	public void setUseMulticast(boolean enable)
	{
		useMulticast = enable;
		
		mcastDelegate.enable(enable);
	}
	
	public boolean getUseMulticast()
	{
		return useMulticast;
	}
	
	public void setUpdateAllSleepTime(long time)
	{
		mcastDelegate.setUpdateAllSleepTime(time);
	}
	
	public long getUpdateAllSleepTime()
	{
		return mcastDelegate.getUpdateAllSleepTime();
	}

	private boolean readConfiguration(String filename, boolean loadBots)
	{
		removeAllBots();
		
		XMLConfiguration config;
		
		filename = BOTMANAGER_CONFIG_PATH + filename;
		
		try
		{
			config = new XMLConfiguration(filename);
		} catch (ConfigurationException err)
		{
			log.error("Could not read botfile: " + filename);
			return false;
		}
		
		useMulticast = config.getBoolean("multicast", false);
		mcastDelegate.setUpdateAllSleepTime(config.getLong("updateAllSleep", 20));
		
		List<?> bots = config.configurationsAt("bots.bot");
		for (Iterator<?> it = bots.iterator(); it.hasNext();)
		{
			SubnodeConfiguration botConfig = (SubnodeConfiguration) it.next();
			
			try
			{
				ABot bot = BotFactory.createBot(botConfig);
				
				botTable.put(bot.getBotId(), bot);
				notifyBotAdded(bot);
				
			} catch (BotInitException err)
			{
				log.error("Error while instantiating bot from '" + filename + "': " + err.getMessage(), err);
				removeAllBots();
				return false;
			}
		}
		
		return true;
	}
	

	private void saveConfiguration(String filename)
	{
		XMLConfiguration config = new XMLConfiguration();
		config.setRootElementName("botmanager");
		config.setEncoding(XML_ENCODING);
		
		CombinedConfiguration botmanager = new CombinedConfiguration();
		
		botmanager.addProperty("multicast", useMulticast);
		botmanager.addProperty("updateAllSleep", mcastDelegate.getUpdateAllSleepTime());
		
		List<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
		
		List<Integer> sortedBots = new ArrayList<Integer>();
		sortedBots.addAll(botTable.keySet());
		Collections.sort(sortedBots);
		
		for (Integer i : sortedBots)
		{
			ABot bot = botTable.get(i);
			nodes.add(bot.getConfiguration().getRootNode().getChild(0));
		}
		
		botmanager.addNodes("bots", nodes);
		
		config.setRootNode(botmanager.getRootNode());
		
		
		// Write formatted string to XML-file
		try
		{
			// Save unformatted data to dummy-out, which creates our document as side-effect...
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			config.save(new OutputStreamWriter(bOut, XML_ENCODING));
			
			Document doc = config.getDocument();
			

			// Format document
			DOMImplementationLS domLS = (DOMImplementationLS) doc.getImplementation();
			
			LSSerializer serializer = domLS.createLSSerializer();
			
			DOMConfiguration domConfig = serializer.getDomConfig();
			if (domConfig.canSetParameter("format-pretty-print", Boolean.TRUE))
			{
				domConfig.setParameter("format-pretty-print", Boolean.TRUE);
			}
			serializer.setNewLine("\n");
			

			// Prepare output...
			FileOutputStream fOut = new FileOutputStream(filename, false);
			
			LSOutput output = domLS.createLSOutput();
			output.setEncoding(XML_ENCODING);
			output.setByteStream(fOut);
			

			// ...and finally write!
			serializer.write(doc, output);
			
			fOut.close();
		} catch (IOException err)
		{
			log.error("Error while saving file!", err);
		} catch (ConfigurationException err)
		{
			log.error("Error while buffering (unformatted) config!", err);
		}
	}
	

	private void stopBots()
	{
		for (ABot bot : botTable.values())
		{
			bot.stop();
			
			if (bot.getType() == EBotType.TIGER)
			{
				((TigerBot) bot).setMulticastDelegate(null);
			}
		}
	}
	

	private void startBots()
	{
		for (ABot bot : botTable.values())
		{
			if (bot.getType() == EBotType.TIGER)
			{
				((TigerBot) bot).setMulticastDelegate(mcastDelegate);
			}
			
			bot.start();
		}
	}
	

	@Override
	public Map<String, EBotType> getBotTypeMap()
	{
		Map<String, EBotType> types = new Hashtable<String, EBotType>();
		
		types.put("Tiger Bot", EBotType.TIGER);
		types.put("CT Bot", EBotType.CT);
		types.put("Sysout Bot", EBotType.SYSOUT);
		
		return types;
	}

	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		List<Integer> botsOnField = new ArrayList<Integer>();
		
		for(Integer i : wf.tigerBots.keySet())
		{
			botsOnField.add(i);
		}
		
		mcastDelegate.setOnFieldBots(botsOnField);
	}
}
