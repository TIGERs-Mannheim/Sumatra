/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.StubTrajectory;


public class BotStateFromTrajectoryCalculatorTest
{
	BotStateFromTrajectoryCalculator c;
	
	
	@Before
	public void before()
	{
		c = new BotStateFromTrajectoryCalculator();
	}
	
	
	@Test
	public void getState()
	{
		Optional<BotState> botState = updateState(Vector3.fromXY(1, 0), 1);
		assertThat(botState).isNotPresent();
		
		botState = updateState(Vector3.fromXY(3, 0), 3);
		assertThat(botState).isPresent();
		assertThat(botState.get().getPos()).isEqualTo(Vector2.fromXY(2, 0));
	}
	
	
	private Optional<BotState> updateState(IVector3 pos, long timestamp)
	{
		BotParams botParams = new BotParams();
		botParams.setFeedbackDelay(1e-9);
		RobotInfo robotInfo = RobotInfo.stubBuilder(BotID.createBotId(0, ETeamColor.YELLOW), timestamp)
				.withTrajectory(StubTrajectory.vector3Static(pos))
				.withBotParams(botParams)
				.build();
		return c.getState(robotInfo);
	}
	
	
	@Test
	public void getLatestState()
	{
		updateState(Vector3.fromXY(1, 0), 1);
		updateState(Vector3.fromXY(1, 1), 2);
		updateState(Vector3.fromXY(1, 2), 3);
		updateState(Vector3.fromXY(1, 3), 5);
		Optional<BotState> botState = c.getLatestState(BotID.createBotId(0, ETeamColor.YELLOW));
		assertThat(botState).isPresent();
		assertThat(botState.get().getPos()).isEqualTo(Vector2.fromXY(1, 3));
	}
}