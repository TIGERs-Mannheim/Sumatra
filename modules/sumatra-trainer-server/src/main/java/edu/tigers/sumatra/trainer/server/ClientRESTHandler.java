/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.trainer.server.manager.ManagerRegistry;


@Path("client")
public class ClientRESTHandler
{
	private static Logger logger = Logger.getLogger(ClientRESTHandler.class);

	
	
	@POST
	@Path("new")
	@Produces(MediaType.TEXT_PLAIN)
	public synchronized long createNewUserID()
	{
		logger.info("New client registered");
		return ManagerRegistry.getClientManager().newClient();
	}
	
}
