/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.configs;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.fasterxml.jackson.core.PrettyPrinter.DEFAULT_SEPARATORS;


/**
 * Database Manager for saved bot config files.
 *
 * @author UlrikeL
 */
@Log4j2
public class ConfigFileDatabaseManager
{
	private static final String DATABASE_FILE = "config/bot-configs.json";
	private final ConfigFileDatabase database;
	private final List<IConfigFileDatabaseObserver> observers = new CopyOnWriteArrayList<>();


	public ConfigFileDatabaseManager()
	{
		database = loadDatabase();
	}


	public void addObserver(IConfigFileDatabaseObserver observer)
	{
		observers.add(observer);
	}


	public void removeObserver(IConfigFileDatabaseObserver observer)
	{
		observers.remove(observer);
	}


	public IConfigFileDatabase getDatabase()
	{
		return database;
	}


	private ConfigFileDatabase loadDatabase()
	{
		File file = Paths.get(DATABASE_FILE).toFile();
		if (file.exists())
		{
			log.debug("Open existing bot config file database");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

			try
			{
				return mapper.readValue(file, ConfigFileDatabase.class);
			} catch (IOException e)
			{
				log.error("Could not read from database: {}", file, e);
			}
		}
		return new ConfigFileDatabase();
	}


	private void saveDatabase()
	{
		File file = Paths.get(DATABASE_FILE).toFile();

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter()
				.withObjectIndenter(new DefaultIndenter().withLinefeed(System.lineSeparator()))
				.withSeparators(DEFAULT_SEPARATORS.withObjectFieldValueSpacing(Separators.Spacing.AFTER))
		);

		try
		{
			mapper.writeValue(file, database);
		} catch (IOException e)
		{
			log.error("Could not save database: {}", file, e);
		}
	}


	public void addEntry(final ConfigFile file)
	{
		database.addEntry(file);
		saveDatabase();
		observers.forEach(observer -> observer.onConfigFileAdded(file));
	}


	public void deleteEntry(final int configId, final int version)
	{
		database.deleteEntry(configId, version);
		saveDatabase();
		observers.forEach(o -> o.onConfigFileRemoved(configId, version));
	}


	public Optional<ConfigFile> getSelectedEntry(final int configId, final int version)
	{
		return database.getSelectedEntry(configId, version);
	}


	public void setAutoUpdateFor(int configId, int version, boolean update)
	{
		database.setAutoUpdateFor(configId, version, update);
		saveDatabase();
	}


	public boolean isAutoUpdate(final int configId, final int version)
	{
		return database.isAutoUpdate(configId, version);
	}


	public Map<Integer, Map<Integer, Map<String, Object>>> getAllSavedConfigs()
	{
		return database.getSavedConfigFiles();
	}
}



