/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.multiteammessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.Location;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.Pose;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction.EOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author JulianT
 */
public class MultiTeamMessageSender
{
	@SuppressWarnings("unused")
	private static final Logger			log				= Logger.getLogger(MultiTeamMessageSender.class.getName());
																		
	private DatagramSocket					ds;
													
	private final Map<BotID, RobotPlan>	robotPlanMap	= new HashMap<>();
																		
	@Configurable
	private static int						port				= 10012;
																		
	@Configurable
	private static String					address			= "127.0.0.1";
																		
																		
	static
	{
		ConfigRegistration.registerClass("botmgr", MultiTeamMessageSender.class);
	}
	
	
	/**
	  * 
	  */
	public MultiTeamMessageSender()
	{
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		try
		{
			ds = new DatagramSocket(port + 1);
		} catch (SocketException e)
		{
			log.error("Could not create datagram socket", e);
		}
	}
	
	
	/**
	 * 
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
	 * @param aiFrame
	 */
	public void send(final AIInfoFrame aiFrame)
	{
		TeamPlan.Builder teamPlanBuilder = TeamPlan.newBuilder();
		
		for (ITrackedBot bot : aiFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			RobotPlan.Builder robotPlanBuilder = RobotPlan.newBuilder();
			if (robotPlanMap.get(bot.getBotId()) != null)
			{
				teamPlanBuilder.addPlans(robotPlanMap.get(bot.getBotId()));
			} else
			{
				robotPlanBuilder.setRobotId(bot.getBotId().getNumber());
				
				if (belongsToPlay(bot.getBotId(), EPlay.KEEPER, aiFrame))
				{
					robotPlanBuilder.setRole(RobotRole.Goalie);
				} else if (belongsToPlay(bot.getBotId(), EPlay.DEFENSIVE, aiFrame))
				{
					robotPlanBuilder.setRole(RobotRole.Defense);
				} else if (belongsToPlay(bot.getBotId(), EPlay.SUPPORT, aiFrame)
						|| belongsToPlay(bot.getBotId(), EPlay.OFFENSIVE, aiFrame))
				{
					robotPlanBuilder.setRole(RobotRole.Offense);
					
					OffensiveAction oa = aiFrame.getTacticalField().getOffensiveActions()
							.get(bot.getBotId());
					if ((oa != null) && (oa.getType() == EOffensiveAction.PASS))
					{
						// myBotID würde passen wenn er nah genug am ball ist !
						if (aiFrame.getAICom().getOffensiveRolePassTargetID() != null)
						{
							// Es wird gerade gepasst und zwar zu pass Target !
							IVector2 passTarget = aiFrame.getAICom().getOffensiveRolePassTarget();
							// hier weiss ich aber noch nicht wer passt !
							if (aiFrame.getTacticalField().getOffensiveStrategy().getDesiredBots()
									.get(0) == bot
											.getBotId())
							{
								// der der passt ist primaryBot (desiredBot(0).
								
								// hier jetzt senden, dass
								// myBotID gerade passt und zwar zu passTarget!
								Location.Builder shotTargetBuilder = Location.newBuilder();
								shotTargetBuilder.setX(correctToVisionCoordinates(passTarget.x(), aiFrame));
								shotTargetBuilder.setY(correctToVisionCoordinates(passTarget.y(), aiFrame));
								robotPlanBuilder.setShotTarget(shotTargetBuilder);
							}
							// da können vermutlich viele NulPointer drin sein, da zugriff auf prevFrame.
							// musst du vermutlich checken.
						}
					}
				} else
				{
					robotPlanBuilder.setRole(RobotRole.Default);
				}
				
				Pose.Builder navTargetBuilder = Pose.newBuilder();
				Location.Builder locationBuilder = Location.newBuilder();
				// TODO JulianT: Magic Numbers
				locationBuilder.setX(correctToVisionCoordinates(bot.getPosByTime(1000).x(), aiFrame));
				locationBuilder.setY(correctToVisionCoordinates(bot.getPosByTime(1000).y(), aiFrame));
				navTargetBuilder.setLoc(locationBuilder);
				if (aiFrame.getWorldFrame().isInverted())
				{
					navTargetBuilder.setHeading(correctToVisionAngle(bot.getAngleByTime(1000), aiFrame));
				} else
				{
					navTargetBuilder.setHeading((float) bot.getAngleByTime(1000));
				}
				robotPlanBuilder.setNavTarget(navTargetBuilder);
				
				teamPlanBuilder.addPlans(robotPlanBuilder);
			}
		}
		if (ds != null)
		{
			byte[] buffer = teamPlanBuilder.build().toByteArray();
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
	
	
	/**
	 * @param botID
	 * @param robotRole
	 * @param location
	 * @param heading
	 * @param shotTarget
	 */
	public void putRobotPlan(final BotID botID, final RobotRole robotRole, final IVector2 location,
			final Double heading, final IVector2 shotTarget)
	{
		RobotPlan.Builder robotPlanBuilder = RobotPlan.newBuilder();
		robotPlanBuilder.setRobotId(botID.getNumber());
		
		if (robotRole != null)
		{
			robotPlanBuilder.setRole(robotRole);
		}
		
		Pose.Builder navTargetBuilder = Pose.newBuilder();
		if (location != null)
		{
			Location.Builder locationBuilder = Location.newBuilder();
			locationBuilder.setX((int) location.x());
			locationBuilder.setY((int) location.y());
			
			navTargetBuilder.setLoc(locationBuilder);
		}
		
		if (heading != null)
		{
			navTargetBuilder.setHeading((float) heading.doubleValue());
		}
		
		if (navTargetBuilder.hasLoc() || navTargetBuilder.hasHeading())
		{
			robotPlanBuilder.setNavTarget(navTargetBuilder);
		}
		
		if (shotTarget != null)
		{
			Location.Builder locationBuilder = Location.newBuilder();
			locationBuilder.setX((int) shotTarget.x());
			locationBuilder.setY((int) shotTarget.y());
			
			robotPlanBuilder.setShotTarget(locationBuilder);
		}
		
		robotPlanMap.put(botID, robotPlanBuilder.build());
	}
	
	
	private boolean belongsToPlay(final BotID botID, final EPlay play, final AIInfoFrame baseAiFrame)
	{
		return baseAiFrame.getPlayStrategy().getActiveRoles(play).stream()
				.anyMatch(role -> role.getBotID().getNumber() == botID.getNumber());
	}
	
	
	private int correctToVisionCoordinates(final double value, final BaseAiFrame baseAiFrame)
	{
		return (int) ((baseAiFrame.getWorldFrame().isInverted()) ? -value : value);
	}
	
	
	private float correctToVisionAngle(final double angle, final BaseAiFrame baseAiFrame)
	{
		return (float) ((baseAiFrame.getWorldFrame().isInverted()) ? (float) Math.PI - angle : angle);
	}
}
