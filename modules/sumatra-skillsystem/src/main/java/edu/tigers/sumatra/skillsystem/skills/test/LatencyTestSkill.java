/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.math.vector.AVector;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;


/**
 * Measure latency
 */
@Log4j2
public class LatencyTestSkill extends AMoveSkill
{
	@Configurable(comment = "Number of samples to take for calibration", defValue = "100")
	private static int maxSamples = 100;
	@Configurable(comment = "Number of measurements to do", defValue = "5")
	private static int maxMeasures = 5;

	private IVector2 tolerance = Vector2f.ZERO_VECTOR;
	private IVector2 initPos;
	private int numMeasures = 0;

	private enum EEvent implements IEvent
	{
		CALIBRATION_DONE,
		MEASURE_DONE,
		STOP_DONE,
		DONE
	}


	public LatencyTestSkill()
	{
		IState calState = new CalibrateState();
		IState measureState = new MeasureState();
		IState stopState = new StopState();
		IState doneState = new DoneState();
		setInitialState(calState);
		addTransition(calState, EEvent.CALIBRATION_DONE, measureState);
		addTransition(measureState, EEvent.MEASURE_DONE, stopState);
		addTransition(stopState, EEvent.STOP_DONE, calState);
		addTransition(doneState, EEvent.DONE, doneState);
	}


	private class CalibrateState extends AState
	{
		private int frameCounter = 0;
		private final List<IVector> posSamples = new ArrayList<>(maxSamples);


		@Override
		public void doEntryActions()
		{
			frameCounter = 0;
			posSamples.clear();

			setMotorsOff();
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


	}

	private class MeasureState extends AState
	{
		private long startTime;
		private int numFrames = 0;
		private final List<Double> times = new ArrayList<>(maxMeasures);


		@Override
		public void doEntryActions()
		{
			setGlobalVelocity(Vector2.fromXY(5, 0), 0, defaultMoveConstraints());
			startTime = getWorldFrame().getTimestamp();
			numFrames = 0;
		}


		@Override
		public void doUpdate()
		{
			numFrames++;
			IVector2 diff = getPos().subtractNew(initPos).absNew();
			IVector2 diffTol = tolerance.subtractNew(diff);
			if ((diffTol.x() < 0) || (diffTol.y() < 0))
			{
				double timeDiff = (double) getWorldFrame().getTimestamp() - startTime;
				times.add(timeDiff);
				log.info("Measure took " + (timeDiff / 1e6) + "ms, " + numFrames + " frames");
				numMeasures++;
				if (numMeasures < maxMeasures)
				{
					triggerEvent(EEvent.MEASURE_DONE);
				} else
				{
					setMotorsOff();
					double mean = StatisticsMath.mean(times);
					double std = StatisticsMath.std(times);
					log.info("Mean: " + (mean / 1e6) + "ms");
					log.info("Std: " + (std / 1e6) + "ms");
					triggerEvent(EEvent.DONE);
				}
			}
		}
	}

	private class StopState extends AState
	{
		public static final long TIME_TO_WAIT_MS = 500;
		private IVector2 lastPos;
		private long startTime = 0;


		@Override
		public void doEntryActions()
		{
			lastPos = getPos();
			startTime = 0;
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
			setMotorsOff();
		}
	}


	private class DoneState extends AState
	{
		@Override
		public void doEntryActions()
		{
			setMotorsOff();
		}
	}
}
