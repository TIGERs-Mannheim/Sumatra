/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallInterceptorTest
{

	private MoveConstraints moveConstraints;


	@Before
	public void setup()
	{
		BotMovementLimits limits = new BotMovementLimits();
		limits.setVelMax(3.0);
		limits.setAccMax(3.0);
		limits.setBrkMax(6.0);
		limits.setJerkMax(50.0);
		limits.setVelMaxFast(4.0);
		limits.setAccMaxFast(3.5);
		limits.setVelMaxW(25.0);
		limits.setAccMaxW(50.0);
		limits.setJerkMaxW(1000.0);

		moveConstraints = new MoveConstraints(limits);
	}


	private ITrackedBall genBall(final IVector2 vel)
	{
		IBallTrajectory ballTrajectory = Geometry.getBallFactory()
				.createTrajectoryFromKickedBallWithoutSpin(Vector2f.ZERO_VECTOR, Vector3.from2d(vel, 0));
		return TrackedBall.fromBallStateVisible(0, ballTrajectory.getMilliStateAtTime(0));
	}


	@Test
	public void testRandom()
	{
		Random rnd = new Random(42);
		int n = 1000;
		int f = 0;
		for (int i = 0; i < n; i++)
		{
			IVector2 botPos = Vector2.fromXY(
					(rnd.nextDouble() * 4000) - 2000, (rnd.nextDouble() * 4000) - 2000);
			IVector2 botVel = Vector2.fromXY(rnd.nextDouble() * 2, rnd.nextDouble() * 2);
			IVector2 ballVel = Vector2.fromXY(rnd.nextDouble() * 2000, rnd.nextDouble() * 2000);
			ITrackedBall ball = genBall(ballVel);
			ITrackedBot tBot = TrackedBot.stubBuilder(BotID.createBotId(0, ETeamColor.YELLOW), 0)
					.withPos(botPos)
					.withVel(botVel)
					.build();

			double optTime = optimalTime(ball, tBot);
			double optRefTime = optimalTimeRef(ball, botPos, botVel);
			if (Math.abs(optRefTime - optTime) > 0.05)
			{
				f++;
				// System.out.println(optTime + " " + optRefTime);
			}
			// assertThat(optTime).isCloseTo(optRefTime, within(0.05));
		}
		// System.out.println(f + "/" + n + " -> " + ((double) f / n));
		assertThat((double) f / n)
				.withFailMessage("Fail rate is greater than 10 percent")
				.isLessThanOrEqualTo(0.1);
	}


	private double optimalTime(final ITrackedBall ball, final ITrackedBot tBot)
	{
		return BallInterceptor.aBallInterceptor()
				.withBallTrajectory(ball.getTrajectory())
				.withTrackedBot(tBot)
				.withMoveConstraints(moveConstraints)
				.build().optimalTime();
	}


	private double optimalTimeRef(final ITrackedBall ball, final IVector2 botPos, final IVector2 botVel)
	{
		double tEnd = ball.getTrajectory().getTimeByVel(0);
		double minTSlack = Double.MAX_VALUE;
		double tAtMinTSlack = 0;
		for (double t = 0; t < tEnd; t += 0.01)
		{
			IVector2 dest = ball.getTrajectory().getPosByTime(t).getXYVector();
			ITrajectory<IVector2> traj = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos,
					botVel,
					dest);
			double tSlack = Math.abs(traj.getTotalTime() - t);
			if (tSlack < minTSlack)
			{
				minTSlack = tSlack;
				tAtMinTSlack = t;
			}
		}

		return tAtMinTSlack;
	}
}
