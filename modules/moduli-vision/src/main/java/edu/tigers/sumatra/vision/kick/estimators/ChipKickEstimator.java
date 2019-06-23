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
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.CamBallInternal;
import edu.tigers.sumatra.vision.data.ChipBallTrajectory;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.KickEvent;


/**
 * Estimate a single chip kick trajectory by a non-linear fitting and some magic.<br>
 * Estimated parameters: [vx vy vz]
 * The kickoff position is required as input.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ChipKickEstimator implements IKickEstimator
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(ChipKickEstimator.class.getName());
	
	private final long kickTimestamp;
	private final IVector2 kickPosition;
	private final Map<Integer, CamCalibration> camCalib;
	
	private List<CamBallInternal> records = new ArrayList<>();
	private KickFitResult fitResult = null;
	
	private IVector3 kickVelEst = Vector3.fromXYZ(0, 0, 2000);
	private boolean doKickVelEstimation = true;
	
	private int failures = 0;
	
	private RealVector start;
	
	private final BallParameters ballParams;
	
	@Configurable(comment = "Minimum number of records to start estimation")
	private static int minRecords = 8;
	
	@Configurable(comment = "Max fitting failures until this estimator is dropped")
	private static int maxFailures = 12;
	
	@Configurable(comment = "Max fitting error until this estimator is dropped [mm]")
	private static double maxFittingError = 100;
	
	@Configurable(comment = "Stop initial velocity estimation after first hop flight time * factor")
	private static double stopVelEstimationFactor = 1.3;
	
	static
	{
		ConfigRegistration.registerClass("vision", ChipKickEstimator.class);
	}
	
	
	/**
	 * Create new chip kick estimator.
	 * 
	 * @param camCalib Camera calibrations.
	 * @param event Initial kick event.
	 */
	public ChipKickEstimator(final Map<Integer, CamCalibration> camCalib, final KickEvent event)
	{
		this.camCalib = camCalib;
		
		List<CamBallInternal> camBalls = event.getRecordsSinceKick().stream()
				.map(r -> r.getLatestCamBall().get())
				.collect(Collectors.toList());
		
		records.addAll(camBalls);
		
		kickTimestamp = event.getTimestamp();
		kickPosition = event.getPosition();
		ballParams = Geometry.getBallParameters();
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
		
		if (records.size() < minRecords)
		{
			return;
		}
		
		// Do initial kick velocity estimation (non-linear) during first hop
		if (doKickVelEstimation)
		{
			kickVelEstimationNonLin();
		}
		
		// Create fitted cam balls, they use the initial kick vel to estimate a 3D position
		List<FittedCamBall> fittedBalls = records.stream()
				.map(FittedCamBall::new)
				.collect(Collectors.toList());
		
		// Transform fitted balls to a list of orthogonally projected 2D positions
		List<IVector2> orthoPoints = fittedBalls.stream()
				.map(FittedCamBall::getOrthoPos)
				.collect(Collectors.toList());
		
		// make a best-fit line of all ortho points
		Optional<Line> optPath = Line.fromPointsList(orthoPoints);
		if (!optPath.isPresent())
		{
			return;
		}
		
		Line path = optPath.get();
		IVector2 pathDir = path.directionVector().normalizeNew();
		
		// Snap all fitted balls to the new path
		fittedBalls.forEach(b -> b.snapToLine(path));
		
		// Sort fitted balls by hops
		List<FittedCamBallBin> ballsByHops = new ArrayList<>();
		double vz = kickVelEst.z();
		double tNow = 0;
		double velAbs = kickVelEst.getLength2();
		
		// go through hops while max. height is above 10mm
		while (((vz * vz) / (2.0 * 9810)) > ballParams.getMinHopHeight())
		{
			double tFly = (2 * vz) / 9810;
			double tMin = tNow;
			double tMax = tNow + tFly;
			
			FittedCamBallBin bin = new FittedCamBallBin(fittedBalls, tMin, tMax);
			bin.calcVel(velAbs);
			
			if (!bin.samples.isEmpty())
			{
				ballsByHops.add(bin);
			}
			
			velAbs = bin.velAbs * ballParams.getChipDampingXY();
			
			vz *= ballParams.getChipDampingZ();
			tNow += tFly;
		}
		
		// estimate new kick position on path
		if (ballsByHops.isEmpty())
		{
			return;
		}
		velAbs = ballsByHops.get(0).velAbs;
		double tToKick = ((kickTimestamp - records.get(0).gettCapture()) + (long) (records.get(0).getDtDeviation() * 1e9))
				* 1e-9;
		IVector2 kickPos = path.supportVector().addNew(pathDir.multiplyNew(velAbs * tToKick));
		IVector3 kickVel = Vector3.from2d(pathDir.multiplyNew(velAbs), kickVelEst.z());
		
		List<IVector2> ground = new ArrayList<>();
		
		long kickTimestampLocal = kickTimestamp;
		double avgDist = 0;
		vz = kickVelEst.z();
		for (int i = 0; i < ballsByHops.size(); i++)
		{
			FittedCamBallBin bin = ballsByHops.get(i);
			
			ground.addAll(bin.getGroundPos(kickTimestampLocal, kickPos, pathDir));
			avgDist += bin.distToCamSum;
			
			kickVel = Vector3.from2d(pathDir.multiplyNew(bin.velAbs), vz);
			vz *= ballParams.getChipDampingZ();
			
			if (i == (ballsByHops.size() - 1))
			{
				break;
			}
			
			kickTimestampLocal = bin.getTouchdownTimestamp(kickTimestampLocal);
			kickPos = bin.getTouchdownPosition(kickPos, pathDir);
		}
		
		// all remaining fitted balls are rolling, make an extra bin for them
		FittedCamBallBin rollingBin = new FittedCamBallBin(fittedBalls, tNow, Double.POSITIVE_INFINITY);
		if (!rollingBin.samples.isEmpty())
		{
			FittedCamBallBin bin = ballsByHops.get(ballsByHops.size() - 1);
			kickTimestampLocal = bin.getTouchdownTimestamp(kickTimestampLocal);
			kickPos = bin.getTouchdownPosition(kickPos, pathDir);
			
			rollingBin.calcVelRolling(velAbs);
			ground.addAll(rollingBin.getGroundPosRolling(kickTimestampLocal, kickPos, pathDir));
			avgDist += rollingBin.distToCamSum;
			
			kickVel = Vector3.from2d(pathDir.multiplyNew(rollingBin.velAbs), 0);
		}
		
		avgDist /= ground.size();
		
		// save the fit result
		fitResult = new KickFitResult(ground, avgDist,
				new ChipBallTrajectory(kickPos, kickVel, kickTimestampLocal));
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
		
		if (failures >= maxFailures)
		{
			return true;
		}
		
		if (fitResult != null)
		{
			if (fitResult.getAvgDistance() > maxFittingError)
			{
				return true;
			}
			
			FilteredVisionBall state = fitResult.getState(timestamp);
			IVector2 posNow = state.getPos().getXYVector();
			double minDistToRobot = mergedRobots.stream()
					.mapToDouble(r -> r.getPos().distanceTo(posNow))
					.min().orElse(Double.MAX_VALUE);
			
			if ((minDistToRobot < Geometry.getBotRadius()) && !state.isChipped())
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
	private void kickVelEstimationNonLin()
	{
		// determine optimization start point
		if (start == null)
		{
			// guess something if this is the first run
			start = new ArrayRealVector(new double[] { 0, 0, 2000 });
		}
		
		int numRecords = records.size();
		
		// construct target vector (ground locations)
		RealVector target = new ArrayRealVector(numRecords * 2);
		for (int i = 0; i < numRecords; i++)
		{
			CamBallInternal record = records.get(i);
			
			IVector2 g = record.getFlatPos();
			
			target.setEntry(i * 2, g.x());
			target.setEntry((i * 2) + 1, g.y());
		}
		
		// create bounce model with two bounces
		TwoBounceModel model = new TwoBounceModel();
		
		// construct our LS problem
		LeastSquaresProblem problem = new LeastSquaresBuilder()
				.start(start)
				.model(model)
				.target(target)
				.checker(new EvaluationRmsChecker(1e-6))
				.parameterValidator(model)
				.lazyEvaluation(false)
				.maxEvaluations(20)
				.maxIterations(20)
				.build();
		
		LeastSquaresOptimizer.Optimum optimum = null;
		try
		{
			optimum = new GaussNewtonOptimizer().optimize(problem);
		} catch (IllegalStateException e)
		{
			++failures;
			return;
		}
		
		failures = 0;
		start = optimum.getPoint();
		
		// kick off speed, 3D!
		kickVelEst = Vector3.fromArray(optimum.getPoint().getSubVector(0, 3).toArray());
		
		// calculate flight times
		double tFly = (2 * kickVelEst.z()) / 9810;
		CamBall lastRecord = records.get(records.size() - 1);
		
		if (((lastRecord.gettCapture() - kickTimestamp) * 1e-9) > (tFly * stopVelEstimationFactor))
		{
			// 30% of the second bounce are complete, stop kick velocity estimation
			doKickVelEstimation = false;
		}
	}
	
	private class TwoBounceModel implements MultivariateJacobianFunction, ParameterValidator
	{
		private double g = 9810;
		
		private double px0;
		private double py0;
		private double vx0;
		private double vy0;
		private double vz0;
		private double tf0;
		private double fX;
		private double fY;
		private double fZ;
		private double t;
		
		private RealVector value;
		private RealMatrix jacobian;
		
		
		@Override
		public Pair<RealVector, RealMatrix> value(final RealVector point)
		{
			px0 = kickPosition.x();
			py0 = kickPosition.y();
			vx0 = point.getEntry(0);
			vy0 = point.getEntry(1);
			vz0 = point.getEntry(2);
			tf0 = (vz0 * 2.0) / g;
			
			value = new ArrayRealVector(records.size() * 2);
			jacobian = new Array2DRowRealMatrix(records.size() * 2, 3);
			
			final long tZero = kickTimestamp;
			
			for (int i = 0; i < records.size(); i++)
			{
				CamBallInternal record = records.get(i);
				t = ((record.gettCapture() + (long) (record.getDtDeviation() * 1e9)) - tZero) * 1e-9;
				IVector3 f = camCalib.get(record.getCameraId()).getCameraPosition();
				fX = f.x();
				fY = f.y();
				fZ = f.z();
				
				if (t < tf0)
				{
					// before bounce
					calcFirstTraj(i);
				} else
				{
					// after bounce
					calcSecondTrajHigh(i);
				}
			}
			
			return new Pair<>(value, jacobian);
		}
		
		
		private void calcFirstTraj(final int i)
		{
			// helper constants
			double a = (t * vz0) - ((g * t * t) / 2) - fZ;
			
			// values
			double gx0 = (-(fZ * ((t * vx0) + px0)) / a) + ((fX * fZ) / a) + fX;
			double gy0 = (-(fZ * ((t * vy0) + py0)) / a) + ((fY * fZ) / a) + fY;
			
			// partial derivatives
			double gx0Vx = -(fZ * t) / a;
			double gx0Vy = 0;
			double gx0Vz = ((fZ * t * ((t * vx0) + px0)) / (a * a)) - ((fX * fZ * t) / (a * a));
			
			double gy0Vx = 0;
			double gy0Vy = -(fZ * t) / a;
			double gy0Vz = ((fZ * t * ((t * vy0) + py0)) / (a * a)) - ((fY * fZ * t) / (a * a));
			
			value.setEntry(i * 2, gx0);
			value.setEntry((i * 2) + 1, gy0);
			
			jacobian.setEntry(i * 2, 0, gx0Vx);
			jacobian.setEntry(i * 2, 1, gx0Vy);
			jacobian.setEntry(i * 2, 2, gx0Vz);
			jacobian.setEntry((i * 2) + 1, 0, gy0Vx);
			jacobian.setEntry((i * 2) + 1, 1, gy0Vy);
			jacobian.setEntry((i * 2) + 1, 2, gy0Vz);
		}
		
		
		private void calcSecondTrajHigh(final int i)
		{
			double cXY = ballParams.getChipDampingXY();
			double cZ = ballParams.getChipDampingZ();
			
			// helper constants
			double a = ((-(g * (t - ((2 * vz0) / g)) * (t - ((2 * vz0) / g))) / 2)
					+ (cZ * vz0 * (t - ((2 * vz0) / g)))) - fZ;
			
			// values
			double gx1 = (-(fZ * ((cXY * vx0 * (t - ((2 * vz0) / g))) + ((2 * vx0 * vz0) / g) + px0)) / a)
					+ ((fX * fZ) / a) + fX;
			double gy1 = (-(fZ * ((cXY * vy0 * (t - ((2 * vz0) / g))) + ((2 * vy0 * vz0) / g) + py0)) / a)
					+ ((fY * fZ) / a) + fY;
			
			// partial derivatives
			double gx1Vx = -(fZ * ((cXY * (t - ((2 * vz0) / g))) + ((2 * vz0) / g))) / a;
			double gx1Vy = 0;
			double gx1Vz = ((-(fZ * (((2 * vx0) / g) - ((2 * cXY * vx0) / g))) / a) + ((fZ
					* (((cZ * (t - ((2 * vz0) / g))) + (2 * (t - ((2 * vz0) / g)))) - ((2 * cZ * vz0) / g))
					* ((cXY * vx0 * (t - ((2 * vz0) / g))) + ((2 * vx0 * vz0) / g) + px0)) / (a * a)))
					- ((fX * fZ
							* (((cZ * (t - ((2 * vz0) / g))) + (2 * (t - ((2 * vz0) / g)))) - ((2 * cZ * vz0) / g)))
							/ (a * a));
			
			double gy1Vx = 0;
			double gy1Vy = -(fZ * ((cXY * (t - ((2 * vz0) / g))) + ((2 * vz0) / g))) / a;
			double gy1Vz = ((-(fZ * (((2 * vy0) / g) - ((2 * cXY * vy0) / g))) / a) + ((fZ
					* (((cZ * (t - ((2 * vz0) / g))) + (2 * (t - ((2 * vz0) / g)))) - ((2 * cZ * vz0) / g))
					* ((cXY * vy0 * (t - ((2 * vz0) / g))) + ((2 * vy0 * vz0) / g) + py0)) / (a * a)))
					- ((fY * fZ
							* (((cZ * (t - ((2 * vz0) / g))) + (2 * (t - ((2 * vz0) / g)))) - ((2 * cZ * vz0) / g)))
							/ (a * a));
			
			value.setEntry(i * 2, gx1);
			value.setEntry((i * 2) + 1, gy1);
			
			jacobian.setEntry(i * 2, 0, gx1Vx);
			jacobian.setEntry(i * 2, 1, gx1Vy);
			jacobian.setEntry(i * 2, 2, gx1Vz);
			jacobian.setEntry((i * 2) + 1, 0, gy1Vx);
			jacobian.setEntry((i * 2) + 1, 1, gy1Vy);
			jacobian.setEntry((i * 2) + 1, 2, gy1Vz);
		}
		
		
		@Override
		public RealVector validate(final RealVector params)
		{
			if (params.getEntry(2) < 1500)
			{
				params.setEntry(2, 1500);
			}
			
			if (params.getEntry(2) > 8000)
			{
				params.setEntry(2, 8000);
			}
			
			IVector2 velXY = Vector2.fromReal(params.getSubVector(0, 2));
			if (velXY.getLength2() < 1500)
			{
				velXY = velXY.scaleToNew(1500);
				params.setEntry(0, velXY.x());
				params.setEntry(1, velXY.y());
			}
			
			return params;
		}
	}
	
	private class FittedCamBall extends CamBallInternal
	{
		private final double height;
		private final IVector2 orthoPos;
		private final double timeToKickTimestamp;
		
		private IVector2 snappedPos;
		private double distToSupportVec;
		
		
		public FittedCamBall(final CamBallInternal ball)
		{
			super(ball);
			
			timeToKickTimestamp = ((gettCapture() + (long) (getDtDeviation() * 1e9)) - kickTimestamp) * 1e-9;
			height = getHeight(timeToKickTimestamp);
			IVector3 camPos = camCalib.get(getCameraId()).getCameraPosition();
			IVector3 pos3D = Vector3.fromProjection(camPos, getFlatPos(), height);
			orthoPos = pos3D.getXYVector();
		}
		
		
		private void snapToLine(final ILine path)
		{
			snappedPos = path.leadPointOf(orthoPos);
			distToSupportVec = path.supportVector().distanceTo(snappedPos);
		}
		
		
		private double getHeight(final double tQuery)
		{
			if (tQuery < 0)
			{
				return 0;
			}
			
			double vz = kickVelEst.z();
			double tNow = 0;
			
			// go through hops while max. height is above 10mm
			while (((vz * vz) / (2.0 * 9810)) > ballParams.getMinHopHeight())
			{
				double tFly = (2 * vz) / 9810;
				
				if ((tNow + tFly) > tQuery)
				{
					double t = tQuery - tNow;
					return (vz * t) - (0.5 * 9810 * t * t);
				}
				
				vz *= ballParams.getChipDampingZ();
				tNow += tFly;
			}
			
			return 0;
		}
		
		
		public IVector2 getOrthoPos()
		{
			return orthoPos;
		}
		
		
		public IVector2 getTimeDistVector()
		{
			return Vector2.fromXY(timeToKickTimestamp, distToSupportVec);
		}
		
		
		public double getTimeToKickTimestamp()
		{
			return timeToKickTimestamp;
		}
	}
	
	private class FittedCamBallBin
	{
		private final List<FittedCamBall> samples;
		private final double tFly;
		private double velAbs;
		private List<IVector2> ground;
		private double distToCamSum;
		
		
		public FittedCamBallBin(final List<FittedCamBall> balls, final double tMin, final double tMax)
		{
			samples = balls.stream()
					.filter(fb -> (fb.getTimeToKickTimestamp() > tMin) && (fb.getTimeToKickTimestamp() < tMax))
					.collect(Collectors.toList());
			tFly = tMax - tMin;
		}
		
		
		private void calcVel(final double altVel)
		{
			List<IVector2> timeDist = samples.stream()
					.map(FittedCamBall::getTimeDistVector)
					.collect(Collectors.toList());
			
			if (samples.size() < 5)
			{
				velAbs = altVel;
				return;
			}
			
			Optional<Line> optFit = Line.fromPointsList(timeDist);
			if (optFit.isPresent())
			{
				IVector2 dir = optFit.get().directionVector();
				velAbs = dir.y() / dir.x();
				
				// do a smooth transition if we only have few samples
				if (samples.size() < 14)
				{
					double a = samples.size() / 14.0;
					velAbs = (a * velAbs) + ((1.0 - a) * altVel);
				}
			} else
			{
				velAbs = altVel;
			}
		}
		
		
		private void calcVelRolling(final double altVel)
		{
			if (samples.size() < 5)
			{
				velAbs = altVel;
				return;
			}
			
			IVector2 s0 = samples.get(0).getTimeDistVector();
			List<IVector2> timeDist = samples.stream()
					.map(FittedCamBall::getTimeDistVector)
					.map(v -> v.subtractNew(s0))
					.collect(Collectors.toList());
			
			timeDist.remove(0);
			
			velAbs = timeDist.stream()
					.mapToDouble(v -> (v.y() / v.x()) - (0.5 * ballParams.getAccRoll() * v.x()))
					.average().orElse(altVel);
			
			// do a smooth transition if we only have few samples
			if (samples.size() < 14)
			{
				double a = samples.size() / 14.0;
				velAbs = (a * velAbs) + ((1.0 - a) * altVel);
			}
		}
		
		
		private IVector2 getTouchdownPosition(final IVector2 kickPos, final IVector2 dir)
		{
			return kickPos.addNew(dir.multiplyNew(velAbs * tFly));
		}
		
		
		private long getTouchdownTimestamp(final long kickTimestamp)
		{
			return kickTimestamp + (long) (tFly * 1e9);
		}
		
		
		private List<IVector2> getGroundPos(final long kickTimestamp, final IVector2 kickPos, final IVector2 dir)
		{
			ground = new ArrayList<>(samples.size());
			distToCamSum = 0;
			
			for (FittedCamBall b : samples)
			{
				double t = ((b.gettCapture() + (long) (b.getDtDeviation() * 1e9)) - kickTimestamp) * 1e-9;
				IVector2 onPath = kickPos.addNew(dir.multiplyNew(velAbs * t));
				IVector3 onPath3D = Vector3.from2d(onPath, b.height);
				IVector3 camPos = camCalib.get(b.getCameraId()).getCameraPosition();
				IVector2 flat = onPath3D.projectToGroundNew(camPos);
				ground.add(flat);
				distToCamSum += flat.distanceTo(b.getFlatPos());
			}
			
			return ground;
		}
		
		
		private List<IVector2> getGroundPosRolling(final long kickTimestamp, final IVector2 kickPos, final IVector2 dir)
		{
			ground = new ArrayList<>(samples.size());
			distToCamSum = 0;
			
			for (FittedCamBall b : samples)
			{
				double t = ((b.gettCapture() + (long) (b.getDtDeviation() * 1e9)) - kickTimestamp) * 1e-9;
				IVector2 onPath = kickPos
						.addNew(dir.multiplyNew(velAbs * t).add(dir.multiplyNew(ballParams.getAccRoll() * 0.5 * t * t)));
				ground.add(onPath);
				distToCamSum += onPath.distanceTo(b.getFlatPos());
			}
			
			return ground;
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
				DrawableCircle ballPos = new DrawableCircle(g, 20, Color.BLUE);
				ballPos.setStrokeWidth(5);
				shapes.add(ballPos);
			}
			
			DrawableCircle kick = new DrawableCircle(fit.getKickPos(), 30, Color.CYAN);
			shapes.add(kick);
			
			DrawableLine speed = new DrawableLine(Line.fromDirection(
					fit.getKickPos(), fit.getKickVel().getXYVector().multiplyNew(0.1)), Color.CYAN);
			speed.setStrokeWidth(10);
			shapes.add(speed);
			
			DrawableAnnotation err = new DrawableAnnotation(fit.getKickPos(),
					String.format("%.2f (%.1f)", fit.getAvgDistance(), fit.getKickVel().z()));
			err.setOffset(Vector2.fromX(80));
			err.setColor(Color.CYAN);
			shapes.add(err);
		}
		
		return shapes;
	}
	
	
	@Override
	public EKickEstimatorType getType()
	{
		return EKickEstimatorType.CHIP;
	}
}
