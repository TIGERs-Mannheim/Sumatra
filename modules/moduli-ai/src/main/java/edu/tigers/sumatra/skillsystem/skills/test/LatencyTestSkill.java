/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 17, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.math.AVector;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.ABaseDriver;
import edu.tigers.sumatra.skillsystem.driver.DoNothingDriver;
import edu.tigers.sumatra.skillsystem.driver.EPathDriver;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Measue latency
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LatencyTestSkill extends AMoveSkill
{
	private static final Logger	log			= Logger.getLogger(LatencyTestSkill.class.getName());
															
	private IVector2					tolerance	= Vector2.ZERO_VECTOR;
	private IVector2					initPos;
	private final Vector3			velocity		= new Vector3();
	private int							numMeasures	= 0;
															
	@Configurable(comment = "Difference from cur pos to dest to execute")
	private static IVector2			moveDiff		= new Vector2(500, 0);
															
	@Configurable(comment = "Number of samples to take for calibration")
	private static int				maxSamples	= 100;
	@Configurable(comment = "Number of measurements to do")
	private static int				maxMeasures	= 5;
															
	private enum EStateId
	{
		CALIBRATE,
		MEASURE,
		STOP,
		DONE
	}
	
	private enum EEvent
	{
		CALIBRATION_DONE,
		MEASURE_DONE,
		STOP_DONE,
		DONE
	}
	
	
	/**
	 * 
	 */
	public LatencyTestSkill()
	{
		super(ESkill.LATENCY_TEST);
		IState calState = new CalibrateState();
		IState measureState = new MeasureState();
		IState stopState = new StopState();
		IState doneState = new DoneState();
		setInitialState(calState);
		addTransition(EStateId.CALIBRATE, EEvent.CALIBRATION_DONE, measureState);
		addTransition(EStateId.MEASURE, EEvent.MEASURE_DONE, stopState);
		addTransition(EStateId.STOP, EEvent.STOP_DONE, calState);
		addTransition(EStateId.MEASURE, EEvent.DONE, doneState);
		setPathDriver(new MyPathDriver());
	}
	
	
	private class CalibrateState implements IState
	{
		private int							frameCounter	= 0;
		private final List<IVector>	posSamples		= new ArrayList<>(maxSamples);
																	
																	
		@Override
		public void doEntryActions()
		{
			frameCounter = 0;
			posSamples.clear();
			
			velocity.set(AVector3.ZERO_VECTOR);
		}
		
		
		@Override
		public void doExitActions()
		{
		
		}
		
		
		@Override
		public void doUpdate()
		{
			if (frameCounter > maxSamples)
			{
				initPos = AVector.meanVector(posSamples).getXYVector();
				IVector std = AVector.stdVector(posSamples);
				// tolerance within 3*std == 99.7%
				tolerance = std.multiplyNew(3).getXYVector();
				log.info("Tolerance=[" + std.x() + "," + std.y() + "]");
				triggerEvent(EEvent.CALIBRATION_DONE);
			}
			posSamples.add(getPos());
			frameCounter++;
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.CALIBRATE;
		}
	}
	
	private class MeasureState implements IState
	{
		private long					startTime;
		private int						numFrames	= 0;
		private final List<Double>	times			= new ArrayList<Double>(maxMeasures);
															
															
		@Override
		public void doEntryActions()
		{
			velocity.set(new Vector3(5, 0, 0));
			startTime = getWorldFrame().getTimestamp();
			numFrames = 0;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			numFrames++;
			IVector2 diff = getPos().subtractNew(initPos).absNew();
			IVector2 diffTol = tolerance.subtractNew(diff);
			if ((diffTol.x() < 0) || (diffTol.y() < 0))
			{
				long timeDiff = getWorldFrame().getTimestamp() - startTime;
				times.add(Double.valueOf(timeDiff));
				log.info("Measure took " + (timeDiff / 1e6) + "ms, " + numFrames + " frames");
				numMeasures++;
				if (numMeasures < maxMeasures)
				{
					triggerEvent(EEvent.MEASURE_DONE);
				} else
				{
					velocity.set(AVector3.ZERO_VECTOR);
					double mean = SumatraMath.mean(times);
					double std = SumatraMath.std(times);
					log.info("Mean: " + (mean / 1e6) + "ms");
					log.info("Std: " + (std / 1e6) + "ms");
					triggerEvent(EEvent.DONE);
				}
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MEASURE;
		}
	}
	
	private class StopState implements IState
	{
		public static final long	TIME_TO_WAIT_MS	= 500;
		private IVector2				lastPos;
		private long					startTime			= 0;
																	
																	
		@Override
		public void doEntryActions()
		{
			lastPos = getPos();
			startTime = 0;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 diffRem = lastPos.subtractNew(getPos()).absNew().subtractNew(tolerance);
			if ((diffRem.x() <= 0) || (diffRem.y() <= 0))
			{
				if (startTime == 0)
				{
					startTime = getWorldFrame().getTimestamp();
				}
				double diffTime = (getWorldFrame().getTimestamp() - startTime) * 1e-6f;
				if (diffTime > TIME_TO_WAIT_MS)
				{
					triggerEvent(EEvent.STOP_DONE);
				}
			} else
			{
				startTime = 0;
			}
			lastPos = getPos();
			velocity.set(AVector3.ZERO_VECTOR);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.STOP;
		}
	}
	
	
	private class MyPathDriver extends ABaseDriver
	{
		
		/**
		 * 
		 */
		public MyPathDriver()
		{
			addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
		}
		
		
		@Override
		public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
		{
			return new Vector3(bot.getPos(), bot.getAngle());
		}
		
		
		@Override
		public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
		{
			return velocity;
		}
		
		
		@Override
		public EPathDriver getType()
		{
			return EPathDriver.LATENCY;
		}
	}
	
	private class DoneState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			setPathDriver(new DoNothingDriver());
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
		public Enum<?> getIdentifier()
		{
			return EStateId.DONE;
		}
	}
}
