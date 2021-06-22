/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.tracker;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.filter.tracking.TrackingFilterPosVel2D;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.RobotCollisionShape;
import edu.tigers.sumatra.vision.data.RobotCollisionShape.CollisionResult;
import edu.tigers.sumatra.vision.data.RobotCollisionShape.ECollisionLocation;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;
import java.util.Optional;


/**
 * Tracks and filters a single ball.
 */
public class BallTracker
{
	private final TrackingFilterPosVel2D filter;


	private long lastInFieldTimestamp;

	private int health = 2;
	private int age = 0;

	private CamBall lastCamBall;
	private boolean updated = false;

	private double maxDistance = -1.0;


	@Configurable(defValue = "1000.0")
	private static double initialCovarianceXY = 1000.0;
	@Configurable(defValue = "0.1")
	private static double modelError = 0.1;
	@Configurable(defValue = "100.0")
	private static double measError = 100.0;
	@Configurable(defValue = "15000.0", comment = "Maximum assumed ball speed in [mm/s] to filter outliers")
	private static double maxLinearVel = 15000.0;
	@Configurable(defValue = "1.5", comment = "Factor to weight stdDeviation during tracker merging, reasonable range: 1.0 - 2.0. High values lead to more jitter")
	private static double mergePower = 1.5;
	@Configurable(defValue = "20", comment = "Reciprocal health is used as uncertainty, increased on update, decreased on prediction")
	private static int maxHealth = 20;
	@Configurable(defValue = "3", comment = "How many updates are required until this tracker is grown up?")
	private static int grownUpAge = 3;

	static
	{
		ConfigRegistration.registerClass("vision", BallTracker.class);
	}


	/**
	 * Create a new ball tracker.
	 *
	 * @param ball
	 */
	public BallTracker(final CamBall ball)
	{
		filter = new TrackingFilterPosVel2D(ball.getPos().getXYVector(), initialCovarianceXY, modelError, measError,
				ball.gettCapture());

		lastInFieldTimestamp = ball.gettCapture();
		lastCamBall = ball;
	}


	/**
	 * Create a new ball tracker.
	 *
	 * @param camBall
	 * @param filtBall
	 */
	public BallTracker(final CamBall camBall, final FilteredVisionBall filtBall)
	{
		IVector2 filtVel = filtBall.getVel().getXYVector();
		if (filtVel.getLength2() > maxLinearVel)
		{
			filtVel = filtVel.scaleToNew(maxLinearVel);
		}
		RealVector initState = camBall.getPos().getXYVector().toRealVector()
				.append(filtVel.toRealVector());
		filter = new TrackingFilterPosVel2D(initState, initialCovarianceXY, modelError, measError,
				camBall.gettCapture());

		lastInFieldTimestamp = camBall.gettCapture();
		lastCamBall = camBall;
	}


	/**
	 * Do a prediction step on to a specific time.
	 *
	 * @param timestamp time in [ns]
	 * @param bots
	 * @param airborne
	 */
	public void predict(final long timestamp, final List<RobotCollisionShape> bots,
			final boolean airborne)
	{
		if (!airborne)
		{
			processCollisions(bots);
		}

		filter.predict(timestamp);

		if (health > 1)
		{
			health--;
		}
	}


	/**
	 * Update this tracker with a camera measurement.
	 *
	 * @param ball
	 * @param fieldSize field size, can be unknown
	 * @return True if the measurement has been accepted by the tracker (no outlier)
	 */
	public boolean update(final CamBall ball, final Optional<IRectangle> fieldSize)
	{
		IVector2 ballPos2D = ball.getPos().getXYVector();

		long tCapture = ball.gettCapture();

		// calculate delta time since last update
		double dtInSec = (tCapture - lastCamBall.gettCapture()) * 1e-9;

		// calculate distance of this ball to our internal prediction
		double distanceToPrediction = filter.getPositionEstimate().distanceTo(ballPos2D);

		// ignore the ball if it is too far away from our prediction...
		// ... we have a hard limit of maxDistance
		if ((maxDistance > 0) && (distanceToPrediction > maxDistance))
		{
			return false;
		}
		// ... and a variable distance based on the assumed max ball speed
		if (distanceToPrediction > (dtInSec * maxLinearVel))
		{
			// measurement too far away => refuse update
			return false;
		}

		// we have an update, increase health/certainty in this tracker
		if (health < maxHealth)
		{
			health += 2;
			if (age < grownUpAge)
			{
				++age;
			}
		}

		// run correction on tracking filter
		filter.correct(ballPos2D);

		// if we know the field size, check if the ball is inside it
		if (fieldSize.isPresent())
		{
			if (fieldSize.get().isPointInShape(ballPos2D))
			{
				lastInFieldTimestamp = tCapture;
			}
		} else
		{
			lastInFieldTimestamp = tCapture;
		}

		// store cam ball for next run
		lastCamBall = ball;
		updated = true;

		return true;
	}


	private void processCollisions(final List<RobotCollisionShape> bots)
	{
		for (RobotCollisionShape col : bots)
		{
			CollisionResult result = col.getCollision(filter.getPositionEstimate(), filter.getVelocityEstimate());
			if (result.getLocation() != ECollisionLocation.NONE)
			{
				filter.resetCovariance(initialCovarianceXY);
				if (result.getBallReflectedVel() != null)
				{
					filter.setVelocity(result.getBallReflectedVel());
				}
			}
		}
	}


	public double getUncertainty()
	{
		return 1.0 / health;
	}


	/**
	 * Is this tracker old enough.
	 *
	 * @return
	 */
	public boolean isGrownUp()
	{
		return age >= grownUpAge;
	}


	/**
	 * @return timestamp in [ns]
	 */
	public long getLastUpdateTimestamp()
	{
		return lastCamBall.gettCapture();
	}


	public int getCameraId()
	{
		return lastCamBall.getCameraId();
	}


	/**
	 * Get position estimate at specific timestamp.
	 *
	 * @param timestamp Query time.
	 * @return Position in [mm]
	 */
	public IVector2 getPosition(final long timestamp)
	{
		return filter.getPositionEstimate(timestamp);
	}


	/**
	 * Get linear velocity estimate.
	 *
	 * @return Velocity in [mm/s]
	 */
	public IVector2 getVelocity()
	{
		return filter.getVelocityEstimate();
	}


	/**
	 * @return the filter
	 */
	public TrackingFilterPosVel2D getFilter()
	{
		return filter;
	}


	/**
	 * This function merges a variable number of ball trackers and makes a filtered vision ball out of them.
	 * Trackers are weighted according to their state uncertainties. A tracker with high uncertainty
	 * has less influence on the final merge result.
	 *
	 * @param balls List of ball trackers. Must not be empty.
	 * @param timestamp Extrapolation time stamp to use for the final ball.
	 * @return Merged filtered vision ball.
	 */
	public static MergedBall mergeBallTrackers(final List<BallTracker> balls, final long timestamp)
	{
		Validate.notEmpty(balls);

		double totalPosUnc = 0;
		double totalVelUnc = 0;

		CamBall lastCamBall = null;

		// calculate sum of all uncertainties
		for (BallTracker t : balls)
		{
			double f = t.getUncertainty();
			totalPosUnc += Math.pow(t.filter.getPositionUncertainty().getLength() * f, -mergePower);
			totalVelUnc += Math.pow(t.filter.getVelocityUncertainty().getLength() * f, -mergePower);

			if (t.getUpdatedAndReset())
			{
				lastCamBall = t.getLastCamBall();
			}
		}

		// all uncertainties must be > 0, otherwise we found a bug
		Validate.isTrue(totalPosUnc > 0);
		Validate.isTrue(totalVelUnc > 0);

		IVector2 pos = Vector2f.ZERO_VECTOR;
		IVector2 posCam = Vector2f.ZERO_VECTOR;
		IVector2 vel = Vector2f.ZERO_VECTOR;

		// take all trackers and calculate their pos/vel sum weighted by uncertainty.
		// Trackers with high uncertainty have less influence on the merged result.
		for (BallTracker t : balls)
		{
			double f = t.getUncertainty();
			pos = pos.addNew(t.filter.getPositionEstimate(timestamp)
					.multiplyNew(Math.pow(t.filter.getPositionUncertainty().getLength() * f, -mergePower)));
			posCam = posCam.addNew(t.getLastCamBall().getPos().getXYVector()
					.multiplyNew(Math.pow(t.filter.getPositionUncertainty().getLength() * f, -mergePower)));
			vel = vel.addNew(t.filter.getVelocityEstimate()
					.multiplyNew(Math.pow(t.filter.getVelocityUncertainty().getLength() * f, -mergePower)));
		}

		pos = pos.multiplyNew(1.0 / totalPosUnc);
		posCam = posCam.multiplyNew(1.0 / totalPosUnc);
		vel = vel.multiplyNew(1.0 / totalVelUnc);

		return new MergedBall(pos, posCam, vel, timestamp, lastCamBall);
	}

	/**
	 * Merge result of multiple ball trackers.
	 *
	 * @author AndreR
	 */
	public static class MergedBall
	{
		private final IVector2 filtPos;
		private final IVector2 camPos;
		private final IVector2 filtVel;
		private final long timestamp;
		private final CamBall latestCamBall;


		/**
		 * @param filtPos
		 * @param camPos
		 * @param filtVel
		 * @param timestamp
		 * @param latestCamBall
		 */
		public MergedBall(final IVector2 filtPos, final IVector2 camPos, final IVector2 filtVel, final long timestamp,
				final CamBall latestCamBall)
		{
			this.filtPos = filtPos;
			this.camPos = camPos;
			this.filtVel = filtVel;
			this.timestamp = timestamp;
			this.latestCamBall = latestCamBall;
		}


		/**
		 * @return the filtPos
		 */
		public IVector2 getFiltPos()
		{
			return filtPos;
		}


		/**
		 * @return the camPos
		 */
		public IVector2 getCamPos()
		{
			return camPos;
		}


		/**
		 * @return the filtVel
		 */
		public IVector2 getFiltVel()
		{
			return filtVel;
		}


		/**
		 * @return the timestamp
		 */
		public long getTimestamp()
		{
			return timestamp;
		}


		/**
		 * If this optional is empty the merged ball solely depends on predicted data.
		 * Otherwise, the most recent raw CamBall is present.
		 *
		 * @return the latestCamBall
		 */
		public Optional<CamBall> getLatestCamBall()
		{
			return Optional.ofNullable(latestCamBall);
		}
	}


	/**
	 * @return the maxLinearVel
	 */
	public static double getMaxLinearVel()
	{
		return maxLinearVel;
	}


	/**
	 * @return the maxDistance
	 */
	public double getMaxDistance()
	{
		return maxDistance;
	}


	/**
	 * @param maxDistance the maxDistance to set
	 */
	public void setMaxDistance(final double maxDistance)
	{
		this.maxDistance = maxDistance;
	}


	/**
	 * @return the lastInFieldTimestamp
	 */
	public long getLastInFieldTimestamp()
	{
		return lastInFieldTimestamp;
	}


	/**
	 * @return the lastCamBall
	 */
	public CamBall getLastCamBall()
	{
		return lastCamBall;
	}


	public boolean getUpdatedAndReset()
	{
		boolean ret = updated;
		updated = false;
		return ret;
	}
}
