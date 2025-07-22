/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Factory which creates a list of {@link WorldFrame}s with random positioned bots.
 */
public class WorldFrameFactory
{
	private static final Random RND = new Random(0);


	/**
	 * Creates a new WorldFrame with random positioned bots.
	 *
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @param timestamp
	 * @return
	 */
	public static SimpleWorldFrame createSimpleWorldFrame(final long frameNumber, final long timestamp)
	{
		final Map<BotID, ITrackedBot> bots = new HashMap<>();

		for (int i = 0; i < 6; i++)
		{
			BotID idF = BotID.createBotId(i, ETeamColor.BLUE);
			bots.put(idF, createBot(timestamp, idF));

			BotID idT = BotID.createBotId(i, ETeamColor.YELLOW);
			bots.put(idT, createBot(timestamp, idT));
		}

		return new SimpleWorldFrame(frameNumber, timestamp, bots, TrackedBall.createStub(), null);
	}


	/**
	 * Create bot with random positions
	 *
	 * @param timestamp
	 * @param id
	 * @return bot
	 */
	public static ITrackedBot createBot(final long timestamp, final BotID id)
	{
		double x = (RND.nextDouble() * Geometry.getFieldLength())
				- (Geometry.getFieldLength() / 2.0);
		double y = (RND.nextDouble() * Geometry.getFieldWidth())
				- (Geometry.getFieldWidth() / 2.0);
		final IVector2 pos = Vector2.fromXY(x, y);

		RobotInfo robotInfo = RobotInfo.stubBuilder(id, timestamp).build();

		return TrackedBot.stubBuilder(id, timestamp)
				.withPos(pos)
				.withBotInfo(robotInfo)
				.build();
	}
}
