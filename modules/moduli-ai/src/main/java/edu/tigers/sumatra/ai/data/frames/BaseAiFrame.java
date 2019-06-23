/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.frames;

import edu.tigers.sumatra.ai.data.AICom;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.RefereeMsgTeamSpec;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * The base frame contains only the basic frame data without any AI results
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BaseAiFrame
{
	private final WorldFrameWrapper	worldFrameWrapper;
	private final boolean				newRefereeMsg;
	private final ETeamColor			teamColor;
	private final AICom					aiCom;
	
	private final WorldFrame			worldFrame;
	private final RefereeMsgTeamSpec	refereeMsg;
	
	private MultiTeamMessage			multiTeamMessage	= null;
	
	/** previous frame */
	private AIInfoFrame					prevFrame;
	
	
	/**
	 * @param worldFrameWrapper
	 * @param newRefereeMsg
	 * @param prevFrame
	 * @param teamColor
	 */
	public BaseAiFrame(final WorldFrameWrapper worldFrameWrapper, final boolean newRefereeMsg,
			final AIInfoFrame prevFrame, final ETeamColor teamColor)
	{
		this.worldFrameWrapper = worldFrameWrapper;
		this.prevFrame = prevFrame;
		this.teamColor = teamColor;
		this.newRefereeMsg = newRefereeMsg;
		aiCom = new AICom();
		
		worldFrame = worldFrameWrapper.getWorldFrame(teamColor);
		refereeMsg = new RefereeMsgTeamSpec(worldFrameWrapper.getRefereeMsg(), teamColor);
	}
	
	
	/**
	 * @param original
	 */
	public BaseAiFrame(final BaseAiFrame original)
	{
		worldFrameWrapper = original.worldFrameWrapper;
		prevFrame = original.prevFrame;
		teamColor = original.teamColor;
		aiCom = original.aiCom;
		newRefereeMsg = original.newRefereeMsg;
		
		worldFrame = worldFrameWrapper.getWorldFrame(teamColor);
		refereeMsg = original.refereeMsg;
		
	}
	
	
	/**
	 * 
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
		return BotID.createBotId(refereeMsg.getTeamInfoTigers().getGoalie(), teamColor);
	}
	
	
	/**
	 * @return
	 */
	public final BotID getKeeperFoeId()
	{
		return BotID.createBotId(refereeMsg.getTeamInfoThem().getGoalie(), teamColor.opposite());
	}
	
	
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
	public RefereeMsgTeamSpec getRefereeMsg()
	{
		return refereeMsg;
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
	
	
	/**
	 * @return the AICommunication DataHolder
	 */
	public AICom getAICom()
	{
		return aiCom;
	}
	
	
	/**
	 * @return the simpleWorldFrame
	 */
	public final SimpleWorldFrame getSimpleWorldFrame()
	{
		return worldFrameWrapper.getSimpleWorldFrame();
	}
	
	
	/**
	 * @return
	 */
	public MultiTeamMessage getMultiTeamMessage()
	{
		return multiTeamMessage;
	}
	
	
	/**
	 * @param message
	 */
	public void setMultiTeamMessage(final MultiTeamMessage message)
	{
		multiTeamMessage = message;
	}
	
	
	/**
	 * @return the worldFrameWrapper
	 */
	public final WorldFrameWrapper getWorldFrameWrapper()
	{
		return worldFrameWrapper;
	}
	
	
	/**
	 * @return the newRefereeMsg
	 */
	public final boolean isNewRefereeMsg()
	{
		return newRefereeMsg;
	}
}
