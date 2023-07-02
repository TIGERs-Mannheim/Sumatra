/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.stopcondition;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.util.Collection;


@RequiredArgsConstructor
public class BotsNotMovingStopCondition implements AiSimTimeBlocker.IStopCondition
{
	final double duration;
	long firstTimeNoBotIsMoving = 0;


	@Override
	public boolean stopSimulation(final AIInfoFrame frame)
	{
		if (noBotIsMoving(frame.getWorldFrame().getBots().values()))
		{
			if (firstTimeNoBotIsMoving == 0)
			{
				firstTimeNoBotIsMoving = frame.getWorldFrame().getTimestamp();
			}
		} else
		{
			firstTimeNoBotIsMoving = 0;
		}
		double dt = (frame.getWorldFrame().getTimestamp() - firstTimeNoBotIsMoving) / 1e9;
		return firstTimeNoBotIsMoving != 0 && dt > duration;
	}


	private boolean noBotIsMoving(final Collection<ITrackedBot> values)
	{
		return values.stream().allMatch(b -> b.getVel().getLength2() < 0.01);
	}
}
