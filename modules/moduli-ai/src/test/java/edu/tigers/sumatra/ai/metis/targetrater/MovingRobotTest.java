/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


@RunWith(MockitoJUnitRunner.class)
public class MovingRobotTest
{
	private static final double ACC = 3.0;
	private static final double VEL = 2.5;
	
	
	@Test
	public void compareWithLegacy()
	{
		Random rnd = new Random(0);
		for (int i = 0; i < 1000; i++)
		{
			IVector2 pos = Vector2.fromXY(rnd.nextDouble() * 4000 - 2000, rnd.nextDouble() * 4000 - 2000);
			IVector2 vel = Vector2.fromXY(rnd.nextDouble() * 4 - 2, rnd.nextDouble() * 4 - 2);
			ITrackedBot bot = mockBot(pos, vel);
			double maxHorizon = 1.5;
			double radius = 90;
			MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, radius);
			for (double t = 0; t < 2; t += 0.1)
			{
				final ICircle circle = movingRobot.getMovingHorizon(t);
				final ICircle circleLegacy = movingRobot.getMovingHorizonLegacy2d(t);
				assertThat(circle.radius()).isCloseTo(circleLegacy.radius(), within(1e-10));
				assertThat(circle.center()).isEqualTo(circleLegacy.center());
			}
		}
	}
	
	
	@Test
	public void getCircleForStandingBot()
	{
		IVector2 pos = Vector2.fromXY(42, 1337);
		ITrackedBot bot = mockBot(pos, Vector2.zero());
		double maxHorizon = 1.5;
		double radius = 90;
		MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, radius);
		ICircle circle0 = movingRobot.getMovingHorizon(0);
		assertThat(circle0.center()).isEqualTo(pos);
		
		ICircle circle01 = movingRobot.getMovingHorizon(0.1);
		assertThat(circle01.center()).isEqualTo(pos);
		
		ICircle circle10 = movingRobot.getMovingHorizon(1.0);
		assertThat(circle10.center()).isEqualTo(pos);
		
		ICircle circle50 = movingRobot.getMovingHorizon(5.0);
		assertThat(circle50.center()).isEqualTo(pos);
	}
	
	
	@Test
	public void getCircleForMovingBot()
	{
		double vel = 1.5;
		IVector2 pos = Vector2.zero();
		IVector2 vel2 = Vector2.fromXY(vel, 0);
		ITrackedBot bot = mockBot(pos, vel2);
		double maxHorizon = 1.5;
		double radius = 90;
		MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, radius);
		ICircle circle0 = movingRobot.getMovingHorizon(0);
		assertThat(circle0.center()).isEqualTo(pos);
		
		ICircle circle01 = movingRobot.getMovingHorizon(0.1);
		assertThat(circle01.center()).isEqualTo(Vector2.fromX(150.0));
		
		ICircle circle10 = movingRobot.getMovingHorizon(1.0);
		assertThat(circle10.center()).isEqualTo(Vector2.fromX(500.0));
		
		ICircle circle20 = movingRobot.getMovingHorizon(2.0);
		assertThat(circle20.center()).isEqualTo(Vector2.fromX(0.0));
	}
	
	
	@Test
	public void getRadiusForStandingBot()
	{
		ITrackedBot bot = mockBot(Vector2.zero(), Vector2.zero());
		double maxHorizon = 1.5;
		double radius = 90;
		MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, radius);
		assertThat(movingRobot.getMovingHorizon(0).radius()).isCloseTo(radius, within(1e-10));
		assertThat(movingRobot.getMovingHorizon(0.1).radius()).isCloseTo(0.1 * ACC * 0.1 * 1000 + radius, within(1e-10));
		assertThat(movingRobot.getMovingHorizon(0.5).radius()).isCloseTo(0.5 * ACC * 0.5 * 1000 + radius, within(1e-10));
		assertThat(movingRobot.getMovingHorizon(1.0).radius()).isCloseTo(VEL * 1.0 * 1000 + radius, within(1e-10));
		assertThat(movingRobot.getMovingHorizon(2.0).radius()).isCloseTo(VEL * maxHorizon * 1000 + radius, within(1e-10));
	}
	
	
	@Test
	public void getRadiusForMovingBot()
	{
		double vel = 1.5;
		ITrackedBot bot = mockBot(Vector2.zero(), Vector2.fromX(vel));
		double maxHorizon = 1.5;
		double radius = 90;
		MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, radius);
		assertThat(movingRobot.getMovingHorizon(0).radius()).isCloseTo(radius, within(1e-10));
		assertThat(movingRobot.getMovingHorizon(0.1).radius()).isCloseTo(0.1 * ACC * 0.1 * 1000 + radius, within(1e-10));
		assertThat(movingRobot.getMovingHorizon(0.5).radius()).isCloseTo(
				(VEL + (0.5 * ACC - vel)) / 2 * 0.5 * 1000 + radius,
				within(1e-10));
		assertThat(movingRobot.getMovingHorizon(1.0).radius()).isCloseTo(
				(VEL + (1.0 * ACC - vel)) / 2 * 1.0 * 1000 + radius,
				within(1e-10));
		assertThat(movingRobot.getMovingHorizon(2.0).radius()).isCloseTo(VEL * maxHorizon * 1000 + radius, within(1e-10));
	}
	
	
	private ITrackedBot mockBot(IVector2 pos, IVector2 vel)
	{
		ITrackedBot bot = mock(ITrackedBot.class);
		when(bot.getRobotInfo()).thenReturn(mock(RobotInfo.class));
		when(bot.getRobotInfo().getBotParams()).thenReturn(mock(BotParams.class));
		when(bot.getRobotInfo().getBotParams().getMovementLimits()).thenReturn(mock(BotMovementLimits.class));
		when(bot.getRobotInfo().getBotParams().getMovementLimits().getVelMax()).thenReturn(VEL);
		when(bot.getRobotInfo().getBotParams().getMovementLimits().getAccMax()).thenReturn(ACC);
		when(bot.getPos()).thenReturn(pos);
		when(bot.getVel()).thenReturn(vel);
		return bot;
	}
}