/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.ml.model.motor;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.function.Cos;
import org.apache.commons.math3.analysis.function.Sin;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author AndreR
 */
@Ignore
public class MotorLearnerTest
{
	private static final double		BOT_RADIUS	= 0.076;
																
	private static final RealMatrix	Dinv;
												
												
	static
	{
		// specify "real" front and back motor angles in degree
		double frontAngle = 30;
		double backAngle = 45;
		
		// convert to radian
		frontAngle *= Math.PI / 180.0;
		backAngle *= Math.PI / 180.0;
		
		// construct angle vector
		RealVector theta = new ArrayRealVector(new double[] { frontAngle, Math.PI - frontAngle, Math.PI + backAngle,
				(2 * Math.PI) - backAngle });
				
		// construct matrix for conversion from XYW to M1..M4
		RealMatrix D = new Array2DRowRealMatrix(4, 3);
		D.setColumnVector(0, theta.map(new Sin()).mapMultiplyToSelf(-1.0));
		D.setColumnVector(1, theta.map(new Cos()));
		D.setColumnVector(2, new ArrayRealVector(4, BOT_RADIUS));
		
		// construct pseudo-inverse for conversion from M1..M4 to XYW
		Dinv = new SingularValueDecomposition(D).getSolver().getInverse();
	}
	
	private class Sampler implements IMotorSampler
	{
		@Override
		public IVector3 takeSample(final double[] motors)
		{
			RealMatrix M = new Array2DRowRealMatrix(motors.length, 1);
			for (int i = 0; i < motors.length; i++)
			{
				M.setEntry(i, 0, motors[i]);
			}
			
			// we can use the conversion matrix for this test case
			// for the real learning this will lead to a sample acquisition with the real bot
			RealMatrix XYW = Dinv.multiply(M);
			
			return new Vector3(XYW.getEntry(0, 0), XYW.getEntry(1, 0), XYW.getEntry(2, 0));
		}
	}
	
	private class MotorFunc implements MultivariateFunction
	{
		private final double[] target;
		
		
		public MotorFunc(final double[] target)
		{
			this.target = target;
		}
		
		
		@Override
		public double value(final double[] point)
		{
			RealMatrix M = new Array2DRowRealMatrix(point);
			
			RealMatrix XYW = Dinv.multiply(M);
			
			System.out.println(XYW);
			
			double costs = M.getColumnVector(0).getNorm();
			
			RealVector t = new ArrayRealVector(target);
			
			double error = t.subtract(XYW.getColumnVector(0)).getNorm();
			
			return error + (0.7 * costs);
		}
	}
	
	
	/** */
	@Test
	public void simplex()
	{
		SimplexOptimizer optimizer = new SimplexOptimizer(0.0001, -1);
		
		PointValuePair result = optimizer.optimize(
				new MaxEval(20000),
				GoalType.MINIMIZE,
				new ObjectiveFunction(new MotorFunc(new double[] { 1, 0, 0 })),
				new InitialGuess(new double[] { -0.5, -0.5, 0.8, 1.6 }),
				new NelderMeadSimplex(4, 1.0, 1.0, 2.0, 0.5, 0.5)
		// new MultiDirectionalSimplex(4)
		);
		
		
		RealVector p = new ArrayRealVector(result.getPoint());
		System.out.println(optimizer.getEvaluations());
		System.out.println(p);
		System.out.println(result.getValue());
	}
	
	
	/** */
	@Test
	public void knownTransform()
	{
		// System.out.println(D.toString());
		// System.out.println(Dinv.toString());
		
		IMotorSampler sampler = new Sampler();
		MotorModelOptimizer optimizer = new MotorModelOptimizer(sampler);
		
		optimizer.executeLM(new Vector3(1, 0, 0));
		
		return;
	}
}
