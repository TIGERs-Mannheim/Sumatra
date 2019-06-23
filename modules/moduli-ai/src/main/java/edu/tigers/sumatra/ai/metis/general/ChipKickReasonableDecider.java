/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableQuadrilateral;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.quadrilateral.IQuadrilateral;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Checks if its reasonable to chip the ball from a source to a target.
 * This includes touchdown validity and obstacles.
 */
public class ChipKickReasonableDecider
{
	
	@Configurable(defValue = "35.0", comment = "Back width of chip kick detection quadliteral")
	private static double quadliteralWidthOffsetBack = 35;
	
	@Configurable(defValue = "35.0", comment = "Front width of chip kick detection quadliteral")
	private static double quadliteralWidthOffsetFront = 35;
	
	@Configurable(defValue = "0.2", comment = "time offset to estimate kickingRobot movement")
	private static double chipKickDetectionTimeOffset = 0.2;
	
	@Configurable(defValue = "45.0", comment = "chip kick angle in deg")
	private static double chipKickAngle = 45;
	
	
	private final Collection<ITrackedBot> obstacles;
	
	private IVector2 source;
	private IVector2 target;
	private double initialKickVel;
	
	
	static
	{
		ConfigRegistration.registerClass("metis", ChipKickReasonableDecider.class);
	}
	
	
	/**
	 * determines if a ball has to be chipped to go from source to target
	 *
	 * @param source from where to check
	 * @param target shoot Target
	 * @param initialKickVel vel [m/s]
	 * @param obstacles to consider
	 */
	public ChipKickReasonableDecider(final IVector2 source, final IVector2 target,
			final Collection<ITrackedBot> obstacles, final double initialKickVel)
	{
		this.source = source;
		this.target = target;
		this.obstacles = obstacles;
		this.initialKickVel = initialKickVel;
	}
	
	
	/**
	 * Determine weather a chip kick is reasonable
	 */
	public boolean isChipKickReasonable()
	{
		return isChipKickReasonable(Collections.emptyList(), false);
	}
	
	
	/**
	 * Determine weather a chip kick is reasonable and store debbuging shapes
	 */
	public boolean isChipKickReasonable(List<IDrawableShape> shapes)
	{
		return isChipKickReasonable(shapes, true);
	}
	
	
	/**
	 * Determine if a chip kick is reasonable, this method overrides the parameters given in the
	 * constructor
	 *
	 * @param source from where to check
	 * @param target shoot Target
	 * @param initialKickVel vel [m/s]
	 */
	public boolean isChipKickReasonable(IVector2 source, IVector2 target, double initialKickVel)
	{
		this.source = source;
		this.target = target;
		this.initialKickVel = initialKickVel;
		return isChipKickReasonable();
	}
	
	
	private boolean isChipKickReasonable(List<IDrawableShape> shapes, boolean drawShapes)
	{
		final double robotHeight = RuleConstraints.getMaxRobotHeight();
		IVector2 sourceToTarget = target.subtractNew(source);
		double chipAngle = AngleMath.deg2rad(chipKickAngle);
		IVector2 kickVel = sourceToTarget.scaleToNew(initialKickVel * 1000);
		ABallTrajectory traj = BallFactory.createTrajectoryFrom2DKick(source, kickVel, chipAngle,
				true);
		
		double minDistFromBallToFirstObstacleToOverchip = BallFactory.createChipConsultant().getMinimumDistanceToOverChip(
				initialKickVel,
				robotHeight) + Geometry.getBotRadius();
		double minDistFromFirstTouchdownToObstacleToOverchip;
		List<IVector2> touchdowns = traj.getTouchdownLocations();
		if (touchdowns.isEmpty())
		{
			// no touch down, so nothing can be overchipped
			return false;
		}
		
		// they can be the same point, which is perfectly fine
		IVector2 initialTouchDown = touchdowns.get(0);
		addtouchDownShape(shapes, initialTouchDown, drawShapes);
		
		double dist = source.distanceTo(initialTouchDown);
		minDistFromFirstTouchdownToObstacleToOverchip = dist
				- BallFactory.createChipConsultant().getMaximumDistanceToOverChip(initialKickVel, robotHeight)
				+ Geometry.getBotRadius();
		if (dist - minDistFromBallToFirstObstacleToOverchip - minDistFromFirstTouchdownToObstacleToOverchip < 0)
		{
			// we cant overchip anything if the "chip above kickingRobot height is smaller 0"
			return false;
		}
		
		IQuadrilateral quad = createCheckingQuadliteral(sourceToTarget, minDistFromBallToFirstObstacleToOverchip, dist,
				minDistFromFirstTouchdownToObstacleToOverchip);
		double dist2Ball = source.distanceTo(target);
		boolean chip = isQuadFreeOfObstacles(quad, dist2Ball);
		
		drawShapes(shapes, drawShapes, dist, quad, chip, dist2Ball);
		return chip;
	}
	
	
	private IQuadrilateral createCheckingQuadliteral(final IVector2 sourceToTarget,
			final double minDistFromBallToFirstObstacleToOverchip, final double dist,
			final double minDistFromFirstTouchdownToObstacleToOverchip)
	{
		IVector2 normal = sourceToTarget.getNormalVector().normalizeNew();
		IVector2 triB1 = source
				.addNew(normal.scaleToNew(quadliteralWidthOffsetFront + Geometry.getBotRadius()))
				.addNew(sourceToTarget.scaleToNew(minDistFromBallToFirstObstacleToOverchip));
		IVector2 triB2 = source
				.addNew(normal.scaleToNew(-quadliteralWidthOffsetFront - Geometry.getBotRadius()))
				.addNew(sourceToTarget.scaleToNew(minDistFromBallToFirstObstacleToOverchip));
		IVector2 triT1 = source
				.addNew(sourceToTarget.scaleToNew(dist - minDistFromFirstTouchdownToObstacleToOverchip))
				.addNew(normal.scaleToNew(quadliteralWidthOffsetBack + Geometry.getBotRadius()));
		IVector2 triT2 = source
				.addNew(sourceToTarget.scaleToNew(dist - minDistFromFirstTouchdownToObstacleToOverchip))
				.addNew(normal.scaleToNew(-quadliteralWidthOffsetBack - Geometry.getBotRadius()));
		return Quadrilateral.fromCorners(triT1, triB2, triT2, triB1);
	}
	
	
	private boolean isQuadFreeOfObstacles(final IQuadrilateral quad, final double dist2Ball)
	{
		for (ITrackedBot bot : obstacles)
		{
			IVector2 futureBotPos = bot.getPosByTime(chipKickDetectionTimeOffset);
			if ((quad.isPointInShape(bot.getPos())
					|| quad.isPointInShape(futureBotPos))
					&& dist2Ball > OffensiveConstants.getChipKickMinDistToTarget())
			{
				return true;
			}
		}
		return false;
	}
	
	
	private void drawShapes(final List<IDrawableShape> shapes, final boolean drawShapes, final double dist,
			final IQuadrilateral quad, final boolean chip, final double dist2Ball)
	{
		if (drawShapes)
		{
			DrawableQuadrilateral chipCheckerQuadliteral = new DrawableQuadrilateral(
					quad,
					Color.cyan);
			chipCheckerQuadliteral.setColor(new Color(25, 40, 40, 65));
			chipCheckerQuadliteral.setFill(true);
			shapes.add(chipCheckerQuadliteral);
			shapes.add(new DrawableAnnotation(source, "isChipKickRequired " + chip, Color.orange)
					.withOffset(Vector2.fromY(-300)));
			shapes.add(new DrawableAnnotation(source, "dist to Target: " + dist2Ball, Color.orange)
					.withOffset(Vector2.fromY(-200)));
			shapes.add(new DrawableAnnotation(source, "dist to touchdown: " + dist, Color.orange)
					.withOffset(Vector2.fromY(-400)));
		}
	}
	
	
	private void addtouchDownShape(final List<IDrawableShape> shapes, final IVector2 initialTouchDown,
			final boolean drawShapes)
	{
		if (drawShapes)
		{
			ICircle c1 = Circle.createCircle(initialTouchDown, 50);
			DrawableCircle dc1 = new DrawableCircle(c1, Color.red);
			shapes.add(dc1);
		}
	}
	
	
}
