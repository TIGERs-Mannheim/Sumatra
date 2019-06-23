/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.multiteammessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;


/**
 * @author JulianT
 */
public class MultiTeamMessageSender
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MultiTeamMessageSender.class.getName());
	
	private final String address;
	private final int port;
	
	private DatagramSocket ds;
	
	
	/**
	 * @param address
	 * @param port
	 */
	public MultiTeamMessageSender(String address, final int port)
	{
		this.address = address;
		this.port = port;
	}
	
	
	/**
	 * Start the sender
	 */
	public void start()
	{
		try
		{
			ds = new DatagramSocket();
		} catch (SocketException e)
		{
			log.error("Could not create datagram socket", e);
		}
	}
	
	
	/**
	 * Stop the sender
	 */
	public void stop()
	{
		if (ds != null)
		{
			ds.close();
			ds = null;
		}
	}
	
	
	/**
	 * @param teamPlan
	 */
	public void send(final TeamPlan teamPlan)
	{
		if (ds != null)
		{
			byte[] buffer = teamPlan.toByteArray();
			DatagramPacket dp;
			try
			{
				dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(address), port);
				ds.send(dp);
			} catch (UnknownHostException e1)
			{
				log.error("Unknown host", e1);
			} catch (IOException e)
			{
				log.error("Could not send datagram", e);
			}
		}
	}
}
