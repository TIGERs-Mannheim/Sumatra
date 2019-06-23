/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botparams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botparams.BotParamsDatabase.IBotParamsDatabaseObserver;


/**
 * BotParams module.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BotParamsManager extends AModule implements IBotParamsDatabaseObserver
{
	/** */
	public static final String MODULE_TYPE = "BotParams";
	/** */
	public static final String MODULE_ID = "botparams";
	
	private static final String DATABASE_FILE = "config/botParamsDatabase.json";
	
	private BotParamsDatabase database = new BotParamsDatabase();
	
	private static final Logger log = Logger
			.getLogger(BotParamsManager.class.getName());
	
	private final List<IBotParamsManagerObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * Moduli constructor.
	 * 
	 * @param subnodeConfiguration
	 */
	public BotParamsManager(final SubnodeConfiguration subnodeConfiguration)
	{
		// not used
	}
	
	
	@Override
	public void deinitModule()
	{
		// not used
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		loadDatabase();
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		// not used
	}
	
	
	@Override
	public void stopModule()
	{
		saveDatabase();
	}
	
	
	/**
	 * Only used internally for presenter.
	 * 
	 * @return
	 */
	public BotParamsDatabase getDatabase()
	{
		return database;
	}
	
	
	/**
	 * Get robot parameters for a specific label.
	 * 
	 * @param label
	 * @return
	 */
	public IBotParams getBotParams(final EBotParamLabel label)
	{
		return database.getSelectedParams(label);
	}
	
	
	private void saveDatabase()
	{
		File file = Paths.get(DATABASE_FILE).toFile();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter()
				.withObjectIndenter(new DefaultIndenter().withLinefeed("\n"))
				.withoutSpacesInObjectEntries());
		
		try
		{
			mapper.writeValue(file, database);
		} catch (IOException e)
		{
			log.error("", e);
		}
	}
	
	
	private void loadDatabase()
	{
		File file = Paths.get(DATABASE_FILE).toFile();
		if (!file.exists())
		{
			log.info("Initializing empty bot params database");
			database = new BotParamsDatabase();
		} else
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			
			try
			{
				database = mapper.readValue(file, BotParamsDatabase.class);
			} catch (IOException e)
			{
				log.error("", e);
			}
		}
		
		database.addObserver(this);
	}
	
	
	@Override
	public void onEntryAdded(final String entry, final BotParams newParams)
	{
		saveDatabase();
	}
	
	
	@Override
	public void onEntryUpdated(final String entry, final BotParams newParams)
	{
		saveDatabase();
		
		database.getSelectedParams().entrySet().stream()
				.filter(e -> e.getValue().equals(entry))
				.forEach(e -> notifyBotParamsUpdated(e.getKey(), newParams));
	}
	
	
	@Override
	public void onBotParamLabelUpdated(final EBotParamLabel label, final String newEntry)
	{
		saveDatabase();
		
		notifyBotParamsUpdated(label, getBotParams(label));
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBotParamsManagerObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBotParamsManagerObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyBotParamsUpdated(final EBotParamLabel label, final IBotParams params)
	{
		for (IBotParamsManagerObserver observer : observers)
		{
			observer.onBotParamsUpdated(label, params);
		}
	}
	
	
	/**
	 * BotParamsManager observer.
	 */
	@FunctionalInterface
	public static interface IBotParamsManagerObserver
	{
		/**
		 * Bot parameters of a specific label have changed.
		 * 
		 * @param label
		 * @param params
		 */
		void onBotParamsUpdated(EBotParamLabel label, IBotParams params);
	}
}
