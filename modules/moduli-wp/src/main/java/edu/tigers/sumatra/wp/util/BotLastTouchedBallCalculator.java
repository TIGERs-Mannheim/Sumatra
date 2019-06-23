/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This calculator decides which robot has last touched the ball
 */
public class BotLastTouchedBallCalculator
{
	@Configurable(comment = "Threshold [rad] to consider the ball heading to be changed", defValue = "0.1")
	private static double ballHeadingDiffThreshold = 0.1;
	
	@Configurable(comment = "Min Gain [m/s] in velocity that counts as kick", defValue = "0.3")
	private static double velGainThreshold = 0.3;
	
	@Configurable(comment = "Search radius [mm] around robots to look for ball to consider bots close", defValue = "300.0")
	private static double searchRadius = 300;
	
	static
	{
		ConfigRegistration.registerClass("wp", BotLastTouchedBallCalculator.class);
	}
	
	private SimpleWorldFrame wFrame;
	private SimpleWorldFrame prevWFrame;
	
	
	public BotLastTouchedBallCalculator(final SimpleWorldFrame frame, final SimpleWorldFrame prevFrame)
	{
		wFrame = frame;
		prevWFrame = prevFrame;
	}
	
	
	/**
	 * @return botIDs of bots that last touched ball
	 */
	public Set<BotID> currentlyTouchingBots()
	{
		final IVector2 prevHeading = prevWFrame.getBall().getVel();
		final IVector2 curHeading = wFrame.getBall().getVel();
		
		final Set<BotID> botsTouchingBall = getBotsCloseToBall().stream()
				.filter(b -> b.getBotShape().isPointInShape(wFrame.getBall().getPos(), Geometry.getBallRadius() + 10))
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
		
		if (prevHeading.getLength2() < 0.1 || curHeading.getLength2() < 0.1)
		{
			return botsTouchingBall;
		}
		
		if (ballHeadingChanged(curHeading, prevHeading)
				|| ballGainedVelocity(curHeading, prevHeading))
		{
			botsTouchingBall.addAll(getBotsCloseToBall().stream()
					.filter(this::ballOriginatesFromBot)
					.map(ITrackedBot::getBotId)
					.collect(Collectors.toSet()));
		}
		return botsTouchingBall;
	}
	
	
	private boolean ballOriginatesFromBot(final ITrackedBot bot)
	{
		return Lines
				.segmentFromOffset(wFrame.getBall().getPos(),
						wFrame.getBall().getVel().scaleToNew(-Geometry.getBotRadius()))
				.distanceTo(bot.getPos()) < Geometry.getBotRadius();
	}
	
	
	private List<ITrackedBot> getBotsCloseToBall()
	{
		return wFrame.getBots().values().stream()
				.filter(bot -> bot.getPos().distanceTo(wFrame.getBall().getPos()) < searchRadius)
				.collect(Collectors.toList());
	}
	
	
	private boolean ballHeadingChanged(final IVector2 curHeading, final IVector2 prevHeading)
	{
		double absHeadingDiff = curHeading.angleToAbs(prevHeading).orElseThrow(IllegalStateException::new);
		return absHeadingDiff > ballHeadingDiffThreshold;
	}
	
	
	private boolean ballGainedVelocity(final IVector2 curHeading, final IVector2 prevHeading)
	{
		return (curHeading.getLength() - prevHeading.getLength()) > velGainThreshold;
	}
}
