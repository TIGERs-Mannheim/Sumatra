/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;


/**
 * Role for RoboCup 2021 Hardware Challenge 3: Dribbling
 * <a href="https://robocup-ssl.github.io/ssl-hardware-challenge-rules/rules.html#_challenge_3_dribbling">rules</a>
 * Takes the ball and passes through gates of robots. This simple implementation uses some intermediate points
 * and always comes to a full stop at these.
 */
public class DribbleChallengeSimpleRole extends ADribbleChallengeSimpleRole
{
	public DribbleChallengeSimpleRole()
	{
		super(ERole.DRIBBLE_CHALLENGE_SIMPLE_ROLE);
	}


	@Override
	protected ChallengeData getChallengeData()
	{
		List<ILineSegment> gates = new ArrayList<>();
		List<IVector2> waypoints = new ArrayList<>();

		IVector2 ballPos = getBall().getPos();

		List<IVector2> obstacles = getWFrame().getOpponentBots().values().stream()
				.sorted(Comparator.comparingDouble(bot -> bot.getPos().distanceTo(ballPos)))
				.map(ITrackedBot::getPos)
				.toList();

		if (obstacles.size() >= 2)
		{
			gates = IntStream.range(0, obstacles.size() - 1)
					.boxed()
					.map(i -> Lines.segmentFromPoints(obstacles.get(i), obstacles.get(i + 1)))
					.toList();

			for (int i = 0; i < gates.size(); i++)
			{
				var gate = gates.get(i);
				var passpoint = gate.getPathStart().addNew(gate.getPathEnd()).multiplyNew(0.5);

				var rotation = i % 2 == 0 ? AngleMath.DEG_090_IN_RAD : -AngleMath.DEG_090_IN_RAD;

				var first = passpoint.addNew(gate.directionVector().turnNew(-rotation).scaleTo(500.0));
				var second = passpoint.addNew(gate.directionVector().turnNew(rotation).scaleTo(500.0));

				if (i == 0)
				{
					var entry = gate.getPathStart().addNew(gate.directionVector().turnNew(-rotation).scaleTo(500.0));
					waypoints.add(entry);
				}

				waypoints.add(first);
				waypoints.add(second);

				if (i == gates.size() - 1)
				{
					waypoints.add(first);
					waypoints.add(second);
				}
			}
		}

		return new ChallengeData(gates, waypoints, obstacles);
	}
}
