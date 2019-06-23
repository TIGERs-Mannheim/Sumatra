/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.positioning;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.path.MoveAlongPathRole;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.testplays.TestPlayManager;
import edu.tigers.sumatra.testplays.commands.CommandList;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PositioningPlay extends APlay implements IConfigObserver
{
	
	@Configurable(comment = "Repeat all paths when done", defValue = "false")
	private static boolean repeatMoves = false;
	
	
	/**
	 * Creates a new PositioningPlay
	 */
	public PositioningPlay()
	{
		
		super(EPlay.POSITIONING_PLAY);
	}
	
	
	private int getNextFreeRoleId()
	{
		
		int roleIndex = 0;
		List<Integer> assignedCommandIds = new ArrayList<>();
		for (ARole role : getRoles())
		{
			MoveAlongPathRole mr = (MoveAlongPathRole) role;
			assignedCommandIds.add(mr.getCommandId());
		}
		
		while (assignedCommandIds.contains(roleIndex))
		{
			roleIndex++;
		}
		
		return roleIndex;
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		List<CommandList> commandQueue = TestPlayManager.getInstance().getCommandQueue();
		
		if (commandQueue.size() <= getRoles().size())
		{
			throw new IllegalStateException("No more paths available in json file!");
		}
		
		int roleIndex = getNextFreeRoleId();
		
		return new MoveAlongPathRole(commandQueue.get(roleIndex).getCommands(), roleIndex, repeatMoves);
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		
		// Currently all logic is handled in the role
	}
	
	
	@Override
	protected void onGameStateChanged(final GameState gameState)
	{
		
		// This play does not react to gameStateChange events
	}
}
