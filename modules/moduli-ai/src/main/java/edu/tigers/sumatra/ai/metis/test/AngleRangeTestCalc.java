/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeGenerator;
import edu.tigers.sumatra.ai.metis.targetrater.MovingObstacleGen;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.awt.Color;
import java.util.List;


/**
 * Test the covered and uncovered angle ranges
 */
public class AngleRangeTestCalc extends ACalculator
{
	@Configurable(defValue = "false")
	private static boolean active = false;

	@Configurable(comment = "Max time horizon to consider for moving robots", defValue = "2.0")
	private static double maxHorizon = 2.0;

	@Configurable(comment = "The time a robot needs to react to the ball movement", defValue = "0.3")
	private static double timeForBotToReact = 0.3;

	private final MovingObstacleGen movingObstacleGen = new MovingObstacleGen();


	@Override
	protected boolean isCalculationNecessary()
	{
		return active;
	}


	@Override
	public void doCalc()
	{
		AngleRangeGenerator angleRangeGenerator = AngleRangeGenerator.forGoal(Geometry.getGoalTheir());
		movingObstacleGen.setMaxHorizon(maxHorizon);
		movingObstacleGen.setTimeForBotToReact(timeForBotToReact);
		var start = getBall().getPos();
		var obstacles = movingObstacleGen.generateCircles(getWFrame().getBots().values(), start, 0.0);

		var coveredAngleRanges = angleRangeGenerator.findCoveredAngleRanges(start, obstacles);
		var fullRange = angleRangeGenerator.getAngleRange(start);
		var uncoveredAngleRanges = angleRangeGenerator.findUncoveredAngleRanges(coveredAngleRanges, fullRange);

		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.TEST_ANGLE_RANGE_RATER);
		obstacles.stream().map(c -> new DrawableCircle(c, Color.magenta)).forEach(shapes::add);
		coveredAngleRanges.stream().map(r -> createDrawable(angleRangeGenerator, r, new Color(255, 0, 8, 150)))
				.forEach(shapes::add);
		uncoveredAngleRanges.stream().map(r -> createDrawable(angleRangeGenerator, r, new Color(0, 255, 64, 150)))
				.forEach(shapes::add);
	}


	private DrawableTriangle createDrawable(AngleRangeGenerator angleRangeGenerator, AngleRange range, Color color)
	{
		IVector2 origin = getBall().getPos();
		IVector2 start = angleRangeGenerator.getLineSegment().getPathStart();
		IVector2 end = angleRangeGenerator.getLineSegment().getPathEnd();

		IVector2 endCenter = TriangleMath.bisector(origin, end, start);
		double baseAngle = endCenter.subtractNew(origin).getAngle();

		IVector2 p1 = origin.addNew(Vector2.fromAngleLength(baseAngle + range.getRight(), 10000));
		IVector2 p2 = origin.addNew(Vector2.fromAngleLength(baseAngle + range.getLeft(), 10000));

		return new DrawableTriangle(Triangle.fromCorners(origin, p1, p2), color).setFill(true);
	}
}
