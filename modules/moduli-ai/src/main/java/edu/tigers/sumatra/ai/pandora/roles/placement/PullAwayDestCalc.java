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
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import org.apache.commons.lang.Validate;

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
	@Configurable(defValue = "250.0", comment = "[mm] If ball is closer than this distance to a boundary or goal wall, the ball is pulled away. Must be large enough that a bot can position itself between ball and obstacle easily.")
	private static double pullAwayMargin = 250;

	@Configurable(defValue = "100.0", comment = "[mm] Extra distance to cover to accept ball is far enough from the next boundary")
	private static double pullAwayHysteresis = 100;

	static
	{
		ConfigRegistration.registerClass("roles", PullAwayDestCalc.class);
	}

	@Getter
	private List<IDrawableShape> shapes = new ArrayList<>();

	private boolean isPullOutOngoing = false;


	public Optional<IVector2> getPullAwayBallTarget(IVector2 ballPos)
	{
		shapes = new ArrayList<>();

		var pullAwayCorners = getPullAwayCorners();
		pullAwayCorners.stream().flatMap(pac -> pac.getShapes().stream()).forEach(shapes::add);

		var pullAwayAreas = getPullAwayAreas();
		pullAwayAreas.stream().flatMap(paa -> paa.getShapes().stream()).forEach(shapes::add);

		var cornerSubTarget = pullAwayCorners.stream()
				.filter(corner -> corner.mustBePulledOut(ballPos, isPullOutOngoing))
				.findFirst()
				.map(corner -> corner.pullToPoint);

		if (cornerSubTarget.isPresent())
		{
			isPullOutOngoing = true;
			return cornerSubTarget;
		}

		var areaSubTarget = pullAwayAreas.stream()
				.filter(paa -> paa.mustBePulledOut(ballPos, isPullOutOngoing))
				.findFirst()
				.map(paa -> paa.pullToLine.closestPointOnPath(ballPos));


		isPullOutOngoing = areaSubTarget.isPresent();

		return areaSubTarget;
	}


	private static double getPullAwayMargin()
	{
		return SumatraMath.max(Geometry.getBoundaryLength(), Geometry.getBoundaryWidth(), pullAwayMargin);
	}


	private List<PullAwayCorner> getPullAwayCorners()
	{
		List<PullAwayCorner> pullAwayCorners = new ArrayList<>();

		// Field corners
		Geometry.getFieldWBorders().getCorners().stream()
				.map(corner -> PullAwayCorner.create(
								corner,
						Vector2.fromXY(-1, -1)
						)
				).forEach(pullAwayCorners::add);

		// Goal inner corners
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getCorners().stream())
				.map(corner -> PullAwayCorner.create(
						corner,
						Vector2.fromXY(-1, -1)
				))
				.forEach(pullAwayCorners::add);

		// Goal outer corners
		double goalToBoundaryOffset = Geometry.getBoundaryLength() - Geometry.getGoalOur().getDepth();
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getCorners().stream())
				.map(corner -> corner.addMagnitude(Vector2.fromX(goalToBoundaryOffset)))
				.map(corner -> PullAwayCorner.create(
						corner,
						Vector2.fromXY(-1, 1)
				))
				.forEach(pullAwayCorners::add);

		return Collections.unmodifiableList(pullAwayCorners);
	}


	private List<PullAwayArea> getPullAwayAreas()
	{
		List<PullAwayArea> pullAwayAreas = new ArrayList<>();

		// outside of goal side walls
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getGoalPosts()
						.stream()
						.map(goalPost -> Lines.segmentFromPoints(
								goalPost,
								goalPost.addMagnitude(Vector2.fromXY(Geometry.getBoundaryLength(), 0))
						))
				)
				.map(outerWall -> PullAwayArea.create(
						outerWall,
						Vector2.fromXY(0, 1)
						)
				).forEach(pullAwayAreas::add);

		// inside of goal side walls
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getGoalPosts()
						.stream()
						.map(goalPost -> Lines.segmentFromPoints(
								goalPost,
								goalPost.addMagnitude(Vector2.fromXY(goal.getDepth(), 0))
						))
				)
				.map(outerWall -> PullAwayArea.create(
						outerWall,
						Vector2.fromXY(0, -1)
						)
				).forEach(pullAwayAreas::add);

		// Goal Posts towards the field
		Geometry.getGoals().stream()
				.flatMap(goal -> goal.getGoalPosts().stream())
				.map(goalPost -> PullAwayArea.create(
								Lines.segmentFromPoints(
										goalPost.addMagnitude(Vector2.fromXY(0, -getPullAwayMargin())),
										goalPost.addMagnitude(Vector2.fromXY(0, getPullAwayMargin()))
								),
						Vector2.fromXY(-1, 0)
						)
				).forEach(pullAwayAreas::add);

		// inside of goal inner wall (to make sure that independently of boundary width the ball is far enough away from inner goal wall
		Geometry.getGoals().stream()
				.map(goal -> PullAwayArea.create(
								Lines.segmentFromPoints(
										goal.getLeftPost().addMagnitude(Vector2.fromXY(goal.getDepth(), 0)),
										goal.getRightPost().addMagnitude(Vector2.fromXY(goal.getDepth(), 0))
								),
						Vector2.fromXY(-1, 0)
						)
				).forEach(pullAwayAreas::add);

		// Field boundaries
		Geometry.getFieldWBorders().getEdges().stream()
				.map(edge -> PullAwayArea.create(
								edge,
								SumatraMath.isZero(edge.directionVector().x()) ?
										Vector2.fromXY(-1, 0) :
										Vector2.fromXY(0, -1)
						)
				).forEach(pullAwayAreas::add);

		return Collections.unmodifiableList(pullAwayAreas);
	}


	private record PullAwayCorner(IRectangle exclusionArea, IRectangle hysteresisArea, IVector2 pullToPoint)
	{
		static PullAwayCorner create(IVector2 corner, IVector2 pullDirection)
		{
			Validate.isTrue(!SumatraMath.isZero(pullDirection.x()) && !SumatraMath.isZero(pullDirection.y()));
			Validate.isTrue(SumatraMath.isEqual(pullDirection.getLength(), Math.sqrt(2)));

			var magnitudeExclusion = pullDirection.multiplyNew(getPullAwayMargin());
			var magnitudeHysteresis = pullDirection.multiplyNew(pullAwayHysteresis);

			var exclusionCorner = corner.addMagnitude(magnitudeExclusion);
			var hysteresisCorner = exclusionCorner.addMagnitude(magnitudeHysteresis);
			var pullTarget = hysteresisCorner.addMagnitude(magnitudeHysteresis);

			return new PullAwayCorner(
					Rectangle.fromPoints(corner, exclusionCorner),
					Rectangle.fromPoints(corner, hysteresisCorner),
					pullTarget
			);
		}


		public boolean mustBePulledOut(IVector2 point, boolean isCornerPullOutOngoing)
		{
			if (isCornerPullOutOngoing)
			{
				return hysteresisArea.isPointInShape(point);
			} else
			{
				return exclusionArea.isPointInShape(point);
			}
		}


		public List<IDrawableShape> getShapes()
		{
			return List.of(
					new DrawableRectangle(hysteresisArea, new Color(0, 0, 0, 100)).setFill(true),
					new DrawableRectangle(exclusionArea, new Color(255, 0, 0, 100)).setFill(true),
					new DrawablePoint(pullToPoint).setColor(Color.magenta)
			);
		}
	}

	private record PullAwayArea(IRectangle exclusionArea, IRectangle hysteresisArea, ILineSegment pullToLine)
	{
		static PullAwayArea create(ILineSegment boundary, IVector2 pullDirection)
		{
			Validate.isTrue(SumatraMath.isZero(pullDirection.x()) || SumatraMath.isZero(pullDirection.y()));
			Validate.isTrue(!pullDirection.isZeroVector());
			Validate.isTrue(SumatraMath.isEqual(pullDirection.getLength(), 1));

			var magnitudeExclusion = pullDirection.multiplyNew(getPullAwayMargin());
			var magnitudeHysteresis = pullDirection.multiplyNew(pullAwayHysteresis);

			var exclusionLine = Lines.segmentFromOffset(
					boundary.supportVector().addMagnitude(magnitudeExclusion), boundary.directionVector());
			var hysteresisLine = Lines.segmentFromOffset(
					exclusionLine.supportVector().addMagnitude(magnitudeHysteresis), exclusionLine.directionVector()
			);
			var pullTarget = Lines.segmentFromOffset(
					hysteresisLine.supportVector().addMagnitude(magnitudeHysteresis), hysteresisLine.directionVector());

			return new PullAwayArea(
					Rectangle.fromPoints(boundary.getPathStart(), exclusionLine.getPathEnd()),
					Rectangle.fromPoints(boundary.getPathStart(), hysteresisLine.getPathEnd()),
					pullTarget
			);
		}


		public boolean mustBePulledOut(IVector2 point, boolean isAreaPullOutOngoing)
		{
			if (isAreaPullOutOngoing)
			{
				return hysteresisArea.isPointInShape(point);
			} else
			{
				return exclusionArea.isPointInShape(point);
			}
		}


		public List<IDrawableShape> getShapes()
		{
			return List.of(
					new DrawableRectangle(hysteresisArea, new Color(0, 0, 0, 100)).setFill(true),
					new DrawableRectangle(exclusionArea, new Color(255, 0, 0, 100)).setFill(true),
					new DrawableLine(pullToLine).setColor(Color.magenta)
			);
		}
	}
}
