/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AICom;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;


/**
 * The base frame contains only the basic frame data without any AI results
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BaseAiFrame implements IRecordFrame
{
	/** Current worldFrame */
	private final WorldFrame			worldFrame;
	/** only contains new referee messages (for one frame); will be null otherwise. */
	private final RefereeMsg			refereeMsg;
	/** the updated referee message. It will contain current times, scores, etc. */
	private final RefereeMsg			latestRefereeMsg;
	/** previous frame */
	private transient IRecordFrame	prevFrame;
	/** color of the team the AI controls */
	private final ETeamColor			teamColor;
	/** AICommunication */
	private final AICom					aiCom;
	
	
	/**
	 * @param worldFrame
	 * @param newRefereeMsg
	 * @param latestRefereeMsg
	 * @param prevFrame
	 * @param teamColor
	 */
	public BaseAiFrame(final WorldFrame worldFrame, final RefereeMsg newRefereeMsg,
			final RefereeMsg latestRefereeMsg,
			final IRecordFrame prevFrame, final ETeamColor teamColor)
	{
		this.worldFrame = worldFrame;
		refereeMsg = newRefereeMsg;
		this.latestRefereeMsg = latestRefereeMsg;
		this.prevFrame = prevFrame;
		this.teamColor = teamColor;
		aiCom = new AICom();
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
		aiCom = original.aiCom;
	}
	
	
	/**
	 * Clean up old aiFrames
	 */
	@Override
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
			return BotID.createBotId(TeamConfig.getKeeperIdYellow(), teamColor);
		}
		return BotID.createBotId(TeamConfig.getKeeperIdBlue(), teamColor);
	}
	
	
	/**
	 * @return
	 */
	public final BotID getKeeperFoeId()
	{
		if (teamColor == ETeamColor.YELLOW)
		{
			return BotID.createBotId(TeamConfig.getKeeperIdBlue(), teamColor.opposite());
		}
		return BotID.createBotId(TeamConfig.getKeeperIdYellow(), teamColor.opposite());
	}
	
	
	/**
	 * @return the worldFrame
	 */
	@Override
	public WorldFrame getWorldFrame()
	{
		return worldFrame;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	@Override
	public RefereeMsg getNewRefereeMsg()
	{
		return refereeMsg;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	@Override
	public RefereeMsg getLatestRefereeMsg()
	{
		return latestRefereeMsg;
	}
	
	
	/**
	 * @return the prevFrame
	 */
	public IRecordFrame getPrevFrame()
	{
		return prevFrame;
	}
	
	
	/**
	 * @return the teamColor
	 */
	@Override
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	/**
	 * @return the AICommunication DataHolder
	 */
	@Override
	public AICom getAICom()
	{
		return aiCom;
	}
	
	
	@Override
	public float getFps()
	{
		return 0;
	}
	
	
	@Override
	public void setId(final int id)
	{
		throw new IllegalStateException("Its not intended to call this method on this implementation!");
	}
}
