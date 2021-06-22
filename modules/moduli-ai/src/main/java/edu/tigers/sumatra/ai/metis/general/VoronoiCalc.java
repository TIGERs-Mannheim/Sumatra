/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.general.util.Triangulation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class VoronoiCalc extends ACalculator
{
	@Configurable(comment = "Radius of no opponents around", defValue = "1000.0")
	private static double minRadius = 1000;

	@Configurable(comment = "Minimum x value for a valid point", defValue = "-2000.0")
	private static double minX = -2000;

	@Configurable(comment = "Flag whether the voronoi should be drawn or not ", defValue = "false")
	private static boolean drawVoronoi = false;

	@Configurable(comment = "Flag whether the triangles of the delaunay triangulation should be drawn or not", defValue = "false")
	private static boolean drawTriangles = false;

	@Configurable(comment = "Defines whether this calc is active or not", defValue = "true")
	private static boolean isActive = false;

	private final Supplier<BotDistance> tigerClosestToBall;

	@Getter
	private List<ICircle> freeSpots;


	@Override
	public boolean isCalculationNecessary()
	{
		return isActive && tigerClosestToBall.get().getBotId() != BotID.noBot();
	}


	@Override
	protected void reset()
	{
		freeSpots = Collections.emptyList();
	}


	@Override
	public void doCalc()
	{
		Triangulation tri = generateTriangulation(getConsideredPoints());
		freeSpots = convertTrianglesToCircles(new ArrayList<>(tri));
		freeSpots = filterCircles(freeSpots);

		drawPoints(freeSpots, getShapes(EAiShapesLayer.SUPPORT_MOVE_FREE));

		if (drawVoronoi)
		{
			drawVoronoi(tri, getShapes(EAiShapesLayer.SUPPORT_MOVE_FREE));
		}
		if (drawTriangles)
		{
			drawTriangles(tri, getShapes(EAiShapesLayer.SUPPORT_MOVE_FREE));
		}
	}


	private List<IVector2> getConsideredPoints()
	{
		List<IVector2> points = getWFrame().getOpponentBots().values().stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperOpponentId())
				.map(ITrackedObject::getPos)
				.collect(Collectors.toList());
		points.add(getWFrame().getTiger(tigerClosestToBall.get().getBotId()).getPos());

		IRectangle movingRect = Rectangle.fromCenter(
				Vector2.fromXY(Math.min(getBall().getPos().x(), Geometry.getFieldLength() / 4.), 0),
				Geometry.getFieldLength() / 2. + minRadius,
				Geometry.getFieldWidth() + minRadius);
		points.addAll(movingRect.getCorners());
		points.add(getWFrame().getBall().getPos());
		return points;
	}


	private Triangulation generateTriangulation(List<IVector2> points)
	{
		IVector2 a = Geometry.getGoalTheir().getCenter()
				.addNew(Vector2.fromXY(Geometry.getFieldLength(), 5 * Geometry.getFieldWidth()));
		IVector2 b = Geometry.getGoalTheir().getCenter()
				.addNew(Vector2.fromXY(Geometry.getFieldLength(), -5 * Geometry.getFieldWidth()));
		IVector2 c = Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(-5 * Geometry.getFieldLength()));
		Triangle t = Triangle.fromCorners(a, b, c);
		Triangulation triangulation = new Triangulation(t);
		for (IVector2 opponent : points)
		{
			if (t.isPointInShape(opponent))
			{
				triangulation.delaunayPlace(opponent);
			}
		}
		return triangulation;
	}


	private List<ICircle> convertTrianglesToCircles(List<ITriangle> triangles)
	{
		List<ICircle> circles = new ArrayList<>();
		for (ITriangle triangle : triangles)
		{
			Optional<ICircle> circle = Circle.from3Points(triangle.getA(), triangle.getB(), triangle.getC());
			circle.ifPresent(circles::add);
		}
		return circles;
	}


	private List<ICircle> filterCircles(List<ICircle> circles)
	{
		List<ICircle> validCircles = new ArrayList<>();
		PointChecker pointChecker = new PointChecker()
				.checkNotInPenaltyAreas()
				.checkInsideField()
				.checkBallDistances()
				.checkConfirmWithKickOffRules();
		for (ICircle circle : circles)
		{
			if (circle.radius() > minRadius && pointChecker.allMatch(getAiFrame(), circle.center())
					&& circle.center().x() > minX)
			{
				validCircles.add(circle);
			}
		}
		List<ICircle> validAndFreeCircles = new ArrayList<>();
		for (ICircle circle : validCircles)
		{
			boolean isFree = true;
			for (ICircle circle2 : validCircles)
			{
				if (circle.center().distanceTo(circle2.center()) < minRadius && circle.radius() < circle2.radius())
				{
					isFree = false;
					break;
				}
			}
			if (isFree)
			{
				validAndFreeCircles.add(circle);
			}
		}
		return validAndFreeCircles;
	}


	private void drawTriangles(Triangulation triangulation, List<IDrawableShape> shapes)
	{
		for (ITriangle triangle : triangulation)
		{
			shapes.add(new DrawableTriangle(triangle));
		}
	}


	private void drawVoronoi(Triangulation triangulation, List<IDrawableShape> shapes)
	{
		for (ITriangle triangle : triangulation)
		{
			Optional<ICircle> centerCircle = Circle.fromNPoints(triangle.getCorners());
			if (centerCircle.isPresent())
			{
				IVector2 center = centerCircle.get().center();
				for (ITriangle neighbor : triangulation.neighbors(triangle))
				{
					Optional<ICircle> neighborCenterCircle = Circle.fromNPoints(neighbor.getCorners());
					neighborCenterCircle
							.ifPresent(nc -> shapes.add(new DrawableLine(Lines.segmentFromPoints(center, nc.center()))));
				}
			}
		}

	}


	private void drawPoints(List<ICircle> circles, List<IDrawableShape> shapes)
	{
		for (ICircle circle : circles)
		{
			DrawableCircle drawableCircle = new DrawableCircle(circle.center(), 25, Color.ORANGE);
			shapes.add(drawableCircle);
		}
	}
}
