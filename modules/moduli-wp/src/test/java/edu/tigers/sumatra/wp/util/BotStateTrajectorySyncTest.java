/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.StubTrajectory;
import edu.tigers.sumatra.wp.data.DelayedBotState;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class BotStateTrajectorySyncTest
{
	private BotStateTrajectorySync c;


	@Before
	public void before()
	{
		c = new BotStateTrajectorySync();
	}


	@Test
	public void getState()
	{
		Optional<State> botState = updateState(Vector3.fromXY(1, 0), 1);
		assertThat(botState).isNotPresent();

		botState = updateState(Vector3.fromXY(3, 0), 3);
		assertThat(botState).isPresent();
		assertThat(botState.get().getPos()).isEqualTo(Vector2.fromXY(2, 0));
	}


	private Optional<State> updateState(IVector3 pos, long timestamp)
	{
		c.add(StubTrajectory.vector3Static(pos), timestamp);
		DelayedBotState state = DelayedBotState.of(BotID.createBotId(0, ETeamColor.YELLOW), State.zero(), 1e-9);
		return Optional.ofNullable(c.updateState(timestamp, state));
	}
}