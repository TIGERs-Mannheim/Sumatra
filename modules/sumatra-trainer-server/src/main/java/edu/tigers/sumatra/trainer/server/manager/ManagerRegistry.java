/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server.manager;

import java.io.PrintWriter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;


public class ManagerRegistry
{
	
	private static final ManagerRegistry instance = new ManagerRegistry();
	
	private static ResultManager resultManager;
	private static TrainingSetManager trainingSetManager;
	private static ClientManager clientManager;
	
	
	@SuppressWarnings("squid:S106") // Needed System.out for tomcat
	private ManagerRegistry()
	{
		ConsoleAppender appender = new ConsoleAppender();
		appender.activateOptions();
		appender.setWriter(new PrintWriter(System.out));
		appender.setLayout(new SimpleLayout());
		Logger.getRootLogger().addAppender(appender);
		
		resultManager = new ResultManager();
		trainingSetManager = new TrainingSetManager();
		clientManager = new ClientManager();
	}
	
	
	public static ClientManager getClientManager()
	{
		return clientManager;
	}
	
	
	public static ResultManager getResultManager()
	{
		return resultManager;
	}
	
	
	public static TrainingSetManager getTrainingSetManager()
	{
		return trainingSetManager;
	}
	
	
	public static synchronized ManagerRegistry getInstance()
	{
		return instance;
	}
}
