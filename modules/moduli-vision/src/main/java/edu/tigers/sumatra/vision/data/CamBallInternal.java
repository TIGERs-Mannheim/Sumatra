/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.data;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Vision Filter internal cam ball representation with additional data.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class CamBallInternal extends CamBall
{
	private final double dtDeviation;
	
	
	/**
	 * Create an internal cam ball from a vision cam ball.
	 * 
	 * @param orig
	 */
	public CamBallInternal(final CamBall orig)
	{
		super(orig);
		dtDeviation = 0;
	}
	
	
	/**
	 * Copy constructor.
	 * 
	 * @param orig
	 */
	public CamBallInternal(final CamBallInternal orig)
	{
		super(orig);
		dtDeviation = orig.dtDeviation;
	}
	
	
	/**
	 * Create an internal cam ball from a vision cam ball.
	 * 
	 * @param orig
	 * @param dtDeviation Deviation from average camera dt in [s].
	 */
	public CamBallInternal(final CamBall orig, final double dtDeviation)
	{
		super(orig);
		this.dtDeviation = dtDeviation;
	}
	
	
	public double getDtDeviation()
	{
		return dtDeviation;
	}
	
	
	/**
	 * Get kick speed from a list of internal cam balls.
	 * 
	 * @param balls
	 * @param kickPos
	 * @return
	 */
	@SuppressWarnings("squid:S1166")
	public static double getKickSpeed(final List<CamBallInternal> balls, final IVector2 kickPos)
	{
		int numPoints = balls.size();
		
		RealMatrix matA = new Array2DRowRealMatrix(numPoints, 2);
		RealVector b = new ArrayRealVector(numPoints);
		
		for (int i = 0; i < numPoints; i++)
		{
			double time = (balls.get(i).gettCapture() - balls.get(0).gettCapture()) * 1e-9;
			matA.setEntry(i, 0, time);
			matA.setEntry(i, 1, 1.0);
			
			b.setEntry(i, balls.get(i).getPos().getXYVector().distanceTo(kickPos));
		}
		
		DecompositionSolver solver = new QRDecomposition(matA).getSolver();
		RealVector x;
		try
		{
			x = solver.solve(b);
		} catch (SingularMatrixException e)
		{
			return 0;
		}
		
		if (x.getEntry(0) < 0)
		{
			return 0;
		}
		
		return x.getEntry(0);
	}
}
