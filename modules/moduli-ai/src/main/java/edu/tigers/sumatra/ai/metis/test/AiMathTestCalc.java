/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.test;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.CircularObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.KickBallObstacleV2;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiMathTestCalc extends ACalculator
{
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getTeamColor() == ETeamColor.BLUE)
		{
			// testShapes(newTacticalField, baseAiFrame);
			
			// ITrajPathFinder finder = new TrajPathFinderNoObs();
			// final TrajPathFinderInput localPathFinderInput = new TrajPathFinderInput(
			// baseAiFrame.getWorldFrame().getTimestamp());
			// localPathFinderInput.getMoveCon().getMoveConstraints().setAccLimit(3).setVelLimit(4);
			// localPathFinderInput.setPos(new Vector2(0, 0));
			// localPathFinderInput.setOrientation(0);
			// localPathFinderInput.setVel(new Vector2(3, 0));
			// IVector2 dest = new Vector2(3000, 0);
			// localPathFinderInput.setDest(dest);
			// Optional<TrajectoryWithTime<IVector2>> path = finder.calcPath(localPathFinderInput);
			// List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.UNSORTED);
			// shapes.add(new DrawableTrajectoryPath(path.get().getTrajectory()));
			// shapes.add(new DrawableBot(localPathFinderInput.getPos(), localPathFinderInput.getOrientation(), Color.cyan,
			// 90, 75));
			// shapes.add(new DrawableBot(dest, 0, Color.red, 100, 75));
		}
		
		
	}
	
	
	private CircularObstacle getPenAreaArc(final IVector2 center, final double radius)
	{
		double startAngle = AVector2.X_AXIS.multiplyNew(-center.x()).getAngle();
		double stopAngle = AVector2.Y_AXIS.multiplyNew(center.y()).getAngle();
		double rotation = AngleMath.getShortestRotation(startAngle, stopAngle);
		return CircularObstacle.arc(center, radius, startAngle, rotation);
	}
	
	
	@SuppressWarnings("unused")
	private void testShapes(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.UNSORTED);
		PenaltyArea penAreaOur = Geometry.getPenaltyAreaOur();
		PenaltyArea penAreaTheir = Geometry.getPenaltyAreaTheir();
		double radius = penAreaOur.getRadiusOfPenaltyArea() + Geometry.getPenaltyAreaMargin();
		
		// Arc arc = new Arc(new Vector2(0, 0), 1000, 4, 6);
		CircularObstacle arc = getPenAreaArc(penAreaTheir.getPenaltyCircleNegCentre(), radius);
		
		
		// shapes.add(new DrawableArc(arc, Color.red));
		// for (double x = -4200; x < 4200; x += 50)
		// {
		// for (double y = -3200; y < 3200; y += 50)
		// {
		// if (arc.isPointCollidingWithObstacle(new Vector2(x, y), 0))
		// {
		// shapes.add(new DrawablePoint(new Vector2(x, y)));
		// } else
		// {
		// shapes.add(new DrawablePoint(new Vector2(x, y), Color.GREEN));
		// }
		// }
		// }
		
		
		// CatchBallObstacle obs = new CatchBallObstacle(baseAiFrame.getWorldFrame().getBall(), -50,
		// new DynamicPosition(new Vector2(4000, 0)), 8);
		//
		// for (double x = -500; x < 500; x += 20)
		// {
		// for (double y = -500; y < 500; y += 20)
		// {
		// if (obs.isPointCollidingWithObstacle(new Vector2(x, y), 0))
		// {
		// shapes.add(new DrawablePoint(new Vector2(x, y)));
		// } else
		// {
		// shapes.add(new DrawablePoint(new Vector2(x, y), Color.GREEN));
		// }
		// }
		// }
		// shapes.add(obs);
		
		
		KickBallObstacleV2 obs = new KickBallObstacleV2(baseAiFrame.getWorldFrame().getBall(), new Vector2(0, -1),
				null);
		// KickBallObstacle obs = new KickBallObstacle(baseAiFrame.getWorldFrame().getBall(), new Vector2(1, 0));
		
		for (double x = -500; x < 500; x += 20)
		{
			for (double y = -500; y < 500; y += 20)
			{
				if (obs.isPointCollidingWithObstacle(new Vector2(x, y), 0))
				{
					shapes.add(new DrawablePoint(new Vector2(x, y)));
					
					shapes.add(new DrawableLine(
							Line.newLine(new Vector2(x, y), obs.nearestPointOutsideObstacle(new Vector2(x, y), 0)), Color.blue,
							false));
				} else
				{
					// shapes.add(new DrawablePoint(new Vector2(x, y), Color.GREEN));
				}
				
				
			}
		}
		obs.setColor(Color.black);
		shapes.add(obs);
		
		
	}
}
