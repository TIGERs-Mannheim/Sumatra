/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author ArneS <arne.sachtler@dlr.de>
 */
public class TwoPhaseDynamicsModelTest
{
	private IBallDynamicsModel		dynamicsModel;
	private IState						currentState;
	
	private static IVector3			initialPos		= Vector3.fromXYZ(0, 0, 0);
	private static IVector3			initialVel		= Vector3.fromXYZ(7.0, 0, 0);
	private static IVector3 initialAcc = Vector3f.ZERO_VECTOR;
	private static IState			initialState	= new BallState(initialPos, initialVel, initialAcc);
	private static IAction ballAction = new BallAction(Vector3f.ZERO_VECTOR);
	private static MotionContext	context			= new MotionContext();
	
	
	@Before
	public void setUp()
	{
		dynamicsModel = new BallDynamicsModelTwoPhase();
		currentState = new BallState(initialState);
		
	}
	
	
	/**
	 * check if ball stops in finite time after being kicked.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConvergence()
	{
		final double interval = 25.0;
		final double dt = 0.05;
		for (double t = 0.0; t <= interval; t += dt)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, dt, context);
		}
		assertEquals(currentState.getVel().getLength(), 0.0, 1e-3);
	}
}
