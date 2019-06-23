/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Pair;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.CamBallInternal;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.data.StraightBallTrajectory;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class StraightKickEstimator implements IKickEstimator
{
	private final List<CamBallInternal>	records							= new ArrayList<>();
	private final double						initialAcc;
	
	private Optional<Line>					fitFirstLine					= Optional.empty();
	private Optional<Line>					fitLastLine						= Optional.empty();
	
	private KickFitResult					fitResult						= null;
	private final BallParameters			ballParams;
	
	@Configurable(comment = "Number of initial samples where linear fitting is used")
	private static int						numInitialKickVelSamples	= 10;
	
	@Configurable(comment = "Max fitting error until this estimator is dropped [mm]")
	private static double					maxFittingError				= 100;
	
	@Configurable(comment = "Max direction deviation until this estimator is dropped [deg]")
	private static double					maxDirectionError				= 20;
	
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
		ballParams = Geometry.getBallParameters();
		
		List<CamBallInternal> camBalls = event.getRecordsSinceKick().stream()
				.map(r -> r.getLatestCamBall().get())
				.collect(Collectors.toList());
		
		records.addAll(camBalls);
		
		double avgKickVel = CamBallInternal.getKickSpeed(camBalls, event.getPosition());
		if (avgKickVel < ballParams.getAvgKickVelThresholdForAcc())
		{
			initialAcc = ballParams.getAccRoll();
		} else
		{
			initialAcc = ballParams.getAccSlide();
		}
		
		if (records.size() < numInitialKickVelSamples)
		{
			solve3Lin();
		} else
		{
			nonLinSolve();
		}
	}
	
	
	/**
	 * Initialize config.
	 */
	public static void touch()
	{
		// Invoke static constructor
	}
	
	
	@Override
	public void addCamBall(final CamBallInternal record)
	{
		records.add(record);
		
		if (records.size() < numInitialKickVelSamples)
		{
			solve3Lin();
		} else
		{
			nonLinSolve();
		}
	}
	
	
	@Override
	public Optional<KickFitResult> getFitResult()
	{
		return Optional.ofNullable(fitResult);
	}
	
	
	@Override
	public boolean isDone(final List<FilteredVisionBot> mergedRobots, final long timestamp)
	{
		if (((records.get(records.size() - 1).gettCapture() - records.get(0).gettCapture()) * 1e-9) < 0.1)
		{
			// keep this estimator for at least 0.1s
			return false;
		}
		
		if (records.size() > 20)
		{
			Map<Integer, List<CamBallInternal>> groupedBalls = records.stream()
					.collect(Collectors.groupingBy((final CamBallInternal b) -> b.getCameraId()));
			
			for (List<CamBallInternal> group : groupedBalls.values())
			{
				if (group.size() < 10)
				{
					continue;
				}
				
				List<IVector2> lastRecords = group.subList(group.size() - 10, group.size()).stream()
						.map(CamBallInternal::getFlatPos)
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
				
			}
		}
		
		if (fitResult != null)
		{
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
			
			if (!Geometry.getField().withMargin(100).isPointInShape(posNow))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	@SuppressWarnings("squid:S1166")
	private boolean solve3Lin()
	{
		int numRecords = records.size();
		
		if (numRecords < 2)
		{
			return false;
		}
		
		long tZero = records.get(0).gettCapture() + (long) (records.get(0).getDtDeviation() * 1e9);
		
		List<IVector2> groundPos = records.stream()
				.map(CamBall::getFlatPos)
				.collect(Collectors.toList());
		
		Optional<Line> kickLine = Line.fromPointsList(groundPos);
		if (!kickLine.isPresent())
		{
			return false;
		}
		
		IVector2 dir = kickLine.get().directionVector().normalizeNew();
		
		// linear solving, construct matrices...
		RealMatrix matA = new Array2DRowRealMatrix(numRecords * 2, 3);
		RealVector b = new ArrayRealVector(numRecords * 2);
		
		for (int i = 0; i < numRecords; i++)
		{
			CamBallInternal record = records.get(i);
			
			IVector2 g = record.getPos().getXYVector();
			double t = ((record.gettCapture() + (long) (record.getDtDeviation() * 1e9)) - tZero) * 1e-9;
			
			matA.setRow(i * 2, new double[] { 1, 0, dir.x() * t });
			matA.setRow((i * 2) + 1, new double[] { 0, 1, dir.y() * t });
			
			b.setEntry(i * 2, g.x() - (0.5 * dir.x() * t * t * initialAcc));
			b.setEntry((i * 2) + 1, g.y() - (0.5 * dir.y() * t * t * initialAcc));
		}
		
		DecompositionSolver solver = new QRDecomposition(matA).getSolver();
		RealVector x;
		try
		{
			x = solver.solve(b);
		} catch (SingularMatrixException e)
		{
			return false;
		}
		
		IVector2 p = Vector2.fromXY(x.getEntry(0), x.getEntry(1));
		IVector2 v = dir.scaleToNew(x.getEntry(2));
		IVector2 a = dir.scaleToNew(initialAcc);
		
		List<IVector2> ground = new ArrayList<>(numRecords);
		
		for (CamBallInternal record : records)
		{
			double t = ((record.gettCapture() + (long) (record.getDtDeviation() * 1e9)) - tZero) * 1e-9;
			IVector2 g = p.addNew(v.multiplyNew(t)).addNew(a.multiplyNew(0.5 * t * t));
			ground.add(g);
		}
		
		double avgDist = 0;
		for (int i = 0; i < numRecords; i++)
		{
			IVector2 cam = records.get(i).getPos().getXYVector();
			IVector2 fit = ground.get(i);
			
			avgDist += cam.distanceTo(fit);
		}
		
		avgDist /= numRecords;
		
		fitResult = new KickFitResult(ground, avgDist,
				new StraightBallTrajectory(p, Vector3.from2d(v, 0), tZero));
		
		return true;
	}
	
	
	@SuppressWarnings("squid:S1166")
	private void nonLinSolve()
	{
		int numRecords = records.size();
		
		List<IVector2> groundPos = records.stream()
				.map(CamBall::getFlatPos)
				.collect(Collectors.toList());
		
		Optional<Line> kickLine = Line.fromPointsList(groundPos);
		if (!kickLine.isPresent())
		{
			return;
		}
		
		IVector2 dir = kickLine.get().directionVector().normalizeNew();
		
		RealVector start = new ArrayRealVector(new double[] { fitResult.getKickPos().x(), fitResult.getKickPos().y(),
				fitResult.getKickVel().getLength2() });
		
		RealVector target = new ArrayRealVector(numRecords * 2);
		for (int i = 0; i < numRecords; i++)
		{
			CamBallInternal record = records.get(i);
			
			IVector2 g = record.getPos().getXYVector();
			
			target.setEntry(i * 2, g.x());
			target.setEntry((i * 2) + 1, g.y());
		}
		
		TwoPhaseModel model = new TwoPhaseModel(dir);
		
		LeastSquaresProblem problem = new LeastSquaresBuilder()
				.start(start)
				.model(model)
				.target(target)
				.checker(new EvaluationRmsChecker(1e-6))
				.lazyEvaluation(false)
				.maxEvaluations(10)
				.maxIterations(10)
				.build();
		
		LeastSquaresOptimizer.Optimum optimum = null;
		try
		{
			optimum = new GaussNewtonOptimizer().optimize(problem);
		} catch (IllegalStateException e)
		{
			return;
		}
		
		RealVector groundVector = model.value(optimum.getPoint()).getFirst();
		
		IVector2 p = Vector2.fromXY(optimum.getPoint().getEntry(0), optimum.getPoint().getEntry(1));
		IVector2 v = dir.scaleToNew(optimum.getPoint().getEntry(2));
		
		List<IVector2> ground = new ArrayList<>(numRecords);
		
		double avgDist = 0;
		for (int i = 0; i < numRecords; i++)
		{
			IVector2 cam = records.get(i).getPos().getXYVector();
			IVector2 fit = Vector2.fromReal(groundVector.getSubVector(i * 2, 2));
			ground.add(fit);
			
			avgDist += cam.distanceTo(fit);
		}
		
		avgDist /= numRecords;
		
		fitResult = new KickFitResult(ground, avgDist,
				new StraightBallTrajectory(p, Vector3.from2d(v, 0),
						records.get(0).gettCapture() + (long) (records.get(0).getDtDeviation() * 1e9)));
	}
	
	private class TwoPhaseModel implements MultivariateJacobianFunction
	{
		private final double	dx;
		private final double	dy;
		private final double	cSw;
		private final double	cT;
		
		
		public TwoPhaseModel(final IVector2 dir)
		{
			dx = dir.x();
			dy = dir.y();
			cSw = ballParams.getkSwitch();
			cT = (cSw - 1) / ballParams.getAccSlide();
		}
		
		
		@Override
		public Pair<RealVector, RealMatrix> value(final RealVector point)
		{
			final double px = point.getEntry(0);
			final double py = point.getEntry(1);
			final double vk = point.getEntry(2);
			
			final double tSw = vk * cT;
			
			double accSlide = ballParams.getAccSlide();
			double accRoll = ballParams.getAccRoll();
			
			RealVector value = new ArrayRealVector(records.size() * 2);
			RealMatrix jacobian = new Array2DRowRealMatrix(records.size() * 2, 3);
			
			final long tZero = records.get(0).gettCapture() + (long) (records.get(0).getDtDeviation() * 1e9);
			
			for (int i = 0; i < records.size(); i++)
			{
				CamBallInternal record = records.get(i);
				double t = ((record.gettCapture() + (long) (record.getDtDeviation() * 1e9)) - tZero) * 1e-9;
				
				if (t < tSw)
				{
					value.setEntry(i * 2, px + (vk * dx * t) + (0.5 * accSlide * dx * t * t));
					value.setEntry((i * 2) + 1, py + (vk * dy * t) + (0.5 * accSlide * dy * t * t));
					
					jacobian.setEntry(i * 2, 0, 1);
					jacobian.setEntry(i * 2, 1, 0);
					jacobian.setEntry(i * 2, 2, dx * t);
					jacobian.setEntry((i * 2) + 1, 0, 0);
					jacobian.setEntry((i * 2) + 1, 1, 1);
					jacobian.setEntry((i * 2) + 1, 2, dy * t);
				} else
				{
					final double c2x = ((0.5 * accRoll * dx * cT * cT) - (cT * cSw * dx)) + (cT * dx)
							+ (0.5 * accSlide * dx * cT * cT);
					final double c2y = ((0.5 * accRoll * dy * cT * cT) - (cT * cSw * dy)) + (cT * dy)
							+ (0.5 * accSlide * dy * cT * cT);
					final double c3x = (cSw * t * dx) - (accRoll * dx * cT * t);
					final double c3y = (cSw * t * dy) - (accRoll * dy * cT * t);
					final double c4x = 0.5 * accRoll * dx * t * t;
					final double c4y = 0.5 * accRoll * dy * t * t;
					
					value.setEntry(i * 2, px + (vk * vk * c2x) + (vk * c3x) + c4x);
					value.setEntry((i * 2) + 1, py + (vk * vk * c2y) + (vk * c3y) + c4y);
					
					jacobian.setEntry(i * 2, 0, 1);
					jacobian.setEntry(i * 2, 1, 0);
					jacobian.setEntry(i * 2, 2, (2 * vk * c2x) + c3x);
					jacobian.setEntry((i * 2) + 1, 0, 0);
					jacobian.setEntry((i * 2) + 1, 1, 1);
					jacobian.setEntry((i * 2) + 1, 2, (2 * vk * c2y) + c3y);
				}
			}
			
			return new Pair<>(value, jacobian);
		}
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
			err.setOffset(Vector2.fromXY(80, 40));
			err.setColor(Color.RED);
			shapes.add(err);
		}
		
		if (fitFirstLine.isPresent())
		{
			DrawableLine first = new DrawableLine(fitFirstLine.get(), Color.YELLOW);
			shapes.add(first);
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
