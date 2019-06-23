/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
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
	private final WorldFrameWrapper worldFrameWrapper;
	private final boolean newRefereeMsg;
	private final EAiTeam aiTeam;
	
	private final WorldFrame worldFrame;
	private final RefereeMsg refereeMsg;
	private final GameState gamestate;
	
	/** previous frame */
	private AIInfoFrame prevFrame;
	
	
	/**
	 * @param worldFrameWrapper
	 * @param newRefereeMsg
	 * @param prevFrame
	 * @param aiTeam
	 */
	public BaseAiFrame(final WorldFrameWrapper worldFrameWrapper, final boolean newRefereeMsg,
			final AIInfoFrame prevFrame, final EAiTeam aiTeam)
	{
		this.worldFrameWrapper = worldFrameWrapper;
		this.prevFrame = prevFrame;
		this.aiTeam = aiTeam;
		this.newRefereeMsg = newRefereeMsg;
		
		worldFrame = worldFrameWrapper.getWorldFrame(aiTeam);
		refereeMsg = worldFrameWrapper.getRefereeMsg();
		gamestate = GameState.Builder.create()
				.withGameState(worldFrameWrapper.getGameState())
				.withOurTeam(aiTeam.getTeamColor())
				.build();
	}
	
	
	/**
	 * @param original
	 */
	public BaseAiFrame(final BaseAiFrame original)
	{
		worldFrameWrapper = original.worldFrameWrapper;
		prevFrame = original.prevFrame;
		aiTeam = original.aiTeam;
		newRefereeMsg = original.newRefereeMsg;
		
		worldFrame = worldFrameWrapper.getWorldFrame(aiTeam);
		refereeMsg = original.refereeMsg;
		gamestate = original.gamestate;
	}
	
	
	/**
	 * clear prevFrame to avoid memory leak
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
		return BotID.createBotId(refereeMsg.getTeamInfo(aiTeam.getTeamColor()).getGoalie(), aiTeam.getTeamColor());
	}
	
	
	/**
	 * @return
	 */
	public final BotID getKeeperFoeId()
	{
		return BotID.createBotId(refereeMsg.getTeamInfo(aiTeam.getTeamColor().opposite()).getGoalie(),
				aiTeam.getTeamColor().opposite());
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
	public RefereeMsg getRefereeMsg()
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
		return aiTeam.getTeamColor();
	}
	
	
	/**
	 * @return the source AI
	 */
	public EAiTeam getAiTeam()
	{
		return aiTeam;
	}
	
	
	/**
	 * @return the simpleWorldFrame
	 */
	public final SimpleWorldFrame getSimpleWorldFrame()
	{
		return worldFrameWrapper.getSimpleWorldFrame();
	}
	
	
	/**
	 * @return the newRefereeMsg
	 */
	public final boolean isNewRefereeMsg()
	{
		return newRefereeMsg;
	}
	
	
	/**
	 * @return the current game state
	 */
	public GameState getGamestate()
	{
		return gamestate;
	}
}