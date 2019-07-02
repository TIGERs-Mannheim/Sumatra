/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.teamclient;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.protobuf.ByteString;

import edu.tigers.sumatra.SslGameControllerCommon;
import edu.tigers.sumatra.SslGameControllerTeam;
import edu.tigers.sumatra.SslGameControllerTeam.TeamToController;
import edu.tigers.sumatra.SslGameControllerTeam.TeamToController.AdvantageResponse;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.GameControllerProtocol;
import edu.tigers.sumatra.referee.MessageSigner;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class TeamClientTask implements Runnable, GameControllerProtocol.IConnectedHandler
{

	private static final Logger log = Logger.getLogger(TeamClientTask.class);
	private static final String DEFAULT_HOSTNAME = "localhost";
	private static final int DEFAULT_PORT = 10008;
private static final long PING_INTERVAL = 1_000_000_000;

	private GameControllerProtocol gameController;
	private String teamName;
	private ETeamColor teamColor;
	private String nextToken;
	private MessageSigner signer;

	private BotID lastRequestedKeeperId = null;
	private boolean botSubstitutionDesired = false;

	private boolean active = true;

	private BlockingQueue<AIInfoFrame> aiFrames = new LinkedBlockingQueue<>();

private long nextPing = 0;

	public TeamClientTask(ETeamColor teamColor)
	{
		this.teamColor = teamColor;

		AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
		String hostname = referee.getActiveSource().getRefBoxAddress()
				.map(InetAddress::getHostAddress)
				.orElse(DEFAULT_HOSTNAME);
		int port = SumatraModel.getInstance().getModule(TeamClientModule.class).getSubnodeConfiguration()
				.getInt("gameControllerPort", DEFAULT_PORT);

		gameController = new GameControllerProtocol(hostname, port);
		gameController.addConnectedHandler(this);
	}


	public void setBotSubstitutionDesired(boolean desired)
	{
		botSubstitutionDesired = desired;
	}


	public void submitNewAiFrame(AIInfoFrame aiFrame)
	{
		this.aiFrames.add(aiFrame);
	}


	public boolean getActive()
	{
		return active;
	}


	public void setActive(boolean active)
	{
		this.active = active;
	}


	public ETeamColor getTeamColor()
	{
		return this.teamColor;
	}


	/**
	 * The basic loop of the team client is as follows:
	 * 1) check if we have a new advantage choice
	 * 2) if not, check if the keeper is correct
	 * 3) Check if we would like to substitute a bot
	 */
	@Override
	public void run()
	{
		Thread.currentThread().setName("TeamClient::unknown-" + teamColor.toString());
		AIInfoFrame aiFrame;
		while (active)
		{
			try
			{
				aiFrame = aiFrames.take();
				checkAndHandleAiFrame(aiFrame);

			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}

		gameController.disconnect();
	}

private void checkConnection()
	{
		SslGameControllerTeam.TeamToController.Builder ping = SslGameControllerTeam.TeamToController.newBuilder()
				.setPing(true);
		signMessage(ping);

		/*
		 * If the connection should fail, the GameControllerProtocol class
		 * will try to reconnect.
		 */
		gameController.sendMessage(ping.build());
		SslGameControllerTeam.ControllerToTeam reply = gameController.receiveMessage(
				SslGameControllerTeam.ControllerToTeam.parser());

		if (reply != null)
		{
			nextToken = reply.getControllerReply().getNextToken();
		}
	}


	@Override
	public void onConnect()
	{
		SslGameControllerTeam.ControllerToTeam reply = gameController
				.receiveMessage(SslGameControllerTeam.ControllerToTeam.parser());
		if (reply == null || !reply.hasControllerReply())
		{
			log.error("Receiving initial Message failed!");
			return;
		}

		nextToken = reply.getControllerReply().getNextToken();

		SslGameControllerTeam.TeamRegistration.Builder registration = SslGameControllerTeam.TeamRegistration.newBuilder();

		registration.setTeamName(teamName);

		registration.getSignatureBuilder().setToken(nextToken).setPkcs1V15(ByteString.EMPTY);
		byte[] signature = signer.sign(registration.build().toByteArray());
		registration.getSignatureBuilder().setPkcs1V15(ByteString.copyFrom(signature));

		gameController.sendMessage(registration.build());
		reply = gameController.receiveMessage(SslGameControllerTeam.ControllerToTeam.parser());
		if (reply.getControllerReply().getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.error("GameController rejected team registration for team '" + teamName + "': "
					+ reply.getControllerReply().getReason());
		}
		nextToken = reply.getControllerReply().getNextToken();
		nextPing = 0;
	}


	private MessageSigner loadKeys()
	{
		try
		{
			return new MessageSigner(
					IOUtils.resourceToString("/edu/tigers/sumatra/teamclient/" + teamName + "-team.key.pem.pkcs8",
							Charset.forName("UTF-8")),
					IOUtils.resourceToString("/edu/tigers/sumatra/teamclient/" + teamName + "-team.pub.pem",
							Charset.forName("UTF-8")));
		} catch (IOException e)
		{
			log.warn("Could not find Certificates for Team " + teamName, e);
			return new MessageSigner();
		}
	}


	private void signMessage(TeamToController.Builder ttcBuilder)
	{
		ttcBuilder.getSignatureBuilder().setPkcs1V15(ByteString.EMPTY).setToken(nextToken);
		byte[] signature = signer.sign(ttcBuilder.build().toByteArray());
		ttcBuilder.getSignatureBuilder().setPkcs1V15(ByteString.copyFrom(signature));
	}


	/**
	 * If the {@link AIInfoFrame} contains team information, it will be processed.
	 * Processing will be skipped as long as the game controller connection
	 * is not yet established.
	 * Furthermore it asserts that this task is connected to the correct team name
	 *
	 * @param aiFrame
	 */
	private void checkAndHandleAiFrame(AIInfoFrame aiFrame)
	{
		if (StringUtils.isBlank(aiFrame.getRefereeMsg().getTeamInfo(teamColor).getName()))
		{
			return;
		}

		if (StringUtils.isBlank(teamName))
		{
			teamName = aiFrame.getRefereeMsg().getTeamInfo(teamColor).getName();
			Thread.currentThread().setName("TeamClient::" + teamName);
			signer = loadKeys();
		} else if (!teamName.equals(aiFrame.getRefereeMsg().getTeamInfo(teamColor).getName()))
		{
			log.info("Team " + teamName + " is no longer connected to GameController as " + teamColor
					+ ": shutting down");
			setActive(false);
		} else
		{
			if (!gameController.isConnected())
			{
				gameController.connectBlocking();
			}
			processAiFrame(aiFrame);
			if (System.nanoTime() > nextPing)
			{
				checkConnection();
				nextPing = System.nanoTime() + PING_INTERVAL;
			}
		}
	}


	/**
	 * Peform all Actions assuming that we have a valid {@link AIInfoFrame}
	 * and we are connected to the GC
	 *
	 * @param aiFrame
	 */
	private void processAiFrame(AIInfoFrame aiFrame)
	{
		if (gameController.newMessageAvailable())
		{
			handleAdvantageChoice(aiFrame);
		} else if (aiFrame.getGamestate().isStop())
		{
			Optional<BotID> newDesiredKeeper = getDesiredNewKeeper(aiFrame);
			newDesiredKeeper.ifPresent(this::requestNewKeeper);
		}

		if (botSubstitutionDesired)
		{
			sendSubstitutionRequest();
		}
	}


	private void handleAdvantageChoice(AIInfoFrame aiFrame)
	{
		SslGameControllerTeam.ControllerToTeam reply;
		reply = gameController.receiveMessage(SslGameControllerTeam.ControllerToTeam.parser());
		if (!reply.hasAdvantageChoice())
		{
			log.warn("Received unknown message: " + reply);
			return;
		}

		final AdvantageResponse advantageChoice = aiFrame.getTacticalField().getAdvantageChoice();
		TeamToController.Builder choiceBuilder = TeamToController.newBuilder()
				.setAdvantageResponse(advantageChoice);

		signMessage(choiceBuilder);

		log.debug("Sending choice for advantage response: " + advantageChoice);

		gameController.sendMessage(choiceBuilder.build());

		reply = gameController.receiveMessage(SslGameControllerTeam.ControllerToTeam.parser());
		if (reply.getControllerReply().getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.warn("GameController responded to AdvantageChoice with non-OK: " + reply);
		}
	}


	private Optional<BotID> getDesiredNewKeeper(AIInfoFrame aiFrame)
	{
		BotID currentKeeper = aiFrame.getRefereeMsg().getKeeperBotID(teamColor);
		if (aiFrame.getWorldFrame().getTigerBotsAvailable().keySet().contains(currentKeeper))
		{
			// Keeper is available -> no need to change anything
			return Optional.empty();
		}

		Optional<ITrackedBot> closestBot = aiFrame.getWorldFrame().getTigerBotsAvailable()
				.values().stream()
				.min(Comparator.comparingDouble(i -> i.getPos().distanceToSqr(Geometry.getGoalOur().getCenter())));

		if (closestBot.isPresent() && closestBot.get().getBotId() != lastRequestedKeeperId)
		{
			return Optional.of(closestBot.get().getBotId());
		}

		return Optional.empty();
	}


	private void sendSubstitutionRequest()
	{

		TeamToController.Builder req = TeamToController.newBuilder();
		req.setSubstituteBot(true);

		signMessage(req);
		gameController.sendMessage(req.build());

		SslGameControllerTeam.ControllerToTeam reply = gameController
				.receiveMessage(SslGameControllerTeam.ControllerToTeam.parser());
		if (reply.getControllerReply().getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.warn("GameController did not allow bot substitution: " + reply.getControllerReply());
		} else
		{
			log.info("Substituting bots at next possible situation");
			setBotSubstitutionDesired(false);
		}

		nextToken = reply.getControllerReply().getNextToken();
	}


	private void requestNewKeeper(BotID id)
	{
		lastRequestedKeeperId = id;
		TeamToController.Builder requestBuilder = TeamToController
				.newBuilder();
		requestBuilder.setDesiredKeeper(id.getNumber());
		signMessage(requestBuilder);

		gameController.sendMessage(requestBuilder.build());

		SslGameControllerTeam.ControllerToTeam reply = gameController
				.receiveMessage(SslGameControllerTeam.ControllerToTeam.parser());
		if (reply.getControllerReply().getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.warn("GameController rejected changing keeper to " + id + ": " + reply.getControllerReply().getReason());
			// rejected -> we want to request this bot as keeper again
			lastRequestedKeeperId = null;
		} else
		{
			log.info("Keeper was changed to " + id);
		}
		nextToken = reply.getControllerReply().getNextToken();
	}
}
