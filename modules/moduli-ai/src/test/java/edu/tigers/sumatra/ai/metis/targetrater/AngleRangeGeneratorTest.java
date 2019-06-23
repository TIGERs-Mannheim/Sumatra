/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;


@RunWith(MockitoJUnitRunner.class)
public class AngleRangeGeneratorTest
{
	@Mock
	private IStraightBallConsultant ballConsultant;
	
	private AngleRangeGenerator processor;
	
	
	@Before
	public void before()
	{
		processor = new AngleRangeGenerator();
		processor.setKickSpeed(6.5);
		processor.setBallConsultant(ballConsultant);
		// make sure we get reproducible results
		processor.setMovingRobots(new LinkedHashMap<>());
		
		processor.setTimeToKick(0);
		
		processor.setStart(Vector2.zero());
		processor.setEndLeft(Vector2.fromXY(2000, 500));
		processor.setEndRight(Vector2.fromXY(2000, -500));
	}
	
	
	@Test
	public void processOneBotInside()
	{
		when(ballConsultant.getTimeForKick(anyDouble(), anyDouble())).thenReturn(0.2);
		IVector2 pos = Vector2.fromXY(1000, 0);
		ICircle horizon = Circle.createCircle(pos, 150);
		mockBot(pos, horizon, 0);
		
		List<AngleRange> coveredAngles = processor.findCoveredAngleRanges();
		assertThat(coveredAngles).hasSize(1);
		assertThat(coveredAngles.get(0).getLeftAngle()).isBetween(0.0, 0.2);
		assertThat(coveredAngles.get(0).getRightAngle()).isBetween(-0.2, 0.0);
	}
	
	
	@Test
	public void processTwoBotsInside()
	{
		when(ballConsultant.getTimeForKick(anyDouble(), anyDouble())).thenReturn(0.2);
		IVector2 pos = Vector2.fromXY(1000, 0);
		ICircle horizon = Circle.createCircle(pos, 150);
		mockBot(pos, horizon, 0);
		pos = Vector2.fromXY(1500, 300);
		horizon = Circle.createCircle(pos, 200);
		mockBot(pos, horizon, 1);
		
		List<AngleRange> coveredAngles = processor.findCoveredAngleRanges();
		assertThat(coveredAngles).hasSize(2);
		assertThat(coveredAngles.get(0).getLeftAngle()).isBetween(0.0, 0.4);
		assertThat(coveredAngles.get(0).getRightAngle()).isBetween(-0.4, 0.0);
		assertThat(coveredAngles.get(1).getLeftAngle()).isBetween(0.3, 0.4);
		assertThat(coveredAngles.get(1).getRightAngle()).isBetween(0.0, 0.2);
	}
	
	
	@Test
	public void processOneBotOutside()
	{
		when(ballConsultant.getTimeForKick(anyDouble(), anyDouble())).thenReturn(0.2);
		IVector2 pos = Vector2.fromXY(1000, 1000);
		ICircle horizon = Circle.createCircle(pos, 150);
		mockBot(pos, horizon, 0);
		
		List<AngleRange> coveredAngles = processor.findCoveredAngleRanges();
		assertThat(coveredAngles).hasSize(0);
	}
	
	
	@Test
	public void processOneBotCoveringAll()
	{
		when(ballConsultant.getTimeForKick(anyDouble(), anyDouble())).thenReturn(0.2);
		IVector2 pos = Vector2.fromXY(1000, 0);
		ICircle horizon = Circle.createCircle(pos, 2000);
		mockBot(pos, horizon, 0);
		
		List<AngleRange> coveredAngles = processor.findCoveredAngleRanges();
		assertThat(coveredAngles).hasSize(1);
	}
	
	
	private void mockBot(IVector2 pos, ICircle horizon, int i)
	{
		MovingRobot bot = mock(MovingRobot.class);
		when(bot.getPos()).thenReturn(pos);
		when(bot.getMovingHorizon(anyDouble())).thenReturn(horizon);
		processor.getMovingRobots().put(BotID.createBotId(i, ETeamColor.YELLOW), bot);
	}
}