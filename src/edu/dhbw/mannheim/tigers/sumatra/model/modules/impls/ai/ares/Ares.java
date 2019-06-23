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
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.commons.NoOldFrameCriteria;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.IAIProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.ISyncedFIFO;


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
	protected final Logger						log				= Logger.getLogger(getClass());
	

	private final ISyncedFIFO<WorldFrame>	wfFifo;
	private final NoOldFrameCriteria			criteria			= new NoOldFrameCriteria();
	private CountDownLatch						startSignal		= new CountDownLatch(1);
	
	private final Sisyphus						sisyphus;
	private ASkillSystem							skillSystem;
	

	private final Map<Integer, Boolean>		botIsStopped	= new HashMap<Integer, Boolean>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param sisyphus
	 * @param wfFifo
	 */
	public Ares(Sisyphus sisyphus, ISyncedFIFO<WorldFrame> wfFifo)
	{
		this.sisyphus = sisyphus;
		this.wfFifo = wfFifo;
	}
	

	@Override
	public AIInfoFrame process(AIInfoFrame frame, AIInfoFrame previousFrame)
	{
		// ### Go and see whether there already is a newer WorldFrame in the buffer...
		WorldFrame wFrame = wfFifo.peekFirstIfMatches(criteria);
		if (wFrame == null)
		{
			wFrame = frame.worldFrame;
		}
		criteria.setOldFrame(wFrame);
		

		// ### Restore certain state
		// First, identify bots whose roles changed
		Set<Integer> roleChanged = new HashSet<Integer>(7);	// Contains the ids of bots whose roles changed in the current cycle
//		if (!frame.playStrategy.getFinishedPlays().isEmpty())
//		{
//			roleChanged = new HashSet<Integer>(5);
//			for (APlay oldPlay : frame.playStrategy.getFinishedPlays())
//			{
//				for (ARole oldRole : oldPlay.getRoles())
//				{
//					if (oldRole.hasBeenAssigned())
//					{
//						roleChanged.add(oldRole.getBotID());
//					}
//				}
//			}
//		}
		
		for (ARole oldRole : previousFrame.assignedRoles.values())
		{
			if (!frame.assignedRoles.containsValue(oldRole))
			{
				roleChanged.add(oldRole.getBotID());
			}
		}
		

		// ### Iterate over current assigned roles and execute them. If a bot has no role, stop him
		for (Entry<Integer, TrackedTigerBot> entry : wFrame.tigerBots.entrySet())
		{
			Integer botId = entry.getKey();
			ARole role = frame.assignedRoles.get(botId);
			if (role != null)
			{
				// # Calc skills
				final SkillFacade facade = new SkillFacade();
				role.calculateSkills(wFrame, facade);
				
				// # If this role is new (changed from previousFrame to frame), disable dribbler and disarm kicker
				// (if the role has no other plans)
				if (roleChanged != null && roleChanged.contains(botId))
				{
					if (facade.isSlotFree(ESkillGroup.DRIBBLE))
					{
						facade.dribble(false);
					}
					
					if (facade.isSlotFree(ESkillGroup.KICK))
					{
						facade.disarm();
					}
				}
				
				// # Execute skills!
				skillSystem.execute(role.getBotID(), facade);
				botIsStopped.put(botId, Boolean.FALSE);
			} else
			{
				// No role for this bot: Stop him (if not yet done)
				Boolean stopped = botIsStopped.get(botId);
				if (stopped == null || !stopped)
				{
					SkillFacade stopBag = new SkillFacade();
					stopBag.dribble(false);
					stopBag.disarm();
					stopBag.stop();
					
					skillSystem.execute(botId, stopBag);
					botIsStopped.put(botId, Boolean.TRUE);
				}
			}
		}
		
		return frame;
	}
	

	public void setSkillSystem(ASkillSystem skillSystem)
	{
		if (skillSystem != null)
		{
			GenericSkillSystem gss = (GenericSkillSystem) skillSystem;
			gss.setSisyphus(sisyphus);
			startSignal.countDown();
		} else
		{
			GenericSkillSystem oldGss = (GenericSkillSystem) this.skillSystem;
			oldGss.setSisyphus(null);
		}
		
		this.skillSystem = skillSystem;
	}
}
