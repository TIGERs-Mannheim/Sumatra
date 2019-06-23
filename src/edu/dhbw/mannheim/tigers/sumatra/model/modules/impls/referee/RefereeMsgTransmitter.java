/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;


import java.net.NetworkInterface;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.TeamInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPTransmitter;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * This class sends given RefereeMessages via Multicast UDP.
 * The official SSL-Protocol is used.
 * 
 * @author Malte
 */
public class RefereeMsgTransmitter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final MulticastUDPTransmitter	multicastUDPTransmitter;
	
	private static final int					LOCAL_PORT	= 17611;
	private final String							address;
	private final NetworkInterface			nif;
	private final int								targetPort;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param config
	 */
	public RefereeMsgTransmitter(final SubnodeConfiguration config)
	{
		address = config.getString("address");
		targetPort = Integer.valueOf(config.getString("port"));
		String network = config.getString("interface");
		if (network == null)
		{
			nif = null;
		} else
		{
			nif = NetworkUtility.chooseNetworkInterface(network, 3);
		}
		multicastUDPTransmitter = new MulticastUDPTransmitter(LOCAL_PORT, address, targetPort, nif);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param id
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 */
	public void sendOwnRefereeMsg(final int id, final Command cmd, final int goalsBlue, final int goalsYellow,
			final int timeLeft)
	{
		TeamInfo.Builder teamBlueBuilder = TeamInfo.newBuilder();
		teamBlueBuilder.setGoalie(TeamConfig.getKeeperIdBlue());
		teamBlueBuilder.setName("Blue");
		teamBlueBuilder.setRedCards(1);
		teamBlueBuilder.setScore(goalsBlue);
		teamBlueBuilder.setTimeouts(4);
		teamBlueBuilder.setTimeoutTime(360);
		teamBlueBuilder.setYellowCards(3);
		
		TeamInfo.Builder teamYellowBuilder = TeamInfo.newBuilder();
		teamYellowBuilder.setGoalie(TeamConfig.getKeeperIdYellow());
		teamYellowBuilder.setName("Yellow");
		teamYellowBuilder.setRedCards(0);
		teamYellowBuilder.setScore(goalsYellow);
		teamYellowBuilder.setTimeouts(2);
		teamYellowBuilder.setTimeoutTime(65);
		teamYellowBuilder.setYellowCards(1);
		
		SSL_Referee.Builder builder = SSL_Referee.newBuilder();
		builder.setPacketTimestamp(SumatraClock.currentTimeMillis());
		builder.setBlue(teamBlueBuilder.build());
		builder.setYellow(teamYellowBuilder.build());
		builder.setCommand(cmd);
		builder.setCommandCounter(id);
		builder.setCommandTimestamp(SumatraClock.currentTimeMillis());
		builder.setStageTimeLeft(timeLeft);
		builder.setStage(Stage.NORMAL_FIRST_HALF);
		
		byte[] data = builder.build().toByteArray();
		multicastUDPTransmitter.send(data);
	}
	
	
	/**
	 */
	public void stop()
	{
		multicastUDPTransmitter.cleanup();
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
