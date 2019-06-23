/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.BallMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Ignore
public class LearnedBallModelTest
{
	static
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(LearnedBallModelTest.class.getName());
	
	
	private IVector2 getPosFromMatrix(final Matrix state)
	{
		return new Vector2(state.get(0, 0), state.get(1, 0));
	}
	
	
	private IVector2 getVelFromMatrix(final Matrix state)
	{
		return new Vector2(state.get(3, 0) / 1000, state.get(4, 0) / 1000);
	}
	
	
	private Matrix getState(final IVector2 pos, final IVector2 vel)
	{
		return new Matrix(new double[] { pos.x(), pos.y(), 0, vel.x() * 1000, vel.y() * 1000, 0, 0, 0, 0 }, 9);
	}
	
	
	private float simulate(final Matrix initState, final Matrix finalState, final float maxt)
	{
		PredictionContext context = new PredictionContext();
		BallMotionModel model = new BallMotionModel(context);
		float dt = 0.016f;
		float refTime = 0;
		Matrix state = initState;
		while ((Math.sqrt((state.get(3, 0) * state.get(3, 0)) + (state.get(4, 0) * state.get(4, 0))) > 0)
				&& (refTime <= maxt))
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		finalState.setMatrix(0, 8, 0, 0, state);
		return refTime;
	}
	
	
	/**
	 */
	@Test
	public void testgetPosByTime()
	{
		IVector2 pos = new Vector2(100, 200);
		IVector2 vel = new Vector2(-1, 0.5);
		float time = 1.5f;
		IVector2 dest = AIConfig.getBallModel().getPosByTime(pos, vel, time);
		float dist = GeoMath.distancePP(pos, dest);
		float ref = AIConfig.getBallModel().getDistByTime(vel.getLength2(), time);
		Assert.assertTrue(SumatraMath.isEqual(ref, dist));
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetDistByTime()
	{
		float time = 1.6f;
		float vel = 2.0f;
		Matrix initState = getState(new Vector2(1000, 2000), new Vector2(1, 1).scaleTo(vel));
		Matrix finalState = initState.copy();
		simulate(initState, finalState, time);
		IVector2 initPos = getPosFromMatrix(initState);
		IVector2 finalPos = getPosFromMatrix(finalState);
		float refDist = GeoMath.distancePP(initPos, finalPos);
		
		float dist = AIConfig.getBallModel().getDistByTime(vel, time);
		Assert.assertEquals(refDist, dist, 50);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetPosByVel()
	{
		IVector2 currentPos = new Vector2(1000, 2000);
		IVector2 currentVel = new Vector2(0.5f, 1.1f);
		float velocity = currentVel.getLength2() - 0.5f;
		Matrix initState = getState(currentPos, currentVel);
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		Matrix state = initState;
		float refTime = 0;
		while ((Math.sqrt((state.get(3, 0) * state.get(3, 0)) + (state.get(4, 0) * state.get(4, 0))) > (velocity * 1000)))
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		IVector2 pos = AIConfig.getBallModel().getPosByVel(currentPos, currentVel, velocity);
		Assert.assertEquals(0, GeoMath.distancePP(pos, getPosFromMatrix(state)), 50);
		Assert.assertEquals(velocity, getVelFromMatrix(state).getLength2(), 0.01);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetDistByVel()
	{
		float currentVel = 1.5f;
		float velocity = 0.0f;
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		Matrix state = initState;
		float refTime = 0;
		while ((Math.sqrt((state.get(3, 0) * state.get(3, 0)) + (state.get(4, 0) * state.get(4, 0))) > (velocity * 1000)))
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		float dist = AIConfig.getBallModel().getDistByVel(currentVel, velocity);
		float refDist = getPosFromMatrix(state).x();
		Assert.assertEquals(refDist, dist, 50);
		Assert.assertEquals(velocity, getVelFromMatrix(state).getLength2(), 0.01);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetTimeByDist()
	{
		float currentVel = 1.5f;
		float dist = 1000;
		float time = AIConfig.getBallModel().getTimeByDist(currentVel, dist);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		Matrix state = initState;
		float refTime = 0;
		while (getPosFromMatrix(state).x() < dist)
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(refTime, time, dt);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetTimeByVel()
	{
		float currentVel = 1.5f;
		float velocity = 1.0f;
		float time = AIConfig.getBallModel().getTimeByVel(currentVel, velocity);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		Matrix state = initState;
		float refTime = 0;
		while (getVelFromMatrix(state).getLength2() > velocity)
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(refTime, time, dt);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelByDist()
	{
		float currentVel = 1.5f;
		float dist = 1000;
		float vel = AIConfig.getBallModel().getVelByDist(currentVel, dist);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		Matrix state = initState;
		float time = 0;
		while (getPosFromMatrix(state).getLength2() < dist)
		{
			state = model.dynamics(state, null, dt);
			time += dt;
			if (time > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(getVelFromMatrix(state).getLength2(), vel, 0.1f);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelByTime()
	{
		float currentVel = 1.5f;
		float time = 1.0f;
		float vel = AIConfig.getBallModel().getVelByTime(currentVel, time);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		float refTime = 0;
		Matrix state = initState;
		while (refTime < time)
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(getVelFromMatrix(state).getLength2(), vel, 0.1f);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelForTime()
	{
		float endVel = 1.1f;
		float time = 1.5f;
		float vel = AIConfig.getBallModel().getVelForTime(endVel, time);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(vel, 0));
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		float refTime = 0;
		Matrix state = initState;
		while (getVelFromMatrix(state).getLength2() > endVel)
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(time, refTime, dt);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelForDist()
	{
		float endVel = 1.1f;
		float dist = 2000;
		float vel = AIConfig.getBallModel().getVelForDist(dist, endVel);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(vel, 0));
		BallMotionModel model = new BallMotionModel(new PredictionContext());
		float dt = 0.016f;
		Matrix state = initState;
		float refTime = 0;
		while (getVelFromMatrix(state).x() > endVel)
		{
			state = model.dynamics(state, null, dt);
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(dist, getPosFromMatrix(state).x(), 50);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetAcc()
	{
	}
}
