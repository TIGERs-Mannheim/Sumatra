/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.grsim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimPacket;
import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimReplacement;
import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimReplacement.grSim_BallReplacement;
import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimReplacement.grSim_Replacement;
import edu.tigers.sumatra.cam.IBallReplacer;
import edu.tigers.sumatra.math.IVector3;


/**
 * Replace the ball in the simulator grSim
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GrSimBallReplacer implements IBallReplacer
{
	private static final Logger	log	= Logger.getLogger(GrSimBallReplacer.class.getName());
													
	private final String				ip;
	private final int					port;
											
											
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param ip
	 * @param port
	 */
	public GrSimBallReplacer(final String ip, final int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	
	/**
	 * @param pos
	 */
	@Override
	public void replaceBall(final IVector3 pos, final IVector3 vel)
	{
		grSim_BallReplacement.Builder builder = GrSimReplacement.grSim_BallReplacement.newBuilder();
		builder.setX(pos.x() / 1000.0);
		builder.setY(pos.y() / 1000.0);
		builder.setVx(vel.x());
		builder.setVy(vel.y());
		grSim_Replacement.Builder replacementBuilder = grSim_Replacement.newBuilder();
		replacementBuilder.setBall(builder.build());
		
		GrSimPacket.grSim_Packet packet = GrSimPacket.grSim_Packet.newBuilder()
				.setReplacement(replacementBuilder.build()).build();
		byte[] buffer = packet.toByteArray();
		
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
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
			ds.send(dp);
		} catch (IOException e)
		{
			log.error("Could not send package to grSim", e);
		} finally
		{
			ds.close();
		}
	}
}
