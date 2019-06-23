/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.referee.TeamConfig;


/**
 * Wrapper for different worldframes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class WorldFrameWrapper
{
	private final SimpleWorldFrame								simpleWorldFrame;
	private final RefereeMsg										refereeMsg;
	private final ShapeMap											shapeMap;
																			
	private transient final Map<ETeamColor, WorldFrame>	worldFrames	= new HashMap<ETeamColor, WorldFrame>(2);
	private EGameStateNeutral										gameState	= EGameStateNeutral.UNKNOWN;
																							
																							
	@SuppressWarnings("unused")
	private WorldFrameWrapper()
	{
		simpleWorldFrame = null;
		refereeMsg = new RefereeMsg();
		shapeMap = new ShapeMap();
	}
	
	
	/**
	 * @param swf
	 * @param refereeMsg
	 * @param shapeMap
	 */
	public WorldFrameWrapper(final SimpleWorldFrame swf, final RefereeMsg refereeMsg, final ShapeMap shapeMap)
	{
		assert refereeMsg != null;
		assert swf != null;
		simpleWorldFrame = swf;
		this.refereeMsg = refereeMsg;
		this.shapeMap = shapeMap;
	}
	
	
	/**
	 * @param wfw
	 */
	public WorldFrameWrapper(final WorldFrameWrapper wfw)
	{
		simpleWorldFrame = wfw.simpleWorldFrame;
		refereeMsg = wfw.refereeMsg;
		shapeMap = new ShapeMap(wfw.shapeMap);
		worldFrames.putAll(wfw.worldFrames);
		gameState = wfw.gameState;
	}
	
	
	/**
	 * Create WF from swf
	 * 
	 * @param swf
	 * @param teamColor
	 * @return
	 */
	private WorldFrame createWorldFrame(final SimpleWorldFrame swf, final RefereeMsg refereeMsg,
			final ETeamColor teamColor)
	{
		final WorldFrame wf;
		// if (refereeMsg.getLeftTeam() == teamColor)
		if (TeamConfig.getLeftTeam() == teamColor)
		{
			wf = new WorldFrame(swf, teamColor, false);
		} else
		{
			// right team will be mirrored
			wf = new WorldFrame(swf.mirrorNew(), teamColor, true);
		}
		return wf;
	}
	
	
	/**
	 * @return the simpleWorldFrame
	 */
	public SimpleWorldFrame getSimpleWorldFrame()
	{
		return simpleWorldFrame;
	}
	
	
	/**
	 * @param teamColor
	 * @return the worldFrames
	 */
	public synchronized WorldFrame getWorldFrame(final ETeamColor teamColor)
	{
		WorldFrame wf = worldFrames.get(teamColor);
		if (wf == null)
		{
			wf = createWorldFrame(simpleWorldFrame, refereeMsg, teamColor);
			worldFrames.put(teamColor, wf);
		}
		return wf;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	public final RefereeMsg getRefereeMsg()
	{
		return refereeMsg;
	}
	
	
	/**
	 * @return the gameState
	 */
	public final EGameStateNeutral getGameState()
	{
		return gameState;
	}
	
	
	/**
	 * @return the shapeMap
	 */
	public final ShapeMap getShapeMap()
	{
		return shapeMap;
	}
	
	
	/**
	 * @param gameState the gameState to set
	 */
	public final void setGameState(final EGameStateNeutral gameState)
	{
		this.gameState = gameState;
	}
}
