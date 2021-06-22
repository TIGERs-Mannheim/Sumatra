/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;


import com.google.protobuf.ByteString;
import edu.tigers.sumatra.referee.proto.SslGcRcon;
import edu.tigers.sumatra.referee.proto.SslGcRconTeam;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class TestTeamClient
{
	private static final Logger log = LogManager.getLogger(TestTeamClient.class);
	private static MessageSigner signer;
	private static String nextToken;


	public static void main(String[] args) throws IOException
	{
		signer = new MessageSigner(
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.key.pem.pkcs8", StandardCharsets.UTF_8),
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.pub.pem", StandardCharsets.UTF_8));
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
		SslGcRconTeam.ControllerToTeam request;
		request = gc.receiveMessage(SslGcRconTeam.ControllerToTeam.parser());
		if (request == null)
		{
			log.error("Failed to receive Controller Request");
		}
	}


	private static void setKeeper(GameControllerProtocol gc, int keeper)
	{
		SslGcRconTeam.TeamToController.Builder keeperRequest = SslGcRconTeam.TeamToController
				.newBuilder();
		keeperRequest.setDesiredKeeper(keeper);
		keeperRequest.getSignatureBuilder().setPkcs1V15(ByteString.EMPTY).setToken(nextToken);

		ByteString signature = ByteString.copyFrom(signer.sign(keeperRequest.build().toByteArray()));
		keeperRequest.getSignatureBuilder().setPkcs1V15(signature);

		gc.sendMessage(keeperRequest.build());
	}


	private static boolean register(GameControllerProtocol gc)
	{
		SslGcRcon.ControllerReply reply;
		reply = gc.receiveMessage(SslGcRcon.ControllerReply.parser());
		if (reply == null)
		{
			log.error("Receiving initial Message failed");
			return false;
		}

		SslGcRconTeam.TeamRegistration.Builder registration = SslGcRconTeam.TeamRegistration.newBuilder();
		registration.setTeamName("TIGERs Mannheim");
		registration.getSignatureBuilder().setToken(reply.getNextToken()).setPkcs1V15(ByteString.EMPTY);

		ByteString signature = ByteString.copyFrom(signer.sign(registration.build().toByteArray()));
		registration.getSignatureBuilder().setPkcs1V15(signature);

		gc.sendMessage(registration.build());

		reply = gc.receiveMessage(SslGcRcon.ControllerReply.parser());
		if (reply == null)
		{
			log.error("Receiving Registration Reply failed");
			return false;
		}

		if (reply.getStatusCode() != SslGcRcon.ControllerReply.StatusCode.OK)
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
