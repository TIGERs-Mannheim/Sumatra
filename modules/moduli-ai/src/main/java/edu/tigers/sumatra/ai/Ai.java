/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.ares.Ares;
import edu.tigers.sumatra.ai.ares.AresData;
import edu.tigers.sumatra.ai.athena.Athena;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.metis.Metis;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * The AI takes a {@link WorldFrameWrapper} as input, processes the whole AI chain and produces an {@link AIInfoFrame}
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Ai
{
	private static final Logger log = Logger.getLogger(Ai.class.getName());
	
	private AIInfoFrame previousAIFrame = null;
	private long lastRefMsgCounter = -1;
	
	private final Metis metis = new Metis();
	private final Athena athena = new Athena();
	private final EAiTeam aiTeam;
	private final Ares ares;
	
	
	/**
	 * Create a new AI
	 * 
	 * @param aiTeam
	 * @param skillSystem
	 */
	public Ai(EAiTeam aiTeam, ASkillSystem skillSystem)
	{
		this.aiTeam = aiTeam;
		ares = new Ares(skillSystem);
	}
	
	
	/**
	 * @param mode
	 */
	public void changeMode(final EAIControlState mode)
	{
		athena.changeMode(mode);
	}
	
	
	/**
	 * Stop what has to be stopped
	 */
	public void stop()
	{
		ares.getSkillSystem().emergencyStop(aiTeam.getTeamColor());
		metis.stop();
	}
	
	
	/**
	 * Process a {@link WorldFrame} (one AI cycle)
	 * Do not call this on the two module agents! Create your own agent, please :)
	 *
	 * @param wfw
	 * @return
	 */
	public AIInfoFrame processWorldFrame(final WorldFrameWrapper wfw)
	{
		RefereeMsg refereeMsg = wfw.getRefereeMsg();
		boolean newRefereeMsg = false;
		if ((refereeMsg != null) && isNewMessage(refereeMsg))
		{
			log.trace("Referee cmd: " + refereeMsg.getCommand());
			newRefereeMsg = true;
		}
		
		BaseAiFrame baseAiFrame = new BaseAiFrame(wfw, newRefereeMsg, previousAIFrame, aiTeam);
		
		if (previousAIFrame == null)
		{
			// Skip first frame
			previousAIFrame = AIInfoFrame.fromBaseAiFrame(baseAiFrame);
			return null;
		}
		
		previousAIFrame.cleanUp();
		
		
		// ### Process!
		MetisAiFrame metisAiFrame;
		AthenaAiFrame athenaAiFrame = null;
		try
		{
			// Analyze
			metisAiFrame = metis.process(baseAiFrame);
			
			// Choose and calculate behavior
			athenaAiFrame = athena.process(metisAiFrame);
			
			// Execute!
			AresData aresData = new AresData();
			ares.process(athenaAiFrame, aresData);
			
			// ### Populate used AIInfoFrame (for visualization etc)
			previousAIFrame = new AIInfoFrame(athenaAiFrame, aresData);
			return previousAIFrame;
		} catch (@SuppressWarnings("squid:S1181") final Throwable ex) // we want to catch runtime exceptions, too!
		{
			log.error("Exception in AI " + aiTeam + ": " + ex.getMessage(), ex);
			
			// # Undo everything we've done this cycle to restore previous state
			// - RefereeMsg
			if (refereeMsg != null)
			{
				lastRefMsgCounter--;
			}
			
			if (athenaAiFrame != null)
			{
				athena.onException(athenaAiFrame);
			}
			
			ares.getSkillSystem().reset(aiTeam.getTeamColor());
			
			previousAIFrame.cleanUp();
			previousAIFrame = AIInfoFrame.fromBaseAiFrame(baseAiFrame);
		}
		return null;
	}
	
	
	/**
	 * @param msg The recently received message
	 * @return Whether this message does really new game-state information
	 * @author FriederB
	 */
	private boolean isNewMessage(final RefereeMsg msg)
	{
		if (msg.getCommandCounter() != lastRefMsgCounter)
		{
			lastRefMsgCounter = msg.getCommandCounter();
			return true;
		}
		
		return false;
	}
	
	
	public EAiTeam getAiTeam()
	{
		return aiTeam;
	}
	
	
	public Athena getAthena()
	{
		return athena;
	}
	
	
	public Metis getMetis()
	{
		return metis;
	}
	
	
	public AIInfoFrame getLatestAiFrame()
	{
		return previousAIFrame;
	}
}
