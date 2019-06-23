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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ImmediateStopSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;


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
	 * @return
	 */
	public AresData process(final AthenaAiFrame frame)
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
				skillSystem.execute(botId, new ImmediateStopSkill());
				botIsStopped.put(botId, Boolean.TRUE);
			}
		}
		
		Map<BotID, Path> paths = new HashMap<BotID, Path>(skillSystem.getSisyphus().getCurrentPaths());
		Map<BotID, Path> latestPaths = new HashMap<BotID, Path>(skillSystem.getSisyphus().getLatestPaths());
		Map<BotID, Integer> numPaths = new HashMap<BotID, Integer>(skillSystem.getSisyphus().getNumberOfPaths());
		
		for (BotID botId : BotID.getAll(frame.getTeamColor().opposite()))
		{
			paths.remove(botId);
			latestPaths.remove(botId);
			numPaths.remove(botId);
		}
		
		return new AresData(paths, latestPaths, numPaths);
	}
}
