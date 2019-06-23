/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators.straight;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.KickSolverResult;
import edu.tigers.sumatra.vision.kick.estimators.IKickSolver;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class StraightKickSolverLin3 implements IKickSolver
{
	@Override
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public Optional<KickSolverResult> solve(final List<CamBall> records)
	{
		final int numRecords = records.size();
		long tZero = records.get(0).gettCapture();
		double acc = Geometry.getBallParameters().getAccSlide();
		
		List<IVector2> groundPos = records.stream()
				.map(CamBall::getFlatPos)
				.collect(Collectors.toList());
		
		Optional<Line> kickLine = Line.fromPointsList(groundPos);
		if (!kickLine.isPresent())
		{
			return Optional.empty();
		}
		
		IVector2 dir = kickLine.get().directionVector().normalizeNew();
		
		// linear solving, construct matrices...
		RealMatrix matA = new Array2DRowRealMatrix(numRecords * 2, 3);
		RealVector b = new ArrayRealVector(numRecords * 2);
		
		for (int i = 0; i < numRecords; i++)
		{
			CamBall record = records.get(i);
			
			IVector2 g = record.getPos().getXYVector();
			double t = ((record.gettCapture()) - tZero) * 1e-9;
			
			matA.setRow(i * 2, new double[] { 1, 0, dir.x() * t });
			matA.setRow((i * 2) + 1, new double[] { 0, 1, dir.y() * t });
			
			b.setEntry(i * 2, g.x() - (0.5 * dir.x() * t * t * acc));
			b.setEntry((i * 2) + 1, g.y() - (0.5 * dir.y() * t * t * acc));
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
		
		IVector2 kickPos = Vector2.fromXY(x.getEntry(0), x.getEntry(1));
		IVector3 kickVel = dir.scaleToNew(x.getEntry(2)).getXYZVector();
		
		return Optional.of(new KickSolverResult(kickPos, kickVel, tZero));
	}
}
