/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.ares;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
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
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Ares is responsible for executing skills that were requested by AI.<br>
 * It is also responsible for any other AI -> skill communication.
 */
@Log4j2
public class Ares
{
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


	/**
	 * @param frame
	 * @param aresData
	 */
	public void process(final AthenaAiFrame frame, final AresData aresData)
	{
		executeSkills(frame);

		PathFinderPrioMap map = frame.getTacticalField().getPathFinderPrioMap();

		List<ISkill> skills = skillSystem.getCurrentSkills(frame.getTeamColor());
		Map<BotID, BotAiInformation> aiInfos = new HashMap<>();
		for (ISkill skill : skills)
		{
			if (!frame.getWorldFrame().getTigerBotsAvailable().containsKey(skill.getBotId()))
			{
				continue;
			}
			MultimediaControl control = frame.getBaseAiFrame().getMultimediaControl().get(skill.getBotId());
			if (control != null)
			{
				skill.setMultimediaControl(control);
				drawMultimediaControl(frame, skill.getBotId(), control);
			}
			skill.setPrioMap(map);

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
		final List<IDrawableShape> shapes = frame.getShapeMap()
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

		aiInfos.put(botId, aiInfo);

		if (bot != null)
		{
			double curVel = bot.getFilteredState().orElse(bot.getBotState()).getVel2().getLength2();
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
