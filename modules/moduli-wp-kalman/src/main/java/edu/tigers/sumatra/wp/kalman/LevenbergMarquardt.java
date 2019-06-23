/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 29, 2016
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.util.Incrementor;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author dirk
 */
@SuppressWarnings({ "unused", "deprecation" })
public class LevenbergMarquardt
{
	private List<List<CamBall>> balls;
	
	
	/**
	 * @param balls
	 */
	public LevenbergMarquardt(final List<List<CamBall>> balls)
	{
		this.balls = balls;
		// CurveFitter fitter = new CurveFitter(lmo);
		
		
		// new LeastSquaresBuilder().start()
		// LeastSquaresBuilder lsp = new LeastSquaresBuilder();
		
		// final double[] weights = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		//
		// final double[] initialSolution = { 0, 0, 0, 0, 0, 0, 0, 0 };
		//
		// QuadraticProblem problem = new QuadraticProblem();
		// PointVectorValuePair optimum = lmo.optimize(100,
		// problem,
		// problem.calculateTarget(),
		// weights,
		// initialSolution);
		
	}
	
	
	/**
	 * @return
	 */
	public RealVector optimize()
	{
		LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
		Optimum optimum = lmo.optimize(new FlyingBall(balls));
		System.out.println(optimum.getPoint());
		System.out.println(optimum.getIterations());
		System.out.println(optimum.getEvaluations());
		// final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
		// fitter.fit(points)
		return optimum.getPoint();
		
		
	}
	
	
	private static class FlyingBall implements LeastSquaresProblem
	{
		List<List<CamBall>>	balls;
		double[][]				observationsArray;
		double[]					model		= new double[7];
		Incrementor				incEval	= new Incrementor();
		Incrementor				incIter	= new Incrementor();
		
		
		public FlyingBall(final List<List<CamBall>> balls)
		{
			incEval.setMaximalCount(Integer.MAX_VALUE);
			incIter.setMaximalCount(Integer.MAX_VALUE);
			
			
			this.balls = balls;
			// [time position(2D) camera timeDifference]
			Collection<WeightedObservedPoint> observations = new ArrayList<WeightedObservedPoint>();
			int counts = 0;
			for (List<CamBall> ballsOfFrame : balls)
			{
				for (CamBall ball : ballsOfFrame)
				{
					counts++;
				}
			}
			
			observationsArray = new double[counts][4];
			
			int t = 0;
			int counter = 0;
			long firstTimestamp = balls.get(0).get(0).getTimestamp();
			for (List<CamBall> ballsOfFrame : balls)
			{
				for (CamBall ball : ballsOfFrame)
				{
					// observations.add(new WeightedObservedPoint(t, ball.getCameraId(), ball.getPixelX(), ball.getPixelY());
					// System.out.println(ball.getTimestamp());
					// System.out.println(ball.getTimestamp() - firstTimestamp);
					observationsArray[counter][0] = (TimeUnit.NANOSECONDS
							.toMillis(ball.getTimestamp() - firstTimestamp)) / 1000.0;
					// System.out.println(observationsArray[counter][0]);
					observationsArray[counter][1] = ball.getCameraId();
					observationsArray[counter][2] = ball.getPixelX();
					observationsArray[counter][3] = ball.getPixelY();
					
					counter++;
				}
				t++;
			}
			System.out.println("Timesteps: " + observationsArray.length);
		}
		
		
		@Override
		public Incrementor getEvaluationCounter()
		{
			incEval.incrementCount();
			System.out.println("evaluation");
			return incEval;
		}
		
		
		@Override
		public Incrementor getIterationCounter()
		{
			incIter.incrementCount();
			System.out.println("iteration");
			return incIter;
		}
		
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public ConvergenceChecker<Evaluation> getConvergenceChecker()
		{
			return new TigersConvergenceChecker();// (ConvergenceChecker) new SimpleVectorValueChecker(-1, 0.1);
		}
		
		
		@Override
		public RealVector getStart()
		{
			RealVector vector = MatrixUtils.createRealVector(new double[] { 0, 0, 0, 0, 0, 0, 0 });
			return vector;
			
		}
		
		
		@Override
		public int getObservationSize()
		{
			return observationsArray.length;
		}
		
		
		@Override
		public int getParameterSize()
		{
			return 7;
		}
		
		
		@Override
		public Evaluation evaluate(final RealVector point)
		{
			RealVector residuals = MatrixUtils.createRealVector(new double[] {});
			System.out.println(point);
			// input is model
			for (double[] observation : observationsArray)
			{
				double t = observation[0];
				int camera = (int) observation[1];
				
				RealVector position = positionInModel(point, t);
				RealVector pixel = position2pixel(camera, position);
				
				RealVector real = MatrixUtils
						.createRealVector(new double[] { observation[2], observation[3], 1 });
				residuals = residuals.append(real.subtract(pixel).getNorm());
				
			}
			return new TigersEvaluation(point, residuals, observationsArray);
			
			
			// TODO
			// pNow = p + v*dT - [a 0.5*g*dT^2];
			// ydata(i,:) = cams(cam).field2image(pNow');
			
			// x = obj.W*[P; 1];
			// y = x/x(3);
			// pixel = y(1:2);
			
		}
		
		
		private RealVector positionInModel(final RealVector model, final double t)
		{
			// evaluate expected position
			double[] pNow = evaluateModel(t, model);
			RealVector pNowVector = MatrixUtils.createRealVector(pNow);
			return pNowVector.append(1);
		}
		
		
		private RealVector position2pixel(final int camera, final RealVector position)
		{
			// field2image
			RealMatrix W = Geometry.getW(camera);
			// RealMatrix W = MatrixUtils.createRealMatrix(wInput);
			RealVector x = W.operate(position);
			RealVector y = x.mapMultiply(1 / x.getEntry(2));
			return y;
		}
		
		private class TigersConvergenceChecker implements ConvergenceChecker<Evaluation>
		{
			
			@Override
			public boolean converged(final int iteration, final Evaluation previous, final Evaluation current)
			{
				return false;
			}
			
		}
		
		private class TigersEvaluation implements Evaluation
		{
			
			private RealVector	pixels;
			private RealVector	residuals;
			private double[][]	observationArray;
			
			
			public TigersEvaluation(final RealVector pixels, final RealVector residuals, final double[][] observationArray)
			{
				this.pixels = pixels;
				this.residuals = residuals;
				this.observationArray = observationArray;
			}
			
			
			@Override
			public RealMatrix getCovariances(final double threshold)
			{
				return null;
			}
			
			
			@Override
			public RealVector getSigma(final double covarianceSingularityThreshold)
			{
				return null;
			}
			
			
			@Override
			public double getRMS()
			{
				return residuals.getMaxValue();
			}
			
			
			@Override
			public RealMatrix getJacobian()
			{
				RealMatrix realMatrix = MatrixUtils.createRealMatrix(observationArray.length, 7);
				int counter = 0;
				for (double[] observation : observationArray)
				{
					RealVector position = positionInModel(getPoint(), observation[0]);
					RealVector pixel = position2pixel((int) observation[1], position);
					
					RealMatrix W = Geometry.getW((int) observation[1]);
					
					realMatrix.setEntry(counter, 0, 1);
					realMatrix.setEntry(counter, 1, 1);
					realMatrix.setEntry(counter, 2, observation[0]);
					realMatrix.setEntry(counter, 3, observation[0]);
					realMatrix.setEntry(counter, 4, observation[0]);
					realMatrix.setEntry(counter, 5, observation[0] * observation[0]);
					realMatrix.setEntry(counter, 6, observation[0] * observation[0]);
					counter++;
				}
				return realMatrix;
			}
			
			
			@Override
			public double getCost()
			{
				return residuals.getMaxValue();
			}
			
			
			@Override
			public RealVector getResiduals()
			{
				return residuals;
			}
			
			
			@Override
			public RealVector getPoint()
			{
				return pixels;
			}
			
		}
		
		
		private double[] evaluateModel(final double dT, final RealVector modelRv)
		{
			double[] model = modelRv.toArray();
			
			double[] p = new double[3];
			p[0] = model[0];
			p[1] = model[1];
			p[2] = 0;
			
			double[] v = new double[3];
			v[0] = model[2];
			v[1] = model[3];
			v[2] = model[4];
			
			double[] a = new double[3];
			a[0] = model[5];
			a[1] = model[6];
			a[2] = 0.5 * 9.81;
			
			double[] pnow = { 0, 0, 0 };
			for (int i = 0; i <= 2; i++)
			{
				pnow[i] = p[i] + (v[i] * dT) + (a[i] * (dT * dT));
			}
			
			return pnow;
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private static class QuadraticProblem
			implements DifferentiableMultivariateVectorFunction, Serializable
	{
		
		private static final long	serialVersionUID	= 7072187082052755854L;
		private List<Double>			x;
		private List<Double>			y;
		
		
		public QuadraticProblem()
		{
			x = new ArrayList<Double>();
			y = new ArrayList<Double>();
		}
		
		
		public void addPoint(final double x, final double y)
		{
			this.x.add(x);
			this.y.add(y);
		}
		
		
		public double[] calculateTarget()
		{
			double[] target = new double[y.size()];
			for (int i = 0; i < y.size(); i++)
			{
				target[i] = y.get(i).doubleValue();
			}
			return target;
		}
		
		
		private double[][] jacobian(final double[] variables)
		{
			double[][] jacobian = new double[x.size()][3];
			for (int i = 0; i < jacobian.length; ++i)
			{
				jacobian[i][0] = x.get(i) * x.get(i);
				jacobian[i][1] = x.get(i);
				jacobian[i][2] = 1.0;
			}
			return jacobian;
		}
		
		
		@Override
		public double[] value(final double[] variables)
		{
			double[] values = new double[x.size()];
			for (int i = 0; i < values.length; ++i)
			{
				values[i] = (((variables[0] * x.get(i)) + variables[1]) * x.get(i)) + variables[2];
			}
			return values;
		}
		
		
		@Override
		public MultivariateMatrixFunction jacobian()
		{
			return new MultivariateMatrixFunction()
			{
				private static final long serialVersionUID = -8673650298627399464L;
				
				
				@Override
				public double[][] value(final double[] point)
				{
					return jacobian(point);
				}
			};
		}
	}
}
