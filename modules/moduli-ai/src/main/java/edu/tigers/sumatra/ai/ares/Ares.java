/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.ares;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.AresData;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.data.MultiTeamRobotPlan;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Ares is responsible for executing skills that were requested by AI.<br>
 * It is also responsible for any other AI -> skill communication.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Ares
{
	private static final Logger log = Logger.getLogger(Ares.class.getName());
	private final ASkillSystem skillSystem;
	private final Map<BotID, Boolean> botIsStopped = new HashMap<>();
	private final Map<BotID, Double> maxVels = new HashMap<>();
	
	
	/**
	 * @param skillSystem
	 */
	public Ares(final ASkillSystem skillSystem)
	{
		this.skillSystem = skillSystem;
	}
	
	
	private void executeSkills(final AthenaAiFrame frame)
	{
		Set<BotID> botsAssigned = new HashSet<>();
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				BotID botId = role.getBotID();
				if (!botId.isBot())
				{
					log.error("Role " + role.getType() + " has no assigned bot id!");
				}
				if (botsAssigned.contains(botId))
				{
					log.error("Bot with id " + botId.getNumber() + " already has another role assigned. Can not assign "
							+ role.getType());
					logCurrentRoleAssignment(frame);
					continue;
				}
				
				botsAssigned.add(botId);
				ISkill skill = role.getNewSkill();
				if (skill != null)
				{
					// # Execute skills!
					skillSystem.execute(role.getBotID(), skill);
					botIsStopped.put(botId, Boolean.FALSE);
				}
			}
		}
		
		stopBotsNotAssigned(frame, botsAssigned);
	}
	
	
	private void logCurrentRoleAssignment(final AthenaAiFrame frame)
	{
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				log.warn(play.getType() + ": " + role.getType() + " -> " + role.getBotID());
			}
		}
	}
	
	
	private void stopBotsNotAssigned(final AthenaAiFrame frame, final Set<BotID> botsAssigned)
	{
		Set<BotID> botsLeft = new HashSet<>(frame.getWorldFrame().getTigerBotsAvailable().keySet());
		botsLeft.removeAll(botsAssigned);
		
		for (BotID botId : botsLeft)
		{
			// No role for this bot: Stop him (if not yet done)
			final Boolean stopped = botIsStopped.get(botId);
			if ((stopped == null) || !stopped)
			{
				skillSystem.execute(botId, new IdleSkill());
				botIsStopped.put(botId, Boolean.TRUE);
			}
		}
	}
	
	
	private PathFinderPrioMap updatePathFinderPrioMap(final AthenaAiFrame frame)
	{
		PathFinderPrioMap map = PathFinderPrioMap.byBotId(frame.getTeamColor());
		int prio = 100;
		for (ARole role : frame.getPlayStrategy().getActiveRoles(EPlay.KEEPER))
		{
			map.setPriority(role.getBotID(), prio--);
		}
		Set<BotID> activeDefenders = frame.getPlayStrategy().getActiveRoles(EPlay.DEFENSIVE).stream()
				.map(ARole::getBotID).collect(Collectors.toSet());
		for (BotID botId : frame.getTacticalField().getCrucialDefender())
		{
			if (activeDefenders.contains(botId))
			{
				map.setPriority(botId, prio--);
				activeDefenders.remove(botId);
			}
		}
		for (BotID botId : frame.getTacticalField().getDesiredBotMap().get(EPlay.DEFENSIVE))
		{
			if (activeDefenders.contains(botId))
			{
				map.setPriority(botId, prio--);
				activeDefenders.remove(botId);
			}
		}
		prio = updatePrio(map, prio, activeDefenders);
		
		Set<BotID> activeOffenders = frame.getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE).stream()
				.map(ARole::getBotID).collect(Collectors.toSet());
		for (BotID botId : frame.getTacticalField().getCrucialOffender())
		{
			if (activeOffenders.contains(botId))
			{
				map.setPriority(botId, prio--);
				activeOffenders.remove(botId);
			}
		}
		prio = updatePrio(map, prio, activeOffenders);
		
		for (ARole role : frame.getPlayStrategy().getActiveRoles(EPlay.SUPPORT).stream()
				.sorted((r1, r2) -> BotID.getComparator().compare(r1.getBotID(), r2.getBotID()))
				.collect(Collectors.toList()))
		{
			map.setPriority(role.getBotID(), prio--);
		}
		
		return map;
	}
	
	
	private int updatePrio(final PathFinderPrioMap map, int prio, final Set<BotID> bots)
	{
		int nextPrio = prio;
		for (BotID botId : bots.stream().sorted((r1, r2) -> BotID.getComparator().compare(r1, r2))
				.collect(Collectors.toList()))
		{
			map.setPriority(botId, nextPrio--);
		}
		return nextPrio;
	}
	
	
	/**
	 * @param frame
	 * @param aresData
	 */
	public void process(final AthenaAiFrame frame, final AresData aresData)
	{
		executeSkills(frame);
		
		PathFinderPrioMap map = updatePathFinderPrioMap(frame);
		
		
		List<ISkill> skills = skillSystem.getCurrentSkills(frame.getTeamColor());
		Map<BotID, BotAiInformation> aiInfos = new HashMap<>();
		for (ISkill skill : skills)
		{
			if (!frame.getWorldFrame().getTigerBotsAvailable().containsKey(skill.getBotId()))
			{
				continue;
			}
			MultimediaControl led = frame.getTacticalField().getMultimediaControl().get(skill.getBotId());
			if (led != null)
			{
				skill.setMultimediaControl(led);
			}
			skill.getMoveCon().setPrioMap(map);
			frame.getTacticalField().getDrawableShapes().merge(skill.exportShapeMap());
			
			updateBotAiInfo(frame, skill, aiInfos);
			
			updateMultiTeamPlan(frame, skill);
		}
		aresData.setBotAiInformation(aiInfos);
	}
	
	
	private void updateMultiTeamPlan(final AthenaAiFrame frame, final ISkill skill)
	{
		IVector2 dest = skill.getMoveCon().getDestination();
		Double targetAngle = skill.getMoveCon().getTargetAngle();
		if (dest != null && targetAngle != null)
		{
			MultiTeamRobotPlan robotPlan = frame.getTacticalField().getMultiTeamPlan().getRobotPlans()
					.get(skill.getBotId());
			if (robotPlan != null)
			{
				robotPlan.setTargetPose(Pose.from(dest, targetAngle));
			}
		}
	}
	
	
	private void updateBotAiInfo(final AthenaAiFrame frame, final ISkill skill,
			final Map<BotID, BotAiInformation> aiInfos)
	{
		BotID botId = skill.getBotId();
		ITrackedBot bot = frame.getWorldFrame().getBot(botId);
		BotAiInformation aiInfo = skill.getBotAiInfo();
		if (aiInfo == null)
		{
			return;
		}
		
		aiInfo.setSkill(skill.getType());
		aiInfo.setSkillState(skill.getCurrentState());
		aiInfos.put(botId, aiInfo);
		
		if (bot != null)
		{
			double curVel = bot.getVel().getLength2();
			double maxVel = maxVels.compute(botId,
					(o, curMax) -> (curMax == null || curMax < curVel) ? curVel : curMax);
			aiInfo.setVelocityMax(maxVel);
		}
		
		for (APlay play : frame.getPrevFrame().getPlayStrategy().getActivePlays())
		{
			setAiInfoForPlay(aiInfo, play, botId);
		}
	}
	
	
	private void setAiInfoForPlay(BotAiInformation aiInfo, APlay play, BotID botId)
	{
		for (ARole role : play.getRoles())
		{
			if (!role.getBotID().equals(botId))
			{
				continue;
			}
			aiInfo.setPlay(play.getType().name());
			aiInfo.setRole(role.getType().name());
			IState roleState = role.getCurrentState();
			aiInfo.setRoleState(roleState);
			return;
		}
	}
	
	
	public ASkillSystem getSkillSystem()
	{
		return skillSystem;
	}
}
