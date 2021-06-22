/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamCalibration;
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
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.data.KickSolverResult;
import edu.tigers.sumatra.vision.kick.estimators.chip.AChipKickSolver;
import edu.tigers.sumatra.vision.kick.estimators.chip.ChipKickSolverLin3Offset;
import edu.tigers.sumatra.vision.kick.estimators.chip.ChipKickSolverLin5Offset;
import edu.tigers.sumatra.vision.kick.estimators.chip.ChipKickSolverNonLin3Direct;
import edu.tigers.sumatra.vision.kick.estimators.chip.ChipKickSolverNonLinIdentDirect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Estimate a single chip kick trajectory by a non-linear fitting and some magic.<br>
 * Estimated parameters: [vx vy vz]
 * The kickoff position is required as input.
 */
public class ChipKickEstimator implements IKickEstimator
{
	private static final Logger log = LogManager.getLogger(ChipKickEstimator.class.getName());

	private final Map<Integer, CamCalibration> camCalib;
	private final List<CamBall> records = new ArrayList<>();
	private final List<CamBall> allRecords = new ArrayList<>();
	private final long kickEventTimestamp;
	private boolean usesPriorKnowledge;

	private int pruneIndex = 0;

	private final ChipKickSolverLin3Offset solverLin3;
	private final ChipKickSolverLin5Offset solverLin5;
	private AChipKickSolver solverNonLin;

	private boolean doFirstHopFit = true;

	private IBallTrajectory currentTraj;
	private KickFitResult fitResult = null;
	private long kickTimestamp;
	private double avgDistLatest10;
	private int failures;


	@Configurable(comment = "Minimum number of records to start estimation", defValue = "8")
	private static int minRecords = 8;

	@Configurable(comment = "Max fitting failures until this estimator is dropped", defValue = "12")
	private static int maxFailures = 12;

	@Configurable(comment = "Max fitting error until this estimator is dropped [mm]", defValue = "100.0")
	private static double maxFittingError = 100.0;

	@Configurable(comment = "Max number of records to keep over all cameras", defValue = "50")
	private static int maxNumberOfRecords = 50;

	@Configurable(comment = "Estimate kick position if the ball is visible on two cameras", defValue = "false")
	private static boolean useKickPositionEstimator = false;

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
		kickEventTimestamp = event.getTimestamp();
		kickTimestamp = event.getTimestamp();

		solverLin3 = new ChipKickSolverLin3Offset(event.getPosition(), event.getTimestamp(), camCalib);
		solverLin5 = new ChipKickSolverLin5Offset(event.getPosition(), event.getTimestamp(), camCalib);

		List<CamBall> camBalls = event.getRecordsSinceKick().stream()
				.map(r -> r.getLatestCamBall().get())
				.filter(b -> b.getFlatPos().distanceTo(event.getPosition()) > 50.0)
				.collect(Collectors.toList());

		records.addAll(camBalls);
		allRecords.addAll(camBalls);

		usesPriorKnowledge = false;
	}


	/**
	 * Create new chip kick estimator with known kick speed.
	 *
	 * @param camCalib Camera calibrations.
	 * @param event Initial kick event.
	 * @param kickSpeed in [mm/s], absolute 3D speed
	 * @param kickAngle in [deg]
	 */
	public ChipKickEstimator(final Map<Integer, CamCalibration> camCalib, final KickEvent event,
			final double kickSpeed, final double kickAngle)
	{
		this(camCalib, event);

		double angle = AngleMath.deg2rad(kickAngle);
		double kickVelZ = Math.sin(angle);
		double kickVelXY = Math.cos(angle);

		IVector2 kickBotDir = Vector2.fromAngle(event.getBotDirection());
		IVector3 kickVel = Vector3.from2d(kickBotDir.scaleToNew(kickVelXY * kickSpeed), kickVelZ * kickSpeed);

		currentTraj = Geometry.getBallFactory()
				.createTrajectoryFromKickedBallWithoutSpin(event.getPosition(), kickVel);
		kickTimestamp = event.getTimestamp();
		fitResult = new KickFitResult(new ArrayList<>(), 0, currentTraj, kickTimestamp, "");

		usesPriorKnowledge = true;
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

		if (records.size() < minRecords)
		{
			if (usesPriorKnowledge)
			{
				computeFitResult("prior");
			}

			return;
		}

		pruneRecords();

		Optional<KickSolverResult> optSolverResult = runSolvers();

		if (optSolverResult.isEmpty())
		{
			failures++;
			return;
		}

		IVector2 kickPos = optSolverResult.get().getKickPosition();
		IVector3 kickVel = optSolverResult.get().getKickVelocity();
		kickTimestamp = optSolverResult.get().getKickTimestamp();

		if (doFirstHopFit)
		{
			// calculate flight time
			double tFly = (2 * kickVel.z()) / 9810;
			CamBall lastRecord = records.get(records.size() - 1);

			if ((((lastRecord.gettCapture() - kickTimestamp) * 1e-9) > tFly) && (records.size() > 12))
			{
				doFirstHopFit = false;

				solverNonLin = new ChipKickSolverNonLin3Direct(kickPos, kickTimestamp, camCalib, kickVel);
			}
		}

		currentTraj = Geometry.getBallFactory()
				.createTrajectoryFromKickedBallWithoutSpin(kickPos, kickVel);

		computeFitResult(optSolverResult.get().getSolverName());
	}


	private void computeFitResult(String solverName)
	{
		List<IVector2> modelPoints = records.stream()
				.map(r -> currentTraj.getMilliStateAtTime((r.gettCapture() - kickTimestamp) * 1e-9).getPos()
						.projectToGroundNew(getCameraPosition(r.getCameraId())))
				.collect(Collectors.toList());

		double avgDist = IntStream.range(0, records.size())
				.mapToDouble(i -> modelPoints.get(i).distanceTo(records.get(i).getFlatPos()))
				.average().orElse(0.0);

		if (records.size() >= 10)
		{
			avgDistLatest10 = IntStream.range(records.size() - 10, records.size())
					.mapToDouble(i -> modelPoints.get(i).distanceTo(records.get(i).getFlatPos()))
					.average().orElse(Double.MAX_VALUE);
		}

		// save the fit result
		fitResult = new KickFitResult(modelPoints, avgDist, currentTraj, kickTimestamp, solverName);
	}


	private Optional<KickSolverResult> runSolvers()
	{
		Optional<KickSolverResult> optSolverResult;

		if (doFirstHopFit)
		{
			optSolverResult = solverLin3.solve(records);
			Optional<KickSolverResult> optLin5Result = Optional.empty();

			Map<Integer, List<CamBall>> groupedRecords = records.stream()
					.collect(Collectors.groupingBy(CamBall::getCameraId));

			if ((groupedRecords.size() > 1) && useKickPositionEstimator)
			{
				optLin5Result = solverLin5.solve(records);
			}

			if (optSolverResult.isPresent() && optLin5Result.isPresent()
					&& (solverLin5.getLastL1Error() < solverLin3.getLastL1Error()))
			{
				optSolverResult = optLin5Result;
			}
		} else
		{
			optSolverResult = solverNonLin.solve(records);
		}

		if (optSolverResult.isPresent())
		{
			IVector3 kickVel = optSolverResult.get().getKickVelocity();
			double chipAngle = Vector2.fromXY(kickVel.getLength2(), kickVel.z()).angleToAbs(Vector2f.X_AXIS)
					.orElse(AngleMath.PI_HALF);
			if (chipAngle > AngleMath.deg2rad(60.0))
			{
				return Optional.empty();
			}
		}

		return optSolverResult;
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
			pruneIndex = 0;
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
		if (records.isEmpty())
		{
			boolean kickWasSomeTimeAgo = ((timestamp - kickEventTimestamp) * 1e-9) > 0.2;
			return usesPriorKnowledge && kickWasSomeTimeAgo;
		}

		long tCaptureLastRecord = records.get(records.size() - 1).gettCapture();
		if (((tCaptureLastRecord - records.get(0).gettCapture()) * 1e-9) < 0.1)
		{
			// keep this estimator for at least 0.1s
			return false;
		}

		if (failures > maxFailures)
		{
			return true;
		}

		if (fitResult == null)
		{
			return false;
		}

		if ((fitResult.getAvgDistance() > maxFittingError) || (avgDistLatest10 > maxFittingError))
		{
			return true;
		}

		return isBallStateInvalid(mergedRobots, timestamp);
	}


	private boolean isBallStateInvalid(List<FilteredVisionBot> mergedRobots, long timestamp)
	{
		BallState state = fitResult.getState(timestamp);
		IVector2 posNow = state.getPos().getXYVector();
		double minDistToRobot = mergedRobots.stream()
				.mapToDouble(r -> r.getPos().distanceTo(posNow))
				.min().orElse(Double.MAX_VALUE);

		if ((minDistToRobot < Geometry.getBotRadius()) && !state.isChipped())
		{
			return true;
		}

		if (state.getVel().getLength() < 1.0 && records.size() > maxNumberOfRecords / 2)
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

		if (solverNonLin == null)
		{
			return result;
		}

		long timeAfterKick = allRecords.get(0).gettCapture() + 500_000_000;

		// keep all records directly after the kick and within the field (-10cm)
		List<CamBall> usedRecords = allRecords.stream()
				.filter(r -> (r.gettCapture() < timeAfterKick)
						|| Geometry.getField().withMargin(-100).isPointInShape(r.getFlatPos()))
				.collect(Collectors.toList());

		// solve to estimate all parameters
		ChipKickSolverNonLinIdentDirect identSolver = new ChipKickSolverNonLinIdentDirect(
				solverNonLin.getKickPosition(), solverNonLin.getKickTimestamp(), camCalib, fitResult.getKickVel());

		Optional<IBallModelIdentResult> chipResult = identSolver.identModel(usedRecords);
		if (chipResult.isPresent())
		{
			result.add(chipResult.get());

			final String lineSeparator = System.lineSeparator();
			log.info("Chip Model:{}{}", lineSeparator, chipResult.get());
		} else
		{
			log.info("Chip model identification failed.");
		}

		return result;
	}


	private IVector3 getCameraPosition(final int camId)
	{
		if (camCalib.containsKey(camId))
		{
			return camCalib.get(camId).getCameraPosition();
		}

		// return an arbitrary value => fitting will fail with bad values
		return Vector3.fromXYZ(0, 0, 2000.0);
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
					fit.getKickPos(), fit.getKickVel().getXYVector().multiplyNew(100)), Color.CYAN);
			speed.setStrokeWidth(10);
			shapes.add(speed);

			DrawableAnnotation err = new DrawableAnnotation(fit.getKickPos(),
					String.format(Locale.ENGLISH, "%.2f (%.1f)", fit.getAvgDistance(), fit.getKickVel().getLength() * 1000));
			err.withOffset(Vector2.fromX(80));
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
