/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;


import java.net.NetworkInterface;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.TeamInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPTransmitter;
import edu.dhbw.mannheim.tigers.sumatra.util.network.NetworkUtility;


/**
 * This class sends given RefereeMessages via Multicast UDP.
 * The official SSL-Protocol is used.
 * 
 * @author Malte
 * 
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
	public RefereeMsgTransmitter(SubnodeConfiguration config)
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
	 * @param newTeamProps
	 */
	public void sendOwnRefereeMsg(int id, Command cmd, int goalsBlue, int goalsYellow, short timeLeft,
			TeamProps newTeamProps)
	{
		TeamInfo.Builder teamFoeBuilder = TeamInfo.newBuilder();
		teamFoeBuilder.setGoalie(0);
		teamFoeBuilder.setName("Foes");
		teamFoeBuilder.setRedCards(1);
		teamFoeBuilder.setScore(goalsBlue);
		teamFoeBuilder.setTimeouts(4);
		teamFoeBuilder.setTimeoutTime(360);
		teamFoeBuilder.setYellowCards(3);
		
		TeamInfo.Builder teamTigersBuilder = TeamInfo.newBuilder();
		teamTigersBuilder.setGoalie(newTeamProps.getKeeperId().getNumber());
		teamTigersBuilder.setName("Tigers");
		teamTigersBuilder.setRedCards(0);
		teamTigersBuilder.setScore(goalsYellow);
		teamTigersBuilder.setTimeouts(2);
		teamTigersBuilder.setTimeoutTime(65);
		teamTigersBuilder.setYellowCards(1);
		
		SSL_Referee.Builder builder = SSL_Referee.newBuilder();
		builder.setPacketTimestamp(System.currentTimeMillis());
		if (newTeamProps.getTigersAreYellow())
		{
			builder.setBlue(teamFoeBuilder.build());
			builder.setYellow(teamTigersBuilder.build());
		} else
		{
			builder.setBlue(teamTigersBuilder.build());
			builder.setYellow(teamFoeBuilder.build());
		}
		builder.setCommand(cmd);
		builder.setCommandCounter(id);
		builder.setCommandTimestamp(System.currentTimeMillis());
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
