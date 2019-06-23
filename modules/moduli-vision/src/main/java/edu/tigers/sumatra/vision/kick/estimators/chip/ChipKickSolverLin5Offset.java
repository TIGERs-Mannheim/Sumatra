/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators.chip;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.KickSolverResult;


/**
 * Estimate kick position, velocity and time offset for a single hop.
 * <br>
 * Requires records from at least two cameras.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ChipKickSolverLin5Offset extends AChipKickSolver
{
	private double lastL1Error;
	
	
	/**
	 * @param kickPosition
	 * @param kickTimestamp
	 * @param camCalib
	 */
	public ChipKickSolverLin5Offset(final IVector2 kickPosition, final long kickTimestamp,
			final Map<Integer, CamCalibration> camCalib)
	{
		super(kickPosition, kickTimestamp, camCalib);
	}
	
	private static class LinSolve5OffsetResult
	{
		private RealVector x;
		private double l1Error;
		private double tOffset;
		
		
		/**
		 * @param x
		 * @param l1Error
		 * @param tOffset
		 */
		public LinSolve5OffsetResult(final RealVector x, final double l1Error, final double tOffset)
		{
			this.x = x;
			this.l1Error = l1Error;
			this.tOffset = tOffset;
		}
	}
	
	
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	private Optional<LinSolve5OffsetResult> linSolve5Offset(final List<CamBall> records, final double tOffset)
	{
		int numRecords = records.size();
		
		// linear solving, construct matrices...
		RealMatrix matA = new Array2DRowRealMatrix(numRecords * 2, 5);
		RealVector b = new ArrayRealVector(numRecords * 2);
		
		final long tZero = records.get(0).gettCapture();
		final double a = 9810;
		
		for (int i = 0; i < numRecords; i++)
		{
			CamBall record = records.get(i);
			
			double t = ((record.gettCapture() - tZero) * 1e-9) + tOffset;
			IVector3 f = getCameraPosition(record.getCameraId());
			IVector2 g = record.getPos().getXYVector();
			
			matA.setRow(i * 2, new double[] { f.z(), 0, f.z() * t, 0, (g.x() - f.x()) * t });
			matA.setRow((i * 2) + 1, new double[] { 0, f.z(), 0, f.z() * t, (g.y() - f.y()) * t });
			
			b.setEntry(i * 2, (0.5 * a * t * t * (g.x() - f.x())) + (g.x() * f.z()));
			b.setEntry((i * 2) + 1, (0.5 * a * t * t * (g.y() - f.y())) + (g.y() * f.z()));
		}
		
		DecompositionSolver solver = new QRDecomposition(matA).getSolver();
		RealVector x;
		try
		{
			x = solver.solve(b);
		} catch (SingularMatrixException e)
		{
			return Optional.empty();
		}
		
		double l1Norm = matA.operate(x).subtract(b).getL1Norm();
		
		return Optional.of(new LinSolve5OffsetResult(x, l1Norm, tOffset));
	}
	
	
	@Override
	public Optional<KickSolverResult> solve(final List<CamBall> records)
	{
		if (records.size() < 2)
		{
			return Optional.empty();
		}
		
		double tOff = 0.05;
		double inc = tOff / 2;
		
		// solve via binary search algorithm
		while (inc > 1e-3)
		{
			Optional<LinSolve5OffsetResult> optResultNeg = linSolve5Offset(records, tOff - 1e-5);
			Optional<LinSolve5OffsetResult> optResultPos = linSolve5Offset(records, tOff + 1e-5);
			
			if (!optResultNeg.isPresent() || !optResultPos.isPresent())
			{
				return Optional.empty();
			}
			
			if (optResultNeg.get().l1Error > optResultPos.get().l1Error)
			{
				tOff += inc;
			} else
			{
				tOff -= inc;
			}
			
			inc /= 2;
		}
		
		Optional<LinSolve5OffsetResult> optResult = linSolve5Offset(records, tOff);
		
		return postProcessSolveResult(records, optResult);
	}
	
	
	private Optional<KickSolverResult> postProcessSolveResult(final List<CamBall> records,
			final Optional<LinSolve5OffsetResult> optBestResult)
	{
		if (!optBestResult.isPresent())
		{
			return Optional.empty();
		}
		
		LinSolve5OffsetResult bestResult = optBestResult.get();
		
		IVector3 kickVelEst = Vector3.fromArray(bestResult.x.getSubVector(2, 3).toArray());
		kickPosition = Vector2.fromXY(bestResult.x.getEntry(0), bestResult.x.getEntry(1));
		IVector3 kickPos = kickPosition.getXYZVector();
		IVector3 acc = Vector3.fromXYZ(0, 0, -0.5 * 9810);
		
		double a = kickVelEst.getLength2();
		
		if (a < 100.0)
		{
			return Optional.empty();
		}
		
		if (kickVelEst.z() < 0)
		{
			return Optional.empty();
		}
		
		kickTimestamp = records.get(0).gettCapture() - (long) (bestResult.tOffset * 1e9);
		
		double l1Error = 0;
		for (CamBall b : records)
		{
			double t = (b.gettCapture() - kickTimestamp) * 1e-9;
			IVector3 posNow = kickPos.addNew(kickVelEst.multiplyNew(t)).add(acc.multiplyNew(t * t));
			IVector2 ground = posNow.projectToGroundNew(getCameraPosition(b.getCameraId()));
			l1Error += ground.subtractNew(b.getFlatPos()).getL1Norm();
		}
		
		lastL1Error = l1Error;
		
		return Optional.of(new KickSolverResult(kickPosition, kickVelEst, kickTimestamp));
	}
	
	
	/**
	 * @return the lastL1Error
	 */
	public double getLastL1Error()
	{
		return lastL1Error;
	}
}
