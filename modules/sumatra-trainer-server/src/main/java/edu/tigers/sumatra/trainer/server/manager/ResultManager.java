/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.trainer.Result;
import edu.tigers.sumatra.trainer.ResultSet;


public class ResultManager
{
	private static Logger logger = Logger.getLogger(ResultManager.class);
	private static final String RESULT_PATH = "data/";
	private Map<Long, ResultSet> allResults = new ConcurrentHashMap<>();
	
	
	ResultManager()
	{
	}
	
	
	public synchronized boolean acceptResult(ResultSet resultSet)
	{
		if (allResults.containsKey(resultSet.getTrainingSet().getTrainingSetID()))
		{
			return false;
		}
		allResults.put(resultSet.getTrainingSet().getTrainingSetID(), resultSet);
		ManagerRegistry.getTrainingSetManager().releaseTrainingSet(resultSet.getClientID(), true);
		saveResultSet(resultSet);
		return true;
	}
	
	
	private void saveResultSet(ResultSet resultSet)
	{
		for (Result result : resultSet.getResults())
		{
			String path = RESULT_PATH + result.getName();
			createFileIfNotExist(RESULT_PATH, result.getName());
			appendResult(path, result);
		}
	}
	
	
	private void createFileIfNotExist(String path, String name)
	{
		try
		{
			if (!Files.exists(Paths.get(path)))
			{
				Files.createDirectories(Paths.get(path));
			}
			if (!Files.exists(Paths.get(path + name)))
			{
				Files.createFile(Paths.get(path + name));
			}
		} catch (IOException e)
		{
			logger.error(e);
		}
	}
	
	
	private void appendResult(String path, Result result)
	{
		try
		{
			String line = result.getValue() + "\n";
			Files.write(Paths.get(path), line.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e)
		{
			logger.error(e);
		}
	}
	
}
