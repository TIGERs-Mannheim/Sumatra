/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.trajectory.flat.FlatBallTrajectory;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.data.KickSolverResult;
import edu.tigers.sumatra.vision.kick.estimators.straight.FlatKickSolverNonLin3Factor;
import edu.tigers.sumatra.vision.kick.estimators.straight.StraightKickSolverLin3;
import edu.tigers.sumatra.vision.kick.estimators.straight.StraightKickSolverNonLin3Direct;
import edu.tigers.sumatra.vision.kick.estimators.straight.StraightKickSolverNonLinIdentDirect;
import edu.tigers.sumatra.vision.tracker.BallTracker;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 *
 */
public class StraightKickEstimator implements IKickEstimator
{
	private static final Logger log = LogManager.getLogger(StraightKickEstimator.class.getName());
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

	private final List<CamBall> records = new ArrayList<>();
	private final List<CamBall> allRecords = new ArrayList<>();
	private final StraightKickSolverLin3 solverSliding;
	private final StraightKickSolverNonLin3Direct solverFull;
	private final FlatKickSolverNonLin3Factor solverFlatFull;
	private int pruneIndex = 1;
	private ILineSegment fitLastLine = null;
	private KickFitResult fitResult = null;
	private List<KickFitResult> activeSolvers = new ArrayList<>();


	/**
	 * Create new straight kick estimator.
	 *
	 * @param event Initial kick event from detector.
	 */
	public StraightKickEstimator(final KickEvent event, List<FilteredVisionBall> filteredBalls)
	{
		List<CamBall> camBalls = event.getRecordsSinceKick().stream()
				.map(BallTracker.MergedBall::getLatestCamBall)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

		if (camBalls.size() > 2 && (camBalls.get(1).getTimestamp() - camBalls.get(0).getTimestamp()) * 1e-9 > 0.1)
		{
			// remove first sample, as it is much older than the next one
			// This can happen, if the ball is not visible for some time before it is kicked
			camBalls.remove(0);
		}

		var ballStateAtKick = getBallStateAtKick(event, filteredBalls);

		records.addAll(camBalls);
		allRecords.addAll(camBalls);

		double avgKickVel = getKickSpeed(camBalls, event.getPosition());

		solverSliding = new StraightKickSolverLin3();
		solverFull = new StraightKickSolverNonLin3Direct(event.getPosition(), avgKickVel);
		solverFlatFull = ballStateAtKick
				.map(bsk -> new FlatKickSolverNonLin3Factor(
						Pose.from(event.getKickingBotPosition(), event.getBotDirection()), bsk))
				.orElseGet(() -> new FlatKickSolverNonLin3Factor(
						Pose.from(event.getKickingBotPosition(), event.getBotDirection())));

		runSolvers();
	}


	/**
	 * Initialize config.
	 */
	public static void touch()
	{
		// Invoke static constructor
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


	private Optional<FilteredVisionBall> getBallStateAtKick(final KickEvent event,
			final List<FilteredVisionBall> filteredBalls)
	{
		// go through balls backward in time until one is found with a plausible inbound state
		for (int i = filteredBalls.size() - 1; i > 1; i--)
		{
			FilteredVisionBall last = filteredBalls.get(i);
			FilteredVisionBall prev = filteredBalls.get(i - 1);

			// travel direction away from kicking robot, this cannot be the inbound ball state
			boolean travelDirectionAwayFromKicker =
					last.getVel().getXYVector().angleToAbs(Vector2.fromAngle(event.getBotDirection())).orElse(0.0)
							< AngleMath.PI_HALF;

			// The previous ball has at least a 25% greater velocity than the current one.
			// This is too much for normal ball deceleration, it probably hit the kicking bot already
			boolean rapidlyDeceleratingBall = prev.getVel().getLength2() > last.getVel().getLength2() * 1.25;

			// If ball speed is increasing this state is already past the kick
			boolean increasingBallSpeed = prev.getVel().getLength2() < last.getVel().getLength2();

			if (travelDirectionAwayFromKicker || rapidlyDeceleratingBall || increasingBallSpeed)
			{
				continue;
			}

			return Optional.of(last);
		}

		return Optional.empty();
	}


	@Override
	public void addCamBall(final CamBall newRecord)
	{
		records.add(newRecord);
		allRecords.add(newRecord);

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

		results.add(solverSliding.solve(records).map(this::generateFitResult));
		results.add(solverFull.solve(records).map(this::generateFitResult));
		results.add(solverFlatFull.solve(records).map(this::generateFitResult)
				.map(k -> k.toBuilder().withAvgDistance(k.getAvgDistance() * 0.5).build()));

		activeSolvers = results.stream()
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();

		fitResult = results.stream()
				.filter(Optional::isPresent)
				.map(Optional::get)
				.min(Comparator.comparingDouble(KickFitResult::getAvgDistance))
				.orElse(null);
	}


	private KickFitResult generateFitResult(KickSolverResult result)
	{
		List<IVector2> ground = new ArrayList<>(records.size());

		IVector2 kickPos = result.getKickPosition();
		IVector3 kickVel = result.getKickVelocity();
		long tZero = result.getKickTimestamp();

		var traj = Geometry.getBallFactory()
				.createTrajectoryFromKickedBall(kickPos, kickVel, result.getKickSpin().orElse(
						Vector2f.ZERO_VECTOR));

		double error = 0;
		for (CamBall ball : records)
		{
			IVector2 modelPos = traj.getMilliStateAtTime((ball.gettCapture() - tZero) * 1e-9).getPos().getXYVector();
			ground.add(modelPos);

			error += modelPos.distanceTo(ball.getFlatPos());
		}

		error /= records.size();

		return new KickFitResult(ground, error, traj, tZero, result.getSolverName());
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
	public List<IBallModelIdentResult> getModelIdentResult()
	{
		List<IBallModelIdentResult> result = new ArrayList<>();

		if (allRecords.size() < 20)
		{
			return result;
		}

		long timeAfterKick = allRecords.get(0).gettCapture() + 500_000_000;

		// keep all records directly after the kick and within the field (-10cm)
		List<CamBall> usedRecords = allRecords.stream()
				.filter(r -> (r.gettCapture() < timeAfterKick)
						|| Geometry.getField().withMargin(-100).isPointInShape(r.getFlatPos()))
				.toList();

		// solve to estimate all parameters
		StraightKickSolverNonLinIdentDirect identSolver = new StraightKickSolverNonLinIdentDirect();

		Optional<IBallModelIdentResult> straightResult = identSolver.identModel(usedRecords);
		if (straightResult.isPresent())
		{
			result.add(straightResult.get());

			final String lineSeparator = System.lineSeparator();
			log.info("Straight Model:{}{}", lineSeparator, straightResult.get());
		} else
		{
			log.info("Straight model identification failed.");
		}

		// check redirect identification
		var redirectResult = solverFlatFull.identModel(usedRecords);

		if (redirectResult.isPresent())
		{
			result.add(redirectResult.get());

			final String lineSeparator = System.lineSeparator();
			log.info("Redirect Model:{}{}", lineSeparator, redirectResult.get());
		}

		return result;
	}


	private boolean isMaxDirectionErrorExceeded(final List<CamBall> group)
	{
		List<IVector2> lastRecords = group.subList(group.size() - 10, group.size()).stream()
				.map(CamBall::getFlatPos)
				.toList();

		Optional<ILineSegment> lastLine = Lines.regressionLineFromPointsList(lastRecords);
		fitLastLine = lastLine.orElse(null);

		if (lastLine.isPresent() && (fitResult != null))
		{
			IVector2 firstDir = fitResult.getKickVel().getXYVector().normalizeNew();
			IVector2 lastDir = lastLine.get().directionVector().normalizeNew();
			return firstDir.angleToAbs(lastDir).orElse(0.0) > AngleMath.deg2rad(maxDirectionError);
		}

		return false;
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

			if (fit.getTrajectory() instanceof FlatBallTrajectory flatBallTrajectory)
			{
				DrawableCircle switchPos = new DrawableCircle(flatBallTrajectory.getPosSwitch().getXYVector(), 30,
						Color.MAGENTA);
				shapes.add(switchPos);
			}

			DrawableLine speed = new DrawableLine(Lines.segmentFromOffset(
					fit.getKickPos(), fit.getKickVel().getXYVector().multiplyNew(100)), Color.RED);
			speed.setStrokeWidth(10);
			shapes.add(speed);

			DrawableAnnotation err = new DrawableAnnotation(fit.getKickPos(),
					String.format("%.2f (%.1f)", fit.getAvgDistance(), fit.getKickVel().getLength2() * 1000));
			err.withOffset(Vector2.fromXY(80, 40));
			err.setColor(Color.RED);
			shapes.add(err);

			double solverOffsetY = 0;
			for (var result : activeSolvers)
			{
				DrawableAnnotation solver = new DrawableAnnotation(fit.getKickPos(),
						String.format("%s: %.2f", result.getSolverName(), result.getAvgDistance()));
				solver.withOffset(Vector2.fromXY(200, 100 + solverOffsetY));
				solver.setColor(Color.MAGENTA);
				solver.setStrokeWidth(2);
				solver.withFontHeight(30);
				shapes.add(solver);

				solverOffsetY += 30;
			}
		}

		if (fitLastLine != null)
		{
			DrawableLine last = new DrawableLine(fitLastLine, Color.BLUE);
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
