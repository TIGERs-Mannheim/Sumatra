/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.awt.Color;


public class KickInsBlaueModeFarBackspin extends KickInsBlaueModeFar
{
	@Override
	protected void drawFilterParameter()
	{
		drawTurnAngleSector(KickInsBlaueActionMove.minKickDistance, KickInsBlaueActionMove.maxKickDistance,
				Math.max(filterParameters.getMaxAllowedTurnAngleBACKSPIN(), 0.), Color.ORANGE);
		drawPenaltyAreas(Color.CYAN);
	}
	
	
	@Override
	protected KickTarget createKickTarget(final RatedPosition finalPosition)
	{
		return KickTarget.kickInsBlaue(new DynamicPosition(finalPosition.getPosition(), 0.4),
				OffensiveConstants.getBallSpeedAtTargetKickInsBlaue(),
				KickTarget.ChipPolicy.FORCE_CHIP);
	}
	
	
	@Override
	protected boolean preFilterByKickDistance(final IVector2 gridPoint)
	{
		// Use min/max kickDistance
		return (filterParameters.getBallPosition().distanceToSqr(gridPoint) < minKickDistanceSqr
				|| filterParameters.getBallPosition().distanceToSqr(gridPoint) > maxKickDistanceSqr);
	}
	
	
	@Override
	protected boolean preFilterByTurnAngle(final double ball2GridPointAngle)
	{
		final double neededTurnAngle = Math
				.abs(AngleMath.difference(filterParameters.getBotAngleToBall(), ball2GridPointAngle));
		return (Double.compare(filterParameters.getMaxAllowedTurnAngleBACKSPIN(), neededTurnAngle) == -1);
	}
	
	
	@Override
	protected boolean preFilterByBallLeaveField(final IVector2 gridPoint, final double ball2GridPointAngle)
	{
		return false;
	}
	
	
	@Override
	protected boolean isPassWayFree(final BotID id, final IVector2 target, final WorldFrame worldFrame)
	{
		// This mode will always chip, but isChipPassWayFree only returns true if there's a bot in the passWay
		return isStraightPassWayFree(target, worldFrame) || isChipPassWayFree(id, target, worldFrame, 1);
	}
	
}
