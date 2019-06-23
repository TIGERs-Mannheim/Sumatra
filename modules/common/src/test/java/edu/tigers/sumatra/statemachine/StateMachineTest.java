/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statemachine;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class StateMachineTest
{
	/**
	 * 
	 */
	@Test
	public void testInstanceTransition()
	{
		StateMachine<IState> sm = new StateMachine<IState>();
		IState s1 = new DummyState();
		IState s2 = new DummyState();
		sm.setInitialState(s1);
		sm.addTransition(new EventStatePair(EEvent.EVENT_1, s1), s2);
		Assert.assertTrue(sm.getCurrentStateId() == s1);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		Assert.assertTrue(sm.getCurrentStateId() == s1);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		Assert.assertTrue(sm.getCurrentStateId() == s2);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testIdentifierTransition()
	{
		StateMachine<IState> sm = new StateMachine<IState>();
		IState s1 = new Dummy1State();
		IState s2 = new Dummy2State();
		sm.setInitialState(s1);
		sm.addTransition(new EventStatePair(EEvent.EVENT_1, EStateId.S1), s2);
		Assert.assertTrue(sm.getCurrentStateId() == s1);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		Assert.assertTrue(sm.getCurrentStateId() == s1);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		Assert.assertTrue(sm.getCurrentStateId() == s2);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testMixedTransition()
	{
		StateMachine<IState> sm = new StateMachine<IState>();
		IState s11 = new Dummy1State();
		IState s1 = new Dummy1State();
		IState s2 = new Dummy2State();
		sm.setInitialState(s11);
		sm.addTransition(new EventStatePair(EEvent.EVENT_1, s1), s2);
		Assert.assertTrue(sm.getCurrentStateId() == s11);
		sm.triggerEvent(EEvent.EVENT_2);
		sm.update();
		Assert.assertTrue(sm.getCurrentStateId() == s11);
		sm.triggerEvent(EEvent.EVENT_1);
		sm.update();
		Assert.assertTrue(sm.getCurrentStateId() == s11);
	}
	
	
	private static enum EEvent
	{
		EVENT_1,
		EVENT_2
	}
	
	private static enum EStateId
	{
		S1,
		S2
	}
	
	private static class DummyState implements IState
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
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return null;
		}
	}
	
	private static class Dummy1State implements IState
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
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.S1;
		}
	}
	
	private static class Dummy2State implements IState
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
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.S2;
		}
	}
}
