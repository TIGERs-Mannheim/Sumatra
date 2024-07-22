/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.placement;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Calculates the pull away destination when the ball is near a boundary, corner or goal.
 */
public class PullAwayDestCalc
{
	@Configurable(defValue = "120.0", comment = "If ball is closer than this distance [mm] to a boundary, ball is pulled away orthogonally to boundary or corner")
	private static double pullAwayMargin = 120;

	@Configurable(defValue = "300.0", comment = "Distance [mm] that the ball has to be away from the boundary. Must be large enough that the bot can position between ball and boundary easily.")
	private static double pullAwayMinDistance = 300;

	@Configurable(defValue = "100.0", comment = "Distance [mm] to add when pulling ball away (hysteresis)")
	private static double pullAwayHysteresis = 100;

	static
	{
		ConfigRegistration.registerClass("roles", PullAwayDestCalc.class);
	}

	@Getter
	private List<IDrawableShape> shapes = new ArrayList<>();


	public Optional<IVector2> getPullAwayBallTarget(IVector2 ballPos)
	{
		shapes = new ArrayList<>();

		List<PullAwayCorner> pullAwayCorners = getPullAwayCorners();
		pullAwayCorners.stream().flatMap(pac -> pac.getShapes().stream()).forEach(shapes::add);

		List<PullAwayArea> pullAwayAreas = getPullAwayAreas();
		pullAwayAreas.stream().flatMap(paa -> paa.getShapes().stream()).forEach(shapes::add);

		IRectangle pullInRect = Geometry.getFieldWBorders().withMargin(-pullAwayMinDistance);
		IRectangle pullOutRect = pullInRect.withMargin(-pullAwayHysteresis);
		shapes.add(new DrawableRectangle(pullInRect).setColor(Color.black));
		shapes.add(new DrawableRectangle(pullOutRect).setColor(Color.magenta));

		Optional<IVector2> cornerSubTarget = pullAwayCorners.stream()
				.filter(corner -> corner.isPointInShape(ballPos))
				.findFirst()
				.map(corner -> corner.pullToPoint);

		if (cornerSubTarget.isPresent())
		{
			return cornerSubTarget;
		}

		Optional<IVector2> areaSubTarget = pullAwayAreas.stream()
				.filter(paa -> paa.isPointInShape(ballPos))
				.findFirst()
				.map(paa -> paa.pullToLine.closestPointOnPath(ballPos));
		if (areaSubTarget.isPresent())
		{
			return areaSubTarget;
		}

		// find a point that is at some distance away from boundary, so that robot can
		// approach the ball from the side
		if (pullInRect.isPointInShape(ballPos))
		{
			// ball is already far enough away from boundary, we can directly use the target
			return Optional.empty();
		}
		// pull straight from boundary backwards to get the ball away from the boundary
		return Optional.of(pullOutRect.nearestPointInside(ballPos));
	}


	private List<PullAwayCorner> getPullAwayCorners()
	{
		List<PullAwayCorner> pullAwayCorners = new ArrayList<>();

		// Field corners
		Geometry.getFieldWBorders().getCorners().stream()
				.map(corner -> createPullAwayCorner(
								corner,
								Vector2.fromXY(-pullAwayMargin, -pullAwayMargin),
								Vector2.fromXY(-pullAwayMinDistance, -pullAwayMinDistance)
						)
				).forEach(pullAwayCorners::add);

		// Goal inner corners
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getCorners().stream())
				.map(corner -> createPullAwayCorner(
						corner,
						Vector2.fromXY(-pullAwayMargin, -pullAwayMargin),
						Vector2.fromXY(-pullAwayMinDistance, -pullAwayMinDistance)
				))
				.forEach(pullAwayCorners::add);

		// Goal outer corners
		double goalToBoundaryOffset = Geometry.getBoundaryLength() - Geometry.getGoalOur().getDepth();
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getCorners().stream())
				.map(corner -> corner.addMagnitude(Vector2.fromX(goalToBoundaryOffset)))
				.map(corner -> createPullAwayCorner(
						corner,
						Vector2.fromXY(-pullAwayMargin, pullAwayMargin),
						Vector2.fromXY(-pullAwayMinDistance, pullAwayMinDistance)
				))
				.forEach(pullAwayCorners::add);

		return Collections.unmodifiableList(pullAwayCorners);
	}


	private PullAwayCorner createPullAwayCorner(IVector2 corner, IVector2 magnitudeArea, IVector2 magnitudePullAway)
	{
		return new PullAwayCorner(
				Rectangle.fromPoints(corner, corner.addMagnitude(magnitudeArea)),
				corner.addMagnitude(magnitudePullAway.addMagnitude(Vector2.fromXY(pullAwayHysteresis, pullAwayHysteresis)))
		);
	}


	private List<PullAwayArea> getPullAwayAreas()
	{
		List<PullAwayArea> pullAwayAreas = new ArrayList<>();

		// outside of goal side walls
		double pullAwayDist = pullAwayMargin + pullAwayHysteresis;
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getGoalPosts().stream())
				.map(goalPost -> new PullAwayArea(
								Rectangle.fromPoints(
										goalPost,
										goalPost.addMagnitude(Vector2.fromXY(Geometry.getBoundaryLength(), pullAwayMargin))
								),
								Lines.segmentFromPoints(
										goalPost.addMagnitude(Vector2.fromXY(0, pullAwayDist)),
										goalPost.addMagnitude(Vector2.fromXY(Geometry.getBoundaryLength(), pullAwayDist))
								)
						)
				).forEach(pullAwayAreas::add);

		// inside of goal side walls
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getGoalPosts().stream())
				.map(goalPost -> new PullAwayArea(
								Rectangle.fromPoints(
										goalPost,
										goalPost.addMagnitude(Vector2.fromXY(Geometry.getGoalOur().getDepth(), -pullAwayMargin))
								),
								Lines.segmentFromPoints(
										goalPost.addMagnitude(Vector2.fromXY(0, -pullAwayDist)),
										goalPost.addMagnitude(Vector2.fromXY(Geometry.getGoalOur().getDepth(), -pullAwayDist))
								)
						)
				).forEach(pullAwayAreas::add);

		// inside of goal inner wall (to make sure that independently of boundary width the ball is far enough away from inner goal wall
		double goalPostOffsetX = Geometry.getGoalOur().getDepth() - pullAwayMinDistance;
		Geometry.getGoals().stream()
				.map(goal -> new PullAwayArea(
								Rectangle.fromPoints(
										goal.getLeftPost()
												.addMagnitude(Vector2.fromXY(goalPostOffsetX, 0)),
										goal.getRightPost().addMagnitude(Vector2.fromXY(Geometry.getGoalOur().getDepth(), 0))
								),
								Lines.segmentFromPoints(
										goal.getLeftPost().addMagnitude(Vector2.fromXY(goalPostOffsetX - pullAwayHysteresis, 0)),
										goal.getRightPost().addMagnitude(Vector2.fromXY(goalPostOffsetX - pullAwayHysteresis, 0))
								)
						)
				).forEach(pullAwayAreas::add);
		return Collections.unmodifiableList(pullAwayAreas);
	}


	private record PullAwayCorner(Rectangle area, IVector2 pullToPoint)
	{
		public boolean isPointInShape(IVector2 point)
		{
			return area.isPointInShape(point);
		}


		public List<IDrawableShape> getShapes()
		{
			return List.of(
					new DrawableRectangle(area),
					new DrawablePoint(pullToPoint).setColor(Color.magenta)
			);
		}
	}

	private record PullAwayArea(IRectangle area, ILineSegment pullToLine)
	{
		public boolean isPointInShape(IVector2 point)
		{
			return area.isPointInShape(point);
		}


		public List<IDrawableShape> getShapes()
		{
			return List.of(
					new DrawableRectangle(area),
					new DrawableLine(pullToLine).setColor(Color.magenta)
			);
		}
	}
}
