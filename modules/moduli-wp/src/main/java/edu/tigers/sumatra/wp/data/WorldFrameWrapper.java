/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.persistence.PersistenceTable;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;

import java.util.EnumMap;
import java.util.Map;


/**
 * Wrapper for different worldframes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class WorldFrameWrapper implements PersistenceTable.IEntry<WorldFrameWrapper>
{
	private final long timestamp;
	
	private final long timestampMs = System.currentTimeMillis();
	private final SimpleWorldFrame simpleWorldFrame;
	private final RefereeMsg refereeMsg;
	private GameState gameState = GameState.HALT;
	
	private final transient Map<EAiTeam, WorldFrame> worldFrames = new EnumMap<>(EAiTeam.class);
	
	
	@SuppressWarnings("unused") // Required (to be public) by Fury for transient field initialization.
	public WorldFrameWrapper()
	{
		timestamp = 0;
		simpleWorldFrame = null;
		refereeMsg = new RefereeMsg();
	}
	
	
	public WorldFrameWrapper(final SimpleWorldFrame swf, final RefereeMsg refereeMsg, final GameState gameState)
	{
		assert refereeMsg != null;
		assert swf != null;
		timestamp = swf.getTimestamp();
		simpleWorldFrame = swf;
		this.refereeMsg = refereeMsg;
		this.gameState = gameState;
		worldFrames.computeIfAbsent(EAiTeam.YELLOW, t -> createWorldFrame(swf, t));
		worldFrames.computeIfAbsent(EAiTeam.BLUE, t -> createWorldFrame(swf, t));
	}
	
	
	/**
	 * @param wfw instance to copy
	 */
	public WorldFrameWrapper(final WorldFrameWrapper wfw)
	{
		timestamp = wfw.getSimpleWorldFrame().getTimestamp();
		simpleWorldFrame = wfw.simpleWorldFrame;
		refereeMsg = wfw.refereeMsg;
		worldFrames.putAll(wfw.worldFrames);
		gameState = wfw.gameState;
	}
	
	
	/**
	 * Create WF from swf
	 * 
	 * @param swf
	 * @param aiTeam
	 * @return
	 */
	private WorldFrame createWorldFrame(final SimpleWorldFrame swf, final EAiTeam aiTeam)
	{
		final WorldFrame wf;
		if (refereeMsg.getNegativeHalfTeam() == aiTeam.getTeamColor())
		{
			wf = new WorldFrame(swf, aiTeam, false);
		} else
		{
			// right team will be mirrored
			wf = new WorldFrame(swf.mirrored(), aiTeam, true);
		}
		return wf;
	}
	
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the unix timestamp in [ms]
	 */
	public final long getUnixTimestamp()
	{
		return timestampMs;
	}
	
	
	/**
	 * @return the simpleWorldFrame
	 */
	public SimpleWorldFrame getSimpleWorldFrame()
	{
		return simpleWorldFrame;
	}
	
	
	/**
	 * @param aiTeam
	 * @return the worldFrames
	 */
	public final WorldFrame getWorldFrame(final EAiTeam aiTeam)
	{
		return worldFrames.computeIfAbsent(aiTeam, t -> createWorldFrame(simpleWorldFrame, t));
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
	public final GameState getGameState()
	{
		return gameState;
	}


	@Override
	public long getKey()
	{
		return getTimestamp();
	}


	@Override
	public void merge(WorldFrameWrapper other)
	{
		worldFrames.putAll(other.worldFrames);
	}
}
