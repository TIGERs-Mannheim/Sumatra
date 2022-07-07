/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collection;


/**
 * Rate pass interception by generating moving robot circles based on robot speed and acceleration
 * and intersect them with the ball travel line at different points in time.
 * The distance that the circle covers on the ball travel line is used for the rating.
 */
@RequiredArgsConstructor
public class PassInterceptionMovingRobotRater extends APassRater
{
	@Configurable(defValue = "2.0")
	private static double maxHorizon = 2;

	@Configurable(defValue = "2.0", comment = "Factor on relative distance. Larger -> less pessimistic")
	private static double scoringFactor = 2;

	@Configurable(defValue = "170", comment = "Step size [mm] over the pass trajectory")
	private static double stepSize = 170;

	@Configurable(defValue = "30", comment = "Min distance [mm] between pass line and opponent robot (in addition to robot and ball radius")
	private static double minDistToRobot = 30;

	@Configurable(defValue = "0.2", comment = "Chip kick score penalty")
	private static double chipKickPenalty = 0.2;

	static
	{
		ConfigRegistration.registerClass("metis", PassInterceptionMovingRobotRater.class);
	}

	private final Collection<ITrackedBot> consideredBots;
	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();


	@Override
	public double rate(Pass pass)
	{
		IVector2 origin = pass.getKick().getSource();
		Vector3 velMM = pass.getKick().getKickVel().multiplyNew(1000);
		var passTrajectory = Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(origin, velMM);

		double minScore = passTrajectory.getTravelLinesInterceptable().stream()
				.map(l -> l.getSteps(stepSize))
				.flatMap(Collection::stream)
				.mapToDouble(pos -> ratePos(passTrajectory, pos, pass.getDuration()))
				.min()
				.orElse(1.0);

		if (pass.getKick().getKickVel().z() > 0)
		{
			minScore -= chipKickPenalty;
		}

		return Math.max(0, minScore);
	}


	private double ratePos(IBallTrajectory passTrajectory, IVector2 pos, double duration)
	{
		if (Geometry.getPenaltyAreaTheir().isPointInShape(pos, Geometry.getBotRadius()))
		{
			return 1;
		}
		double t = passTrajectory.getTimeByPos(pos);
		if (t > duration)
		{
			return 1;
		}
		double score = Math.min(1, consideredBots.stream()
				.filter(bot -> bot.getPos().distanceTo(pos) < 3000)
				.map(bot -> MovingRobot.fromTrackedBot(bot, maxHorizon, Geometry.getBotRadius()))
				.map(movingRobot -> movingRobot.getMovingHorizon(t))
				.peek(circle -> draw(() -> new DrawableCircle(circle).setColor(new Color(213, 6, 6, 159)).setFill(true)))
				.mapToDouble(circle -> scoringFunction(circle.center().distanceTo(pos), circle.radius()))
				.min()
				.orElse(1));
		draw(() -> new DrawablePoint(pos).setColor(colorPicker.getColor(score)));
		return score;
	}


	private double scoringFunction(double distance, double radius)
	{
		double minDist = Geometry.getBotRadius() + Geometry.getBallRadius() + minDistToRobot;
		double adaptedDist = distance - minDist;
		double relativeDistance = adaptedDist / radius;
		return relativeDistance * scoringFactor;
	}
}
