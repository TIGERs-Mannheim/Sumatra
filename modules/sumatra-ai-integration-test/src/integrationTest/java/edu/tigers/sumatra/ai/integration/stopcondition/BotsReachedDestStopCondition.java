/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.stopcondition;

import edu.tigers.sumatra.ai.integration.blocker.WpSimTimeBlocker;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BotsReachedDestStopCondition implements WpSimTimeBlocker.IStopCondition
{
	private final Snapshot snapshot;


	@Override
	public boolean stopSimulation(WorldFrameWrapper wfw)
	{
		return allBotsReachedDest(wfw.getSimpleWorldFrame(), snapshot);
	}


	private boolean allBotsReachedDest(SimpleWorldFrame wFrame, Snapshot snapshot)
	{
		for (var entry : snapshot.getMoveDestinations().entrySet())
		{
			BotID id = entry.getKey();
			IVector2 targetPos = entry.getValue().getXYVector();
			ITrackedBot bot = wFrame.getBot(id);
			if (bot == null)
			{
				return false;
			}
			IVector2 currentPos = bot.getPos();
			if (currentPos.distanceTo(targetPos) > 1)
			{
				return false;
			}
		}
		return true;
	}
}
