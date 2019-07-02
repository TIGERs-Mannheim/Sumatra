/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;


import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.protobuf.ByteString;

import edu.tigers.sumatra.SslGameControllerCommon;
import edu.tigers.sumatra.SslGameControllerTeam;


public class TestTeamClient
{
	
	private static final Logger log = Logger.getLogger(TestTeamClient.class);
	private static MessageSigner signer;
	private static String nextToken;
	
	
	public static void main(String[] args) throws IOException
	{
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		
		signer = new MessageSigner(
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.key.pem.pkcs8", Charset.forName("UTF-8")),
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.pub.pem", Charset.forName("UTF-8")));
		GameControllerProtocol gc = new GameControllerProtocol("localhost", 10008);
		gc.addConnectedHandler(() -> {
			if (!register(gc))
			{
				log.error("Registration Failed!");
				gc.disconnect();
				System.exit(1);
			}
		});
		
		gc.connectBlocking();
		if (!gc.isConnected())
		{
			log.error("Connection failed!");
			return;
		}
		
		setKeeper(gc, 1);
		
		// noinspection InfiniteLoopStatement
		while (true)
		{
			handleRequests(gc);
		}
	}
	
	
	private static void handleRequests(GameControllerProtocol gc)
	{
		SslGameControllerTeam.ControllerToTeam request;
		request = gc.receiveMessage(SslGameControllerTeam.ControllerToTeam.parser());
		if (request == null)
		{
			log.error("Failed to receive Controller Request");
			return;
		}
		
		if (request.hasAdvantageChoice())
		{
			log.info("Replying to Advantage choice request: " + request.getAdvantageChoice().getFoul());
			SslGameControllerTeam.TeamToController.Builder response = SslGameControllerTeam.TeamToController
					.newBuilder();
			response.setAdvantageResponse(SslGameControllerTeam.TeamToController.AdvantageResponse.CONTINUE);
			response.getSignatureBuilder().setToken(nextToken).setPkcs1V15(ByteString.EMPTY);
			
			ByteString signature = ByteString.copyFrom(signer.sign(response.build().toByteArray()));
			response.getSignatureBuilder().setPkcs1V15(signature);
			
			gc.sendMessage(response.build());
		}
	}
	
	
	private static void setKeeper(GameControllerProtocol gc, int keeper)
	{
		SslGameControllerTeam.TeamToController.Builder keeperRequest = SslGameControllerTeam.TeamToController
				.newBuilder();
		keeperRequest.setDesiredKeeper(keeper);
		keeperRequest.getSignatureBuilder().setPkcs1V15(ByteString.EMPTY).setToken(nextToken);
		
		ByteString signature = ByteString.copyFrom(signer.sign(keeperRequest.build().toByteArray()));
		keeperRequest.getSignatureBuilder().setPkcs1V15(signature);
		
		gc.sendMessage(keeperRequest.build());
	}
	
	
	private static boolean register(GameControllerProtocol gc)
	{
		SslGameControllerCommon.ControllerReply reply;
		reply = gc.receiveMessage(SslGameControllerCommon.ControllerReply.parser());
		if (reply == null)
		{
			log.error("Receiving initial Message failed");
			return false;
		}
		
		SslGameControllerTeam.TeamRegistration.Builder registration = SslGameControllerTeam.TeamRegistration.newBuilder();
		registration.setTeamName("TIGERs Mannheim");
		registration.getSignatureBuilder().setToken(reply.getNextToken()).setPkcs1V15(ByteString.EMPTY);
		
		ByteString signature = ByteString.copyFrom(signer.sign(registration.build().toByteArray()));
		registration.getSignatureBuilder().setPkcs1V15(signature);
		
		gc.sendMessage(registration.build());
		
		reply = gc.receiveMessage(SslGameControllerCommon.ControllerReply.parser());
		if (reply == null)
		{
			log.error("Receiving Registration Reply failed");
			return false;
		}
		
		if (reply.getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.error(
					"Server did not allow Registration: " + reply.getStatusCode().toString() + " - " + reply.getReason());
			return false;
		} else
		{
			nextToken = reply.getNextToken();
			log.info("Successfully registered");
		}
		
		return true;
	}
}
