/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.control.motor;

import org.apache.commons.math3.analysis.function.Cos;
import org.apache.commons.math3.analysis.function.Sin;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 * Motor model for a robot with wheel setup encoded in a matrix
 */
public class MatrixMotorModel
{
	private RealMatrix d;
	private RealMatrix dInv;
	private double wheelRadius;


	/**
	 * Default constructor
	 */
	public MatrixMotorModel()
	{
		// default values for v2016 robots
		this(30, 45, 0.076, 0.025);
	}


	/**
	 * Create motor model with given angles.
	 *
	 * @param frontAngleDeg
	 * @param backAngleDeg
	 */
	public MatrixMotorModel(final double frontAngleDeg, final double backAngleDeg, final double botRadius,
			final double wheelRadius)
	{
		updateGeometry(frontAngleDeg, backAngleDeg, botRadius, wheelRadius);
	}


	/**
	 * Update robot geometry used for motor velocity computation.
	 *
	 * @param frontAngleDeg
	 * @param backAngleDeg
	 * @param botRadius
	 * @param wheelRadius
	 */
	public void updateGeometry(final double frontAngleDeg, final double backAngleDeg, final double botRadius,
			final double wheelRadius)
	{
		// convert to radian
		final double frontAngleRad = frontAngleDeg * Math.PI / 180.0;
		final double backAngleRad = backAngleDeg * Math.PI / 180.0;

		// construct angle vector
		RealVector theta = new ArrayRealVector(
				new double[] { frontAngleRad, Math.PI - frontAngleRad, Math.PI + backAngleRad,
						(2 * Math.PI) - backAngleRad });

		// construct matrix for conversion from XYW to M1..M4
		d = new Array2DRowRealMatrix(4, 3);
		d.setColumnVector(0, theta.map(new Sin()).mapMultiplyToSelf(-1.0));
		d.setColumnVector(1, theta.map(new Cos()));
		d.setColumnVector(2, new ArrayRealVector(4, botRadius));
		dInv = new SingularValueDecomposition(d).getSolver().getInverse();

		this.wheelRadius = wheelRadius;
	}


	public IVectorN getWheelSpeed(final IVector3 targetVel)
	{
		RealMatrix xyw = new Array2DRowRealMatrix(targetVel.toArray());
		RealMatrix speedOverGround = d.multiply(xyw);
		RealVector wheelSpeed = speedOverGround.getColumnVector(0).mapMultiply(1.0 / wheelRadius);
		return VectorN.fromReal(wheelSpeed);
	}


	public IVector3 getXywSpeed(final IVectorN wheelSpeed)
	{
		RealMatrix wheel = new Array2DRowRealMatrix(wheelSpeed.toArray());
		RealVector result = dInv.multiply(wheel).getColumnVector(0).mapMultiply(wheelRadius);
		return Vector3.fromXYZ(result.getEntry(0),
				result.getEntry(1),
				result.getEntry(2));
	}
}
