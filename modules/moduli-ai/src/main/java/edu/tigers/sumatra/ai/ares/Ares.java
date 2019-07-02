/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.ares;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.Vector2;
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

		for (BotID botId : frame.getTacticalField().getDesiredBotMap().getOrDefault(EPlay.DEFENSIVE,
				Collections.emptySet()))
		{
			if (activeDefenders.contains(botId))
			{
				map.setPriority(botId, prio--);
				activeDefenders.remove(botId);
			}
		}
		prio = updatePrio(map, prio, activeDefenders);

		for (ARole role : frame.getPlayStrategy().getActiveRoles(EPlay.KEEPER))
		{
			map.setPriority(role.getBotID(), prio--);
		}

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
			MultimediaControl control = frame.getTacticalField().getMultimediaControl().get(skill.getBotId());
			if (control != null)
			{
				skill.setMultimediaControl(control);
				drawMultimediaControl(frame, skill.getBotId(), control);
			}
			skill.getMoveCon().setPrioMap(map);

			updateBotAiInfo(frame, skill, aiInfos);
		}
		aresData.setBotAiInformation(aiInfos);
	}


	private void drawMultimediaControl(final AthenaAiFrame frame, BotID botID, MultimediaControl control)
	{
		final ITrackedBot bot = frame.getWorldFrame().getBot(botID);
		if (bot == null)
		{
			return;
		}
		final List<IDrawableShape> shapes = frame.getTacticalField().getDrawableShapes()
				.get(EAiShapesLayer.TEST_MULTIMEDIA);
		shapes.add(new DrawableCircle(
				Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 10),
				control.getLedColor().getColor()));
		shapes.add(new DrawableAnnotation(bot.getPos(), control.getSong().name())
				.withOffset(Vector2.fromX(120)));
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
		aiInfo.setMaxProcTime(skill.getAverageTimeMeasure().getMaxTime());
		aiInfo.setAvgProcTime(skill.getAverageTimeMeasure().getAverageTime());
		aiInfos.put(botId, aiInfo);

		if (bot != null)
		{
			double curVel = bot.getFilteredState().orElse(bot.getBotState()).getVel2().getLength2() * 1e-3;
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
