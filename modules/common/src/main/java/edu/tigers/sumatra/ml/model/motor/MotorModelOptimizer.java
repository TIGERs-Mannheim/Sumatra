/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ml.model.motor;

import java.util.function.Function;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 * @author AndreR
 */
public class MotorModelOptimizer implements MultivariateJacobianFunction, MultivariateFunction
{
	private static final double EPS = 2;
	private static final double NORM_FACTOR = 1e-9;
	
	private static final Logger log = Logger.getLogger(MotorModelOptimizer.class.getName());
	
	private double[] target;
	private final IMotorSampler sampler;
	private final MatrixMotorModel motorModel = new MatrixMotorModel();
	
	private final Function<IVector3, IVectorN> getWheelSpeed;
	
	
	static
	{
		
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param sampler
	 * @param getWheelSpeed
	 */
	public MotorModelOptimizer(final IMotorSampler sampler, final Function<IVector3, IVectorN> getWheelSpeed)
	{
		this.sampler = sampler;
		this.getWheelSpeed = getWheelSpeed;
	}
	
	
	/**
	 * @param sampler
	 */
	public MotorModelOptimizer(final IMotorSampler sampler)
	{
		this.sampler = sampler;
		getWheelSpeed = (target) -> motorModel.getWheelSpeed(target);
	}
	
	
	private Pair<RealVector, RealVector> precompute(final IVector3 target)
	{
		RealMatrix XYW = new Array2DRowRealMatrix(target.toArray());
		
		IVectorN wheelSpeed = getWheelSpeed.apply(target);
		RealVector initialGuess = wheelSpeed.toRealVector();
		// RealVector initialGuess = new ArrayRealVector(new double[] { wheelSpeed.get(0), wheelSpeed.get(2) });
		
		RealVector t = XYW.getColumnVector(0).append(initialGuess.getNorm());
		
		this.target = t.toArray();
		
		log.info("Target: " + t);
		log.info("Initial Guess: " + initialGuess);
		
		return new Pair<RealVector, RealVector>(t, initialGuess);
	}
	
	
	/**
	 * Start the optimizer.
	 * 
	 * @param target
	 * @return
	 */
	public double[] executeLM(final IVector3 target)
	{
		Pair<RealVector, RealVector> helper = precompute(target);
		
		ParameterValidator validator = new Validator();
		LeastSquaresProblem problem = LeastSquaresFactory.create(this,
				helper.getFirst(),
				helper.getSecond(),
				MatrixUtils.createRealDiagonalMatrix(new double[] { 1, 1, 1, 1e-11 }),
				new EvaluationRmsChecker(0.00000000001),
				100,
				50, false,
				validator);
		
		LeastSquaresOptimizer opti = new LevenbergMarquardtOptimizer()
				.withInitialStepBoundFactor(100.0)
				.withRankingThreshold(1e-12);
		
		LeastSquaresOptimizer.Optimum optimum = opti.optimize(problem);
		
		log.info("Result:");
		log.info(optimum.getPoint());
		log.info(optimum.getResiduals());
		log.info("Iterations: " + optimum.getIterations());
		log.info("Evaluations: " + optimum.getEvaluations());
		
		double[] result = new double[4];
		for (int i = 0; i < 4; i++)
		{
			result[i] = optimum.getPoint().getEntry(i);
		}
		return result;
	}
	
	
	/**
	 * @param target
	 * @return
	 */
	public double[] executeSimplex(final IVector3 target)
	{
		Pair<RealVector, RealVector> helper = precompute(target);
		
		this.target[3] = 0;
		
		SimplexOptimizer optimizer = new SimplexOptimizer(0.00001, -1);
		
		PointValuePair optimum = optimizer.optimize(
				new MaxEval(20000),
				GoalType.MINIMIZE,
				new ObjectiveFunction(this),
				new InitialGuess(helper.getSecond().toArray()),
				new NelderMeadSimplex(4, 2.0, 1.0, 2.0, 0.5, 0.5)
		// new MultiDirectionalSimplex(4)
		);
		
		
		RealVector p = new ArrayRealVector(optimum.getPoint());
		log.info("Evals: " + optimizer.getEvaluations());
		log.info("Optimum: " + p);
		log.info("Value: " + optimum.getValue());
		
		double[] result = new double[4];
		for (int i = 0; i < 4; i++)
		{
			result[i] = p.getEntry(i);
		}
		return result;
	}
	
	
	/**
	 * @param target
	 */
	public void executeCMAES(final IVector3 target)
	{
		Pair<RealVector, RealVector> helper = precompute(target);
		
		CMAESOptimizer optimizer = new CMAESOptimizer(2000, 0, true, 10, 0, new MersenneTwister(), false,
				new SimpleValueChecker(0.0000000001, -1));
		
		PointValuePair result = optimizer.optimize(
				new MaxEval(20000),
				GoalType.MINIMIZE,
				new ObjectiveFunction(this),
				new InitialGuess(helper.getSecond().toArray()),
				new CMAESOptimizer.PopulationSize(10),
				new CMAESOptimizer.Sigma(new double[] { 2.0, 2.0, 2.0, 2.0 }),
				SimpleBounds.unbounded(4));
		
		
		RealVector p = new ArrayRealVector(result.getPoint());
		log.info("Evals: " + optimizer.getEvaluations());
		log.info("Optimum: " + p);
		log.info("Value: " + result.getValue());
	}
	
	
	/**
	 * @param target
	 * @return
	 */
	public double[] executePowell(final IVector3 target)
	{
		Pair<RealVector, RealVector> helper = precompute(target);
		
		this.target[3] = 0;
		
		PowellOptimizer optimizer = new PowellOptimizer(0.0001, 0.000001);
		
		PointValuePair optimum = optimizer.optimize(
				new MaxEval(20000),
				GoalType.MINIMIZE,
				new ObjectiveFunction(this),
				new InitialGuess(helper.getSecond().toArray()));
		
		
		RealVector p = new ArrayRealVector(optimum.getPoint());
		log.info("Evals: " + optimizer.getEvaluations());
		log.info("Optimum: " + p);
		log.info("Value: " + optimum.getValue());
		
		double[] result = new double[4];
		for (int i = 0; i < 4; i++)
		{
			result[i] = p.getEntry(i);
		}
		return result;
	}
	
	
	/**
	 * @param target
	 * @return
	 */
	public double[] executeBOBYQA(final IVector3 target)
	{
		Pair<RealVector, RealVector> helper = precompute(target);
		
		this.target[3] = 0;
		
		BOBYQAOptimizer optimizer = new BOBYQAOptimizer(8);
		
		PointValuePair optimum = optimizer.optimize(
				new MaxEval(20000),
				GoalType.MINIMIZE,
				new ObjectiveFunction(this),
				new InitialGuess(helper.getSecond().toArray()),
				new CMAESOptimizer.PopulationSize(10),
				new CMAESOptimizer.Sigma(new double[] { 2.0, 2.0, 2.0, 2.0 }),
				SimpleBounds.unbounded(4));
		
		
		RealVector p = new ArrayRealVector(optimum.getPoint());
		log.info("Evals: " + optimizer.getEvaluations());
		log.info("Optimum: " + p);
		log.info("Value: " + optimum.getValue());
		
		double[] result = new double[4];
		for (int i = 0; i < 4; i++)
		{
			result[i] = p.getEntry(i);
		}
		return result;
	}
	
	private static class MinimizeRotationResult
	{
		RealVector sample;
		RealVector wheels;
		double rotSummand = 0;
		
		
		public MinimizeRotationResult()
		{
		}
		
		
		public MinimizeRotationResult(final RealVector s, final RealVector w, final double r)
		{
			sample = s;
			wheels = w;
			rotSummand = r;
		}
	}
	
	
	private MinimizeRotationResult minimizeRotation(final RealVector start, final double targetRot,
			final double maxError)
	{
		double rotMax = 2;
		double rotAdd = 0.0;
		double lastRotAdd = 0.2;
		
		RealVector sample = internalSample(start.mapAdd(lastRotAdd));
		double lastRot = sample.getEntry(2);
		double err = 0;
		do
		{
			sample = internalSample(start.mapAdd(rotAdd));
			double rot = sample.getEntry(2);
			
			double slope = (lastRotAdd - rotAdd) / (lastRot - rot);
			double offset = rotAdd - (slope * rot);
			
			lastRot = rot;
			lastRotAdd = rotAdd;
			
			rotAdd = (slope * targetRot) + offset;
			
			if (Math.abs(rotAdd) > rotMax)
			{
				rotAdd = (Math.signum(rotAdd) * rotMax) - 0.2;
			}
			err = Math.abs(targetRot - sample.getEntry(2));
			log.debug("Rot: " + rot + ", Slope: " + slope + ", Off: " + offset + ", Err: "
					+ err);
		} while (err > maxError);
		
		return new MinimizeRotationResult(sample, start.mapAdd(rotAdd), rotAdd);
	}
	
	
	/**
	 * @param target
	 * @return
	 */
	public double[] executeCleverOne(final IVector3 target)
	{
		// configure this to required precision
		double maxRotError = 0.1;
		double maxXYError = 0.05;
		double maxScale = 2;
		
		// variables with function scope
		MinimizeRotationResult result = new MinimizeRotationResult();
		
		// use the default (mathematical) motor model as basis
		RealVector wheelX = getWheelSpeed.apply(Vector3.fromXYZ(1, 0, 0)).toRealVector();
		RealVector wheelY = getWheelSpeed.apply(Vector3.fromXYZ(0, 1, 0)).toRealVector();
		
		// gather first sample
		double lastXFactor = target.x() + 0.2;
		double lastYFactor = target.y() + 0.2;
		RealVector toTest = wheelX.mapMultiply(lastXFactor).add(wheelY.mapMultiply(lastYFactor))
				.mapAdd(result.rotSummand);
		MinimizeRotationResult lastResult = minimizeRotation(toTest, target.z(), maxRotError);
		
		// configure second sample
		double xFactor = target.x();
		double yFactor = target.y();
		
		do
		{
			log.info("xF: " + xFactor + ", yF: " + yFactor);
			
			// take a sample with minimized rotation
			toTest = wheelX.mapMultiply(xFactor).add(wheelY.mapMultiply(yFactor)).mapAdd(result.rotSummand);
			result = minimizeRotation(toTest, target.z(), maxRotError);
			
			// calculate linear X and Y factor function
			double slopeX = (lastXFactor - xFactor) / (lastResult.sample.getEntry(0) - result.sample.getEntry(0));
			double offsetX = xFactor - (slopeX * result.sample.getEntry(0));
			
			double slopeY = (lastYFactor - yFactor) / (lastResult.sample.getEntry(1) - result.sample.getEntry(1));
			double offsetY = yFactor - (slopeY * result.sample.getEntry(1));
			
			// store last values for next iteration
			lastXFactor = xFactor;
			lastYFactor = yFactor;
			lastResult = result;
			
			// adjust factors
			xFactor = (slopeX * target.x()) + offsetX;
			yFactor = (slopeY * target.y()) + offsetY;
			
			if (Math.abs(xFactor) > maxScale)
			{
				xFactor = Math.signum(xFactor) * maxScale;
			}
			if (Math.abs(yFactor) > maxScale)
			{
				yFactor = Math.signum(yFactor) * maxScale;
			}
			
			double errX = Math.abs(target.x() - result.sample.getEntry(0));
			double errY = Math.abs(target.y() - result.sample.getEntry(1));
			log.info("Error: " + errX + " " + errY + " slope: " + slopeX + " " + slopeY);
		} while ((Math.abs(target.x() - result.sample.getEntry(0)) > maxXYError)
				|| (Math.abs(target.y() - result.sample.getEntry(1)) > maxXYError));
		
		log.info("Final sample: " + result.sample + " <= " + result.wheels);
		
		return (VectorN.fromReal(result.wheels)).toArray();
	}
	
	
	@Override
	public double value(final double[] point)
	{
		RealVector x = new ArrayRealVector(point);
		log.info("Sampling: " + x);
		
		RealVector sample = internalSample(x);
		
		sample.setEntry(3, sample.getEntry(3) * NORM_FACTOR);
		sample.setEntry(2, sample.getEntry(2) * 0.05);
		
		RealVector t = new ArrayRealVector(target);
		
		return t.subtract(sample).getNorm();
	}
	
	
	@Override
	public Pair<RealVector, RealMatrix> value(final RealVector x)
	{
		log.info("Sampling: " + x);
		
		// take sample at requested point
		RealVector fx = internalSample(x);
		
		RealMatrix J = new Array2DRowRealMatrix(fx.getDimension(), x.getDimension());
		
		// Finite Difference approach to numerically construct the Jacobian with the minimum number of samples
		for (int i = 0; i < x.getDimension(); i++)
		{
			ArrayRealVector test = new ArrayRealVector(x);
			test.addToEntry(i, EPS);
			
			RealVector feval = internalSample(test);
			
			J.setColumnVector(i, feval.subtract(fx).mapDivideToSelf(EPS));
		}
		
		// return sampled point and its Jacobian
		return new Pair<RealVector, RealMatrix>(fx, J);
	}
	
	
	private RealVector internalSample(final RealVector x)
	{
		// construct double array
		double[] xDouble = new double[] { x.getEntry(0), x.getEntry(1), x.getEntry(2),
				x.getEntry(3) };
		// double[] xDouble = new double[] { (double) x.getEntry(0), (double) x.getEntry(0), (double) x.getEntry(1),
		// (double) x.getEntry(1) };
		
		// call sample function
		IVector3 sample = sampler.takeSample(xDouble);
		
		// calculate norm (costs)
		double norm = x.getNorm();
		
		// build result vector
		RealVector result = new ArrayRealVector(4);
		
		result.setEntry(0, sample.x());
		result.setEntry(1, sample.y());
		result.setEntry(2, sample.z());
		result.setEntry(3, norm);
		
		// log.info("Sample: " + result);
		
		return result;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class SampleError extends Error
	{
		/**  */
		private static final long serialVersionUID = 1191480135726238699L;
		
		
		/**
		 * 
		 */
		public SampleError()
		{
			super("Error during sampling");
		}
	}
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class SampleException extends Exception
	{
		/**  */
		private static final long serialVersionUID = 1191480135726238699L;
		
		
		/**
		 * 
		 */
		public SampleException()
		{
			super("Sampling failed");
		}
	}
	
	private static class Validator implements ParameterValidator
	{
		@Override
		public RealVector validate(final RealVector params)
		{
			for (int i = 0; i < params.getDimension(); i++)
			{
				if (Math.abs(params.getEntry(i)) > 100)
				{
					params.set(Math.signum(params.getEntry(i)) * 100);
				}
			}
			return params;
		}
	}
}
