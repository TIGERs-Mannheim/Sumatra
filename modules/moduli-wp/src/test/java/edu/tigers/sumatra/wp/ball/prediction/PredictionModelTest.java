/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.prediction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.wp.ball.dynamics.BallAction;
import edu.tigers.sumatra.wp.ball.dynamics.BallDynamicsModelSimple;
import edu.tigers.sumatra.wp.ball.dynamics.BallDynamicsModelTwoPhase;
import edu.tigers.sumatra.wp.ball.dynamics.BallState;
import edu.tigers.sumatra.wp.ball.dynamics.IAction;
import edu.tigers.sumatra.wp.ball.dynamics.IBallDynamicsModel;
import edu.tigers.sumatra.wp.ball.dynamics.IState;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelBallTrajectory.TwoPhaseDynamicVelParameters;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelBallTrajectory.TwoPhaseFixedVelParameters;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelConsultant;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author ArneS <arne.sachtler@dlr.de>
 */
@RunWith(Parameterized.class)
public class PredictionModelTest
{
	
	private static final double DT = 1e-3;
	private static final double INTERVAL = 5.0;
	
	private static final double DISTANCE_TOLERANCE = 10.0;
	private static final double VELOCITY_TOLERANCE = 0.02;
	private static final double TIME_TOLERANCE = 0.01;
	private static IVector3 initialPos = Vector3.fromXYZ(0, 0, 0);
	private static IVector3 initialAcc = Vector3f.ZERO_VECTOR;
	private IBallDynamicsModel dynamicsModel;
	private IBallTrajectory predictionModel;
	private IState currentState;
	private IAction ballAction;
	private MotionContext context;
	private IStraightBallConsultant straightConsultant;
	
	// model under test
	private ETestableModel eTestableModel;
	
	// parametrized fields:
	private IVector3 initialVel;
	private IState initialState;
	
	
	public PredictionModelTest(final IVector3 initialVel, final ETestableModel eModel)
	{
		this.initialVel = initialVel;
		initialState = new BallState(initialPos, initialVel, initialAcc);
		this.eTestableModel = eModel;
	}
	
	
	@Parameterized.Parameters
	public static Collection<Object[]> testParameters()
	{
		return Arrays.asList(new Object[][] {
				{ Vector3.fromXYZ(7, 2, 0), ETestableModel.FIXED_VEL },
				{ Vector3.fromXYZ(8, 0, 0), ETestableModel.FIXED_VEL },
				{ Vector3.fromXYZ(0, -8, 0), ETestableModel.FIXED_VEL },
				{ Vector3.fromXYZ(7, 2, 0), ETestableModel.DYNAMIC_VEL },
				{ Vector3.fromXYZ(8, 0, 0), ETestableModel.DYNAMIC_VEL },
				{ Vector3.fromXYZ(0, -8, 0), ETestableModel.DYNAMIC_VEL }
		});
	}
	
	
	@Before
	public void setUp() throws Exception
	{
		dynamicsModel = eTestableModel.createDynamicsModel();
		currentState = new BallState(initialState);
		predictionModel = eTestableModel.createBallTrajectory(initialPos, initialVel);
		context = new MotionContext();
		ballAction = new BallAction(Vector3f.ZERO_VECTOR);
		straightConsultant = eTestableModel.createBallConsultant();
	}
	
	
	@Test
	public void testDistByTime()
	{
		for (double t = 0.0; t <= INTERVAL; t += DT)
		{
			double distPrediction = predictionModel.getDistByTime(t + DT);
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
			double distDynamicsModel = currentState.getPos().getLength();
			assertThat(distPrediction).isCloseTo(distDynamicsModel, within(DISTANCE_TOLERANCE));
		}
	}
	
	
	@Test
	public void testInitVelForTimeDist()
	{
		parametrizedInitVelForTimeDist(1e3, 1.0);
		parametrizedInitVelForTimeDist(1e3, 0.5);
		parametrizedInitVelForTimeDist(.5e3, 1.0);
		parametrizedInitVelForTimeDist(.5e3, 0.5);
		parametrizedInitVelForTimeDist(0, 0);
	}
	
	
	private void parametrizedInitVelForTimeDist(double distance, double time)
	{
		double calculatedInitialVel = straightConsultant.getInitVelForTimeDist(distance, time);
		IVector3 initialVel = this.initialVel.normalizeNew().multiplyNew(calculatedInitialVel);
		currentState = new BallState(initialPos, initialVel, initialAcc);
		for (double t = 0.0; t <= time; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		}
		double actualDistance = currentState.getPos().subtractNew(initialPos).getLength2();
		do
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		} while (currentState.getVel().getLength2() > 1e-3);
		currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		assertThat(actualDistance).isCloseTo(distance, within(DISTANCE_TOLERANCE));
	}
	
	
	@Test
	public void testInitVelForTime()
	{
		parametrizedTestInitVelForTime(2.5, 0.5);
		parametrizedTestInitVelForTime(2.5, 1.0);
		parametrizedTestInitVelForTime(0.0, 1.0);
		parametrizedTestInitVelForTime(1.5, 3.0);
		parametrizedTestInitVelForTime(0.0, 10.0);
		parametrizedTestInitVelForTime(0.0, 0.0);
	}
	
	
	private void parametrizedTestInitVelForTime(final double endVel, final double time)
	{
		double calculatedInitialVelForTime = straightConsultant.getInitVelForTime(endVel, time);
		IVector3 initialVel = this.initialVel.normalizeNew().multiplyNew(calculatedInitialVelForTime);
		currentState = new BallState(initialPos, initialVel, initialAcc);
		for (double t = 0.0; t <= time; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		}
		double actualVel = currentState.getVel().getLength2();
		do
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		} while (currentState.getVel().getLength2() > 1e-3);
		currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		assertThat(actualVel).isCloseTo(endVel, within(VELOCITY_TOLERANCE));
		
	}
	
	
	@Test
	public void testTimeByVel()
	{
		for (double t = 0.0; t <= INTERVAL; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
			double predictedTime = predictionModel.getTimeByVel(currentState.getVel().getLength());
			assertEquals(t, predictedTime, TIME_TOLERANCE);
		}
	}
	
	
	@Test
	public void testTimeByDist()
	{
		for (double t = 0.0; t <= INTERVAL; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
			double currentDistance = currentState.getPos().getLength2();
			double predictedTime = predictionModel.getTimeByDist(currentDistance);
			assertEquals(t, predictedTime, TIME_TOLERANCE);
		}
	}
	
	
	@Test
	public void testTimeByPos()
	{
		for (double t = 0.0; t <= INTERVAL; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
			IVector3 currentPos = currentState.getPos();
			double predictedTime = predictionModel.getTimeByPos(currentPos.getXYVector());
			assertEquals(t, predictedTime, TIME_TOLERANCE);
		}
	}
	
	
	@Test
	public void testVelByPos()
	{
		for (double t = 0.0; t <= INTERVAL; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
			IVector3 currentPos = currentState.getPos();
			double currentVel = currentState.getVel().getLength2();
			double predictedVel = predictionModel.getAbsVelByPos(currentPos.getXYVector());
			assertEquals(currentVel, predictedVel, VELOCITY_TOLERANCE);
		}
	}
	
	
	@Test
	public void testVelByTime()
	{
		for (double t = 0.0; t <= INTERVAL; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
			double currentVel = currentState.getVel().getLength2();
			double predictedVel = predictionModel.getAbsVelByTime(t + DT);
			assertEquals(currentVel, predictedVel, VELOCITY_TOLERANCE);
		}
	}
	
	
	@Test
	public void testVelByDist()
	{
		for (double t = 0.0; t <= INTERVAL; t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
			double currentDistance = currentState.getPos().getLength2();
			double predictedVel = predictionModel.getAbsVelByDist(currentDistance);
			assertEquals(currentState.getVel().getLength2(), predictedVel, VELOCITY_TOLERANCE);
		}
		
	}
	
	
	@Test
	public void testInitVelForDist()
	{
		parametrizedInitVelForDist(10e3, 0.0);
		parametrizedInitVelForDist(10e3, 1.0);
		parametrizedInitVelForDist(10e3, 2.5);
		parametrizedInitVelForDist(1000, 0.0);
		parametrizedInitVelForDist(0, 0);
	}
	
	
	private void parametrizedInitVelForDist(final double distance, final double endVel)
	{
		double predictedInitVel = straightConsultant.getInitVelForDist(distance, endVel);
		IState predictedInitialState = new BallState(Vector3f.ZERO_VECTOR,
				initialVel.normalizeNew().multiplyNew(predictedInitVel), Vector3f.ZERO_VECTOR);
		IState updatedState = new BallState(predictedInitialState);
		while (updatedState.getVel().getLength2() > (endVel + 1e-3))
		{
			updatedState = dynamicsModel.dynamics(updatedState, ballAction, DT, context);
		}
		double actualDist = updatedState.getPos().getLength2();
		do
		{
			updatedState = dynamicsModel.dynamics(updatedState, ballAction, DT, context);
		} while (updatedState.getVel().getLength2() > 1e-3);
		dynamicsModel.dynamics(updatedState, ballAction, DT, context);
		assertEquals(distance, actualDist, 5 * DISTANCE_TOLERANCE);
	}
	
	
	@Test
	public void testTimeForKick()
	{
		parametrizedTimeForKick(0, 0);
		parametrizedTimeForKick(1e3, 8.0);
		parametrizedTimeForKick(8e3, 6.0);
		parametrizedTimeForKick(1e3, 1.8);
		parametrizedTimeForKick(0, 1.9);
		parametrizedTimeForKick(0, 8);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testTimeForKickIllegalArgument()
	{
		parametrizedTimeForKick(1e3, 0.0);
	}
	
	
	private void parametrizedTimeForKick(final double distance, final double kickVel)
	{
		double predictedTime = straightConsultant.getTimeForKick(distance, kickVel);
		if (!Double.isFinite(predictedTime))
		{
			throw new IllegalArgumentException();
		}
		IVector3 initialVel = this.initialVel.normalizeNew().multiplyNew(kickVel);
		IState currentState = new BallState(initialPos, initialVel, initialAcc);
		for (double t = 0.0; t <= Math.min(20, predictedTime); t += DT)
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		}
		double actualDist = currentState.getPos().subtractNew(initialPos).getLength2();
		do
		{
			currentState = dynamicsModel.dynamics(currentState, ballAction, DT, context);
		} while (currentState.getVel().getLength2() > 1e-3);
		dynamicsModel.dynamics(currentState, ballAction, DT, context);
		assertThat(actualDist).isCloseTo(distance, within(DISTANCE_TOLERANCE));
	}
	
	protected enum ETestableModel
	{
		FIXED_VEL(BallDynamicsModelSimple.class,
				TwoPhaseFixedVelBallTrajectory.class,
				TwoPhaseFixedVelConsultant.class,
				TwoPhaseFixedVelParameters.class),
		DYNAMIC_VEL(BallDynamicsModelTwoPhase.class,
				TwoPhaseDynamicVelBallTrajectory.class,
				TwoPhaseDynamicVelConsultant.class,
				TwoPhaseDynamicVelParameters.class);
		
		private Class<? extends IBallDynamicsModel> dynamicsModelClass;
		private Class<? extends IBallTrajectory> ballTrajectoryClass;
		private Class<? extends IStraightBallConsultant> straightBallConsultantClass;
		private Class<?> trajParamsClass;
		
		private Object params;
		
		
		ETestableModel(Class<? extends IBallDynamicsModel> dynamicsModelClass,
				Class<? extends IBallTrajectory> ballTrajectoryClass,
				Class<? extends IStraightBallConsultant> straightBallConsultantClass, Class<?> trajParamsClass)
		{
			this.dynamicsModelClass = dynamicsModelClass;
			this.ballTrajectoryClass = ballTrajectoryClass;
			this.straightBallConsultantClass = straightBallConsultantClass;
			this.trajParamsClass = trajParamsClass;
			
			try
			{
				params = trajParamsClass.getConstructor().newInstance();
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e)
			{
				e.printStackTrace();
				params = new TwoPhaseFixedVelParameters();
			}
		}
		
		
		public IBallDynamicsModel createDynamicsModel()
		{
			IBallDynamicsModel dynamicsModel = null;
			try
			{
				dynamicsModel = dynamicsModelClass.getConstructor().newInstance();
			} catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
			return dynamicsModel;
		}
		
		
		public IBallTrajectory createBallTrajectory(IVector3 initialPos, IVector3 initialVel) throws Exception
		{
			IBallTrajectory trajectory;
			try
			{
				Method fromKick = ballTrajectoryClass
						.getMethod("fromKick", IVector2.class, IVector3.class, trajParamsClass);
				trajectory = (IBallTrajectory) fromKick.invoke(null, initialPos.getXYVector(),
						initialVel.multiplyNew(1e3), params);
			} catch (NoSuchMethodException e)
			{
				Method fromKick = ballTrajectoryClass
						.getMethod("fromKick", IVector2.class, IVector2.class, trajParamsClass);
				trajectory = (IBallTrajectory) fromKick.invoke(null, initialPos.getXYVector(),
						initialVel.getXYVector().multiplyNew(1e3), params);
			}
			return trajectory;
		}
		
		
		public IStraightBallConsultant createBallConsultant()
		{
			IStraightBallConsultant consultant = null;
			try
			{
				consultant = straightBallConsultantClass.getConstructor(trajParamsClass).newInstance(params);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
			return consultant;
		}
	}
}
