/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.trainer.TrainingSet;
import edu.tigers.sumatra.trainer.server.manager.ManagerRegistry;
import edu.tigers.sumatra.trainer.server.manager.TrainingSetManager;


@Path("training-set")
public class TrainingSetRESTHandler
{
	
	private static Logger logger = Logger.getLogger(TrainingSetRESTHandler.class);
	private TrainingSetManager trainingSetManager = ManagerRegistry.getTrainingSetManager();
	
	
	@GET
	@Path("get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrainingSet(@PathParam("id") long id)
	{
		TrainingSet trainingSet = trainingSetManager.acquireTrainingSet(id);
		logger.info("Delivered TrainingSet " + trainingSet.getTrainingSetID() + " to Client " + id);
		return Response.ok(trainingSet).build();
		
	}
	
	
	@POST
	@Path("new")
	@Consumes(MediaType.APPLICATION_JSON)
	public void postTrainingSet(TrainingSet trainingSet)
	{
		trainingSetManager.addTrainingSet(trainingSet);
		logger.info("Add training set " + trainingSet.toString());
	}
	
}
