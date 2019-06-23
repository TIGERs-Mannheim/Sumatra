/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimPacket;
import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimReplacement;
import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimReplacement.grSim_BallReplacement;
import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimReplacement.grSim_Replacement;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Replace the ball in the simulator grSim
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class GrSimBallReplacer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(GrSimBallReplacer.class.getName());
	
	private final String				ip;
	private final int					port;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param subconfig
	 */
	public GrSimBallReplacer(SubnodeConfiguration subconfig)
	{
		ip = subconfig.getString("simIp");
		port = subconfig.getInt("simPort");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * @param pos
	 */
	public void replaceBall(IVector2 pos)
	{
		grSim_BallReplacement.Builder builder = GrSimReplacement.grSim_BallReplacement.newBuilder();
		builder.setX(pos.x() / 1000f);
		builder.setY(pos.y() / 1000f);
		builder.setVx(0);
		builder.setVy(0);
		grSim_Replacement.Builder replacementBuilder = grSim_Replacement.newBuilder();
		replacementBuilder.setBall(builder.build());
		
		GrSimPacket.grSim_Packet packet = GrSimPacket.grSim_Packet.newBuilder()
				.setReplacement(replacementBuilder.build()).build();
		byte[] buffer2 = packet.toByteArray();
		DatagramSocket ds;
		try
		{
			ds = new DatagramSocket();
		} catch (SocketException err)
		{
			log.error("Could not open datagram socket on " + ip + ":" + port);
			return;
		}
		try
		{
			DatagramPacket dp = new DatagramPacket(buffer2, buffer2.length, InetAddress.getByName(ip), port);
			ds.send(dp);
		} catch (IOException e)
		{
			log.error("Could not send package to grSim", e);
		} finally
		{
			ds.close();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
