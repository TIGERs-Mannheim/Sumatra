/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.client;

import java.io.PrintWriter;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import edu.tigers.sumatra.trainer.Result;
import edu.tigers.sumatra.trainer.ResultSet;
import edu.tigers.sumatra.trainer.TrainingSet;


public class SumatraTrainerClient
{
	private static Logger logger = Logger.getLogger(SumatraTrainerClient.class);
	private static final String HOSTNAME = "http://sumatra-trainer-server:8080";
	private static final String BASE_PATH = "/sumatra-trainer-server/sumatra/";
	private static final int WAITING_DURATION = 30000;
	
	
	@SuppressWarnings("squid:S106") // Needed to see from docker
	public static void main(String... args) throws InterruptedException
	{
		ConsoleAppender appender = new ConsoleAppender();
		appender.activateOptions();
		appender.setWriter(new PrintWriter(System.out));
		appender.setLayout(new SimpleLayout());
		Logger.getRootLogger().addAppender(appender);
		
		Client client = ClientBuilder.newClient();
		client.register(JacksonJsonProvider.class);
		WebTarget target = client.target(HOSTNAME + BASE_PATH);
		
		Long id = Long.parseLong(target.path("client/new").request().post(null, String.class));
		try
		{
			logger.info("Receive client id:" + id);
			
			TrainingSet trainingSet = getTrainingSet(target, id);
			
			while (trainingSet != null)
			{
				SumatraTrainer trainer = new SumatraTrainer();
				List<Result> results = trainer.doTraining(trainingSet.getDuration());
				ResultSet rs = new ResultSet();
				rs.setResults(results);
				rs.setTrainingSet(trainingSet);
				rs.setClientID(id);
				
				target.path("training/result").request().post(Entity.json(rs)).close();
				
				trainingSet = getTrainingSet(target, id);
				logger.info("Get new training set " + trainingSet.getTrainingSetID());
			}
		} catch (InterruptedException e)
		{
			client.close();
			throw e;
		}
	}
	
	
	private static TrainingSet getTrainingSet(WebTarget target, long id) throws InterruptedException
	{
		TrainingSet trainingSet;
		do
		{
			trainingSet = target.path("training-set/get/" + id).request(MediaType.APPLICATION_JSON)
					.get(TrainingSet.class);
			if (trainingSet == null)
			{
				Thread.sleep(WAITING_DURATION);
			}
		} while (trainingSet == null);
		return trainingSet;
	}
	
}
