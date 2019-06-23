/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.trainer.ResultSet;
import edu.tigers.sumatra.trainer.server.manager.ManagerRegistry;


@Path("training")
public class ResultSetRESTHandler
{
	
	private static Logger logger = Logger.getLogger(ResultSetRESTHandler.class);
	
	
	@Path("result")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void putResult(ResultSet resultSet)
	{
		logger.info("Received result set from " + resultSet.getClientID() + " of TrainingSet "
				+ resultSet.getTrainingSet().getTrainingSetID());
		ManagerRegistry.getResultManager().acceptResult(resultSet);
	}
	
}
