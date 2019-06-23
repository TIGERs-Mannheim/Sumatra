/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.Location;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.Pose;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction.EOffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.util.config.UserConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPTransmitter;


/**
 * @author JulianT
 */
public class MultiTeamMessageSender
{
	// private static final Logger log = Logger.getLogger(MultiTeamMessageSender.class.getName());
	private final MulticastUDPTransmitter	transmitter;
	private Map<BotID, RobotPlan>				robotPlanMap;
	
	
	/**
	  * 
	  */
	public MultiTeamMessageSender()
	{
		transmitter = new MulticastUDPTransmitter(UserConfig.getMultiTeamMessageLocalPort(),
				UserConfig.getMultiTeamMessageAddress(), UserConfig.getMultiTeamMessagePort());
		
		robotPlanMap = new HashMap<>();
	}
	
	
	/**
	 * @param baseAiFrame
	 */
	public void send(final BaseAiFrame baseAiFrame)
	{
		TeamPlan.Builder teamPlanBuilder = TeamPlan.newBuilder();
		
		for (TrackedTigerBot bot : baseAiFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			RobotPlan.Builder robotPlanBuilder = RobotPlan.newBuilder();
			if (robotPlanMap.get(bot.getId()) != null)
			{
				teamPlanBuilder.addPlans(robotPlanMap.get(bot.getId()));
			} else
			{
				robotPlanBuilder.setRobotId(bot.getId().getNumber());
				
				if (belongsToPlay(bot.getId(), EPlay.KEEPER, baseAiFrame))
				{
					robotPlanBuilder.setRole(RobotRole.Goalie);
				} else if (belongsToPlay(bot.getId(), EPlay.DEFENSIVE, baseAiFrame))
				{
					robotPlanBuilder.setRole(RobotRole.Defense);
				} else if (belongsToPlay(bot.getId(), EPlay.SUPPORT, baseAiFrame)
						|| belongsToPlay(bot.getId(), EPlay.OFFENSIVE, baseAiFrame))
				{
					robotPlanBuilder.setRole(RobotRole.Offense);
					
					OffensiveAction oa = baseAiFrame.getPrevFrame().getTacticalField().getOffensiveActions()
							.get(bot.getId());
					if ((oa != null) && (oa.getType() == EOffensiveAction.PASS))
					{
						// myBotID würde passen wenn er nah genug am ball ist !
						if (baseAiFrame.getPrevFrame().getAICom().getOffensiveRolePassTargetID() != null)
						{
							// Es wird gerade gepasst und zwar zu pass Target !
							IVector2 passTarget = baseAiFrame.getPrevFrame().getAICom().getOffensiveRolePassTarget();
							// hier weiss ich aber noch nicht wer passt !
							if (baseAiFrame.getPrevFrame().getTacticalField().getOffensiveStrategy().getDesiredBots().get(0) == bot
									.getId())
							{
								// der der passt ist primaryBot (desiredBot(0).
								
								// hier jetzt senden, dass
								// myBotID gerade passt und zwar zu passTarget!
								Location.Builder shotTargetBuilder = Location.newBuilder();
								shotTargetBuilder.setX((int) passTarget.x());
								shotTargetBuilder.setY((int) passTarget.y());
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
				locationBuilder.setX((int) bot.getPosByTime(1000).x());
				locationBuilder.setY((int) bot.getPosByTime(1000).y());
				navTargetBuilder.setLoc(locationBuilder);
				navTargetBuilder.setHeading(bot.getAngleByTime(1000));
				robotPlanBuilder.setNavTarget(navTargetBuilder);
				
				teamPlanBuilder.addPlans(robotPlanBuilder);
			}
		}
		
		transmitter.send(teamPlanBuilder.build().toByteArray());
		// log.info("Team plan sent");
	}
	
	
	/**
	 * @param botID
	 * @param robotRole
	 * @param location
	 * @param heading
	 * @param shotTarget
	 */
	public void putRobotPlan(final BotID botID, final RobotRole robotRole, final IVector2 location,
			final Float heading, final IVector2 shotTarget)
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
			navTargetBuilder.setHeading(heading.floatValue());
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
	
	
	private boolean belongsToPlay(final BotID botID, final EPlay play, final BaseAiFrame baseAiFrame)
	{
		return baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(play).stream()
				.anyMatch(role -> role.getBotID().getNumber() == botID.getNumber());
	}
}
