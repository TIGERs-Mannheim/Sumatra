/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;


public class KickInsBlaueModeNear extends AKickInsBlaueMode
{
	@Override
	protected void drawFilterParameter()
	{
		// PenArea -> NEAR_TO_GOAL mode not allowed in it
		shapes.add(new DrawableRectangle(Geometry.getPenaltyAreaTheir().getRectangle(), Color.RED));
		
		// Max Distance for NEAR_TO_GOAL mode
		ICircle circle = Circle.createCircle(Geometry.getGoalTheir().getCenter(),
				KickInsBlaueActionMove.maxDistanceToGoalCenter);
		List<IVector2> intersections = circle.lineIntersections(Geometry.getField().getEdgesAsSegments().get(1));
		double angle = intersections.isEmpty() ? AngleMath.PI_HALF
				: Vector2.fromPoints(Geometry.getGoalTheir().getCenter(), intersections.get(0)).getAngle();
		DrawableArc arc = new DrawableArc(
				Arc.createArc(Geometry.getGoalTheir().getCenter(), KickInsBlaueActionMove.maxDistanceToGoalCenter,
						angle, AngleMath.difference(-angle, angle)),
				Color.RED);
		arc.setArcType(Arc2D.OPEN);
		shapes.add(arc);
		
		
		drawTurnAngleSector(KickInsBlaueActionMove.minKickDistance / 2, KickInsBlaueActionMove.maxKickDistance / 1.5,
				Math.max(filterParameters.getMaxAllowedTurnAngle(), 0.), Color.BLUE);
	}
	
	
	@Override
	protected List<IVector2> createFullPositionGrid()
	{
		List<IVector2> grid = new ArrayList<>();
		for (int length = 0; length < KickInsBlaueActionMove.pointNumberLength; length++)
		{
			for (int width = 0; width < KickInsBlaueActionMove.pointNumberWidth * 2; width++)
			{
				// Grid Point with length=width=0 is at 0/-y coordinates
				// | - 0 - 1 - | Two Points 0 1; Three spacers overall - - -; One spacer to get to point 0
				IVector2 possibleGridPoint = Vector2.fromXY(
						(length + 1) * Geometry.getFieldLength() / ((KickInsBlaueActionMove.pointNumberLength + 1) * 2),
						-0.5 * Geometry.getFieldWidth()
								+ (width + 1) * Geometry.getFieldWidth() / (KickInsBlaueActionMove.pointNumberWidth * 2 + 1));
				
				if (Geometry.getGoalTheir().getCenter().distanceToSqr(possibleGridPoint) <= maxDistanceToGoalCenterSqr
						&& !Geometry.getPenaltyAreaTheir().isPointInShape(possibleGridPoint))
				{
					grid.add(possibleGridPoint);
				}
			}
		}
		return grid;
	}
	
	
	@Override
	protected KickTarget createKickTarget(final RatedPosition finalPosition)
	{
		return KickTarget.kickInsBlaue(new DynamicPosition(finalPosition.getPosition(), 0.4),
				OffensiveConstants.getBallSpeedAtTargetKickInsBlaue() / 2.,
				KickTarget.ChipPolicy.NO_CHIP);
		
	}
	
	
	@Override
	protected boolean preFilterByKickDistance(final IVector2 gridPoint)
	{
		// Use customized min/max KickDistance
		return (filterParameters.getBallPosition().distanceToSqr(gridPoint) < minKickDistanceSqr / 4.
				|| filterParameters.getBallPosition().distanceToSqr(gridPoint) > maxKickDistanceSqr / 2.25);
	}
	
	
	@Override
	protected boolean preFilterByPosition(final IVector2 gridPoint)
	{
		// Always play towards the goal
		if (filterParameters.getBallPosition().x() > gridPoint.x()
				&& filterParameters.getPenaltyAreaTheirsWithMargin().getRectangle().minX() > gridPoint.x())
		{
			return true;
		}
		return (Math.abs(filterParameters.getBallPosition().y()) < Math.abs(gridPoint.y())
				&& filterParameters.getPenaltyAreaTheirsWithMargin().getRectangle().maxY() < Math.abs(gridPoint.y()));
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
		
		return (ballLeaveFieldPoint.get(0).distanceToSqr(gridPoint) < filterParameters.getMinDistanceToOutSqrNEAR());
	}
	
	
	@Override
	protected void rateByBotDistance(final BaseAiFrame baseAiFrame, final List<RatedPosition> grid)
	{
		for (RatedPosition rp : grid)
		{
			rp.setScore(getSmallestFoeDistance(rp, baseAiFrame)
					- getSmallestTigerDistance(rp, baseAiFrame));
		}
	}
	
	
	@Override
	protected void rateAndFilterByFreePassWay(final BotID id, final BaseAiFrame baseAiFrame,
			final List<RatedPosition> grid)
	{
		grid.stream().filter(e -> !isStraightPassWayFree(e.getPosition(), baseAiFrame.getWorldFrame()))
				.forEach(e -> e.setStatus(ERatedPositionStatus.OUT_BY_NO_PASS_WAY));
	}
}
