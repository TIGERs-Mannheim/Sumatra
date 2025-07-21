/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.modelidentification.movement;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TeamMovementObserver
{
	@Configurable(defValue = "2000", comment = "Minimum sample amount to start parameter determination")
	private static int minSamples = 2000;

	@Configurable(defValue = "5.0", comment = "Maximum velocity limit [m/s]")
	private static double maximumVelocity = 5.0;
	@Configurable(defValue = "0.1", comment = "Minimum velocity for acceleration determination [m/s]")
	private static double minimumVelocity = 0.1;
	@Configurable(defValue = "10.0", comment = "Maximum acceleration limit [m/s²]")
	private static double maximumAcceleration = 10.0;

	@Configurable(defValue = "0.995", comment = "Selected velocity percentile")
	private static double velocityPercentile = 0.995;
	@Configurable(defValue = "0.75", comment = "Selected acceleration percentile")
	private static double accelerationPercentile = 0.75;
	@Configurable(defValue = "0.95", comment = "Selected breaking percentile")
	private static double breakingPercentile = 0.95;

	@Configurable(defValue = "10.0", comment = "Decimal place precision")
	private static double decimalPlacePrecision = 10.0;

	static
	{
		ConfigRegistration.registerClass("wp", TeamMovementObserver.class);
	}

	private final Histogram velHistogram = new Histogram(0.0, 0.1, maximumVelocity);
	private final Histogram accHistogram = new Histogram(0.0, 0.1, maximumAcceleration);
	private final Histogram brkHistogram = new Histogram(0.0, 0.1, maximumAcceleration);


	public void addSamples(boolean updateVel, double dt1, double dt2,
			List<CamRobot> robots0, List<CamRobot> robots1, List<CamRobot> robots2)
	{
		velHistogram.setMax(maximumVelocity);
		accHistogram.setMax(maximumAcceleration);
		brkHistogram.setMax(maximumAcceleration);

		Map<BotID, CamRobot> map0 = robots0.stream()
				.collect(Collectors.toMap(CamRobot::getBotId, Function.identity(), (r1, r2) -> r1));
		Map<BotID, CamRobot> map1 = robots1.stream()
				.collect(Collectors.toMap(CamRobot::getBotId, Function.identity(), (r1, r2) -> r1));

		for (CamRobot r2 : robots2)
		{
			CamRobot r0 = map0.get(r2.getBotId());
			CamRobot r1 = map1.get(r2.getBotId());
			if (r0 == null || r1 == null)
				continue;

			Vector2 d2 = velocity(r1, r2, dt2);
			double v2 = d2.getLength();
			if (updateVel)
				velHistogram.add(v2);

			if (v2 >= minimumVelocity)
			{
				Vector2 d1 = velocity(r0, r1, dt1);
				(v2 > d1.getLength() ? accHistogram : brkHistogram).add(d2.subtractNew(d1).getLength() / dt2);
			}
		}
	}


	private Vector2 velocity(CamRobot r0, CamRobot r1, double dt)
	{
		return r1.getPos().subtractNew(r0.getPos()).multiply(0.001 / dt);
	}


	public void publish(BotMovementLimits limits)
	{
		publish(velHistogram, velocityPercentile, limits::setVelMax);
		publish(accHistogram, accelerationPercentile, limits::setAccMax);
		publish(brkHistogram, breakingPercentile, limits::setBrkMax);
	}


	private void publish(Histogram histogram, double percentile, DoubleConsumer setter)
	{
		if (histogram.getSamples() < minSamples)
			return;

		setter.accept(
				Math.round(histogram.getPercentile(percentile) * decimalPlacePrecision) / decimalPlacePrecision
		);
	}


	public void clear()
	{
		velHistogram.clear();
		accHistogram.clear();
		brkHistogram.clear();
	}
}
