/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statemachine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


/**
 */
public class StateMachineTest
{
	@Test
	public void testInstanceTransition()
	{
		StateMachine<IState> sm = new StateMachine<>("test");
		IState s1 = new DummyState();
		IState s2 = new DummyState();
		sm.setInitialState(s1);
		sm.addTransition(s1, EEvent.EVENT_1, s2);
		assertThat(sm.getCurrentState()).isEqualTo(s1);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		assertThat(sm.getCurrentState()).isEqualTo(s1);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		assertThat(sm.getCurrentState()).isEqualTo(s2);
	}


	@Test
	public void testIdentifierTransition()
	{
		StateMachine<IState> sm = new StateMachine<>("test");
		IState s1 = new Dummy1State();
		IState s2 = new Dummy2State();
		sm.setInitialState(s1);
		sm.addTransition(s1, EEvent.EVENT_1, s2);
		assertThat(sm.getCurrentState()).isEqualTo(s1);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		assertThat(sm.getCurrentState()).isEqualTo(s1);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		assertThat(sm.getCurrentState()).isEqualTo(s2);
	}


	@Test
	public void testMixedTransition()
	{
		StateMachine<IState> sm = new StateMachine<>("test");
		IState s11 = new Dummy1State();
		IState s1 = new Dummy1State();
		IState s2 = new Dummy2State();
		sm.setInitialState(s11);
		sm.addTransition(s1, EEvent.EVENT_1, s2);
		assertThat(sm.getCurrentState()).isEqualTo(s11);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		assertThat(sm.getCurrentState()).isEqualTo(s11);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		assertThat(sm.getCurrentState()).isEqualTo(s11);
	}


	private enum EEvent implements IEvent
	{
		EVENT_1,
		EVENT_2
	}

	private static class DummyState extends AState
	{
	}

	private static class Dummy1State extends AState
	{
	}

	private static class Dummy2State extends AState
	{
	}
}
