/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Role for RoboCup 2022 Technical Challenge: Dribbling
 * Takes the ball and passes through gates of robots. This simple implementation uses some intermediate points
 * and always comes to a full stop at these.
 */
public class DribbleChallenge2022SimpleRole extends ADribbleChallengeSimpleRole
{
	private final List<IVector2> expectedRobotPositions = new ArrayList<>();


	public DribbleChallenge2022SimpleRole()
	{
		super(ERole.DRIBBLE_CHALLENGE_2022_SIMPLE_ROLE);

		expectedRobotPositions.add(Vector2.fromXY(-3000, -2300));
		expectedRobotPositions.add(Vector2.fromXY(-3500, -2750));
		expectedRobotPositions.add(Vector2.fromXY(-4000, -2300));
		expectedRobotPositions.add(Vector2.fromXY(-3500, -2000));
		expectedRobotPositions.add(Vector2.fromXY(-2500, -2300));
		expectedRobotPositions.add(Vector2.fromXY(500, 2500));
		expectedRobotPositions.add(Vector2.fromXY(1000, 2500));
	}


	@Override
	protected ChallengeData getChallengeData()
	{
		List<ILineSegment> gates = new ArrayList<>();
		List<IVector2> waypoints = new ArrayList<>();
		List<IVector2> obstacles = new ArrayList<>();

		// map opponent robots to expected positions to figure out actual positions
		var opponents = new ArrayList<>(getWFrame().getOpponentBots().values());

		for (var expectedLocation : expectedRobotPositions)
		{
			Optional<ITrackedBot> closestBot = opponents.stream()
					.min(Comparator.comparingDouble(bot -> bot.getPos().distanceTo(expectedLocation)));

			if (closestBot.isPresent())
			{
				obstacles.add(closestBot.get().getPos());
				opponents.remove(closestBot.get());
			} else
			{
				// not enough robots on the field to match all expected locations
				return new ChallengeData(gates, waypoints, obstacles);
			}
		}

		// compute gates from actual positions
		gates.add(Lines.segmentFromPoints(obstacles.get(0), obstacles.get(1))); // Gate A
		gates.add(Lines.segmentFromPoints(obstacles.get(1), obstacles.get(2))); // Gate B
		gates.add(Lines.segmentFromPoints(obstacles.get(2), obstacles.get(3))); // Gate C
		gates.add(Lines.segmentFromPoints(obstacles.get(3), obstacles.get(0))); // Gate D
		gates.add(Lines.segmentFromPoints(obstacles.get(0), obstacles.get(4))); // Gate E
		gates.add(Lines.segmentFromPoints(obstacles.get(5), obstacles.get(6))); // Gate F

		// compute first set of waypoints: gates A, B, and C are passed twice
		for (int i = 0; i < 3; i++)
		{
			var gate = gates.get(i);
			var passpoint = gate.getPathStart().addNew(gate.directionVector().multiplyNew(0.5));

			var inner = passpoint.addNew(gate.directionVector().turnNew(-AngleMath.DEG_090_IN_RAD).scaleTo(200.0));
			var outer = passpoint.addNew(gate.directionVector().turnNew(AngleMath.DEG_090_IN_RAD).scaleTo(380.0));

			if (i != 0)
			{
				waypoints.add(inner);
			}

			waypoints.add(outer);

			if (i == 2)
			{
				var lastInner = passpoint.addNew(
						gate.directionVector().turnNew(-AngleMath.DEG_090_IN_RAD).scaleTo(350.0));
				waypoints.add(lastInner);
			}
		}

		// gates D to F are passed only once
		IVector2 betweenRobot0And4 = obstacles.get(0).addNew(obstacles.get(4)).multiply(0.5);
		IVector2 betweenRobot5And6 = obstacles.get(5).addNew(obstacles.get(6)).multiply(0.5);

		waypoints.add(obstacles.get(0).addNew(Vector2.fromXY(-100, 400))); // -3100, -2000
		waypoints.add(betweenRobot0And4.addNew(Vector2.fromXY(-50, 500))); // -2800, -2000
		waypoints.add(betweenRobot0And4.addNew(Vector2.fromXY(0, -500))); // -2750, -2600
		waypoints.add(betweenRobot0And4.addNew(Vector2.fromXY(500, -500))); // -2250, -2600
		waypoints.add(betweenRobot5And6.addNew(Vector2.fromXY(-500, 300))); // 250, 2800
		waypoints.add(betweenRobot5And6.addNew(Vector2.fromXY(0, 300))); // 750, 2800
		waypoints.add(betweenRobot5And6.addNew(Vector2.fromXY(0, -1200))); // 750, 1300

		return new ChallengeData(gates, waypoints, obstacles);
	}
}
