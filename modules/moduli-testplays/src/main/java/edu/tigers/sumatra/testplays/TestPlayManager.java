/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.testplays.commands.CommandList;
import edu.tigers.sumatra.testplays.commands.PathCommand;
import edu.tigers.sumatra.testplays.commands.SynchronizeCommand;
import edu.tigers.sumatra.testplays.util.Point;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class TestPlayManager implements IConfigObserver
{
	
	private static final String									DEFAULT_FILE	= "data/testplay.json";
	
	@Configurable(comment = "The json file to load the paths from")
	private static String											pathFile			= DEFAULT_FILE;
	
	private static TestPlayManager								instance			= null;
	private static final Logger									log				= Logger
			.getLogger(TestPlayManager.class.getName());
	
	private List<CommandList>										commandQueue	= new ArrayList<>();
	private final transient List<ITestPlayDataObserver>	observers		= new CopyOnWriteArrayList<>();
	
	static
	{
		ConfigRegistration.registerClass("testPlays", TestPlayManager.class);
	}
	
	
	private TestPlayManager()
	{
		
		if (pathFile.equals(DEFAULT_FILE) && !(new File(pathFile)).exists())
		{
			try
			{
				log.info("Creating default path file.");
				savePathToFile();
			} catch (IOException e)
			{
				log.error("Not able to create default file.", e);
			}
		}
		
		afterApply(null);
		ConfigRegistration.registerConfigurableCallback("testPlays", this);
	}
	
	
	public static synchronized TestPlayManager getInstance()
	{
		
		if (instance == null)
		{
			instance = new TestPlayManager();
		}
		
		return instance;
		
	}
	
	
	public static synchronized void setPathFile(String path)
	{
		
		pathFile = path;
	}
	
	
	public static synchronized String getPathFile()
	{
		
		return pathFile;
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		
		if (configClient != null)
		{
			
			TestPlayManager.setPathFile(configClient.getConfig().getString("pathFile"));
		}
		
		reloadFromFile();
	}
	
	
	/**
	 * Reloads the list from the configured file.
	 */
	public void reloadFromFile()
	{
		
		File file = new File(pathFile);
		if (!file.exists())
		{
			log.error("Not able to open json path file (file does not exist).");
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			commandQueue = mapper.readValue(file, new TypeReference<Queue<CommandList>>()
			{
			});
		} catch (IOException e)
		{
			log.error("Not able to read command file!", e);
		}
		
		notifyObservers();
	}
	
	
	/**
	 * Saves the currently loaded role list to the configured file
	 * 
	 * @throws IOException
	 */
	public void savePathToFile() throws IOException
	{
		
		File file = new File(pathFile);
		if (!file.exists())
		{
			File parent = new File(file.getParent());
			if (!parent.exists() && !parent.mkdirs())
			{
				log.error("Not able to create directory '" + parent + "'!");
				return;
			}
		}
		
		log.info("Saving to " + pathFile);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		
		mapper.writeValue(file, commandQueue);
	}
	
	
	public List<CommandList> getCommandQueue()
	{
		
		return commandQueue;
	}
	
	
	/**
	 * Adds a new list of commands to the currently loaded command list
	 * 
	 * @param role
	 */
	public void addRole(CommandList role)
	{
		
		commandQueue.add(role);
		notifyObservers();
	}
	
	
	/**
	 * Removes a list of commands from the currently loaded command list
	 * 
	 * @param role
	 */
	public void removeRole(CommandList role)
	{
		
		commandQueue.remove(role);
		notifyObservers();
	}
	
	
	/**
	 * Creates a dummy role list
	 */
	public void createDummyPath()
	{
		
		CommandList cl = new CommandList();
		List<Point> points = new ArrayList<>();
		points.add(new Point(0, 0));
		points.add(new Point(1000, 1000));
		
		cl.add(new PathCommand(points));
		cl.add(new SynchronizeCommand());
		
		addRole(cl);
	}
	
	
	/**
	 * Adds an observer
	 * 
	 * @param o
	 */
	public void addObserver(ITestPlayDataObserver o)
	{
		
		observers.add(o);
	}
	
	
	/**
	 * Removes an observer
	 * 
	 * @param o
	 */
	public void removeObserver(ITestPlayDataObserver o)
	{
		
		observers.remove(o);
	}
	
	
	private void notifyObservers()
	{
		
		for (ITestPlayDataObserver o : observers)
		{
			o.onTestPlayDataUpdate();
		}
	}
	
}
