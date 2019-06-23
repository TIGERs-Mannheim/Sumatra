/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ml.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ALearnedModel
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(ALearnedModel.class.getName());
	private final String				configPath;
	private final String				identifier;
	
	protected double[]				p;
	
	
	/**
	 * 
	 */
	protected ALearnedModel(final String base, final String identifier)
	{
		configPath = "config/" + base + "/";
		this.identifier = identifier;
		
		String parameters = "";
		try
		{
			Path path = getConfigPath(identifier);
			if (path.toFile().exists())
			{
				List<String> lines = Files.readAllLines(path);
				
				if (lines.isEmpty())
				{
					log.error("Invalid config file. Expected one line, but is: " + lines.size());
				} else
				{
					parameters = lines.get(0);
				}
			}
		} catch (IOException err)
		{
			log.error("Could not read config file for identifier " + identifier, err);
		}
		
		if (!parameters.isEmpty())
		{
			String[] parametersList = parameters.split(",");
			p = new double[parametersList.length];
			for (int i = 0; i < parametersList.length; i++)
			{
				p[i] = Double.parseDouble(parametersList[i]);
			}
		} else
		{
			log.warn("Loading default parameters from class");
			p = getDefaultParams();
		}
	}
	
	
	protected Path getConfigPath(final String identifier)
	{
		return Paths.get(configPath + identifier + ".cfg");
	}
	
	
	protected abstract double[] getDefaultParams();
	
	
	/**
	 * @param params
	 */
	public void applyNewParameters(final double[] params)
	{
		StringBuilder sb = new StringBuilder();
		p = new double[params.length];
		for (int i = 0; i < params.length; i++)
		{
			p[i] = params[i];
			sb.append(p[i]);
			sb.append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		
		try
		{
			Files.write(getConfigPath(identifier), sb.toString().getBytes());
		} catch (IOException e)
		{
			log.error("Fatal error, couldn't write configFile: " + getConfigPath(identifier), e);
		}
		
		log.info("new parameters have been set: " + Arrays.toString(p));
		onNewParameters();
	}
	
	
	protected void onNewParameters()
	{
	}
	
	
	/**
	 * @return the p
	 */
	public double[] getP()
	{
		return p;
	}
}
