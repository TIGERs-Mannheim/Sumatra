/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.tracker;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.filter.tracking.TrackingFilterPosVel1D;
import edu.tigers.sumatra.filter.tracking.TrackingFilterPosVel2D;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;


/**
 * Tracks and filters a single robot.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class RobotTracker
{
	private final TrackingFilterPosVel2D filterXY;
	private final TrackingFilterPosVel1D filterW;
	private final BotID botId;
	private final int camId;
	private final List<Long> updateTimestamps = new ArrayList<>();

	private long lastUpdateTimestamp;

	private double visionQuality;
	private double lastCamOrientation;
	private long orientationTurns = 0;
	private double botHeight;

	private int health = 2;


	@Configurable(defValue = "100.0")
	private static double initialCovarianceXY = 100.0;
	@Configurable(defValue = "0.1")
	private static double modelErrorXY = 0.1;
	@Configurable(defValue = "20.0")
	private static double measErrorXY = 20.0;
	@Configurable(defValue = "100.0")
	private static double initialCovarianceW = 100.0;
	@Configurable(defValue = "0.1")
	private static double modelErrorW = 0.1;
	@Configurable(defValue = "2.0")
	private static double measErrorW = 2.0;
	@Configurable(defValue = "1.5", comment = "Factor to weight stdDeviation during tracker merging, reasonable range: 1.0 - 2.0. High values lead to more jitter")
	private static double mergePower = 1.5;
	@Configurable(defValue = "6000.0", comment = "Maximum assumed robot speed in [mm/s] to filter outliers")
	private static double maxLinearVel = 6000.0;
	@Configurable(defValue = "30.0", comment = "Maximum assumed angular robot speed in [rad/s] to filter outliers")
	private static double maxAngularVel = 30.0;
	@Configurable(defValue = "20", comment = "Reciprocal health is used as uncertainty, increased on update, decreased on prediction")
	private static int maxHealth = 20;

	static
	{
		ConfigRegistration.registerClass("vision", RobotTracker.class);
	}


	/**
	 * Create a new tracker from a camera measurement.
	 *
	 * @param robot
	 */
	public RobotTracker(final CamRobot robot)
	{
		filterXY = new TrackingFilterPosVel2D(robot.getPos(), initialCovarianceXY, modelErrorXY, measErrorXY,
				robot.getTimestamp()
		);
		filterW = new TrackingFilterPosVel1D(robot.getOrientation(), initialCovarianceW, modelErrorW, measErrorW,
				robot.getTimestamp()
		);

		lastCamOrientation = robot.getOrientation();
		lastUpdateTimestamp = robot.getTimestamp();
		botId = robot.getBotId();
		botHeight = robot.getHeight();
		camId = robot.getCameraId();
	}


	/**
	 * Create a new tracker from camera measurement and prior state knowledge.
	 * Position and velocity is initialized with filtered vision bot state.
	 *
	 * @param robot
	 * @param filtered
	 */
	public RobotTracker(final CamRobot robot, final FilteredVisionBot filtered)
	{
		RealVector xy = new ArrayRealVector(filtered.getPos().toArray(), filtered.getVel().multiplyNew(1000.0).toArray());
		RealVector w = new ArrayRealVector(new double[] { filtered.getOrientation(), filtered.getAngularVel() });

		filterXY = new TrackingFilterPosVel2D(xy, initialCovarianceXY, modelErrorXY, measErrorXY,
				robot.getTimestamp()
		);
		filterW = new TrackingFilterPosVel1D(w, initialCovarianceW, modelErrorW, measErrorW,
				robot.getTimestamp()
		);

		lastCamOrientation = robot.getOrientation();
		lastUpdateTimestamp = robot.getTimestamp();
		botId = robot.getBotId();
		botHeight = robot.getHeight();
		camId = robot.getCameraId();
	}


	/**
	 * Do a prediction step on all filters to a specific time.
	 *
	 * @param timestamp time in [ns]
	 * @param avgFrameDt average frame delta time in [s]
	 */
	public void predict(final long timestamp, final double avgFrameDt)
	{
		filterXY.predict(timestamp);
		filterW.predict(timestamp);

		if (health > 1)
		{
			health--;
		}

		updateTimestamps.removeIf(t -> (timestamp - t) > 1_000_000_000L);

		visionQuality = (updateTimestamps.size() * avgFrameDt) + 0.01;
	}


	/**
	 * Update this tracker with a camera measurement.
	 *
	 * @param robot
	 */
	public void update(final CamRobot robot)
	{
		double dtInSec = Math.abs(robot.getTimestamp() - lastUpdateTimestamp) * 1e-9;
		double distanceToPrediction = filterXY.getPositionEstimate().distanceTo(robot.getPos());
		if (distanceToPrediction > (dtInSec * maxLinearVel))
		{
			// measurement too far away => refuse update
			return;
		}

		double angDiff = Math.abs(AngleMath.difference(filterW.getPositionEstimate(), robot.getOrientation()));
		if (angDiff > (dtInSec * maxAngularVel))
		{
			// orientation mismatch, maybe a +-90° vision switch => refuse update
			return;
		}

		// we have an update, increase health/certainty in this tracker
		if (health < maxHealth)
		{
			health += 2;
		}

		lastUpdateTimestamp = robot.getTimestamp();
		updateTimestamps.add(lastUpdateTimestamp);

		filterXY.correct(robot.getPos());

		double orient = robot.getOrientation();

		// multi-turn angle correction
		if ((orient < -AngleMath.PI_HALF) && (lastCamOrientation > AngleMath.PI_HALF))
		{
			++orientationTurns;
		}

		if ((orient > AngleMath.PI_HALF) && (lastCamOrientation < -AngleMath.PI_HALF))
		{
			--orientationTurns;
		}

		lastCamOrientation = orient;

		orient += orientationTurns * AngleMath.PI_TWO;

		filterW.correct(orient);

		botHeight = robot.getHeight();
	}


	private double getUncertainty()
	{
		return 1.0 / health;
	}


	/**
	 * Get position estimate at specific timestamp.
	 *
	 * @param timestamp Query time.
	 * @return Position in [mm]
	 */
	public IVector2 getPosition(final long timestamp)
	{
		return filterXY.getPositionEstimate(timestamp);
	}


	/**
	 * Get normalized orientation estimate at specific timestamp.
	 *
	 * @param timestamp Query time.
	 * @return Orientation in [rad]
	 */
	public double getOrientation(final long timestamp)
	{
		return AngleMath.normalizeAngle(filterW.getPositionEstimate(timestamp));
	}


	/**
	 * Get linear velocity estimate.
	 *
	 * @return Velocity in [mm/s]
	 */
	public IVector2 getVelocity()
	{
		return filterXY.getVelocityEstimate();
	}


	/**
	 * Get angular velocity estimate.
	 *
	 * @return angular velocity in [rad/s]
	 */
	public double getAngularVelocity()
	{
		return filterW.getVelocityEstimate();
	}

	/**
	 * @return timestamp in [ns]
	 */
	public long getLastUpdateTimestamp()
	{
		return lastUpdateTimestamp;
	}


	/**
	 * @return the id
	 */
	public BotID getBotId()
	{
		return botId;
	}


	/**
	 * @return Robot Height in [mm]
	 */
	public double getBotHeight()
	{
		return botHeight;
	}

	/**
	 * This function merges a variable number of robot trackers and makes a filtered vision bot out of them.
	 * Trackers are weighted according to their state uncertainties. A tracker with high uncertainty
	 * has less influence on the final merge result.
	 *
	 * @param id BotID of the final robot.
	 * @param robots List of robot trackers. Must not be empty.
	 * @param timestamp Extrapolation time stamp to use for the final robot.
	 * @return Merged filtered vision robot.
	 */
	public static FilteredVisionBot mergeRobotTrackers(final BotID id, final List<RobotTracker> robots,
			final long timestamp)
	{
		Validate.notEmpty(robots);

		double totalPosUnc = 0;
		double totalVelUnc = 0;
		double totalOrientUnc = 0;
		double totalAVelUnc = 0;

		double maxQuality = 0;

		// calculate sum of all uncertainties
		for (RobotTracker t : robots)
		{
			double f = t.getUncertainty();
			totalPosUnc += Math.pow(t.filterXY.getPositionUncertainty().getLength() * f, -mergePower);
			totalVelUnc += Math.pow(t.filterXY.getVelocityUncertainty().getLength() * f, -mergePower);
			totalOrientUnc += Math.pow(t.filterW.getPositionUncertainty() * f, -mergePower);
			totalAVelUnc += Math.pow(t.filterW.getVelocityUncertainty() * f, -mergePower);
			if (t.visionQuality > maxQuality)
			{
				maxQuality = t.visionQuality;
			}
		}

		// all uncertainties must be > 0, otherwise we found a bug
		Validate.isTrue(totalPosUnc > 0);
		Validate.isTrue(totalVelUnc > 0);
		Validate.isTrue(totalOrientUnc > 0);
		Validate.isTrue(totalAVelUnc > 0);

		IVector2 pos = Vector2f.ZERO_VECTOR;
		IVector2 vel = Vector2f.ZERO_VECTOR;
		double orient = 0;
		double aVel = 0;

		// cyclic coordinates don't like mean calculations, we will work with offsets though
		double orientOffset = robots.get(0).getOrientation(timestamp);

		// take all trackers and calculate their pos/vel sum weighted by uncertainty.
		// Trackers with high uncertainty have less influence on the merged result.
		for (RobotTracker t : robots)
		{
			double f = t.getUncertainty();
			pos = pos.addNew(t.filterXY.getPositionEstimate(timestamp)
					.multiplyNew(Math.pow(t.filterXY.getPositionUncertainty().getLength() * f, -mergePower)));
			vel = vel.addNew(t.filterXY.getVelocityEstimate()
					.multiplyNew(Math.pow(t.filterXY.getVelocityUncertainty().getLength() * f, -mergePower)));
			double o = AngleMath.difference(t.filterW.getPositionEstimate(timestamp), orientOffset);
			orient += o * Math.pow(t.filterW.getPositionUncertainty() * f, -mergePower);
			aVel += t.filterW.getVelocityEstimate() * Math.pow(t.filterW.getVelocityUncertainty() * f, -mergePower);
		}

		pos = pos.multiplyNew(1.0 / totalPosUnc);
		vel = vel.multiplyNew(1.0 / totalVelUnc);
		orient /= totalOrientUnc;
		aVel /= totalAVelUnc;

		return FilteredVisionBot.builder()
				.withBotID(id)
				.withTimestamp(timestamp)
				.withPos(pos)
				.withVel(vel.multiplyNew(0.001))
				.withAngularVel(aVel)
				.withOrientation(AngleMath.normalizeAngle(orient + orientOffset))
				.withQuality(maxQuality)
				.build();
	}


	/**
	 * Info shapes for visualizer.
	 *
	 * @param timestamp
	 * @return
	 */
	public List<IDrawableShape> getInfoShapes(final long timestamp)
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		IVector2 pos = getPosition(timestamp);
		double orient = getOrientation(timestamp);

		DrawableBotShape botShape = new DrawableBotShape(pos, orient, 120, 100);
		botShape.setFillColor(null);
		shapes.add(botShape);

		DrawableAnnotation id = new DrawableAnnotation(pos, Integer.toString(botId.getNumber()), true);
		id.withOffset(Vector2.fromY(150));
		id.setColor(botId.getTeamColor().getColor());
		shapes.add(id);

		DrawableAnnotation unc = new DrawableAnnotation(pos,
				String.format("%3.2f", filterXY.getPositionUncertainty().getLength() * getUncertainty()));
		unc.withOffset(Vector2.fromX(-150));
		unc.setColor(botId.getTeamColor().getColor());
		shapes.add(unc);

		return shapes;
	}


	/**
	 * @return camera id
	 */
	public int getCamId()
	{
		return camId;
	}
}
