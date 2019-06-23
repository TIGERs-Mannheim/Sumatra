/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 17, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.ABaseDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.EventStatePair;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.StateMachine;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Measue latency
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LatencyTestSkill extends AMoveSkill
{
	private static final Logger			log				= Logger.getLogger(LatencyTestSkill.class.getName());
	private final StateMachine<IState>	stateMachine	= new StateMachine<>();
	
	private IVector2							tolerance		= Vector2.ZERO_VECTOR;
	private IVector2							initPos;
	private Vector3							velocity			= new Vector3();
	private int									numMeasures		= 0;
	
	@Configurable(comment = "Difference from cur pos to dest to execute")
	private static IVector2					moveDiff			= new Vector2(500, 0);
	
	@Configurable(comment = "Number of samples to take for calibration")
	private static int						maxSamples		= 100;
	@Configurable(comment = "Number of measurements to do")
	private static int						maxMeasures		= 5;
	
	private enum EStateId
	{
		CALIBRATE,
		MEASURE,
		STOP
	}
	
	private enum EEvent
	{
		CALIBRATION_DONE,
		MEASURE_DONE,
		STOP_DONE
	}
	
	
	/**
	 * 
	 */
	public LatencyTestSkill()
	{
		super(ESkillName.LATENCY_TEST);
		IState calState = new CalibrateState();
		IState measureState = new MeasureState();
		IState stopState = new StopState();
		stateMachine.setInitialState(calState);
		stateMachine.addTransition(new EventStatePair(EEvent.CALIBRATION_DONE, EStateId.CALIBRATE), measureState);
		stateMachine.addTransition(new EventStatePair(EEvent.MEASURE_DONE, EStateId.MEASURE), stopState);
		stateMachine.addTransition(new EventStatePair(EEvent.STOP_DONE, EStateId.STOP), calState);
		setCommandType(ECommandType.VEL);
		setPathDriver(new MyPathDriver());
	}
	
	
	private class CalibrateState implements IState
	{
		private int					frameCounter	= 0;
		private List<IVector2>	posSamples		= new ArrayList<>(maxSamples);
		
		
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
				initPos = SumatraMath.meanVector(posSamples);
				IVector2 std = SumatraMath.stdVector(posSamples);
				// tolerance within 3*std == 99.7%
				tolerance = std.multiplyNew(3);
				log.info("Tolerance=[" + std.x() + "," + std.y() + "]");
				stateMachine.triggerEvent(EEvent.CALIBRATION_DONE);
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
		private long			startTime;
		private int				numFrames	= 0;
		private List<Float>	times			= new ArrayList<Float>(maxMeasures);
		
		
		@Override
		public void doEntryActions()
		{
			velocity.set(new Vector3(5, 0, 0));
			startTime = SumatraClock.nanoTime();
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
				long timeDiff = SumatraClock.nanoTime() - startTime;
				times.add(Float.valueOf(timeDiff));
				log.info("Measure took " + (timeDiff / 1e6) + "ms, " + numFrames + " frames");
				numMeasures++;
				if (numMeasures < maxMeasures)
				{
					stateMachine.triggerEvent(EEvent.MEASURE_DONE);
				} else
				{
					velocity.set(AVector3.ZERO_VECTOR);
					float mean = SumatraMath.mean(times);
					float std = SumatraMath.std(times);
					log.info("Mean: " + (mean / 1e6) + "ms");
					log.info("Std: " + (std / 1e6) + "ms");
					complete();
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
					startTime = SumatraClock.nanoTime();
				}
				float diffTime = (SumatraClock.nanoTime() - startTime) * 1e-6f;
				if (diffTime > TIME_TO_WAIT_MS)
				{
					stateMachine.triggerEvent(EEvent.STOP_DONE);
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
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		stateMachine.update();
	}
	
	private class MyPathDriver extends ABaseDriver
	{
		
		/**
		 * 
		 */
		public MyPathDriver()
		{
			addSupportedCommand(ECommandType.VEL);
		}
		
		
		@Override
		public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			return new Vector3(bot.getPos(), bot.getAngle());
		}
		
		
		@Override
		public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			return velocity;
		}
		
		
		@Override
		public void setMovingSpeed(final EMovingSpeed speed)
		{
		}
		
		
		@Override
		public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
		{
			
		}
		
		
		@Override
		public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
		{
		}
		
		
		@Override
		public EPathDriver getType()
		{
			return EPathDriver.LATENCY;
		}
	}
	
	
	@Override
	protected void doCalcEntryActions(final List<ACommand> cmds)
	{
	}
	
	
	@Override
	protected void doCalcExitActions(final List<ACommand> cmds)
	{
	}
}
