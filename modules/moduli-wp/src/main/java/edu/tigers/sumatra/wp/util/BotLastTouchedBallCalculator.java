/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This calculator decides which robot has last touched the ball
 */
public class BotLastTouchedBallCalculator
{
	@Configurable(comment = "Distance factor on ball velocity ", defValue = "50.0")
	private static double searchDistanceFactor = 50;

	@Configurable(comment = "Max search distance in front of the ball [mm]", defValue = "100.0")
	private static double searchDistanceMaxFwd = 100;

	@Configurable(comment = "Max search distance behind the ball [mm]", defValue = "10.0")
	private static double searchDistanceMaxBwd = 10;

	static
	{
		ConfigRegistration.registerClass("wp", BotLastTouchedBallCalculator.class);
	}


	/**
	 * @return botIDs of bots that last touched ball
	 */
	public Set<BotID> currentlyTouchingBots(final SimpleWorldFrame wFrame)
	{
		if (wFrame.getBall().getPos3().z() > RuleConstraints.getMaxRobotHeight())
		{
			return Collections.emptySet();
		}

		var pos = wFrame.getBall().getPos();
		var speed = wFrame.getBall().getVel().getLength2();
		var offsetFwd = wFrame.getBall().getVel()
				.scaleToNew(Math.min(searchDistanceMaxFwd, speed * searchDistanceFactor));
		var offsetBwd = wFrame.getBall().getVel()
				.scaleToNew(Math.min(searchDistanceMaxBwd, speed * searchDistanceFactor));
		var line = Lines.segmentFromPoints(pos.addNew(offsetFwd), pos.subtractNew(offsetBwd));

		double minDistToBot = Geometry.getBotRadius() + Geometry.getBallRadius() + 10;
		return wFrame.getBots().values().stream()
				.filter(bot -> line.distanceTo(bot.getPos()) < minDistToBot)
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}
}
