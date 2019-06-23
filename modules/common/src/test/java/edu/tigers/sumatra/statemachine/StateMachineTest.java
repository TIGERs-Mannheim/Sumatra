/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statemachine;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class StateMachineTest
{
	@Test
	public void testInstanceTransition()
	{
		StateMachine<IState> sm = new StateMachine<>();
		IState s1 = new DummyState();
		IState s2 = new DummyState();
		sm.setInitialState(s1);
		sm.addTransition(s1, EEvent.EVENT_1, s2);
		Assert.assertTrue(sm.getCurrentState() == s1);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		Assert.assertTrue(sm.getCurrentState() == s1);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		Assert.assertTrue(sm.getCurrentState() == s2);
	}
	
	
	@Test
	public void testIdentifierTransition()
	{
		StateMachine<IState> sm = new StateMachine<>();
		IState s1 = new Dummy1State();
		IState s2 = new Dummy2State();
		sm.setInitialState(s1);
		sm.addTransition(s1, EEvent.EVENT_1, s2);
		Assert.assertTrue(sm.getCurrentState() == s1);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		Assert.assertTrue(sm.getCurrentState() == s1);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		Assert.assertTrue(sm.getCurrentState() == s2);
	}
	
	
	@Test
	public void testMixedTransition()
	{
		StateMachine<IState> sm = new StateMachine<>();
		IState s11 = new Dummy1State();
		IState s1 = new Dummy1State();
		IState s2 = new Dummy2State();
		sm.setInitialState(s11);
		sm.addTransition(s1, EEvent.EVENT_1, s2);
		Assert.assertTrue(sm.getCurrentState() == s11);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		Assert.assertTrue(sm.getCurrentState() == s11);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		Assert.assertTrue(sm.getCurrentState() == s11);
	}
	
	
	private enum EEvent implements IEvent
	{
		EVENT_1,
		EVENT_2
	}
	
	private static class DummyState extends AState
	{
		@Override
		public void doEntryActions()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
	}
	
	private static class Dummy1State extends AState
	{
		@Override
		public void doEntryActions()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
	}
	
	private static class Dummy2State extends AState
	{
		@Override
		public void doEntryActions()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
	}
}
