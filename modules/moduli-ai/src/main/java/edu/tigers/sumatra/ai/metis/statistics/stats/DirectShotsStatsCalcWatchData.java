/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallKickFitState;

import java.util.HashSet;
import java.util.Set;


public record DirectShotsStatsCalcWatchData(IKickEvent currentShot, long timestampDirectShotEnded, double maxShotSpeed,
                                            Set<BotID> ballTouchingOpponents)
{

	public static DirectShotsStatsCalcWatchData fromCurrentShot(IKickEvent currentShot)
	{
		return new DirectShotsStatsCalcWatchData(currentShot, -1, 0.0, new HashSet<>());
	}


	public DirectShotsStatsCalcWatchData update(Set<BotID> currentlyTouchingOpponents,
			final BallKickFitState kickFitState)
	{
		var newBallTouchingOpponents = new HashSet<>(ballTouchingOpponents);
		newBallTouchingOpponents.addAll(currentlyTouchingOpponents);
		var newMaxShotSpeed = kickFitState != null && kickFitState.getKickPos().isCloseTo(currentShot.getPosition())
				? kickFitState.getAbsoluteKickSpeed()
				: maxShotSpeed;
		return new DirectShotsStatsCalcWatchData(currentShot, timestampDirectShotEnded, newMaxShotSpeed,
				newBallTouchingOpponents);
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj.getClass() != DirectShotsStatsCalcWatchData.class)
		{
			return false;
		}
		var objAsData = (DirectShotsStatsCalcWatchData) obj;
		return objAsData.currentShot.getTimestamp() == this.currentShot.getTimestamp();
	}
}
