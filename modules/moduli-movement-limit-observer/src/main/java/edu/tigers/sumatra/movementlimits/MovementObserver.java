/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movementlimits;

import static edu.tigers.sumatra.ids.ETeamColor.YELLOW;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.bot.params.IBotMovementLimits;
import edu.tigers.sumatra.data.TimeLimitedBuffer;
import edu.tigers.sumatra.math.SumatraMath;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Observes bot velocities and accelerations and updates the live limits accordingly.
 */
@RequiredArgsConstructor
public class MovementObserver implements IWorldFrameObserver
{
	private static final Logger log = LogManager.getLogger(MovementObserver.class);

	@Configurable(defValue = "500", comment = "Update the observed values every x Running world frame")
	private static int updateEvery = 500;

	@Configurable(defValue = "500", comment = "Size of the rolling history")
	private static int historySize = 500;

	@Configurable(defValue = "0.95", comment = "Percentile to use for limit observation")
	private static double percentile = 0.95;

	@Configurable(defValue = "false", comment = "Update velocity limits")
	private static boolean updateVelocity = false;

	@Configurable(defValue = "0.1", comment = "Minimum velocity limit [m/s]")
	private static double minimumVelocity = 0.1;
	@Configurable(defValue = "5.0", comment = "Maximum velocity limit [m/s]")
	private static double maximumVelocity = 5.0;
	@Configurable(defValue = "0.1", comment = "Minimum acceleration limit [m/s²]")
	private static double minimumAcceleration = 0.1;
	@Configurable(defValue = "10.0", comment = "Maximum acceleration limit [m/s²]")
	private static double maximumAcceleration = 10.0;


	@Configurable(defValue = "0.1", comment = "Minimum velocity limit [rad/s]")
	private static double minimumWVelocity = 0.1;
	@Configurable(defValue = "30.0", comment = "Maximum velocity limit [rad/s]")
	private static double maximumWVelocity = 30.0;
	@Configurable(defValue = "0.1", comment = "Minimum acceleration limit [rad/s²]")
	private static double minimumWAcceleration = 0.1;
	@Configurable(defValue = "75.0", comment = "Maximum acceleration limit [rad/s²]")
	private static double maximumWAcceleration = 75.0;

	static
	{
		ConfigRegistration.registerClass("wp", MovementObserver.class);
	}

	private final Map<BotID, TimeLimitedBuffer<Double>> velocityHistory = new HashMap<>();
	private final Map<BotID, TimeLimitedBuffer<Double>> wVelocityHistory = new HashMap<>();
	private final Map<BotID, TimeLimitedBuffer<Double>> accelerationHistory = new HashMap<>();
	private final Map<BotID, TimeLimitedBuffer<Double>> wAccelerationHistory = new HashMap<>();
	private final Map<BotID, TimeLimitedBuffer<Double>> breakingHistory = new HashMap<>();

	private final BotParamsManager botParamsManager;

	private Map<BotID, ITrackedBot> lastFrame = Collections.emptyMap();
	private int waitFrames = updateEvery;


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (wFrameWrapper.getGameState().isRunning())
		{
			for (ITrackedBot bot : wFrameWrapper.getSimpleWorldFrame().getBots().values())
			{
				addToHistory(velocityHistory, IBotMovementLimits::getVelMax, bot, bot.getVel().getLength());
				addToHistory(wVelocityHistory, IBotMovementLimits::getVelMaxW, bot, bot.getAngularVel());

				ITrackedBot last = lastFrame.getOrDefault(bot.getBotId(), bot);
				if (last.getTimestamp() == bot.getTimestamp())
					continue;

				double timeDelta = (bot.getTimestamp() - last.getTimestamp()) * 1e-9;
				double acceleration = (bot.getVel().getLength() - last.getVel().getLength()) / timeDelta;
				double wAcceleration = (bot.getAngularVel() - last.getAngularVel()) / timeDelta;

				if (acceleration > 0)
					addToHistory(accelerationHistory, IBotMovementLimits::getAccMax, bot, acceleration);
				else
					addToHistory(breakingHistory, IBotMovementLimits::getBrkMax, bot, -acceleration);

				addToHistory(wAccelerationHistory, IBotMovementLimits::getAccMaxW, bot, Math.abs(wAcceleration));
			}

			if (--waitFrames == 0)
			{
				publish();
				waitFrames = updateEvery;
			}
		}

		lastFrame = wFrameWrapper.getSimpleWorldFrame().getBots();
	}


	private void publish()
	{
		for (final ETeamColor color : ETeamColor.yellowBlueValues())
		{
			EBotParamLabel label = teamToLabel(color);
			BotParams params = botParamsManager.getDatabase().getSelectedParams(label);
			BotMovementLimits limits = (BotMovementLimits) params.getMovementLimits();

			if (updateVelocity)
			{
				limits.setVelMax(getMaxPercentile(
						velocityHistory, color, limits.getVelMax(), minimumVelocity, maximumVelocity
				));
				limits.setVelMaxW(getMaxPercentile(
						wVelocityHistory, color, limits.getVelMaxW(), minimumWVelocity, maximumWVelocity
				));
			}

			limits.setAccMax(getMaxPercentile(
					accelerationHistory, color, limits.getAccMax(), minimumAcceleration, maximumAcceleration
			));
			limits.setBrkMax(getMaxPercentile(
					breakingHistory, color, limits.getBrkMax(), minimumAcceleration, maximumAcceleration
			));
			limits.setAccMaxW(getMaxPercentile(
					wAccelerationHistory, color, limits.getAccMaxW(), minimumWAcceleration, maximumWAcceleration
			));

			log.debug(
					"Update for team {}: VelMax {} m/s {} rad/s AccMax {} m/s² {} rad/s² BrkMax {} m/s²",
					color, limits.getVelMax(), limits.getVelMaxW(), limits.getAccMax(), limits.getAccMaxW(),
					limits.getBrkMax()
			);

			botParamsManager.onEntryUpdated(botParamsManager.getDatabase().getTeamStringForLabel(label), params);
		}
	}


	private void addToHistory(Map<BotID, TimeLimitedBuffer<Double>> history,
			ToDoubleFunction<IBotMovementLimits> defaultSupplier, ITrackedBot bot, double value)
	{
		history
				.computeIfAbsent(
						bot.getBotId(),
						b -> {
							TimeLimitedBuffer<Double> buffer = new TimeLimitedBuffer<>();
							buffer.setMaxElements(historySize);

							double defaultValue = defaultSupplier.applyAsDouble(
									botParamsManager.get(teamToLabel(b.getTeamColor())).getMovementLimits()
							);

							for (int i = 0; i < historySize; i++)
								buffer.add(bot.getTimestamp(), defaultValue);

							return buffer;
						}
				)
				.add(bot.getTimestamp(), value);
	}


	private double getMaxPercentile(Map<BotID, TimeLimitedBuffer<Double>> history, ETeamColor color,
			double defaultValue, double min, double max)
	{
		return SumatraMath.cap(
				history.entrySet()
						.stream()
						.filter(entry -> entry.getKey().getTeamColor() == color)
						.map(entry -> entry.getValue().getValuePercentile(percentile))
						.max(Double::compareTo).orElse(defaultValue),
				min,
				max
		);
	}


	private EBotParamLabel teamToLabel(ETeamColor color)
	{
		if (color == YELLOW)
		{
			return EBotParamLabel.YELLOW_LIVE;
		} else
		{
			return EBotParamLabel.BLUE_LIVE;
		}
	}
}
