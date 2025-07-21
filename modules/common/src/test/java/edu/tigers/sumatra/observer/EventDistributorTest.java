/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class EventDistributorTest
{
	private final List<TestEvent> events = new ArrayList<>();


	@Test
	void test()
	{
		var eventDistributor = new EventDistributor<TestEvent>();

		eventDistributor.subscribe(getClass().getCanonicalName(), this::consumeEvent);

		eventDistributor.newEvent(new TestEvent(1));
		eventDistributor.newEvent(new TestEvent(2));
		eventDistributor.unsubscribe(getClass().getCanonicalName());
		eventDistributor.newEvent(new TestEvent(3));

		assertThat(events).containsExactly(
				new TestEvent(1),
				new TestEvent(2)
		);

		assertThat(eventDistributor.getConsumers()).isEmpty();
	}


	@Test
	void testClear()
	{
		var eventDistributor = new EventDistributor<TestEvent>();
		eventDistributor.subscribe(getClass().getCanonicalName(), this::consumeEvent);

		assertThat(eventDistributor.getConsumers()).hasSize(1);
		assertThatThrownBy(eventDistributor::clear).isInstanceOf(IllegalStateException.class);
	}


	private void consumeEvent(final TestEvent event)
	{
		events.add(event);
	}


	private record TestEvent(int value) {}
}