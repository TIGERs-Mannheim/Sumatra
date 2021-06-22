/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.WorldFrame;


public class CachedBallInterception
{
	@Configurable(defValue = "0.1")
	private static double initialAdditionalTimePercentBase = 0.1;

	@Configurable(defValue = "0.3")
	private static double youngKickAdditionalTimePercent = 0.3;

	@Configurable(defValue = "0.2")
	private static double minKickAge = 0.2;

	static
	{
		ConfigRegistration.registerClass("skills", CachedBallInterception.class);
	}

	private final BotID botID;
	private long timestamp;
	private OptimalBallInterception optimalBallInterception;
	private double initialAdditionalTimePercent;
	private double optimalInterceptionTime;
	private double initialInterceptionTime;
	private double interceptionTime;
	private double interceptionSlackTime;
	private Long lastInterceptionTimestamp = null;


	public CachedBallInterception(final BotID botID)
	{
		this.botID = botID;
	}


	public void update(final WorldFrame worldFrame, final IMoveConstraints mc)
	{
		timestamp = worldFrame.getTimestamp();
		this.optimalBallInterception = createOptimalBallInterception(worldFrame, mc);
		final double additionalTimePercent = calcAdditionalTimePercent(worldFrame.getKickEvent().orElse(null));
		initialAdditionalTimePercent = initialAdditionalTimePercentBase + additionalTimePercent;
		optimalInterceptionTime = calcOptimalInterceptionTime();
		initialInterceptionTime = calcInitialInterceptionTime();
		interceptionTime = calcInterceptionTime();
		lastInterceptionTimestamp = calcLastInterceptionTimestamp();
	}


	private OptimalBallInterception createOptimalBallInterception(final WorldFrame worldFrame,
			final IMoveConstraints mc)
	{
		return OptimalBallInterception.anOptimalBallInterceptor()
				.withBallTrajectory(worldFrame.getBall().getTrajectory())
				.withTrackedBot(worldFrame.getBot(botID))
				.withMoveConstraints(mc)
				.build();
	}


	private double calcAdditionalTimePercent(final IKickEvent kickEvent)
	{
		double kickAge = Optional.ofNullable(kickEvent).map(t -> (timestamp - t.getTimestamp()) / 1e9)
				.orElse(minKickAge);
		if (kickAge < minKickAge)
		{
			return youngKickAdditionalTimePercent;
		} else
		{
			return 0;
		}
	}


	private double calcInterceptionTime()
	{
		if (lastInterceptionTimestamp != null)
		{
			double currentInterceptionTime = (lastInterceptionTimestamp - timestamp) / 1e9;
			interceptionSlackTime = optimalBallInterception.slackTime(currentInterceptionTime);
			if (currentInterceptionTime >= 0 && isAcceptableSlackTime(interceptionSlackTime))
			{
				return currentInterceptionTime;
			}
		}

		return initialInterceptionTime;
	}


	public boolean isAcceptableSlackTime(double slackTime)
	{
		return SumatraMath.isBetween(slackTime, -0.2, 0.5);
	}


	private double calcOptimalInterceptionTime()
	{
		return optimalBallInterception.optimalInterceptionTime();
	}


	private double calcInitialInterceptionTime()
	{
		return interceptionTimeWithAdditionalTime(optimalInterceptionTime, initialAdditionalTimePercent);
	}


	private double interceptionTimeWithAdditionalTime(double interceptionTimestamp, double additionalTimePercent)
	{
		return interceptionTimestamp * (1 + additionalTimePercent);
	}


	private long calcLastInterceptionTimestamp()
	{
		return timestamp + (long) (interceptionTime * 1e9);
	}


	public double getOptimalInterceptionTime()
	{
		return optimalInterceptionTime;
	}


	public double getInitialInterceptionTime()
	{
		return initialInterceptionTime;
	}


	public double getInterceptionTime()
	{
		return interceptionTime;
	}


	public OptimalBallInterception getOptimalBallInterception()
	{
		return optimalBallInterception;
	}


	public double getInterceptionSlackTime()
	{
		return interceptionSlackTime;
	}
}
