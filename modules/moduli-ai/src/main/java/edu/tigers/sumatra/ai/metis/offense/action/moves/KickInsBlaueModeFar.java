/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class KickInsBlaueModeFar extends AKickInsBlaueMode
{
	@Override
	protected void drawFilterParameter()
	{
		drawTurnAngleSector(KickInsBlaueActionMove.minKickDistance, KickInsBlaueActionMove.maxKickDistance,
				Math.max(filterParameters.getMaxAllowedTurnAngle(), 0.), Color.BLUE);
		drawPenaltyAreas(Color.RED);
	}
	
	
	@Override
	protected List<IVector2> createFullPositionGrid()
	{
		List<IVector2> grid = new ArrayList<>();
		for (int length = (int) Math
				.floor((filterParameters.getBallPosition().x() > 0) ? 0.5 * KickInsBlaueActionMove.pointNumberLength
						: (KickInsBlaueActionMove.pointNumberLength + 1)
								* (filterParameters.getBallPosition().x() + 0.5 * Geometry.getFieldLength())
								/ Geometry.getFieldLength()); length < KickInsBlaueActionMove.pointNumberLength; length++)
		{
			for (int width = 0; width < KickInsBlaueActionMove.pointNumberWidth; width++)
			{
				// Grid Point with length=width=0 is at -x/-y coordinates
				// | - 0 - 1 - | Two Points 0 1; Three spacers overall - - -; One spacer to get to point 0
				IVector2 possibleGridPoint = Vector2.fromXY(
						-0.5 * Geometry.getFieldLength()
								+ (length + 1) * Geometry.getFieldLength() / (KickInsBlaueActionMove.pointNumberLength + 1),
						-0.5 * Geometry.getFieldWidth()
								+ (width + 1) * Geometry.getFieldWidth() / (KickInsBlaueActionMove.pointNumberWidth + 1));
				
				if (!filterParameters.getPenaltyAreaTheirsWithMargin().isPointInShape(possibleGridPoint)
						&& !filterParameters.getPenaltyAreaOursWithMargin().isPointInShape(possibleGridPoint))
				{
					grid.add(possibleGridPoint);
				}
			}
		}
		return grid;
	}
	
	
	protected void drawPenaltyAreas(final Color color)
	{
		shapes.add(new DrawableRectangle(filterParameters.getPenaltyAreaOursWithMargin().getRectangle(), color));
		shapes.add(new DrawableRectangle(filterParameters.getPenaltyAreaTheirsWithMargin().getRectangle(), color));
	}
	
	
	@Override
	protected KickTarget createKickTarget(final RatedPosition finalPosition)
	{
		return KickTarget.kickInsBlaue(new DynamicPosition(finalPosition.getPosition(), 0.4),
				OffensiveConstants.getBallSpeedAtTargetKickInsBlaue(),
				KickTarget.ChipPolicy.ALLOW_CHIP);
	}
	
	
	@Override
	protected boolean preFilterByKickDistance(final IVector2 gridPoint)
	{
		// Use min/max kickDistance
		return (filterParameters.getBallPosition().distanceToSqr(gridPoint) < minKickDistanceSqr
				|| filterParameters.getBallPosition().distanceToSqr(gridPoint) > maxKickDistanceSqr);
	}
	
	
	@Override
	protected boolean preFilterByPosition(final IVector2 gridPoint)
	{
		return false;
	}
	
	
	@Override
	protected boolean preFilterByTurnAngle(final double ball2GridPointAngle)
	{
		final double neededTurnAngle = Math
				.abs(AngleMath.difference(filterParameters.getBotAngleToBall(), ball2GridPointAngle));
		return (Double.compare(filterParameters.getMaxAllowedTurnAngle(), neededTurnAngle) == -1);
	}
	
	
	@Override
	protected boolean preFilterByBallLeaveField(final IVector2 gridPoint, final double ball2GridPointAngle)
	{
		
		List<IVector2> ballLeaveFieldPoint = Geometry.getField()
				.lineIntersections(Lines.halfLineFromDirection(gridPoint, Vector2.fromAngle(ball2GridPointAngle)));
		// Ball with infinite speed never leaving or leaving more than once is weird, filter out
		if (ballLeaveFieldPoint.size() != 1)
		{
			return true;
		}
		
		return (ballLeaveFieldPoint.get(0).distanceToSqr(gridPoint) < filterParameters.getMinDistanceToOutSqr());
	}
	
	
	@Override
	protected void rateByBotDistance(final BaseAiFrame baseAiFrame, final List<RatedPosition> grid)
	{
		for (RatedPosition rp : grid)
		{
			final double deltaX = rp.getPosition().x() - filterParameters.getBallPosition().x();
			rp.setScore(getSmallestFoeDistance(rp, baseAiFrame)
					- getSmallestTigerDistance(rp, baseAiFrame) + 2 * deltaX);
		}
	}
	
	
	@Override
	protected void rateAndFilterByFreePassWay(final BotID id, final BaseAiFrame baseAiFrame,
			final List<RatedPosition> grid)
	{
		grid.stream().filter(e -> !isPassWayFree(id, e.getPosition(), baseAiFrame.getWorldFrame()))
				.forEach(e -> e.setStatus(ERatedPositionStatus.OUT_BY_NO_PASS_WAY));
	}
	
	
	protected boolean isPassWayFree(final BotID id, final IVector2 target, final WorldFrame worldFrame)
	{
		return isStraightPassWayFree(target, worldFrame) || isChipPassWayFree(id, target, worldFrame, 3);
	}
}
