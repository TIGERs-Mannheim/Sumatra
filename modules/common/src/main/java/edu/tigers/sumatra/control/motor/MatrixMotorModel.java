/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 12, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control.motor;

import org.apache.commons.math3.analysis.function.Cos;
import org.apache.commons.math3.analysis.function.Sin;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.math.VectorN;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MatrixMotorModel extends AMotorModel
{
	private static final double	BOT_RADIUS		= 0.076;
	private static final double	WHEEL_RADIUS	= 0.025;
	
	private final RealMatrix		D, Dinv;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param D
	 */
	public MatrixMotorModel(final RealMatrix D)
	{
		this.D = D;
		Dinv = new SingularValueDecomposition(D).getSolver().getInverse();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param Dw
	 * @return
	 */
	public static MatrixMotorModel fromMatrixWithWheelVel(final RealMatrix Dw)
	{
		RealMatrix D = Dw.copy();
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				D.multiplyEntry(i, j, WHEEL_RADIUS);
			}
		}
		return new MatrixMotorModel(D);
	}
	
	
	/**
	 * 
	 */
	public MatrixMotorModel()
	{
		this(30, 45);
	}
	
	
	/**
	 * Create motor model with given angles.
	 * 
	 * @param frontAngle
	 * @param backAngle
	 */
	public MatrixMotorModel(double frontAngle, double backAngle)
	{
		// convert to radian
		frontAngle *= Math.PI / 180.0;
		backAngle *= Math.PI / 180.0;
		
		// construct angle vector
		RealVector theta = new ArrayRealVector(new double[] { frontAngle, Math.PI - frontAngle, Math.PI + backAngle,
				(2 * Math.PI) - backAngle });
		
		// construct matrix for conversion from XYW to M1..M4
		D = new Array2DRowRealMatrix(4, 3);
		D.setColumnVector(0, theta.map(new Sin()).mapMultiplyToSelf(-1.0));
		D.setColumnVector(1, theta.map(new Cos()));
		D.setColumnVector(2, new ArrayRealVector(4, BOT_RADIUS));
		Dinv = new SingularValueDecomposition(D).getSolver().getInverse();
	}
	
	
	@Override
	protected VectorN getWheelSpeedInternal(final IVector3 targetVel)
	{
		RealMatrix XYW = new Array2DRowRealMatrix(targetVel.toDoubleArray());
		RealMatrix speedOverGround = D.multiply(XYW);
		RealVector wheelSpeed = speedOverGround.getColumnVector(0).mapMultiply(1.0 / WHEEL_RADIUS);
		return new VectorN(wheelSpeed);
	}
	
	
	@Override
	protected Vector3 getXywSpeedInternal(final IVectorN wheelSpeed)
	{
		RealMatrix wheel = new Array2DRowRealMatrix(wheelSpeed.toDoubleArray());
		RealVector result = Dinv.multiply(wheel).getColumnVector(0).mapMultiply(WHEEL_RADIUS);
		return new Vector3(result.getEntry(0),
				result.getEntry(1),
				result.getEntry(2));
	}
	
	
	@Override
	public EMotorModel getType()
	{
		return EMotorModel.MATRIX;
	}
}
