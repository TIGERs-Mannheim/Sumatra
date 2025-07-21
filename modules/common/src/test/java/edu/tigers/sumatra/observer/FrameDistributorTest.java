/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class FrameDistributorTest
{
	@Test
	void test()
	{
		var frameDistributor = new FrameDistributor<TestFrame>();
		List<TestFrame> frames1 = new ArrayList<>();
		List<TestFrame> frames2 = new ArrayList<>();

		TestFrame frame0 = new TestFrame(0);

		frameDistributor.subscribe("consumer1", frames1::add);

		TestFrame frame1 = new TestFrame(1);
		frameDistributor.newFrame(frame1);

		frameDistributor.subscribe("consumer2", frames2::add);

		TestFrame frame2 = new TestFrame(2);
		frameDistributor.newFrame(frame2);

		frameDistributor.unsubscribe("consumer1");

		TestFrame frame3 = new TestFrame(3);
		frameDistributor.newFrame(frame3);

		assertThat(frames1).containsExactly(frame1, frame2);
		assertThat(frames2).containsExactly(frame1, frame2, frame3);
	}


	private record TestFrame(int id) {}
}