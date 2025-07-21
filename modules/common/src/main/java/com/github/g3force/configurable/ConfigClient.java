/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package com.github.g3force.configurable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Base implementation for {@link IConfigClient}
 */
public class ConfigClient implements IConfigClient
{
	private static final Logger log = LogManager.getLogger(ConfigClient.class.getName());

	private static final String XML_ENCODING = "UTF-8";

	private final String name;
	private final String path;
	private final List<IConfigObserver> observers = new CopyOnWriteArrayList<>();

	private final ConfigAnnotationProcessor cap;
	private final Set<Class<?>> classes = new LinkedHashSet<>();

	private HierarchicalConfiguration config = new HierarchicalConfiguration();


	public ConfigClient(final String path, final String name)
	{
		this.name = name;
		this.path = path;
		cap = new ConfigAnnotationProcessor(name);
	}


	@Override
	public void addObserver(final IConfigObserver observer)
	{
		observers.add(observer);
	}


	@Override
	public void removeObserver(final IConfigObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * Add a configurable class
	 *
	 * @param clazz the class to add
	 */
	public void putClass(final Class<?> clazz)
	{
		classes.add(clazz);
		cap.loadClass(clazz, false);
	}


	public void applyConfig()
	{
		cap.loadConfiguration(config);
		cap.applyAll();
		notifyAppliedConfig();
	}


	@Override
	public HierarchicalConfiguration getFileConfig()
	{
		String fileName = name + ".xml";
		Path fPath = Paths.get(path, fileName);
		String filePath = fPath.toString();
		XMLConfiguration cfg = new XMLConfiguration();
		try
		{
			cfg.setDelimiterParsingDisabled(true);
			cfg.setFileName(fileName);
			if (fPath.toFile().exists())
			{
				cfg.load(filePath);
			}
		} catch (final ConfigurationException err)
		{
			log.error("Unable to load config '{}' from '{}':", name, filePath, err);
		}

		return cfg;
	}


	private void notifyAppliedConfig()
	{
		for (IConfigObserver o : observers)
		{
			o.afterApply(this);
		}
	}


	@Override
	public boolean saveCurrentConfig()
	{
		String fileName = name + ".xml";
		String filePath = Paths.get(path, fileName).toString();

		try (FileOutputStream targetFile = new FileOutputStream(filePath, false);
				OutputStream prettyOut = new PrettyXMLOutputStream(targetFile, XML_ENCODING))
		{
			XMLConfiguration xmlConfig = new XMLConfiguration(cap.getMinimalConfig());
			xmlConfig.save(prettyOut, XML_ENCODING);
		} catch (final ConfigurationException err)
		{
			log.error("Unable to save config '{}' to '{}'.", name, filePath);
			return false;
		} catch (final FileNotFoundException err)
		{
			log.error("Unable to access the file to save the config to: {}", filePath, err);
		} catch (IOException e)
		{
			log.error("Error while saving config: Unable to close streams!", e);
		}

		return true;
	}


	@Override
	public final String getName()
	{
		return name;
	}


	@Override
	public final String getPath()
	{
		return path;
	}


	@Override
	public final HierarchicalConfiguration loadConfig()
	{
		cap.loadConfiguration(getFileConfig());
		classes.forEach(clazz -> cap.loadClass(clazz, false));
		config = cap.getEffectiveConfig();
		return config;
	}


	@Override
	public final void readClasses()
	{
		classes.forEach(clazz -> cap.loadClass(clazz, true));
		config = cap.getEffectiveConfig();
	}


	@Override
	public final HierarchicalConfiguration getConfig()
	{
		return config;
	}


	/**
	 * @return the cap
	 */
	final ConfigAnnotationProcessor getCap()
	{
		return cap;
	}
}
