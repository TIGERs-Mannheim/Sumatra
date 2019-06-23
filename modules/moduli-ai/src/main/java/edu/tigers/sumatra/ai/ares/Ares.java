/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s):
 * Christian
 * Gero
 * *********************************************************
 */
package edu.tigers.sumatra.ai.ares;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.LedControl;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.PathFinderPrioMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;


/**
 * This Ares implementation manages the roles which have just been assigned to bots, calculates the necessary skills and
 * passes them to the {@link ASkillSystem}.
 * <p>
 * There are two special cases which should <b>not</b> occur during a real game, but catching them gives a lot of
 * safety:</br>
 * <ol>
 * <li><u><b>Less roles then bots:</b></u> In this case the bot is stopped, kicker disarmed and dribbler disabled</li>
 * <li><u><b>Roles changed:</b></u> To prevent situations where an old role left the kicker armed while the new role
 * expect it to be disarmed e.g., Ares identifies changedRoles and disables their dribbler and disarms the kicker. Thus
 * roles can always expect a passive bot, which might be moving, though!</li>
 * </ol>
 * </p>
 * 
 * @author Christian , Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class Ares
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger			log				= Logger.getLogger(Ares.class.getName());
	private final ASkillSystem				skillSystem;
	private final Map<BotID, Boolean>	botIsStopped	= new HashMap<BotID, Boolean>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param skillSystem
	 */
	public Ares(final ASkillSystem skillSystem)
	{
		this.skillSystem = skillSystem;
	}
	
	
	/**
	 * @param frame
	 */
	public void process(final AthenaAiFrame frame)
	{
		Set<BotID> botsAssigned = new HashSet<BotID>();
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
		
		Set<BotID> botsLeft = new HashSet<BotID>(frame.getWorldFrame().getTigerBotsAvailable().keySet());
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
		
		PathFinderPrioMap map = PathFinderPrioMap.byBotId(frame.getTeamColor());
		int prio = 100;
		for (ARole role : frame.getPlayStrategy().getActiveRoles(EPlay.KEEPER))
		{
			map.setPriority(role.getBotID(), prio);
		}
		prio--;
		if (!frame.getPlayStrategy().getActiveRoles(EPlay.DEFENSIVE).isEmpty())
		{
			for (BotID botId : frame.getTacticalField().getCrucialDefenders())
			{
				map.setPriority(botId, prio);
			}
		}
		prio--;
		for (ARole role : frame.getPlayStrategy().getActiveRoles(EPlay.DEFENSIVE))
		{
			if (!frame.getTacticalField().getCrucialDefenders().contains(role.getBotID()))
			{
				map.setPriority(role.getBotID(), prio);
			}
		}
		prio--;
		for (ARole role : frame.getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE).stream()
				.sorted((r1, r2) -> BotID.getComparator().compare(r1.getBotID(), r2.getBotID()))
				.collect(Collectors.toList()))
		{
			map.setPriority(role.getBotID(), prio--);
		}
		prio--;
		for (ARole role : frame.getPlayStrategy().getActiveRoles(EPlay.SUPPORT).stream()
				.sorted((r1, r2) -> BotID.getComparator().compare(r1.getBotID(), r2.getBotID()))
				.collect(Collectors.toList()))
		{
			map.setPriority(role.getBotID(), prio--);
		}
		
		List<ISkill> skills = skillSystem.getCurrentSkills();
		for (ISkill skill : skills)
		{
			if (skill.getBotId().getTeamColor() == frame.getTeamColor())
			{
				LedControl led = frame.getTacticalField().getLedData().get(skill.getBotId());
				if (led != null)
				{
					led.setInsane(OffensiveMath.isKeeperInsane(frame, frame.getTacticalField()));
					skill.setLedControl(led);
				}
				skill.getMoveCon().setPrioMap(map);
				frame.getTacticalField().getDrawableShapes().merge(skill.getShapes());
			}
		}
	}
	
	
	/**
	 * @return the skillSystem
	 */
	public ASkillSystem getSkillSystem()
	{
		return skillSystem;
	}
}
