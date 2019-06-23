/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import static edu.tigers.sumatra.math.SumatraMath.sqrt;
import static edu.tigers.sumatra.math.SumatraMath.square;
import static java.lang.Math.pow;

import Jama.Matrix;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Arne Sachtler <arne.sachtler@dlr.de>
 */
public class BallDynamicsModelTwoPhase implements IBallDynamicsModel
{
	/** [mm/s²] */
	private final double				acc;
	private double						accSlide;
	private double						cSwitch;
	
	private static final double	A_FALL							= -9.810;
	private static final double	BALL_DAMP_GROUND_FACTOR		= 0.37;
	public static final double		BALL_HAS_STOPPED_THRESHOLD	= 1e-3;
	
	/* switch constant for slide --> roll friction transition */
	private static final double	C_SWITCH							= Geometry.getBallParameters().getkSwitch();
	
	/* acceleration constants */
	private static final double	A_SLIDE							= 1e-3 * Geometry.getBallParameters().getAccSlide();
	private static final double	A_ROLL							= 1e-3 * Geometry.getBallParameters().getAccRoll();
	
	private IState						initialState					= new BallState();
	private boolean					initialVelValid				= false;
	
	
	/**
	 * Create a new ball dynamics model for the two phase model
	 */
	public BallDynamicsModelTwoPhase()
	{
		this(A_SLIDE, A_ROLL, C_SWITCH);
	}
	
	
	/**
	 * @param accSlide
	 * @param accRoll
	 * @param cSwitch
	 */
	public BallDynamicsModelTwoPhase(final double accSlide, final double accRoll, final double cSwitch)
	{
		this.accSlide = accSlide;
		acc = accRoll;
		this.cSwitch = cSwitch;
		initialState = new BallState(Vector3.ZERO_VECTOR, Vector3.ZERO_VECTOR, Vector3.ZERO_VECTOR);
	}
	
	
	@Override
	public IState dynamics(final IState state, final IAction action, final double dt, final MotionContext context)
	{
		Matrix currentState = state.getStateMatrix();
		if (state.getVel().getLength() > initialState.getVel().getLength() || !initialVelValid)
		{
			if (state.getVel().getLength2() > BALL_HAS_STOPPED_THRESHOLD)
			{
				initialState = new BallState(state);
				initialVelValid = true;
			}
		} else if (state.getVel().getLength2() < BALL_HAS_STOPPED_THRESHOLD)
		{
			initialState = new BallState();
			initialVelValid = false;
		}
		Matrix newState = dynamicsInternal(currentState, initialState.getStateMatrix(), dt);
		return new BallState(newState);
	}
	
	
	/**
	 * @param state
	 * @param initialState
	 * @param dt
	 * @return
	 */
	private Matrix dynamicsInternal(final Matrix state, final Matrix initialState, final double dt)
	{
		IVector2 dir = Vector2.fromXY(state.get(3, 0), state.get(4, 0)).normalizeNew();
		double vel = sqrt(pow(state.get(3, 0), 2) + pow(state.get(4, 0), 2));
		Matrix curState = state.copy();
		
		
		if ((state.get(2, 0) > 0) || (state.get(5, 0) > 0))
		{
			// flying ball, no acceleration on xy-plane
			curState.set(6, 0, 0); // ax = 0
			curState.set(7, 0, 0); // ay = 0
			curState.set(8, 0, A_FALL); // az = A_FALL
		} else
		{
			if ((Math.abs(state.get(5, 0)) + (A_FALL * dt)) >= 0)
			{
				curState.set(2, 0, 0); // z = 0
				curState.set(5, 0, -BALL_DAMP_GROUND_FACTOR); // vz
				curState.set(8, 0, A_FALL); // az
			} else
			{
				curState.set(2, 0, 0); // z = 0
				curState.set(5, 0, 0); // vz = 0
				curState.set(8, 0, 0); // az = 0
			}
			
			double currentAcc = 0.0;
			if (vel > BALL_HAS_STOPPED_THRESHOLD)
			{
				if (isBallRolling(state, initialState))
				{
					currentAcc = acc;
				} else
				{
					currentAcc = accSlide;
				}
			}
			
			curState.set(6, 0, currentAcc * dir.x()); // ax = acc * cos(phi)
			curState.set(7, 0, currentAcc * dir.y()); // ay = acc * sin(phi)
		}
		
		Matrix stateTransition = calculateStateTransitionMatrix(dt);
		
		return stateTransition.times(curState);
	}
	
	
	private Matrix calculateStateTransitionMatrix(final double dt)
	{
		double[][] transitionArray = {
				{ 1, 0, 0, dt, 0, 0, 0.5 * square(dt), 0, 0, 0 },
				{ 0, 1, 0, 0, dt, 0, 0, 0.5 * square(dt), 0, 0 },
				{ 0, 0, 1, 0, 0, dt, 0, 0, 0.5 * square(dt), 0 },
				{ 0, 0, 0, 1, 0, 0, dt, 0, 0, 0 },
				{ 0, 0, 0, 0, 1, 0, 0, dt, 0, 0 },
				{ 0, 0, 0, 0, 0, 1, 0, 0, dt, 0 },
				{ 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 } };
		
		return new Matrix(transitionArray);
	}
	
	
	private boolean isBallRolling(final Matrix currentState, final Matrix initialState)
	{
		return calculateTSwitch(currentState, initialState) <= 0;
	}
	
	
	private double calculateTSwitch(final Matrix currentState, final Matrix initialState)
	{
		double vc = sqrt(square(currentState.get(3, 0)) + square(currentState.get(4, 0)));
		double vi = sqrt(square(initialState.get(3, 0)) + square(initialState.get(4, 0)));
		return ((cSwitch * vi) - vc) / accSlide;
	}
}
