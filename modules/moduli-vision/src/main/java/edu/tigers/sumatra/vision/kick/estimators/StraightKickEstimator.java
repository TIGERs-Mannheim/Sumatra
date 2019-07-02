/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import java.awt.Color;
import java.util.ArrayList;
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
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.data.KickSolverResult;
import edu.tigers.sumatra.vision.data.StraightBallTrajectory;
import edu.tigers.sumatra.vision.kick.estimators.straight.StraightKickSolverLin3;
import edu.tigers.sumatra.vision.kick.estimators.straight.StraightKickSolverNonLin3Direct;
import edu.tigers.sumatra.vision.kick.estimators.straight.StraightKickSolverNonLinIdentDirect;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class StraightKickEstimator implements IKickEstimator
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(StraightKickEstimator.class.getName());
	
	private final List<CamBall> records = new ArrayList<>();
	private final List<CamBall> allRecords = new ArrayList<>();
	private int pruneIndex = 1;
	
	private final StraightKickSolverLin3 solverSliding;
	private final StraightKickSolverNonLin3Direct solverFull;
	
	private Optional<Line> fitLastLine = Optional.empty();
	
	private KickFitResult fitResult = null;
	
	@Configurable(comment = "Max fitting error until this estimator is dropped [mm]", defValue = "100.0")
	private static double maxFittingError = 100.0;
	
	@Configurable(comment = "Max direction deviation until this estimator is dropped [deg]", defValue = "20.0")
	private static double maxDirectionError = 20.0;
	
	@Configurable(comment = "Max number of records to keep over all cameras", defValue = "50")
	private static int maxNumberOfRecords = 50;
	
	static
	{
		ConfigRegistration.registerClass("vision", StraightKickEstimator.class);
	}
	
	
	/**
	 * Create new straight kick estimator.
	 * 
	 * @param event Initial kick event from detector.
	 */
	public StraightKickEstimator(final KickEvent event)
	{
		List<CamBall> camBalls = event.getRecordsSinceKick().stream()
				.map(r -> r.getLatestCamBall().get())
				.collect(Collectors.toList());
		
		records.addAll(camBalls);
		allRecords.addAll(camBalls);
		
		double avgKickVel = getKickSpeed(camBalls, event.getPosition());
		
		solverSliding = new StraightKickSolverLin3();
		solverFull = new StraightKickSolverNonLin3Direct(event.getPosition(), avgKickVel);
		
		runSolvers();
	}
	
	
	/**
	 * Initialize config.
	 */
	public static void touch()
	{
		// Invoke static constructor
	}
	
	
	@Override
	public void addCamBall(final CamBall record)
	{
		records.add(record);
		allRecords.add(record);
		
		pruneRecords();
		
		runSolvers();
	}
	
	
	private void pruneRecords()
	{
		if (records.size() < maxNumberOfRecords)
		{
			return;
		}
		
		records.remove(pruneIndex);
		pruneIndex++;
		
		if (pruneIndex > (records.size() - (maxNumberOfRecords / 5)))
		{
			pruneIndex = 1;
		}
	}
	
	
	private void runSolvers()
	{
		List<Optional<KickFitResult>> results = new ArrayList<>();
		
		results.add(generateFitResult(solverSliding.solve(records)));
		results.add(generateFitResult(solverFull.solve(records)));
		
		fitResult = results.stream()
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted((r1, r2) -> Double.compare(r1.getAvgDistance(), r2.getAvgDistance()))
				.findFirst()
				.orElse(null);
	}
	
	
	private Optional<KickFitResult> generateFitResult(final Optional<KickSolverResult> result)
	{
		if (!result.isPresent())
		{
			return Optional.empty();
		}
		
		List<IVector2> ground = new ArrayList<>(records.size());
		
		IVector2 kickPos = result.get().getKickPosition();
		IVector3 kickVel = result.get().getKickVelocity();
		long tZero = result.get().getKickTimestamp();
		
		StraightBallTrajectory traj = new StraightBallTrajectory(kickPos, kickVel, tZero);
		
		double error = 0;
		for (CamBall ball : records)
		{
			IVector2 modelPos = traj.getStateAtTimestamp(ball.gettCapture()).getPos().getXYVector();
			ground.add(modelPos);
			
			error += modelPos.distanceTo(ball.getFlatPos());
		}
		
		error /= records.size();
		
		return Optional.of(new KickFitResult(ground, error, traj));
	}
	
	
	@Override
	public Optional<KickFitResult> getFitResult()
	{
		return Optional.ofNullable(fitResult);
	}
	
	
	@Override
	public boolean isDone(final List<FilteredVisionBot> mergedRobots, final long timestamp)
	{
		if (((allRecords.get(allRecords.size() - 1).gettCapture() - allRecords.get(0).gettCapture()) * 1e-9) < 0.1)
		{
			// keep this estimator for at least 0.1s
			return false;
		}
		
		if ((allRecords.size() > 20) && isMaxDirectionErrorExceeded(allRecords))
		{
			return true;
		}
		
		if (fitResult == null)
		{
			return false;
		}
		
		if (fitResult.getAvgDistance() > maxFittingError)
		{
			return true;
		}
		
		IVector2 posNow = fitResult.getState(timestamp).getPos().getXYVector();
		double minDistToRobot = mergedRobots.stream()
				.mapToDouble(r -> r.getPos().distanceTo(posNow))
				.min().orElse(Double.MAX_VALUE);
		
		if (minDistToRobot < Geometry.getBotRadius())
		{
			return true;
		}
		
		return !Geometry.getField().withMargin(100).isPointInShape(posNow);
	}
	
	
	@Override
	public Optional<IBallModelIdentResult> getModelIdentResult()
	{
		if (allRecords.size() < 20)
		{
			return Optional.empty();
		}
		
		long timeAfterKick = allRecords.get(0).gettCapture() + 500_000_000;
		
		// keep all records directly after the kick and within the field (-10cm)
		List<CamBall> usedRecords = allRecords.stream()
				.filter(r -> (r.gettCapture() < timeAfterKick)
						|| Geometry.getField().withMargin(-100).isPointInShape(r.getFlatPos()))
				.collect(Collectors.toList());
		
		// solve to estimate all parameters
		StraightKickSolverNonLinIdentDirect identSolver = new StraightKickSolverNonLinIdentDirect();
		
		Optional<IBallModelIdentResult> result = identSolver.identModel(usedRecords);
		if (result.isPresent())
		{
			log.info("Straight Model:" + System.lineSeparator() + result.get());
		} else
		{
			log.info("Straight model identification failed.");
		}
		
		return result;
	}
	
	
	private boolean isMaxDirectionErrorExceeded(final List<CamBall> group)
	{
		List<IVector2> lastRecords = group.subList(group.size() - 10, group.size()).stream()
				.map(CamBall::getFlatPos)
				.collect(Collectors.toList());
		
		Optional<Line> lastLine = Line.fromPointsList(lastRecords);
		fitLastLine = lastLine;
		
		if (lastLine.isPresent() && (fitResult != null))
		{
			IVector2 firstDir = fitResult.getKickVel().getXYVector().normalizeNew();
			IVector2 lastDir = lastLine.get().directionVector().normalizeNew();
			if (firstDir.angleToAbs(lastDir).orElse(0.0) > AngleMath.deg2rad(maxDirectionError))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Get kick speed from a list of internal cam balls.
	 * 
	 * @param balls
	 * @param kickPos
	 * @return
	 */
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public static double getKickSpeed(final List<CamBall> balls, final IVector2 kickPos)
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
	
	
	@Override
	public List<IDrawableShape> getShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		
		Optional<KickFitResult> fitOpt = getFitResult();
		if (fitOpt.isPresent())
		{
			KickFitResult fit = fitOpt.get();
			
			for (IVector2 g : fit.getGroundProjection())
			{
				DrawableCircle ballPos = new DrawableCircle(g, 15, Color.MAGENTA);
				ballPos.setStrokeWidth(2);
				shapes.add(ballPos);
			}
			
			DrawableCircle kickPos = new DrawableCircle(fit.getKickPos(), 30, Color.RED);
			shapes.add(kickPos);
			
			DrawableLine speed = new DrawableLine(Line.fromDirection(
					fit.getKickPos(), fit.getKickVel().getXYVector().multiplyNew(0.1)), Color.RED);
			speed.setStrokeWidth(10);
			shapes.add(speed);
			
			DrawableAnnotation err = new DrawableAnnotation(fit.getKickPos(),
					String.format("%.2f (%.1f)", fit.getAvgDistance(), fit.getKickVel().getLength2()));
			err.withOffset(Vector2.fromXY(80, 40));
			err.setColor(Color.RED);
			shapes.add(err);
		}
		
		if (fitLastLine.isPresent())
		{
			DrawableLine last = new DrawableLine(fitLastLine.get(), Color.BLUE);
			shapes.add(last);
		}
		
		return shapes;
	}
	
	
	@Override
	public EKickEstimatorType getType()
	{
		return EKickEstimatorType.FLAT;
	}
}
