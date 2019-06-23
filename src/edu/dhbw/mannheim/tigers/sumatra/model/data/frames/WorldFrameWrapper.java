/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.WorldFrameFactory;


/**
 * Wrapper for different worldframes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class WorldFrameWrapper
{
	private final SimpleWorldFrame				simpleWorldFrame;
	private final Map<ETeamColor, WorldFrame>	worldFrames	= new HashMap<ETeamColor, WorldFrame>(2);
	
	
	/**
	 * @param swf
	 */
	public WorldFrameWrapper(final SimpleWorldFrame swf)
	{
		simpleWorldFrame = swf;
		worldFrames.put(ETeamColor.YELLOW, createWorldFrame(swf, ETeamColor.YELLOW));
		worldFrames.put(ETeamColor.BLUE, createWorldFrame(swf, ETeamColor.BLUE));
	}
	
	
	/**
	 * Create a default empty instance
	 * 
	 * @param nextFrameNumber
	 * @return
	 */
	public static WorldFrameWrapper createDefault(final long nextFrameNumber)
	{
		SimpleWorldFrame wFrame = WorldFrameFactory.createEmptyWorldFrame(nextFrameNumber);
		WorldFrameWrapper wfWrapper = new WorldFrameWrapper(wFrame);
		return wfWrapper;
	}
	
	
	/**
	 * Create WF from swf
	 * 
	 * @param swf
	 * @param teamColor
	 * @return
	 */
	private WorldFrame createWorldFrame(final SimpleWorldFrame swf, final ETeamColor teamColor)
	{
		final WorldFrame wf;
		if (TeamConfig.getLeftTeam() != teamColor)
		{
			wf = new WorldFrame(swf.mirrorNew(), teamColor, true);
		} else
		{
			wf = new WorldFrame(swf, teamColor, false);
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
	public WorldFrame getWorldFrame(final ETeamColor teamColor)
	{
		return worldFrames.get(teamColor);
	}
}
