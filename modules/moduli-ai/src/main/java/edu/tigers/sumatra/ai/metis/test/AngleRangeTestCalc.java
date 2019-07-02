/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeGenerator;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Test the covered and uncovered angle ranges
 */
public class AngleRangeTestCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		AngleRangeGenerator angleRangeGenerator = new AngleRangeGenerator();
		angleRangeGenerator.setBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());
		angleRangeGenerator.setStart(baseAiFrame.getWorldFrame().getBall().getPos());
		angleRangeGenerator.setEndRight(Geometry.getGoalTheir().getRightPost());
		angleRangeGenerator.setEndLeft(Geometry.getGoalTheir().getLeftPost());
		angleRangeGenerator.setKickSpeed(6.5);
		angleRangeGenerator.setTimeToKick(0);
		angleRangeGenerator.setTimeForBotToReact(0.1);
		double maxHorizon = 0.3;
		Map<BotID, MovingRobot> movingRobots = baseAiFrame.getWorldFrame().getBots().values().stream()
				.collect(Collectors.toMap(ITrackedBot::getBotId,
						bot -> new MovingRobot(bot, maxHorizon, Geometry.getBotRadius() + Geometry.getBallRadius())));
		angleRangeGenerator.setMovingRobots(movingRobots);

		List<AngleRange> coveredAngleRanges = angleRangeGenerator.findCoveredAngleRanges();
		AngleRange fullRange = angleRangeGenerator.getAngleRange();
		List<AngleRange> uncoveredAngleRanges = angleRangeGenerator.findUncoveredAngleRanges(coveredAngleRanges,
				fullRange);

		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.TEST_ANGLE_RANGE_RATER);
		coveredAngleRanges.stream().map(r -> createDrawable(angleRangeGenerator, r, 0)).forEach(shapes::add);
		uncoveredAngleRanges.stream().map(r -> createDrawable(angleRangeGenerator, r, 200)).forEach(shapes::add);
	}


	private DrawableTriangle createDrawable(AngleRangeGenerator angleRangeGenerator, AngleRange range,
			int g)
	{
		IVector2 start = angleRangeGenerator.getStart();
		IVector2 endLeft = angleRangeGenerator.getEndLeft();
		IVector2 endRight = angleRangeGenerator.getEndRight();

		// line to create intersections with vectors from angles
		ILine targetLine = Lines.lineFromPoints(endLeft, endRight);

		IVector2 endCenter = TriangleMath.bisector(start, endLeft, endRight);
		double baseAngle = endCenter.subtractNew(start).getAngle();

		double scoreChance = 1;

		Color color = new Color((int) ((1 - scoreChance) * 255), g, (int) (scoreChance * 255), 50);

		IVector2 interceptionWithGoalLeft = targetLine.intersectLine(
				Lines.lineFromDirection(angleRangeGenerator.getStart(),
						Vector2.fromAngle(baseAngle + range.getRightAngle())))
				.orElse(Vector2f.ZERO_VECTOR);
		IVector2 interceptionWithGoalRight = targetLine.intersectLine(
				Lines.lineFromDirection(angleRangeGenerator.getStart(),
						Vector2.fromAngle(baseAngle + range.getLeftAngle())))
				.orElse(Vector2f.ZERO_VECTOR);

		DrawableTriangle triangle = new DrawableTriangle(Triangle
				.fromCorners(angleRangeGenerator.getStart(), interceptionWithGoalLeft, interceptionWithGoalRight),
				color);
		triangle.setFill(true);

		return triangle;
	}
}
