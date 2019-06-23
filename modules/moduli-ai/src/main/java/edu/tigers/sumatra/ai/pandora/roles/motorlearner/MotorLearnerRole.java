/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector.ADataCollector;
import edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector.EDataCollector;
import edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector.LocalVelByStartEndCamDataCollector;
import edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector.LocalVelCamSimpleDataCollector;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.control.motor.AMotorModel;
import edu.tigers.sumatra.control.motor.EMotorModel;
import edu.tigers.sumatra.control.motor.IMotorModel;
import edu.tigers.sumatra.control.motor.InterpolationMotorModel;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.export.INumberListable;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.math.VectorN;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.ml.model.motor.IMotorSampler;
import edu.tigers.sumatra.ml.model.motor.MotorModelOptimizer;
import edu.tigers.sumatra.ml.model.motor.MotorModelOptimizer.SampleError;
import edu.tigers.sumatra.sampler.velocity.EVelocitySampler;
import edu.tigers.sumatra.sampler.velocity.IVelocityXywSampler;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.statistics.VectorDataStatistics;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.data.Geometry;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MotorLearnerRole extends ARole
{
	@SuppressWarnings("unused")
	private static final Logger						log							= Logger.getLogger(MotorLearnerRole.class
			.getName());
	
	@Configurable
	private static double								maxSampleTime				= 3;
	
	@Configurable
	private static boolean								exportSamples				= false;
	
	@Configurable
	private static boolean								printSamples				= false;
	
	@Configurable
	private static boolean								accelerateXywVelocity	= false;
	
	@Configurable
	private static boolean								sampleUntilConvergence	= false;
	
	@Configurable
	private static boolean								wheelVel						= false;
	
	private IVector3										initPos;
	private IVector3										targetVel					= Vector3.ZERO_VECTOR;
	private final double									sampleTime;
	private final double									delayTime;
	private final double									acc;
	private double											initPosAngleOffset		= 0;
	private int												samplesRepeated			= 0;
	
	private double[]										input							= new double[4];
	
	private final ExecutorService						execService					= Executors
			.newSingleThreadExecutor(
					new NamedThreadFactory(
							"MotorLearner"));
	private final Sampler								sampler						= new Sampler();
	
	
	private final List<ADataCollector<IVector>>	collectors					= new ArrayList<>();
	private final List<EDataCollector>				collectorsToUse			= new ArrayList<>(3);
	
	
	private EMotorOptimizer								optimizerType;
	
	private EMotorModel									motorModelType;
	private EVelocitySampler							velSamplerType;
	private double											motorNoise					= 0;
	private int												numSamples;
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public enum EMotorOptimizer
	{
		/**  */
		LM,
		/**  */
		SIMPLEX,
		/**  */
		CMAES,
		/**  */
		POWELL,
		/**  */
		BOBYQA,
		/** */
		CLEVER_ONE,
	}
	
	
	/**
	 * @param initPos
	 * @param sampleTime
	 * @param delayTime
	 * @param acc
	 */
	private MotorLearnerRole(
			final IVector3 initPos,
			final double sampleTime,
			final double delayTime,
			final double acc)
	{
		super(ERole.MOTOR_LEARNER);
		this.initPos = initPos;
		this.sampleTime = sampleTime;
		this.delayTime = delayTime;
		this.acc = acc;
		
		IRoleState initState = new InitState();
		IRoleState sampleState = new SampleState();
		IRoleState accState = new AccelerationState();
		
		setInitialState(initState);
		addTransition(initState, EEvent.NEXT, accState);
		addTransition(accState, EEvent.NEXT, sampleState);
		addTransition(sampleState, EEvent.NEXT, initState);
		
	}
	
	
	/**
	 * learn
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param initPos
	 * @param targetVel
	 * @param sampleTime
	 * @param delayTime
	 * @param acc
	 * @param optimizerType
	 */
	public MotorLearnerRole(
			final IVector3 initPos,
			final IVector3 targetVel,
			final double sampleTime,
			final double delayTime,
			final double acc,
			final EMotorOptimizer optimizerType)
	{
		this(initPos, sampleTime, delayTime, acc);
		this.targetVel = targetVel;
		this.optimizerType = optimizerType;
		
		Runnable run;
		switch (optimizerType)
		{
			case BOBYQA:
			case CMAES:
			case LM:
			case POWELL:
			case SIMPLEX:
			case CLEVER_ONE:
				run = new Optimizer();
				break;
			default:
				throw new IllegalStateException();
		}
		
		execService.execute(run);
	}
	
	
	/**
	 * sample
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param initPos
	 * @param sampleTime
	 * @param delayTime
	 * @param acc
	 * @param motorNoise
	 * @param motorModelType
	 * @param velSamplerType
	 */
	public MotorLearnerRole(
			final IVector3 initPos,
			final double sampleTime,
			final double delayTime,
			final double acc,
			final double motorNoise,
			final EMotorModel motorModelType,
			final EVelocitySampler velSamplerType)
	{
		this(initPos, sampleTime, delayTime, acc);
		this.motorModelType = motorModelType;
		this.velSamplerType = velSamplerType;
		this.motorNoise = motorNoise;
		
		execService.execute(new FullSampleCollector());
	}
	
	
	/**
	 * full optimizer
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param initPos
	 * @param sampleTime
	 * @param delayTime
	 * @param acc
	 * @param motorModelType
	 * @param velSamplerType
	 * @param optimizerType
	 */
	public MotorLearnerRole(
			final IVector3 initPos,
			final double sampleTime,
			final double delayTime,
			final double acc,
			final EMotorModel motorModelType,
			final EVelocitySampler velSamplerType,
			final EMotorOptimizer optimizerType)
	{
		this(initPos, sampleTime, delayTime, acc);
		this.motorModelType = motorModelType;
		this.velSamplerType = velSamplerType;
		this.optimizerType = optimizerType;
		
		execService.execute(new FullOptimizer());
	}
	
	
	/**
	 * sample
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param initPos
	 * @param sampleTime
	 * @param delayTime
	 * @param acc
	 * @param numSamples
	 */
	public MotorLearnerRole(
			final IVector3 initPos,
			final double sampleTime,
			final double delayTime,
			final double acc,
			final int numSamples)
	{
		this(initPos, sampleTime, delayTime, acc);
		this.numSamples = numSamples;
		
		execService.execute(new GaussianProcessMatlabCollector());
	}
	
	
	/**
	 * evaluate
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param initPos
	 * @param targetVel
	 * @param sampleTime
	 * @param delayTime
	 * @param acc
	 * @param motorModelType
	 */
	public MotorLearnerRole(
			final IVector3 initPos,
			final IVector3 targetVel,
			final double sampleTime,
			final double delayTime,
			final double acc,
			final EMotorModel motorModelType)
	{
		this(initPos, sampleTime, delayTime, acc);
		this.targetVel = targetVel;
		this.motorModelType = motorModelType;
		
		execService.execute(new Evaluator());
	}
	
	private enum EStateId
	{
		INIT,
		SAMPLE,
		WAIT_FOR_INPUT,
		ACC
	}
	
	private enum EEvent
	{
		NEXT,
		START_SAMPLING
	}
	
	private class InitState implements IRoleState
	{
		private AMoveToSkill move;
		
		
		@Override
		public void doEntryActions()
		{
			move = AMoveToSkill.createMoveToSkill();
			move.getMoveCon().setPenaltyAreaAllowedOur(true);
			move.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(move);
			// setNewSkill(new PositionSkill(initPos.getXYVector(), initPos.z()));
		}
		
		
		@Override
		public void doUpdate()
		{
			double targetAngle = initPos.z();
			if (!targetVel.getXYVector().isZeroVector())
			{
				targetAngle = ((AngleMath.PI_HALF - targetVel.getXYVector().getAngle()) + initPos.z()) - initPosAngleOffset;
			}
			move.getMoveCon().updateDestination(initPos.getXYVector());
			move.getMoveCon().updateTargetAngle(targetAngle);
			
			double distDiff = GeoMath.distancePP(getPos(), initPos.getXYVector());
			double angleDiff = Math.abs(AngleMath.difference(getBot().getAngle(), targetAngle));
			if ((distDiff < 20) && (angleDiff < 0.05) && sampler.readyForSampling)
			{
				triggerEvent(EEvent.NEXT);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.INIT;
		}
	}
	
	
	private ABotSkill getBotSkill()
	{
		if (wheelVel)
		{
			return new BotSkillWheelVelocity(input);
		}
		return new BotSkillLocalVelocity(targetVel.getXYVector(), targetVel.z(), getBot().getBot().getMoveConstraints());
	}
	
	
	private class AccelerationState implements IRoleState
	{
		private long						tStart;
		private BotSkillWrapperSkill	skill;
		private double						accTime;
		
		
		@Override
		public void doEntryActions()
		{
			accTime = Math.max(targetVel.getLength2() / acc, 0.2);
			tStart = getWFrame().getTimestamp();
			skill = new BotSkillWrapperSkill(getBotSkillAcc());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		private ABotSkill getBotSkillAcc()
		{
			double t = (getWFrame().getTimestamp() - tStart) / 1e9;
			double rel = Math.min(1, t / accTime);
			if (accelerateXywVelocity)
			{
				return new BotSkillLocalVelocity(targetVel.getXYVector().multiplyNew(rel), targetVel.z() * rel,
						getBot().getBot().getMoveConstraints());
			}
			double[] in = new double[] { rel * input[0], rel * input[1], rel * input[2], rel * input[3] };
			return new BotSkillWheelVelocity(in);
		}
		
		
		@Override
		public void doUpdate()
		{
			double t = (getWFrame().getTimestamp() - tStart) / 1e9;
			if (t > (accTime + delayTime))
			{
				triggerEvent(EEvent.NEXT);
			} else
			{
				skill.setSkill(getBotSkill());
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.ACC;
		}
	}
	
	private class SampleState implements IRoleState
	{
		private long		tStart;
		private IVector3	startPos;
		
		
		@Override
		public void doEntryActions()
		{
			ASkill skill = new BotSkillWrapperSkill(new BotSkillWheelVelocity(input));
			// ASkill skill = new BotSkillWrapperSkill(new BotSkillLocalVelocity(targetVel.getXYVector(), targetVel.z()));
			setNewSkill(skill);
			tStart = getWFrame().getTimestamp();
			
			for (ADataCollector<IVector> collector : collectors)
			{
				collector.start();
			}
			
			startPos = new Vector3(getBot().getPos(), getBot().getAngle());
		}
		
		
		@Override
		public void doUpdate()
		{
			double length = targetVel.getLength2() * (sampleTime) * 1000;
			ILine line = new Line(startPos.getXYVector(),
					targetVel.getXYVector().turnNew(-AngleMath.PI_HALF + startPos.z())
							.scaleTo(length));
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED).add(
					new DrawableLine(line, Color.RED));
			ILine line2 = Line.newLine(startPos.getXYVector(), getPos());
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED).add(
					new DrawableLine(line2, Color.blue));
			
			int numMus = 5;
			Optional<Result> optResult = getResult(numMus);
			Optional<IVector3> confidence = Optional.empty();
			Optional<IVector3> muResult = Optional.empty();
			boolean converged = false;
			if (optResult.isPresent())
			{
				muResult = Optional.of(new Vector3(optResult.get().avg));
				confidence = Optional.of(new Vector3(optResult.get().std));
				converged = optResult.get().std.getLength() < 0.01;
			}
			
			double timePast = (getWFrame().getTimestamp() - tStart) / 1e9;
			boolean minSampleTimeReached = timePast > sampleTime;
			boolean maxSampleTimeReached = timePast > maxSampleTime;
			
			IVector2 futurePos = getPos().addNew(getBot().getVel().multiplyNew(500));
			boolean leavingField = !Geometry.getField()
					.isPointInShape(futurePos, -Geometry.getBotRadius());
			boolean turned = (targetVel.getLength2() > 1)
					&& (AngleMath.getShortestRotation(startPos.z(), getBot().getAngle()) > AngleMath.deg2rad(100));
			
			boolean done = minSampleTimeReached && (!sampleUntilConvergence || converged);
			boolean cancel = maxSampleTimeReached || leavingField || turned;
			
			if (done || cancel)
			{
				if (printSamples)
				{
					printSamples();
				}
				
				if (exportSamples)
				{
					long timestamp = System.currentTimeMillis();
					for (ADataCollector<IVector> collector : collectors)
					{
						collector.exportSamples("logs/samples/" + timestamp + "_" + collector.getType().name());
					}
				}
				
				if ((samplesRepeated > 5))
				{
					sampler.onResultReady(new Vector3(Double.NaN, Double.NaN, Double.NaN));
					initPosAngleOffset = 0;
					samplesRepeated = 0;
				} else if ((muResult.isPresent() && (confidence.get().getLength() < 1)))
				{
					IVector3 result = muResult.get();
					log.debug(String.format("Sample: %6.1f %6.1f %6.1f %6.1f -> %7.3f %7.3f %7.3f (%.4f,%.4f,%.4f,%.4f%s)",
							input[0], input[1], input[2], input[3],
							result.x(), result.y(), result.z(),
							confidence.get().x(), confidence.get().y(), confidence.get().z(),
							confidence.get().getLength(),
							cancel ? " canceled" : ""));
					sampler.onResultReady(result);
					initPosAngleOffset = 0;
					samplesRepeated = 0;
				} else
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Sampling failed. Repeating. ");
					sb.append("Result: ");
					if (optResult.isPresent())
					{
						sb.append(optResult.get().avg);
						sb.append(optResult.get().std);
					} else
					{
						sb.append("no");
					}
					sb.append("; Confidence: ");
					if (confidence.isPresent())
					{
						sb.append(confidence);
					} else
					{
						sb.append("no");
					}
					
					sb.append("; time: ");
					sb.append(timePast);
					
					log.warn(sb.toString());
					
					double angle = getPos().subtractNew(initPos).getAngle();
					initPosAngleOffset = -AngleMath.getShortestRotation(angle, initPos.z() - initPosAngleOffset) * 0.7;
					samplesRepeated++;
				}
				triggerEvent(EEvent.NEXT);
			}
			
		}
		
		
		private void printSamples()
		{
			for (ADataCollector<IVector> collector : collectors)
			{
				if (collector.getSamples().isEmpty())
				{
					continue;
				}
				VectorDataStatistics stats = new VectorDataStatistics(collector.getSamples(), 3);
				IVector avg = stats.getAverage();
				IVector std = stats.getStandardDeviation();
				IVector min = stats.getMinimas();
				IVector max = stats.getMaximas();
				log.debug(String
						.format(
								"%5.2f (%4.2f,%4.2f,%4.2f) %5.2f (%4.2f,%4.2f,%4.2f) %7.4f (%6.4f,%6.4f,%6.4f); samples: %4d (%s)",
								avg.x(), std.x(), min.x(), max.x(),
								avg.y(), std.y(), min.y(), max.y(),
								avg.z(), std.z(), min.z(), max.z(),
								collector.getNumSamples(),
								collector.getType()));
			}
		}
		
		
		private Optional<Result> getResult(final int numSamples)
		{
			Vector3 result = new Vector3();
			Vector3 resultStd = new Vector3();
			boolean foundSample1 = false;
			boolean foundSample2 = false;
			boolean foundSample3 = false;
			for (ADataCollector<IVector> collector : collectors)
			{
				if (collector.getSamples().size() < numSamples)
				{
					continue;
				}
				int fromIndex = collector.getSamples().size() - numSamples;
				int toIndex = collector.getSamples().size();
				VectorDataStatistics stats = new VectorDataStatistics(collector.getSamples().subList(fromIndex, toIndex),
						3);
				IVector avg = stats.getAverage();
				IVector std = stats.getStandardDeviation();
				
				if (collectorsToUse.get(0) == collector.getType())
				{
					result.set(0, avg.x());
					resultStd.set(0, std.x());
					foundSample1 = true;
				}
				if (collectorsToUse.get(1) == collector.getType())
				{
					result.set(1, avg.y());
					resultStd.set(1, std.y());
					foundSample2 = true;
				}
				if (collectorsToUse.get(2) == collector.getType())
				{
					result.set(2, avg.z());
					resultStd.set(2, std.z());
					foundSample3 = true;
				}
			}
			if (foundSample1 && foundSample2 && foundSample3)
			{
				Result res = new Result();
				res.avg = result;
				res.std = resultStd;
				return Optional.of(res);
			}
			return Optional.empty();
		}
		
		
		@Override
		public void doExitActions()
		{
			for (ADataCollector<IVector> collector : collectors)
			{
				collector.stop();
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.SAMPLE;
		}
	}
	
	
	private class Sampler implements IMotorSampler
	{
		private CountDownLatch	latch					= new CountDownLatch(1);
		private IVector3			result;
		private boolean			readyForSampling	= false;
		
		
		@Override
		public IVector3 takeSample(final double[] motors)
		{
			for (double m : motors)
			{
				if (Math.abs(m) > 150)
				{
					log.warn("motors too high: " + Arrays.toString(motors));
					throw new SampleError();
				}
			}
			
			input = motors;
			
			latch = new CountDownLatch(1);
			readyForSampling = true;
			try
			{
				latch.await();
			} catch (InterruptedException err)
			{
				throw new SampleError();
			}
			readyForSampling = false;
			return result;
		}
		
		
		void onResultReady(final IVector3 result)
		{
			this.result = result;
			latch.countDown();
		}
	}
	
	
	private class FullSampleCollector extends ASampleCollector implements Runnable
	{
		@Override
		public void run()
		{
			AMotorModel mm;
			try
			{
				mm = (AMotorModel) motorModelType.getInstanceableClass().newDefaultInstance();
			} catch (NotCreateableException err1)
			{
				log.error("Could not create motor model.", err1);
				return;
			}
			
			IVelocityXywSampler sam;
			try
			{
				sam = (IVelocityXywSampler) velSamplerType.getInstanceableClass().newDefaultInstance();
			} catch (NotCreateableException err1)
			{
				log.error("Could not create velocity sampler.", err1);
				return;
			}
			
			// mm = InterpolationMotorModel.fromMotorModel(mm, 2, 0.1, AngleMath.PI_QUART / 4);
			//
			// log.debug(mm);
			
			mm.setMotorNoise(new VectorN(4).apply(v -> motorNoise));
			
			try
			{
				while (sam.hasNext())
				{
					targetVel = sam.getNextVelocity();
					IVectorN wheelSpeed = mm.getWheelSpeed(targetVel);
					takeSample(wheelSpeed.toArray());
				}
			} catch (SampleError err)
			{
				log.warn("Error during sampling.", err);
			}
			
			afterSampling("fullSampler");
		}
	}
	
	
	private abstract class ASampleCollector
	{
		List<Sample> samples = new ArrayList<>();
		
		
		protected void takeSample(final double[] in)
		{
			Sample sample = new Sample();
			sample.targetVel = targetVel;
			sample.wheelSpeed = in;
			sample.xywSpeed = sampler.takeSample(in);
			samples.add(sample);
			log.info(sample);
		}
		
		
		protected void afterSampling(final String identifier)
		{
			if (!samples.isEmpty())
			{
				CSVExporter exp = new CSVExporter("logs/" + identifier + "/" + System.currentTimeMillis(), false);
				for (Sample sample : samples)
				{
					exp.addValues(sample.getNumberList());
				}
				exp.close();
				
				RealMatrix M = new Array2DRowRealMatrix(samples.size(), 4);
				RealMatrix XYW = new Array2DRowRealMatrix(samples.size(), 3);
				
				for (int i = 0; i < samples.size(); i++)
				{
					M.setRow(i, samples.get(i).getWheelSpeedDoubleArray());
					XYW.setRow(i, samples.get(i).xywSpeed.toDoubleArray());
				}
				
				DecompositionSolver solver = new SingularValueDecomposition(XYW).getSolver();
				RealMatrix D = solver.solve(M).transpose();
				
				log.info("Motors X: " + D.getColumnVector(0));
				log.info("Motors Y: " + D.getColumnVector(1));
				log.info("Motors W: " + D.getColumnVector(2));
				
				MatrixMotorModel mm = MatrixMotorModel.fromMatrixWithWheelVel(D);
				List<IVector> errors = new ArrayList<>(samples.size());
				for (Sample sample : samples)
				{
					IVectorN wsCalc = mm.getWheelSpeed(sample.xywSpeed);
					IVectorN wsSample = new VectorN(sample.wheelSpeed);
					errors.add(wsCalc.subtractNew(wsSample));
				}
				VectorDataStatistics errorStats = new VectorDataStatistics(errors, 4);
				log.info("Avg Error: " + errorStats.getAverage());
				log.info("Std Error: " + errorStats.getStandardDeviation());
				log.info("Error Range: " + errorStats.getRange());
			}
			
			setCompleted();
		}
	}
	
	private class Evaluator implements Runnable
	{
		@Override
		public void run()
		{
			List<IVector> results = new ArrayList<>();
			try
			{
				AMotorModel mm;
				try
				{
					mm = (AMotorModel) motorModelType.getInstanceableClass().newDefaultInstance();
				} catch (NotCreateableException err1)
				{
					log.error("Could not create motor model.", err1);
					return;
				}
				
				int iterations = 10;
				IVector3 tv = targetVel;
				IVectorN in = mm.getWheelSpeed(tv);
				
				log.info("#### Start Evaluation with " + in);
				for (int i = 0; i < iterations; i++)
				{
					results.add(sampler.takeSample(in.toArray()));
				}
			} catch (SampleError err)
			{
				log.warn("Error during sampling.");
			}
			
			VectorDataStatistics stats = new VectorDataStatistics(results, 3);
			log.info("Mean:  " + stats.getAverage());
			log.info("Std:   " + stats.getStandardDeviation());
			log.info("Range: " + stats.getMinimas() + " - " + stats.getMaximas());
			
			setCompleted();
		}
	}
	
	private class GaussianProcessMatlabCollector extends ASampleCollector implements Runnable
	{
		@Override
		public void run()
		{
			IMotorModel mm = new MatrixMotorModel();
			
			try
			{
				MatlabConnection.getMatlabProxy().eval("motors=mm.getUncertainInputs();");
				MatlabTypeConverter processor = new MatlabTypeConverter(MatlabConnection.getMatlabProxy());
				MatlabNumericArray array = processor.getNumericArray("motors");
				double[][] motorVels = array.getRealArray2D();
				
				for (int i = 0; i < Math.min(motorVels.length, numSamples); i++)
				{
					targetVel = mm.getXywSpeed(new VectorN(motorVels[i]));
					takeSample(motorVels[i]);
				}
			} catch (MatlabInvocationException err)
			{
				log.error("", err);
			} catch (MatlabConnectionException err)
			{
				log.error("", err);
			} catch (SampleError err)
			{
				log.warn("Error during sampling.");
			}
			
			afterSampling("gpOptimizer");
		}
	}
	
	
	private class FullOptimizer implements Runnable
	{
		@Override
		public void run()
		{
			AMotorModel mm;
			try
			{
				mm = (AMotorModel) motorModelType.getInstanceableClass().newDefaultInstance();
			} catch (NotCreateableException err1)
			{
				log.error("Could not create motor model.", err1);
				return;
			}
			
			IVelocityXywSampler sam;
			try
			{
				sam = (IVelocityXywSampler) velSamplerType.getInstanceableClass().newDefaultInstance();
			} catch (NotCreateableException err1)
			{
				log.error("Could not create velocity sampler.", err1);
				return;
			}
			
			int iterations = sam.getNeededSamples();
			InterpolationMotorModel imm = new InterpolationMotorModel();
			Function<IVector3, IVectorN> getWheelSpeed = (target) -> mm.getWheelSpeed(target);
			MotorModelOptimizer mmOptimizer = new MotorModelOptimizer(sampler, getWheelSpeed);
			
			mm.setMotorNoise(new VectorN(4).apply(v -> motorNoise));
			
			try
			{
				for (int it = 0; it < iterations; it++)
				{
					targetVel = sam.getNextVelocity();
					
					log.info(String.format("Target vel:  %.2f %.2f %.2f; sampleTime: %.2f; acc: %.2f", targetVel.x(),
							targetVel.y(), targetVel.z(), sampleTime, acc));
					
					switch (optimizerType)
					{
						case BOBYQA:
							input = mmOptimizer.executeBOBYQA(targetVel); // works similar to LM
							break;
						case CMAES:
							mmOptimizer.executeCMAES(targetVel); // not very good
							break;
						case LM:
							input = mmOptimizer.executeLM(targetVel);
							break;
						case POWELL:
							input = mmOptimizer.executePowell(targetVel); // uses a LineSearch, takes time
							break;
						case SIMPLEX:
							input = mmOptimizer.executeSimplex(targetVel); // uses Nelder-Mead simplex, takes time
							break;
						case CLEVER_ONE:
							input = mmOptimizer.executeCleverOne(targetVel);
							break;
						default:
							break;
					}
					
					log.info(String.format("Final input: %ff, %ff, %ff, %ff", input[0], input[1], input[2], input[3]));
					
					log.info("#### Start Evaluation with " + Arrays.toString(input));
					List<IVector> results = new ArrayList<>();
					for (int i = 0; i < 5; i++)
					{
						results.add(sampler.takeSample(input));
					}
					
					VectorDataStatistics stats = new VectorDataStatistics(results, 3);
					log.info("Mean:  " + stats.getAverage());
					log.info("Std:   " + stats.getStandardDeviation());
					log.info("Range: " + stats.getMinimas() + " - " + stats.getMaximas());
					
					IVector err = targetVel.subtractNew(stats.getAverage());
					
					double angle = 0;
					if (!targetVel.isZeroVector())
					{
						angle = targetVel.getXYVector().getAngle();
					}
					double speed = targetVel.getXYVector().getLength2();
					imm.addSupport(angle, speed, input, err.toArray());
				}
			} catch (SampleError err)
			{
				log.info("Sampling interrupted", err);
			}
			
			imm.writeToFile("last");
			
			log.info(imm);
			
			setCompleted();
		}
	}
	
	private class Optimizer implements Runnable
	{
		@Override
		public void run()
		{
			// double[] speeds = new double[] { 0.1, 0.5, 1.1, 1.3, 1.5, 1.6 };
			// double[] speeds = new double[] { 1, 1.2, 1.4, 1.5, 1.6, 1.7 };
			double[] speeds = new double[] { targetVel.getLength2() };
			// double[] speeds = new double[] { 1 };
			
			List<Double> angles = new ArrayList<>();
			// for (double a = 0; a < AngleMath.PI_TWO; a += AngleMath.PI_QUART)
			// {
			// angles.add(a);
			// }
			angles.add(targetVel.getXYVector().getAngle());
			
			try
			{
				IMotorModel mm = new MatrixMotorModel();
				IVectorN ws = mm.getWheelSpeed(targetVel);
				input = new double[] { ws.get(0), ws.get(1), ws.get(2), ws.get(3) };
				
				for (double angle : angles)
				{
					for (double speed : speeds)
					{
						log.info("Speed: " + speed + ", Angle: " + angle);
						
						IVector3 lastTargetVel = targetVel;
						targetVel = new Vector3(new Vector2(angle).scaleTo(speed), targetVel.z());
						
						double lastVel = lastTargetVel.getLength2();
						double newVel = targetVel.getLength2();
						for (int i = 0; i < 4; i++)
						{
							input[i] = (input[i] / lastVel) * newVel;
						}
						log.info("input: " + Arrays.toString(input));
						// IVectorN vec = new VectorN(input);
						// Function<IVector3, IVectorN> getWheelSpeed = (target) -> vec;
						Function<IVector3, IVectorN> getWheelSpeed = (target) -> mm.getWheelSpeed(target);
						MotorModelOptimizer mmOptimizer = new MotorModelOptimizer(sampler, getWheelSpeed);
						
						switch (optimizerType)
						{
							case BOBYQA:
								input = mmOptimizer.executeBOBYQA(targetVel); // works similar to LM
								break;
							case CMAES:
								mmOptimizer.executeCMAES(targetVel); // not very good
								break;
							case LM:
								input = mmOptimizer.executeLM(targetVel);
								break;
							case POWELL:
								input = mmOptimizer.executePowell(targetVel); // uses a LineSearch, takes time
								break;
							case SIMPLEX:
								input = mmOptimizer.executeSimplex(targetVel); // uses Nelder-Mead simplex, takes time
								break;
							case CLEVER_ONE:
								input = mmOptimizer.executeCleverOne(targetVel);
								break;
							default:
								break;
						}
						
						log.info(String.format("Target vel:  %.2f %.2f %.2f; sampleTime: %.2f; acc: %.2f", targetVel.x(),
								targetVel.y(), targetVel.z(), sampleTime, acc));
						log.info(String.format("Final input: %ff, %ff, %ff, %ff", input[0], input[1], input[2], input[3]));
						double[] normInput = new double[4];
						for (int i = 0; i < 4; i++)
						{
							normInput[i] = input[i] / targetVel.getLength2();
						}
						log.info(String.format("Normalized input: %ff, %ff, %ff, %ff", normInput[0], normInput[1],
								normInput[2],
								normInput[3]));
						
						log.info("#### Start Evaluation with " + Arrays.toString(input));
						List<IVector> results = new ArrayList<>();
						for (int i = 0; i < 10; i++)
						{
							results.add(sampler.takeSample(input));
						}
						
						VectorDataStatistics stats = new VectorDataStatistics(results, 3);
						IVector mean = stats.getAverage();
						log.info("Mean:  " + mean);
						log.info("Std:   " + stats.getStandardDeviation());
						log.info("Range: " + stats.getMinimas() + " - " + stats.getMaximas());
						
						IVector3 error = targetVel.subtractNew(mean).absNew();
						double relError = (error.getLength() / targetVel.getLength());
						log.info("Error: " + error + " Rel: " + relError);
						if (relError > 0.2)
						{
							log.warn("Error >20% Stopping...");
							break;
						}
					}
				}
			} catch (SampleError err)
			{
				log.warn("Error during sampling.", err);
			}
			
			setCompleted();
		}
	}
	
	private static class Sample implements INumberListable
	{
		double[]	wheelSpeed;
		IVector3	xywSpeed;
		IVector3	targetVel;
		
		
		public double[] getWheelSpeedDoubleArray()
		{
			double result[] = new double[wheelSpeed.length];
			
			for (int i = 0; i < wheelSpeed.length; i++)
			{
				result[i] = wheelSpeed[i];
			}
			
			return result;
		}
		
		
		@Override
		public List<Number> getNumberList()
		{
			List<Number> nbrs = new ArrayList<>(7);
			nbrs.add(wheelSpeed[0]);
			nbrs.add(wheelSpeed[1]);
			nbrs.add(wheelSpeed[2]);
			nbrs.add(wheelSpeed[3]);
			nbrs.add(xywSpeed.x());
			nbrs.add(xywSpeed.y());
			nbrs.add(xywSpeed.z());
			nbrs.add(targetVel.x());
			nbrs.add(targetVel.y());
			nbrs.add(targetVel.z());
			return nbrs;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(targetVel);
			builder.append(" -> ");
			builder.append(Arrays.toString(wheelSpeed));
			builder.append(" -> ");
			builder.append(xywSpeed);
			return builder.toString();
		}
	}
	
	private static class Result
	{
		IVector3 avg, std;
	}
	
	
	@Override
	protected void onCompleted()
	{
		execService.shutdownNow();
	}
	
	
	@Override
	protected void beforeFirstUpdate()
	{
		super.beforeFirstUpdate();
		
		// collectors.add(new LocalVelCamDataCollector(getBotID(), 0.001));
		collectors.add(new LocalVelCamSimpleDataCollector(getBotID(), 0.001));
		
		// collectorsToUse.add(EDataCollector.LOCAL_VEL_CAM);
		// collectorsToUse.add(EDataCollector.LOCAL_VEL_CAM);
		// // if (getBot().getBot().getType() == EBotType.TIGER_V3)
		// // {
		// // collectorsToUse.add(EDataCollector.LOCAL_VEL_BOT);
		// // collectors.add(new LocalVelBotDataCollector(getBotID()));
		// // } else
		// // {
		// collectorsToUse.add(EDataCollector.LOCAL_VEL_CAM);
		// // }
		
		// collectors.add(new LocalVelWpDataCollector(getBotID()));
		
		// collectors.add(new GlobalPosCamDataCollector(getBotID()));
		// collectors.add(new GlobalPosWpDataCollector(getBotID()));
		// collectors.add(new GlobalPosBotDataCollector(getBotID()));
		
		// collectors.add(new LocalVelByDistCamDataCollector(getBotID()));
		double t = sampleTime * 0.8;
		if (sampleUntilConvergence)
		{
			t = 0.5;
		}
		collectors.add(new LocalVelByStartEndCamDataCollector(getBotID(), t));
		
		// collectorsToUse.add(EDataCollector.LOCAL_VEL_BY_START_END_CAM);
		// collectorsToUse.add(EDataCollector.LOCAL_VEL_BY_START_END_CAM);
		// collectorsToUse.add(EDataCollector.LOCAL_VEL_BY_START_END_CAM);
		collectorsToUse.add(EDataCollector.LOCAL_VEL_CAM_SIMPLE);
		collectorsToUse.add(EDataCollector.LOCAL_VEL_CAM_SIMPLE);
		collectorsToUse.add(EDataCollector.LOCAL_VEL_CAM_SIMPLE);
	}
	
	
	@Override
	protected void beforeUpdate()
	{
		double length = targetVel.getLength2() * (sampleTime + (targetVel.getLength2() / acc) + delayTime) * 1000;
		if (sampleTime == 0)
		{
			length = 9000;
		}
		ILine line = new Line(initPos.getXYVector(), targetVel.getXYVector().turnToNew(initPos.z() - initPosAngleOffset)
				.scaleTo(length));
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableLine(line, Color.WHITE));
	}
}
