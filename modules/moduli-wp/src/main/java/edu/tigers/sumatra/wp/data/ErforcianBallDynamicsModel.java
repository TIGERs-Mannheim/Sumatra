/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - TIGERs Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import Jama.Matrix;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Arne Sachtler <arne.sachtler@dlr.de>
 */
public class ErforcianBallDynamicsModel
{
	/** [mm/s²] */
	private final double				acc;
	private double						accSlide;
											
	private static final double	A_FALL						= -9810;
	private static final double	BALL_DAMP_GROUND_FACTOR	= 0.37;
																			
	/* switch constant for slide --> roll friction transition */
	private static final double	C_SWITCH						= 0.6;
																			
	/* acceleration constants */
	private static final double	A_SLIDE						= -2500;
	private static final double	A_ROLL						= -600;
																			
																			
	/**
	 * 
	 */
	public ErforcianBallDynamicsModel()
	{
		this(A_SLIDE, A_ROLL);
	}
	
	
	/**
	 * @param accSlide
	 * @param accRoll
	 */
	public ErforcianBallDynamicsModel(final double accSlide, final double accRoll)
	{
		this.accSlide = accSlide;
		acc = accRoll;
	}
	
	
	/**
	 * @param state
	 * @param initialState
	 * @param dt
	 * @param context
	 * @return
	 */
	public Matrix dynamics(final Matrix state, final Matrix initialState, final double dt,
			final MotionContext context)
	{
		IVector2 dir = new Vector2(state.get(3, 0), state.get(4, 0)).normalizeNew();
		Matrix curState = state.copy();
		
		
		if ((state.get(2, 0) > 0) || (state.get(5, 0) > 0))
		{
			// flying ball, no acceleration on xy-plane
			curState.set(6, 0, 0); // ax = 0
			curState.set(7, 0, 0); // ay = 0
			curState.set(8, 0, A_FALL); // az = A_FALL
		} else
		{
			if ((SumatraMath.abs(state.get(5, 0)) + (A_FALL * dt)) >= 0)
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
			
			if (isBallRolling(state, initialState))
			{
				curState.set(6, 0, acc * dir.x()); // ax = acc * cos(phi)
				curState.set(7, 0, acc * dir.y()); // ay = acc * sin(phi)
			} else
			{
				curState.set(6, 0, accSlide * dir.x()); // ax = accSlide * cos(phi)
				curState.set(7, 0, accSlide * dir.y()); // ay = accSlide * sin(phi)
			}
		}
		
		Matrix stateTransition = calculateStateTransitionMatrix(dt);
		return stateTransition.times(curState);
	}
	
	
	private Matrix calculateStateTransitionMatrix(final double dt)
	{
		double[][] transitionArray = {
				{ 1, 0, 0, dt, 0, 0, 0.5 * SumatraMath.square(dt), 0, 0, 0 },
				{ 0, 1, 0, 0, dt, 0, 0, 0.5 * SumatraMath.square(dt), 0, 0 },
				{ 0, 0, 1, 0, 0, dt, 0, 0, 0.5 * SumatraMath.square(dt), 0 },
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
		double vc = SumatraMath
				.sqrt(SumatraMath.square(currentState.get(3, 0)) + SumatraMath.square(currentState.get(4, 0)));
		double vi = SumatraMath
				.sqrt(SumatraMath.square(initialState.get(3, 0)) + SumatraMath.square(initialState.get(4, 0)));
		return ((C_SWITCH * vi) - vc) / accSlide;
	}
}
