/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;


/**
 * The base frame contains only the basic frame data without any AI results
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BaseAiFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** Current worldFrame */
	private final WorldFrame		worldFrame;
	/** only contains new referee messages (for one frame); will be null otherwise. */
	private final RefereeMsg		refereeMsg;
	/** the updated referee message. It will contain current times, scores, etc. */
	private final RefereeMsg		latestRefereeMsg;
	/** previous frame */
	private transient AIInfoFrame	prevFrame;
	/** color of the team the AI controls */
	private final ETeamColor		teamColor;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param worldFrame
	 * @param newRefereeMsg
	 * @param latestRefereeMsg
	 * @param prevFrame
	 * @param teamColor
	 */
	public BaseAiFrame(final WorldFrame worldFrame, final RefereeMsg newRefereeMsg,
			final RefereeMsg latestRefereeMsg,
			final AIInfoFrame prevFrame, final ETeamColor teamColor)
	{
		this.worldFrame = worldFrame;
		refereeMsg = newRefereeMsg;
		this.latestRefereeMsg = latestRefereeMsg;
		this.prevFrame = prevFrame;
		this.teamColor = teamColor;
	}
	
	
	/**
	 * @param original
	 */
	public BaseAiFrame(final BaseAiFrame original)
	{
		worldFrame = new WorldFrame(original.worldFrame);
		refereeMsg = original.refereeMsg;
		latestRefereeMsg = original.latestRefereeMsg;
		prevFrame = original.prevFrame;
		teamColor = original.teamColor;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Clean up old aiFrames
	 */
	public void cleanUp()
	{
		prevFrame = null;
	}
	
	
	/**
	 * @return
	 */
	public final BotID getKeeperId()
	{
		if (teamColor == ETeamColor.YELLOW)
		{
			return BotID.createBotId(TeamConfig.getInstance().getTeamProps().getKeeperIdYellow(), teamColor);
		}
		return BotID.createBotId(TeamConfig.getInstance().getTeamProps().getKeeperIdBlue(), teamColor);
	}
	
	
	/**
	 * @return
	 */
	public final BotID getKeeperFoeId()
	{
		if (teamColor == ETeamColor.YELLOW)
		{
			return BotID.createBotId(TeamConfig.getInstance().getTeamProps().getKeeperIdBlue(), teamColor);
		}
		return BotID.createBotId(TeamConfig.getInstance().getTeamProps().getKeeperIdYellow(), teamColor);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the worldFrame
	 */
	public WorldFrame getWorldFrame()
	{
		return worldFrame;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	public RefereeMsg getNewRefereeMsg()
	{
		return refereeMsg;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	public RefereeMsg getLatestRefereeMsg()
	{
		return latestRefereeMsg;
	}
	
	
	/**
	 * @return the prevFrame
	 */
	public AIInfoFrame getPrevFrame()
	{
		return prevFrame;
	}
	
	
	/**
	 * @return the teamColor
	 */
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
}
