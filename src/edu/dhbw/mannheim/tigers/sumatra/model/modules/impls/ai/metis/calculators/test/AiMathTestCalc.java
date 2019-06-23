/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.CatchBallInput;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableTrajectory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Arc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableArc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiMathTestCalc extends ACalculator
{
	private TrajPathFinderInput	finderInput	= new TrajPathFinderInput();
	private CatchBallInput			input			= null;
	private float						b				= 0;
	private float						simTime		= 0;
	private boolean					simulate		= false;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		testKickSpline(newTacticalField, baseAiFrame);
		
		if (baseAiFrame.getTeamColor() == ETeamColor.BLUE)
		{
			// testRedirectOrientation(newTacticalField, baseAiFrame);
			// testRedirectAngle(newTacticalField, baseAiFrame);
			// testCatchBallDestination(newTacticalField, baseAiFrame);
			// testDrawTrajectory(newTacticalField, baseAiFrame);
			// testTrajPathFinder(newTacticalField, baseAiFrame);
			// testShapes(newTacticalField, baseAiFrame);
		}
		
		// TrackedBall ball = new TrackedBall(new Vector3(0, 0, 0), new Vector3(0, 1, 0), AVector3.ZERO_VECTOR, 0, true);
		// DynamicPosition receiver = new DynamicPosition(new Vector2(-1000, 1000));
		// CatchBallObstacle obs = new CatchBallObstacle(ball, 90, receiver, 8);
		//
		// newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED).add(obs);
		//
		// for (float x = -1100; x < 1100; x += 20)
		// {
		// for (float y = -1100; y < 1100; y += 20)
		// {
		// IVector2 p = new Vector2(x, y);
		// boolean inside = obs.isPointCollidingWithObstacle(p, 0.8f);
		// newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
		// .add(new DrawablePoint(p, inside ? Color.green : Color.red));
		// }
		// }
	}
	
	
	@SuppressWarnings("unused")
	private void testRedirectOrientation(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IVector2 vel = new Vector2(-0.8f, 0.9f);
		IVector2 pos = baseAiFrame.getWorldFrame().getBall().getPos();
		
		IVector2 shootTarget = new Vector2(4050, 0);
		float shootSpeed = 8;
		
		float totalTime = AIConfig.getBallModel().getTimeByVel(vel.getLength2(), 0);
		float baseAngle = shootTarget.subtractNew(pos).getAngle();
		
		for (float t = 0; t < totalTime; t += totalTime / 10f)
		{
			IVector2 ballPos = AIConfig.getBallModel().getPosByTime(pos, vel, t);
			IVector2 botPos = GeoMath.stepAlongLine(ballPos, shootTarget, -70);
			float orientation = AiMath.calcRedirectOrientation(ballPos, baseAngle, vel,
					shootTarget, shootSpeed);
			
			float angle = AiMath.calcRedirectAngle(ballPos, shootTarget, vel, shootSpeed);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
					.add(new DrawableLine(new Line(ballPos, new Vector2(orientation - angle).scaleTo(200)), Color.MAGENTA));
			
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
					.add(new DrawableBot(botPos, orientation, Color.MAGENTA));
		}
		
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
				.add(new DrawableLine(new Line(pos, vel.multiplyNew(1000)), Color.magenta,
						false));
	}
	
	
	@SuppressWarnings("unused")
	private void testTrajPathFinder(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		TrackedTigerBot tBot = baseAiFrame.getWorldFrame().getBot(BotID.createBotId(0, ETeamColor.BLUE));
		if (tBot == null)
		{
			return;
		}
		
		b += (1 / 50f) * AngleMath.PI_TWO;
		
		float a = (float) Math.cos(b);
		
		IVector2 dest = new Vector2(-1400 + (a * 10), -1000 + (a * 10));
		DrawableCircle dCircleDest = new DrawableCircle(dest, 50, Color.RED);
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
				.add(dCircleDest);
		
		List<IObstacle> obstacles = new ArrayList<>();
		Arc circle = new Arc(new Vector2(-1500, -1000), 500, 0.7f, AngleMath.PI_TWO - 1.4f);
		Circle circle2 = new Circle(new Vector2(-1500, 200), 500);
		DrawableArc dCircle = new DrawableArc(circle, Color.RED);
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
				.add(dCircle);
		obstacles.add(circle);
		DrawableCircle dCircle2 = new DrawableCircle(circle2);
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
				.add(dCircle2);
		obstacles.add(circle2);
		
		for (TrackedTigerBot bot : baseAiFrame.getWorldFrame().getBots().values())
		{
			Circle obs = new Circle(bot.getPos(), 200);
			obstacles.add(obs);
			DrawableCircle dObs = new DrawableCircle(obs);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
					.add(dObs);
		}
		
		finderInput.setTrackedBot(tBot);
		finderInput.setDest(dest);
		finderInput.setObstacles(obstacles);
		
		// long t1 = System.nanoTime();
		TrajPath path = tBot.getBot().getPathFinder().calcPath(finderInput);
		// System.out.println((System.nanoTime() - t1) / 1e9);
		// newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED).addAll(finder.getDebugShapes());
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED).add(path);
	}
	
	
	@SuppressWarnings("unused")
	private void testDrawTrajectory(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		TrackedTigerBot tBot = baseAiFrame.getWorldFrame().getBot(BotID.createBotId(1, ETeamColor.BLUE));
		if (tBot == null)
		{
			return;
		}
		IVector2 dest = new Vector2(1000, 2000);
		BangBangTrajectory2D trajXY = TrajectoryGenerator.generatePositionTrajectory(tBot, dest);
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED).add(new DrawableTrajectory(trajXY));
	}
	
	
	@SuppressWarnings("unused")
	private void testCatchBallDestination(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED);
		
		TrackedTigerBot basetBot = baseAiFrame.getWorldFrame().getBot(BotID.createBotId(3, ETeamColor.BLUE));
		if (basetBot == null)
		{
			return;
		}
		
		IVector2 botPos = basetBot.getPos();
		IVector2 botVel = basetBot.getVel();
		
		if ((input != null) && (input.getLastPath() != null))
		{
			botPos = input.getLastPath().getPosition(simTime);
			botVel = input.getLastPath().getVelocity(simTime);
		}
		
		TrackedTigerBot tBot = new TrackedTigerBot(basetBot.getId(), botPos,
				botVel, basetBot.getAcc(), 0, basetBot.getAngle(), basetBot.getaVel(),
				basetBot.getaAcc(), 0, basetBot.getBot(), basetBot.getTeamColor());
		
		IVector3 acc = AVector3.ZERO_VECTOR;
		
		IVector2 initVel = new Vector2(-0.35f, -0.9f);
		float simVel = AIConfig.getBallModel().getVelByTime(initVel.getLength2(), simTime);
		IVector2 vel = initVel.scaleToNew(simVel);
		
		IVector2 initPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 pos = AIConfig.getBallModel().getPosByTime(initPos, initVel, simTime);
		
		TrackedBall ball = new TrackedBall(new Vector3(pos, 0), new Vector3(vel, 0), acc, 1, true);
		// DynamicPosition receiver = new DynamicPosition(new Vector2(-4050, 0));
		DynamicPosition receiver = new DynamicPosition(ball);
		
		shapes.add(new DrawableLine(new Line(pos, vel.multiplyNew(1000)), Color.magenta,
				false));
		shapes.add(new DrawableBot(tBot.getPos(), 0, Color.black));
		
		if (simulate && (input != null))
		{
			shapes.addAll(input.getShapes());
			shapes.add(new DrawableCircle(new Circle(pos, 50), Color.cyan));
			simTime += 0.016f;
			if (simTime > (AIConfig.getBallModel().getTimeByVel(initVel.getLength2(), 0) + 0.1f))
			{
				simTime = 0;
				simulate = false;
			}
			// simTime = 85 * 0.016f;
		} else
		{
			if (input == null)
			{
				input = new CatchBallInput(tBot, ball, receiver);
			} else
			{
				input.setBot(tBot);
				input.setBall(ball);
			}
			input.getObsGen().setUsePenAreaOur(true);
			input.setDebug(true);
			input.setShapes(new ArrayList<>());
			TrajPath path = AiMath.catchBallTrajPath(input);
			if (path != null)
			{
				input.getShapes().add(path);
				input.getShapes().add(new DrawableBot(path.getPosition(path.getTotalTime()), 0, Color.BLUE));
			}
			shapes.addAll(input.getShapes());
			// simTime = 10 * 0.016f;
			// simulate = true;
			// input = null;
		}
		
		
		// IVector2 dest = new Vector2(500, -1250);
		// ITrajectory2D traj = TrajectoryGenerator.generatePositionTrajectory(tBot.getBot(), tBot.getPos(),
		// tBot.getVel(),
		// dest);
		// TrajPathNode trajNode = new TrajPathNode(traj, traj.getTotalTime());
		// List<IObstacle> obstacles = input.getObsGen().generateStaticObstacles();
		// float toBallDist = (AIConfig.getGeometry().getBallRadius() + tBot.getBot().getCenter2DribblerDist()) - 5;
		// CatchBallObstacle obs = new CatchBallObstacle(ball, toBallDist, receiver);
		// obstacles.add(obs);
		// float tCollision = trajNode.getEarliestCollision(obstacles, 0);
		// if (tCollision < trajNode.getEndTime())
		// {
		// shapes.add(new CatchBallObstacle(new TrackedBall(new Vector3(ball.getPosByTime(tCollision), 0),
		// AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0, false), toBallDist, receiver));
		// }
		// shapes.add(new CatchBallObstacle(new TrackedBall(new Vector3(ball.getPosByTime(trajNode.getEndTime()), 0),
		// AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0, false), toBallDist, receiver));
		// shapes.add(trajNode);
		// shapes.add(obs);
		return;
	}
	
	
	private Arc getPenAreaArc(final IVector2 center, final float radius)
	{
		float startAngle = AVector2.X_AXIS.multiplyNew(-center.x()).getAngle();
		float stopAngle = AVector2.Y_AXIS.multiplyNew(center.y()).getAngle();
		float rotation = AngleMath.getShortestRotation(startAngle, stopAngle);
		return new Arc(center, radius, startAngle, rotation);
	}
	
	
	@SuppressWarnings("unused")
	private void testShapes(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED);
		PenaltyArea penAreaOur = AIConfig.getGeometry().getPenaltyAreaOur();
		PenaltyArea penAreaTheir = AIConfig.getGeometry().getPenaltyAreaTheir();
		float radius = penAreaOur.getRadiusOfPenaltyArea() + Geometry.getPenaltyAreaMargin();
		
		// Arc arc = new Arc(new Vector2(0, 0), 1000, 4, 6);
		Arc arc = getPenAreaArc(penAreaTheir.getPenaltyCircleNegCentre(), radius);
		shapes.add(new DrawableArc(arc, Color.red));
		for (float x = -4200; x < 4200; x += 50)
		{
			for (float y = -3200; y < 3200; y += 50)
			{
				if (arc.isPointCollidingWithObstacle(new Vector2(x, y), 0))
				{
					shapes.add(new DrawablePoint(new Vector2(x, y)));
				} else
				{
					shapes.add(new DrawablePoint(new Vector2(x, y), Color.GREEN));
				}
			}
		}
	}
	
	
	@SuppressWarnings("unused")
	private void testRedirectAngle(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED);
		// IVector2 target = new Vector2(0, 0);
		float shootSpeed = 8;
		//
		// Random rnd = new Random(0);
		// for (int i = 0; i < 10; i++)
		// {
		// IVector2 kickerPos = new Vector2((rnd.nextFloat() * 9000) - 4500, (rnd.nextFloat() * 6000) - 3000);
		// IVector2 vBall = new Vector2((rnd.nextFloat() * 2) - 1, (rnd.nextFloat() * 2) - 1);
		//
		// float approxOrient = 0;
		// if (!kickerPos.equals(target))
		// {
		// approxOrient = target.subtractNew(kickerPos).getAngle();
		// }
		// float targetAngle = AiMath.calcRedirectOrientation(kickerPos, approxOrient, vBall, target, shootSpeed);
		//
		// float ballAngle;
		// if (vBall.isZeroVector())
		// {
		// ballAngle = targetAngle;
		// } else
		// {
		// ballAngle = AngleMath.normalizeAngle(vBall.getAngle() + AngleMath.PI);
		// }
		//
		// float angle = AiMath.calcRedirectAngle(kickerPos, target, vBall, shootSpeed);
		//
		// shapes.add(new DrawableBot(kickerPos.addNew(new Vector2(targetAngle + AngleMath.PI).scaleTo(90)), targetAngle,
		// Color.red));
		// shapes.add(new DrawableLine(new Line(kickerPos, new Vector2(ballAngle).scaleTo(200)), Color.blue));
		// shapes.add(new DrawableLine(new Line(kickerPos, vBall.multiplyNew(1000)), Color.RED));
		// shapes.add(new DrawableLine(Line.newLine(kickerPos, target), Color.black, false));
		// shapes.add(new DrawableText(kickerPos, "" + angle, Color.black));
		// }
		
		TrackedTigerBot tBot = baseAiFrame.getWorldFrame().getBot(BotID.createBotId(2, ETeamColor.BLUE));
		IVector2 pBall = new Vector2(-380, 1200);
		DynamicPosition receiver = new DynamicPosition(tBot, 0);
		receiver.update(baseAiFrame.getWorldFrame());
		IVector2 ballVel = new Vector2(-1000, 3000).subtract(new Vector2(80, -95)).scaleTo(2.18f);
		float approxOrient = 0;
		if (!pBall.equals(receiver, 1))
		{
			approxOrient = receiver.subtractNew(pBall).getAngle();
		}
		float targetAngle = AiMath.calcRedirectOrientation(pBall, approxOrient, ballVel, receiver,
				shootSpeed);
		shapes.add(new DrawableBot(pBall.addNew(new Vector2(targetAngle + AngleMath.PI).scaleTo(90)), targetAngle,
				Color.red));
		// shapes.add(new DrawableLine(new Line(pBall, ballVel.multiplyNew(factor)), Color.blue));
		shapes.add(new DrawableLine(new Line(pBall, ballVel.multiplyNew(1000)), Color.RED));
		shapes.add(new DrawableLine(Line.newLine(pBall, receiver), Color.black, false));
		// shapes.add(new DrawableText(pBall, "" + angle, Color.black));
	}
	
	
	private void testKickSpline(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED);
		
		DynamicPosition receiver = new DynamicPosition(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 shootDir = receiver.subtractNew(ballPos).normalize();
		IVector2 botPos = ballPos.addNew(shootDir.multiplyNew(500));
		IVector2 botVel = new Vector2(0, 0);
		SplineTrajectoryGenerator stg = new SplineTrajectoryGenerator();
		
		TrackedTigerBot bot = baseAiFrame.getWorldFrame().getBot(BotID.createBotId(5, baseAiFrame.getTeamColor()));
		if (bot != null)
		{
			botPos = bot.getPos();
		}
		
		IVector2 endVel = shootDir.scaleToNew(0.3f);
		
		List<IVector2> path = new ArrayList<>(3);
		path.add(botPos.multiplyNew(1e-3f));
		
		IVector2 ball2Bot = botPos.subtractNew(ballPos);
		float rotationShoot2Bot = AngleMath.difference(shootDir.getAngle(), ball2Bot.getAngle());
		if (Math.abs(rotationShoot2Bot) < AngleMath.PI_HALF)
		{
			IVector2 supportPoint = ballPos.addNew(shootDir
					.turnNew((AngleMath.PI - 0.7f) * -Math.signum(rotationShoot2Bot)).multiplyNew(300));
			path.add(supportPoint.multiplyNew(1e-3f));
		}
		
		path.add(ballPos.multiplyNew(1e-3f));
		
		SplinePair3D spline3d = stg.create(path, botVel, endVel, 0, 0, 0, 0);
		shapes.add(new DrawableTrajectory(spline3d));
	}
}
