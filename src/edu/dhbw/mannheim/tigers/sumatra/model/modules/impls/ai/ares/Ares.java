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
import java.util.Map.Entry;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.IAIProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ImmediateStopSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;


/**
 * This Ares implementation manages the roles which have just been assigned to bots, calculates the necessary skills and
 * passes them to the {@link ASkillSystem}.
 * 
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
public class Ares implements IAIProcessor
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Sisyphus					sisyphus;
	private ASkillSystem						skillSystem;
	
	
	private final Map<BotID, Boolean>	botIsStopped	= new HashMap<BotID, Boolean>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param sisyphus
	 * @param skillSystem
	 */
	public Ares(Sisyphus sisyphus, ASkillSystem skillSystem)
	{
		this.sisyphus = sisyphus;
		setSkillSystem(skillSystem);
	}
	
	
	@Override
	public void process(AIInfoFrame frame, AIInfoFrame previousFrame)
	{
		WorldFrame wFrame = frame.worldFrame;
		
		// ### Restore certain state
		// First, identify bots whose roles changed
		// Contains the ids of bots whose roles changed in the current cycle
		final Set<BotID> roleChanged = new HashSet<BotID>(7);
		
		for (final Entry<BotID, ARole> oldAssignment : previousFrame.getAssigendRoles())
		{
			if (!frame.getAssigendERoles().containsValue(oldAssignment.getValue()))
			{
				roleChanged.add(oldAssignment.getKey());
			}
		}
		
		
		// ### Iterate over current assigned roles and execute them. If a bot has no role, stop him
		for (final Entry<BotID, TrackedTigerBot> entry : wFrame.tigerBotsAvailable.entrySet())
		{
			final BotID botId = entry.getKey();
			final ARole role = frame.getAssigendRoles().getWithNull(botId);
			if (role != null)
			{
				AMoveSkill skill = role.getNewSkill();
				if (skill != null)
				{
					// # Execute skills!
					skillSystem.execute(role.getBotID(), skill);
					botIsStopped.put(botId, Boolean.FALSE);
				}
			} else
			{
				// No role for this bot: Stop him (if not yet done)
				final Boolean stopped = botIsStopped.get(botId);
				if ((stopped == null) || !stopped)
				{
					skillSystem.execute(botId, new ImmediateStopSkill());
					botIsStopped.put(botId, Boolean.TRUE);
				}
			}
		}
	}
	
	
	/**
	 * @param skillSystem
	 */
	public final void setSkillSystem(ASkillSystem skillSystem)
	{
		if (skillSystem != null)
		{
			final GenericSkillSystem gss = (GenericSkillSystem) skillSystem;
			gss.setSisyphus(sisyphus);
		} else
		{
			final GenericSkillSystem oldGss = (GenericSkillSystem) this.skillSystem;
			oldGss.setSisyphus(null);
		}
		
		this.skillSystem = skillSystem;
	}
}
