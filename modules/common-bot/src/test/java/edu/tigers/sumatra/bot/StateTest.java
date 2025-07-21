/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class StateTest
{
	@Test
	void interpolate()
	{
		State state1 = State.of(Pose.zero(), Vector3.fromXYZ(2, 10, 0));
		State state2 = State.of(Pose.zero(), Vector3.fromXYZ(4, 12, 2));
		assertThat(state1.interpolate(state2, 0.0)).isEqualTo(state1);
		assertThat(state2.interpolate(state1, 0.0)).isEqualTo(state2);
		assertThat(state1.interpolate(state2, 1.0)).isEqualTo(state2);
		assertThat(state2.interpolate(state1, 1.0)).isEqualTo(state1);
		assertThat(state1.interpolate(state2, 0.5)).isEqualTo(State.of(Pose.zero(), Vector3.fromXYZ(3, 11, 1)));
	}
}